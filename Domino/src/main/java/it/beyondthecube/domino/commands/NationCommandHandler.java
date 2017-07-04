package it.beyondthecube.domino.commands;

import java.util.Collection;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import it.beyondthecube.domino.Domino;
import it.beyondthecube.domino.Utility;
import it.beyondthecube.domino.data.EconomyLinker;
import it.beyondthecube.domino.exceptions.DatabaseException;
import it.beyondthecube.domino.politicals.City;
import it.beyondthecube.domino.politicals.Nation;
import it.beyondthecube.domino.politicals.PoliticalManager;
import it.beyondthecube.domino.residents.Resident;
import it.beyondthecube.domino.residents.ResidentManager;

public class NationCommandHandler implements CommandExecutor {
	private void showHelp(Player p) {
		p.sendMessage(Text.of
				(TextColors.GOLD + "Command " + TextColors.GREEN + "/nation" + TextColors.GOLD + " usage:"));
		p.sendMessage(Text.of(TextColors.GREEN + "/nation" + TextColors.WHITE + " - shows own/other nation info info"));
		p.sendMessage(Text.of(TextColors.GREEN + "/nation list" + TextColors.WHITE + " - list all nations"));
		p.sendMessage(Text.of(TextColors.GREEN + "/nation withdraw/deposit <amount>" + TextColors.WHITE
				+ " - withdraw/deposit amount into nation bank"));
		p.sendMessage(Text.of(TextColors.GREEN + "/nation set tax <amount>" + TextColors.WHITE + " - set nation tax"));
	}

	public NationCommandHandler(Domino plugin) {
		CommandSpec nnew = CommandSpec.builder().executor(this::executeNew)
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("name")))).build();
		CommandSpec withdraw = CommandSpec.builder().executor(this::executeWithdraw)
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("amount")))).build();
		CommandSpec deposit = CommandSpec.builder().executor(this::executeDeposit)
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("amount")))).build();
		CommandSpec tax = CommandSpec.builder().executor(this::executeTax)
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("amount")))).build();
		CommandSpec set = CommandSpec.builder().child(tax, "tax").build();
		CommandSpec nation = CommandSpec.builder().executor(this)
				.arguments(
						GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.string(Text.of("mode")))))
				.child(set, "set").child(withdraw, "withdraw").child(deposit, "deposit").child(nnew, "new").build();
		CommandSpec nc = CommandSpec.builder().executor(this::executeNC)
				.arguments(GenericArguments.remainingJoinedStrings(Text.of("message"))).build();
		Sponge.getCommandManager().register(plugin, nation, "nation", "nt");
		Sponge.getCommandManager().register(plugin, nc, "nc");

	}

	public CommandResult executeNew(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		if (p.hasPermission("domino.nation.create")) {
			try {
				String name = (String) args.getOne("name").get();
				PoliticalManager.createNation(name);
				p.sendMessage((Utility.pluginMessage("Created new nation: " + name)));
			} catch (DatabaseException e) {
				p.sendMessage((Utility.errorMessage("ERROR: contact an administrator")));
			}
		} else
			showHelp(p);
		return CommandResult.success();
	}

	public CommandResult executeNC(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		Resident r = ResidentManager.getResident(p.getUniqueId());
		if (!ResidentManager.hasCity(r)) {
			p.sendMessage((Utility.pluginMessage("You don't belong to a city")));
			return CommandResult.success();
		}
		Nation n = PoliticalManager.getNation(ResidentManager.getCity(r).get());
		Collection<Object> cobj = args.getAll("message");
		String msg = "";
		for (Object o : cobj)
			msg += (String) o + " ";
		Text m = Utility.nationChatMessage(p, msg);
		for (Player rec : Sponge.getServer().getOnlinePlayers()) {
			Resident rs = ResidentManager.getResident(rec.getUniqueId());
			if (ResidentManager.hasCity(rs)) {
				if (PoliticalManager.getNation(ResidentManager.getCity(rs).get()).equals(n)) {
					rec.sendMessage(m);
				}
			}
		}
		return CommandResult.success();
	}

	public CommandResult executeDeposit(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		Resident r = ResidentManager.getResident(p.getUniqueId());
		Optional<City> oc = ResidentManager.getCity(r);
		if(!oc.isPresent()) {
			p.sendMessage(Utility.pluginMessage("You don't belong to any city"));
			return CommandResult.success();
		}
		try {
			Double d = Double.valueOf((String) args.getOne("amount").get());
			if (EconomyLinker.canAfford(r.getPlayer(), d)) {
				EconomyLinker.deposit(PoliticalManager.getNation(oc.get()), d);
				EconomyLinker.withdrawPlayer(r.getPlayer(), d);
				p.sendMessage((Utility.pluginMessage("You've deposited " + d + " into your nation bank")));
			} else
				p.sendMessage((Utility.pluginMessage("You can't deposit this much.")));
		} catch (NumberFormatException e) {
			p.sendMessage((Utility.pluginMessage("Not a valid amount")));
		}
		return CommandResult.success();
	}

	public CommandResult executeWithdraw(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		Resident r = ResidentManager.getResident(p.getUniqueId());
		Optional<City> oc = ResidentManager.getCity(r);
		if(!oc.isPresent()) {
			p.sendMessage(Utility.pluginMessage("You don't belong to any city"));
			return CommandResult.success();
		}
		City c = oc.get();
		try {
			Double d = Double.parseDouble((String) args.getOne("amount").get());
			Nation n = PoliticalManager.getNation(c);
			if (n.getCapital().equals(c) && c.isMayor(r)) {
				if (EconomyLinker.canAfford(r.getPlayer(), d)) {
					EconomyLinker.withdraw(n, d);
					EconomyLinker.deposit(r.getPlayer(), d);
					p.sendMessage((Utility.pluginMessage(
							"You've withdrawn " + d + EconomyLinker.getCurrencyNamePlural() + "from nation bank")));
				}
			} else
				p.sendMessage((Utility.pluginMessage("Can't withdraw from your nation bank")));
		} catch (NumberFormatException e) {
			p.sendMessage((Utility.pluginMessage("Not a valid amount")));
		}
		return CommandResult.success();
	}

	public CommandResult executeTax(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		Optional<City> oc = ResidentManager.getCity(ResidentManager.getResident(p.getUniqueId()));
		if(!oc.isPresent()) {
			p.sendMessage(Utility.pluginMessage("You don't belong to any city"));
			return CommandResult.success();
		}
		try {
			PoliticalManager.setTax(
					PoliticalManager.getNation(oc.get()),
					Double.parseDouble((String) args.getOne("amount").get()));
			p.sendMessage((Utility.pluginMessage("Nation tax set")));
		} catch (NumberFormatException ex) {
			p.sendMessage((Utility.pluginMessage("Not a valid amount")));
		} catch (DatabaseException e) {
			p.sendMessage((Utility.errorMessage("Database error. Contact an administrator")));
		}
		return CommandResult.success();
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		Optional<String> mode = args.getOne("mode");
		if (!mode.isPresent()) {

		} else {
			switch (mode.get()) {
			case "help": {
				showHelp(p);
				return CommandResult.success();
			}
			case "list": {
				String ris = "List of nations: " + "\n";
				for (String s : PoliticalManager.getNationsList()) {
					ris += s + "; ";
				}
				p.sendMessage((Utility.pluginMessage(ris)));
				return CommandResult.success();
			}
			default: {
				String nat = (String) args.getOne("mode").get();
				Nation n = PoliticalManager.getNation(nat);
				if (n == null)
					p.sendMessage((Utility.pluginMessage(nat + " is not a valid nation")));
				else
					Utility.nationInfo(n, p);
				return CommandResult.success();
			}
			}
		}
		return CommandResult.success();
	}
}
