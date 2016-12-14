package it.beyondthecube.domino.commands;

import java.io.IOException;
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

import it.beyondthecube.domino.Domino;
import it.beyondthecube.domino.Utility;
import it.beyondthecube.domino.data.database.DatabaseManager;
import it.beyondthecube.domino.exceptions.DatabaseException;
import it.beyondthecube.domino.politicals.City;
import it.beyondthecube.domino.politicals.Nation;
import it.beyondthecube.domino.politicals.PoliticalManager;
import it.beyondthecube.domino.residents.CitizenshipRequestManager;
import it.beyondthecube.domino.residents.Resident;
import it.beyondthecube.domino.residents.ResidentManager;

public class CommandHandler implements CommandExecutor {
	private static Domino plugin;

	public CommandHandler(Domino plugin) {
		CommandSpec nation = CommandSpec.builder().executor(this::executeNation).permission("domino.set.nation")
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("city"))),
					GenericArguments.onlyOne(GenericArguments.string(Text.of("nation")))).build();
		CommandSpec mayor = CommandSpec.builder().executor(this::executeMayor).permission("domino.set.mayor")
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("mayor"))),
					GenericArguments.onlyOne(GenericArguments.string(Text.of("city")))).build();
		CommandSpec set = CommandSpec.builder().child(mayor, "mayor").child(nation, "nation").build();
		CommandSpec bonus = CommandSpec.builder().executor(this::executeBonus).permission("domino.give.bonus")
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("city"))))
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("bonus")))).build();
		CommandSpec give = CommandSpec.builder().child(bonus, "bonus").build();
		CommandSpec dom = CommandSpec.builder().executor(this)
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("mode")))).child(set, "set")
				.child(give, "give").build();
		Sponge.getCommandManager().register(plugin, dom, "dom");
	}

	public CommandResult executeMayor(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		Optional<City> oc = PoliticalManager.getCity((String) args.getOne("city").get());
		if(oc.isPresent()) {
			try {
				City c = oc.get();
				Optional<Resident> or = ResidentManager.getResident((String) args.getOne("mayor").get());
				if(!or.isPresent()) {
					p.sendMessage((Utility.pluginMessage("Player not found")));
					return CommandResult.success();
				}
				Resident r = or.get();
				if(ResidentManager.getCity(r).isPresent()) {
					PoliticalManager.setMayor(c, r);
				}
				else 
					p.sendMessage(Utility.pluginMessage("Player is not part of any town"));
			} catch (DatabaseException e) {
				p.sendMessage((Utility.errorMessage("ERROR: contact an admin")));
			}
		} else
			p.sendMessage((Utility.pluginMessage("The city you've specified doesn't exist")));
		return CommandResult.success();
	}
	
	public CommandResult executeBonus(CommandSource src, CommandContext args) throws CommandException {
		if(!(src instanceof Player)) 
			return CommandResult.empty();
		Optional<City> oc = PoliticalManager.getCity((String) args.getOne("city").get());
		if(oc.isPresent()) 
			PoliticalManager.addPlotBonus(oc.get(), Integer.parseInt((String) args.getOne("bonus").get()));
		return CommandResult.success();	
	}

	public CommandResult executeNation(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		Optional<City> oc = PoliticalManager.getCity((String) args.getOne("city").get());
		if(oc.isPresent()) {
			try {
				City c = oc.get();
				Nation n = PoliticalManager.getNation((String) args.getOne("nation").get());
				if (PoliticalManager.getNation(c).equals(n)) {
					p.sendMessage((Utility.pluginMessage("This city is alredy under this nation")));
				}
				DatabaseManager.getInstance().setNation(c, n);
				PoliticalManager.setNation(c, n);
				p.sendMessage((Utility.pluginMessage("Nation changed")));
			} catch (DatabaseException ex) {
				p.sendMessage((Utility.errorMessage("ERROR: contact an admin")));
			}
		} else 
			p.sendMessage((Utility.pluginMessage("The city you've specified doesn't exist")));
		return CommandResult.success();
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if(!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		Resident r = ResidentManager.getResident(p.getUniqueId());
		switch ((String) args.getOne("mode").get()) {
			case "reload": {
				if (p.hasPermission("dom.reload")) {
					p.sendMessage((Utility.pluginMessage("Reloading plugin...")));
					plugin.load();
					p.sendMessage((Utility.pluginMessage("Reloading complete")));
				}
	     		return CommandResult.success();
			}
		case "accept": {
			if (CitizenshipRequestManager.hasRequest(r))
				try {
					CitizenshipRequestManager.requestAccepted(r);
				} catch (IOException e) {
					p.sendMessage(Utility.errorMessage("Cound not give bonus plots. Contact an administrator"));
				}
			else
				p.sendMessage((Utility.pluginMessage("No invitation to accept")));
			return CommandResult.success();
		}
		case "deny": {
			if (CitizenshipRequestManager.hasRequest(r))
				CitizenshipRequestManager.requestDenied(r);
			else
				p.sendMessage((Utility.pluginMessage("No invitation to deny")));
			return CommandResult.success();
		}
		default: {
			// TODO showHelp(p);
			return CommandResult.success();
		}
					
		} 
	}

}
