package it.beyondthecube.domino.commands;

import java.io.IOException;
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
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import it.beyondthecube.domino.Domino;
import it.beyondthecube.domino.Utility;
import it.beyondthecube.domino.data.EconomyLinker;
import it.beyondthecube.domino.data.config.PluginConfig;
import it.beyondthecube.domino.data.database.DatabaseManager;
import it.beyondthecube.domino.exceptions.CityNotFoundException;
import it.beyondthecube.domino.exceptions.DatabaseException;
import it.beyondthecube.domino.exceptions.InsufficientRankException;
import it.beyondthecube.domino.exceptions.ParseException;
import it.beyondthecube.domino.politicals.City;
import it.beyondthecube.domino.politicals.Nation;
import it.beyondthecube.domino.politicals.PoliticalManager;
import it.beyondthecube.domino.residents.Citizenship;
import it.beyondthecube.domino.residents.CitizenshipRequestManager;
import it.beyondthecube.domino.residents.Resident;
import it.beyondthecube.domino.residents.ResidentManager;
import it.beyondthecube.domino.terrain.AreaManager;
import it.beyondthecube.domino.terrain.ComLocation;

public class CityCommandHandler implements CommandExecutor {
	private Domino plugin;

	public CityCommandHandler(Domino plugin) {
		this.plugin = plugin;
		CommandSpec toggle = CommandSpec.builder().executor(this::executeToggle)
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("type"))),
					GenericArguments.onlyOne(GenericArguments.bool(Text.of("mode")))).build();
		CommandSpec addassistant = CommandSpec.builder().executor(this::executeAddAssistant)
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("player")))).build();
		CommandSpec removeassistant = CommandSpec.builder().executor(this::executeRemoveAssistant)
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("player")))).build();
		CommandSpec add = CommandSpec.builder().executor(this::executeAdd)
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))))
				.child(addassistant, "assistant").build();
		CommandSpec remove = CommandSpec.builder().executor(this::executeRemove)
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))))
				.child(removeassistant, "assistant").build();
		CommandSpec tax = CommandSpec.builder().executor(this::executeTax)
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("amount")))).build();
		CommandSpec mayor = CommandSpec.builder().executor(this::executeMayor)
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("mayor")))).build();
		CommandSpec set = CommandSpec.builder().child(tax, "tax").child(mayor, "mayor").build();
		CommandSpec perm = CommandSpec.builder().executor(this::executePerm)
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("type"))),
					GenericArguments.onlyOne(GenericArguments.string(Text.of("target"))),
					GenericArguments.onlyOne(GenericArguments.string(Text.of("mode")))).build();
		CommandSpec cnew = CommandSpec.builder().executor(this::executeNew)
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("name"))),
					GenericArguments.onlyOne(GenericArguments.string(Text.of("nation"))),
					GenericArguments.onlyOne(GenericArguments.string(Text.of("mayor")))).build();
		CommandSpec withdraw = CommandSpec.builder().executor(this::executeWithdraw)
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("amount")))).build();
		CommandSpec deposit = CommandSpec.builder().executor(this::executeDeposit)
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("amount")))).build();
		CommandSpec city = CommandSpec.builder()
				.arguments(GenericArguments.optional(
						GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.string(Text.of("mode"))))))
				.child(add, "add").child(remove, "remove").child(toggle, "toggle").child(set, "set").child(perm, "perm")
				.child(cnew, "new").child(withdraw, "withdraw").child(deposit, "deposit").executor(this).build();
		CommandSpec cc = CommandSpec.builder().executor(this::executeCC)
				.arguments(GenericArguments.remainingJoinedStrings(Text.of("message"))).build();
		Sponge.getCommandManager().register(plugin, city, "city" , "ct");
		Sponge.getCommandManager().register(plugin, cc, "cc");

	}

	private void showHelp(Player p) {
		p.sendMessage(Text.of(TextColors.GOLD + "Command " + TextColors.GREEN + "/city" + TextColors.GOLD + " usage:"));
		p.sendMessage(Text.of(TextColors.GREEN + "/city [name]" + TextColors.WHITE + " - shows own/other city info"));
		p.sendMessage(Text.of(TextColors.GREEN + "/city list" + TextColors.WHITE + " - list all cities"));
		p.sendMessage(Text.of(TextColors.GREEN + "/city leave" + TextColors.WHITE + " - leave your current city"));
		p.sendMessage(
				Text.of(TextColors.GREEN + "/city claim|unclaim" + TextColors.WHITE + " - add/remove a plot claim"));
		p.sendMessage(
				Text.of(TextColors.GREEN + "/city spawn" + TextColors.WHITE + " - teleport to your city spawnpoint"));
		p.sendMessage(Text.of(TextColors.GREEN + "/city <add|remove> <player>" + TextColors.WHITE
				+ " - add/remove a player from your city"));
		p.sendMessage(Text.of(TextColors.GREEN + "/city <add|remove> assistant <player>" + TextColors.WHITE
				+ " - add/remove assistants from your city"));
		p.sendMessage(Text.of(TextColors.GREEN + "/city <withdraw|deposit> <amount>" + TextColors.WHITE
				+ " - withdraw/deposit amount from/in your city account"));
		p.sendMessage(Text.of(TextColors.GREEN + "/city set tax <amount>" + TextColors.WHITE + " - set city tax"));
		p.sendMessage(Text
				.of(TextColors.GREEN + "/city perm <build|interact|itemuse> <owner|friends|citizens|all> <true|false>"
						+ TextColors.WHITE + " - sets a permission"));
		p.sendMessage(Text.of(TextColors.GREEN + "/city toggle <pvp|fire|mobs>" + TextColors.WHITE
				+ " - turn pvp, fire or mobs on/off"));
	}

	public CommandResult executeToggle(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		try {
			City c = ResidentManager.getCity(ResidentManager.getResident(p.getUniqueId()));
			String toggle = (String) args.getOne("toggle").get();
			switch (toggle) {
			case "fire":
				c.toggleFire();
				p.sendMessage((Utility.pluginMessage("Toggled fire")));
				break;
			case "pvp":
				c.togglePvp();
				p.sendMessage((Utility.pluginMessage("Toggled PVP")));
				break;
			case "mobs":
				c.toggleMobs();
				p.sendMessage((Utility.pluginMessage("Toggled mobs")));
				break;
			default:
				showHelp(p);
				return CommandResult.success();
			}
			DatabaseManager.getInstance().setToggles(c);
			return CommandResult.success();
		} catch (DatabaseException e) {
			p.sendMessage((Utility.errorMessage("Database error")));
		}
		return CommandResult.success();
	}

	public CommandResult executeNew(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		if (p.hasPermission("domino.city.new")) {
			Nation n = PoliticalManager.getNation((String) args.getOne("nation").get());
			Optional<Resident> my = ResidentManager.getResident((String) args.getOne("mayor").get());
			if(!my.isPresent()) {
				p.sendMessage((Utility.pluginMessage("Player not found")));
				return CommandResult.success();
			}
			Resident may = my.get();
			if (ResidentManager.hasCity(may))
				p.sendMessage((Utility.pluginMessage(
						may.getNick() + " is still a citizen of " + ResidentManager.getCity(may).getName())));
			else {
				try {
					String name = (String) args.getOne("name").get();
					PoliticalManager.createCity(p, name, may, n, true, p.getLocation(), 0);
					p.sendMessage((Utility.pluginMessage("Created new city: " + name)));
				} catch (DatabaseException e1) {
					p.sendMessage((Utility.errorMessage("ERROR: contact an administrator")));
				}
			}
		}
		return CommandResult.success();
	}

	public CommandResult executeAdd(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		Resident r = ResidentManager.getResident(p.getUniqueId());
		City c = ResidentManager.getCity(r);
		if (!(c.isAssistant(r) || c.isMayor(r))) {
			p.sendMessage((Utility.pluginMessage("Insufficient rank")));
			return CommandResult.success();
		}
		Optional<Resident> or = ResidentManager.getResident(((String) args.getOne("player").get()));
		if(!or.isPresent()) {
			p.sendMessage((Utility.pluginMessage("Player not found")));
			return CommandResult.success();
		}
		Resident added = or.get();
		if (ResidentManager.isCitizen(added)) {
			p.sendMessage((Utility.pluginMessage("This player is already a citizen of another city")));
			return CommandResult.success();
		}
		if (!Sponge.getServer().getPlayer(added.getPlayer()).get().isOnline()) {
			p.sendMessage((Utility.pluginMessage("This player is not online")));
			return CommandResult.success();
		}
		CitizenshipRequestManager.newRequest(new Citizenship(added, r, c, plugin));
		return CommandResult.success();
	}

	public CommandResult executeRemove(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		Resident remover = ResidentManager.getResident(p.getUniqueId());
		Optional<Resident> or = ResidentManager.getResident((String) args.getOne("player").get());
		if(or.isPresent()) {
			p.sendMessage((Utility.pluginMessage("Player not found")));
			return CommandResult.success();
		}
		Resident removed = or.get();
		try {
			ResidentManager.removeResident(remover, removed, ResidentManager.getCity(remover));
			return CommandResult.success();
		} catch (InsufficientRankException e) {
			p.sendMessage((Utility.pluginMessage("Can't perform this command" + e.getMessage())));
			return CommandResult.success();
		} catch (CityNotFoundException e) {
			p.sendMessage((Utility.pluginMessage("You don't belong to any city")));
			return CommandResult.success();
		} catch (DatabaseException e) {
			p.sendMessage((Utility.errorMessage("ERROR: contact an administrator")));
			return CommandResult.success();
		}
	}

	public CommandResult executePerm(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		Resident r = ResidentManager.getResident(p.getUniqueId());
		try {
			City c = ResidentManager.getCity(r);
			PoliticalManager.setPermissions(r, c, ((String) args.getOne("type").get()),
					((String) args.getOne("target").get()), ((String) args.getOne("mode").get()));
		} catch (InsufficientRankException e) {
			p.sendMessage((Utility.pluginMessage(e.getMessage())));
		} catch (ParseException e) {
			p.sendMessage((Utility.pluginMessage("Syntax error: " + e.getMessage() + " not recognized")));
		} catch (DatabaseException e) {
			p.sendMessage((Utility.errorMessage("ERROR: contact an administrator")));
		}
		return CommandResult.success();
	}

	public CommandResult executeTax(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		Resident r = ResidentManager.getResident(p.getUniqueId());
		try {
			double amount = Double.parseDouble((String) args.getOne("amount").get());
			City c = ResidentManager.getCity(r);
			if (c.isAssistant(r) || c.isMayor(r)) {
				PoliticalManager.setTax(c, amount);
				p.sendMessage((Utility.pluginMessage(
						"City tax have been set to " + amount + " " + EconomyLinker.getCurrencyNamePlural())));
			} else
				p.sendMessage((Utility.pluginMessage("Insufficient rank")));
		} catch (NumberFormatException e) {
			p.sendMessage((Utility.pluginMessage("Not a valid amount")));
		} catch (DatabaseException e) {
			p.sendMessage((Utility.errorMessage("ERROR: contact an administrator")));
		}
		return CommandResult.success();
	}

	public CommandResult executeMayor(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		Resident r = ResidentManager.getResident(p.getUniqueId());

		try {
			City c = ResidentManager.getCity(r);
			if (c.isMayor(r)) {
				Optional<Resident> my = ResidentManager.getResident((String) args.getOne("mayor").get());
				if(!my.isPresent()) {
					p.sendMessage((Utility.pluginMessage("Player not found")));
					return CommandResult.success();
				}
				Resident mayor = my.get();
				if (!ResidentManager.getCity(mayor).equals(c)) {
					p.sendMessage((Utility.pluginMessage("You can only set a citizen of yours as mayor")));
					return CommandResult.success();
				}
				PoliticalManager.setMayor(c, mayor);
				p.sendMessage((Utility.pluginMessage("You're no longer mayor of " + c.getName())));
				Optional<Player> op = Sponge.getServer().getPlayer(mayor.getPlayer());
				if (op.isPresent())
					op.get().sendMessage((Utility.pluginMessage("You're the new mayor of " + c.getName())));
			} else
				p.sendMessage((Utility.pluginMessage("Only a mayor can perform this command")));
		} catch (DatabaseException e) {
			p.sendMessage((Utility.errorMessage("SQL Error")));
		}
		return CommandResult.success();
	}

	public CommandResult executeAddAssistant(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		Resident r = ResidentManager.getResident(p.getUniqueId());
		Optional<Resident> ass = ResidentManager.getResident((String)args.getOne("player").get());
		if(!ass.isPresent()) {
			p.sendMessage((Utility.pluginMessage("Player not found")));
		}
		try {
			PoliticalManager.addAssistant(r,ass.get());
		} catch (InsufficientRankException e) {
			p.sendMessage((Utility.pluginMessage("Insufficient rank")));
		} catch (CityNotFoundException e) {
			p.sendMessage((Utility.pluginMessage("You don't belong to a city")));
		} catch (DatabaseException e) {
			p.sendMessage((Utility.errorMessage("ERROR: contact an administrator")));
		}
		return CommandResult.success();
	}

	public CommandResult executeRemoveAssistant(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		Resident r = ResidentManager.getResident(p.getUniqueId());
		Optional<Resident> rm = ResidentManager.getResident((String) args.getOne("mode").get());
		if(!(rm.isPresent())) {
			p.sendMessage((Utility.pluginMessage("Player not found")));
		}
		Resident removed = rm.get();
		try {
			PoliticalManager.removeAssistant(r, removed);
			p.sendMessage((Utility.pluginMessage(removed.getNick() + " correctly removed from assistants")));
			Optional<Player> op = Sponge.getServer().getPlayer(removed.getPlayer());
			if (op.isPresent())
				op.get().sendMessage((Utility.pluginMessage("You've been removed from assistants")));
		} catch (InsufficientRankException e) {
			p.sendMessage((Utility.pluginMessage("Insufficient rank")));
		} catch (CityNotFoundException e) {
			p.sendMessage((Utility.pluginMessage("You don't belong to any city")));
		} catch (DatabaseException e) {
			p.sendMessage((Utility.errorMessage("ERROR: contact an administrator")));
		}
		return CommandResult.success();
	}

	public CommandResult executeDeposit(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		Resident r = ResidentManager.getResident(p.getUniqueId());
		try {
			Double d = Double.valueOf((String) args.getOne("amount").get());
			if (EconomyLinker.canAfford(r.getPlayer(), d)) {
				EconomyLinker.deposit(ResidentManager.getCity(r), d);
				EconomyLinker.withdrawPlayer(r.getPlayer(), d);
				p.sendMessage((Utility.pluginMessage("You've deposited " + d + " into your town bank")));
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
		City c = ResidentManager.getCity(r);
		if (c.isAssistant(r) || c.isMayor(r)) {
			Double d = Double.valueOf((String) args.getOne("amount").get());
			if (EconomyLinker.canBankAfford(String.valueOf(c.getID()), d)) {
				EconomyLinker.withdraw(ResidentManager.getCity(r), d);
				p.sendMessage((Utility.pluginMessage("You've withdrawn " + d + " from your town bank")));
			} else
				p.sendMessage((Utility.pluginMessage("Your town doesn't have that much money")));
		} else {
			p.sendMessage((Utility.pluginMessage("Insufficeint rank")));
		}
		return CommandResult.success();
	}

	public CommandResult executeCC(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		Resident r = ResidentManager.getResident(p.getUniqueId());
		if (!ResidentManager.hasCity(r)) {
			p.sendMessage((Utility.pluginMessage("You don't belong to a city")));
			return CommandResult.success();
		}
		City c = ResidentManager.getCity(r);
		Collection<Object> cobj = args.getAll("message");
		String msg = "";
		for (Object o : cobj)
			msg += (String) o + " ";
		String m = Utility.cityChatMessage(p, msg);
		for (Player rec : Sponge.getServer().getOnlinePlayers()) {
			Resident rs = ResidentManager.getResident(rec.getUniqueId());
			if (ResidentManager.hasCity(rs)) {
				if (ResidentManager.getCity(rs).equals(c)) {
					rec.sendMessage(Text.of(m));
				}
			}
		}
		return CommandResult.success();
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		Resident r = ResidentManager.getResident(p.getUniqueId());
		Optional<String> os = args.getOne("mode");
		if (os.isPresent()) {
			switch (os.get()) {
			case "help": {
				showHelp(p);
				return CommandResult.success();
			}
			case "spawn": {
				double price=0;
				try {
					price = (Double) PluginConfig.getValue("domino", "city", "spawn", "price");
				} catch (NumberFormatException | IOException e) {
					p.sendMessage(Utility.errorMessage("Error reading config file"));
					return CommandResult.empty();
				}
				if (EconomyLinker.canAfford(r.getPlayer(), price)) {
					City c = ResidentManager.getCity(ResidentManager.getResident(p.getUniqueId()));
					ComLocation spawn = c.getSpawn();
					if (spawn == null)
						p.sendMessage((Utility.pluginMessage("Your city has no spawn point!")));
					else {
						Location<World> sp = spawn.getLocation();
						p.transferToWorld(sp.getExtent(), sp.getPosition());
						EconomyLinker.withdrawPlayer(r.getPlayer(), price);
						EconomyLinker.deposit(c, price);
						p.sendMessage(
								(Utility.pluginMessage("You've been charged " + price + " for city spawn")));
					}
				} else
					p.sendMessage((Utility.pluginMessage("Can't afford city spawn")));
				return CommandResult.success();
			}
			case "claim": {
				try {
					p.sendMessage((Utility.pluginMessage("Processing plot claim")));
					Chunk k = p.getWorld().getChunkAtBlock(p.getLocation().getBlockPosition()).get();
					if (!(PoliticalManager.plotAlreadyClaimed(k, ResidentManager.getCity(r)))) {
						if (PoliticalManager.claim(ResidentManager.getCity(r), k, r))
							p.sendMessage((Utility.pluginMessage("Plot claimed")));
						else
							p.sendMessage((Utility
									.pluginMessage("Can't claim this plot. Maybe you aren't close to your town?")));
					} else
						p.sendMessage((Utility.pluginMessage("Plot already claimed")));
					return CommandResult.success();
				} catch (DatabaseException e) {
					p.sendMessage((Utility.pluginMessage("ERROR: contact an administrator")));
					return CommandResult.success();
				}
			}
			case "unclaim": {
				if (!AreaManager.existsArea(p.getLocation())) {
					p.sendMessage((Utility.pluginMessage("Area not found")));
					return CommandResult.success();
				}
				try {
					PoliticalManager.unclaim(r, p.getLocation());
				} catch (InsufficientRankException e) {
					p.sendMessage((Utility.pluginMessage("You can't perform this command")));
				} catch (CityNotFoundException e) {
					p.sendMessage((Utility.pluginMessage("You don't belong to a city")));
				} catch (DatabaseException e) {
					p.sendMessage((Utility.errorMessage("ERROR: contact an administrator")));
				}
				return CommandResult.success();
			}
			case "list": {
				String ris = "List of cities: " + "\n";
				for (City s : PoliticalManager.getCitiesList()) {
					ris += s.getName() + "; ";
				}
				p.sendMessage((Utility.pluginMessage(ris)));
				return CommandResult.success();
			}
			case "leave": {
				try {
					Resident leaver = ResidentManager.getResident(p.getUniqueId());
					if (ResidentManager.getCity(leaver).isMayor(leaver)) {
						p.sendMessage((Utility.pluginMessage("You can't leave the town you're mayor of!")));
						return CommandResult.success();
					}
					ResidentManager.removeResident(leaver, leaver, ResidentManager.getCity(leaver));
					Sponge.getServer().getPlayer(leaver.getPlayer()).get()
							.sendMessage((Utility.pluginMessage("You left your town")));
				} catch (CityNotFoundException e) {
					p.sendMessage((Utility.pluginMessage("You are already free")));
				} catch (DatabaseException e) {
					p.sendMessage((Utility.errorMessage("ERROR: contact an administrator")));
				} catch (InsufficientRankException e) {
					p.sendMessage((Utility.pluginMessage("Insufficient rank")));
				}
				return CommandResult.success();
			}
			default: {
				try {
					City c = PoliticalManager.getCity(os.get());
					Utility.cityInfo(p, c);
					return CommandResult.success();
				} catch (CityNotFoundException e) {
					p.sendMessage((Utility.pluginMessage("City not found")));
					return CommandResult.success();
				}
			}
			}
		} else {
			if (!ResidentManager.hasCity(r)) {
				p.sendMessage((Utility.pluginMessage("You don't belong to a city")));
				return CommandResult.success();
			}
			Utility.cityInfo(p, ResidentManager.getCity(ResidentManager.getResident(p.getUniqueId())));
			return CommandResult.success();
		}
	}

	/*
	 * 
	 * 
	 * @SuppressWarnings("deprecation") public boolean comm(CommandSender
	 * sender, Command cmd, String label, String[] args) { if (sender instanceof
	 * Player) { Player p = (Player) sender; Resident r =
	 * ResidentManager.getResident(p.getUniqueId()); if
	 * (cmd.getName().equals("cc")) else if (cmd.getName().equals("city")) { if
	 * (!(ResidentManager.hasCity(r))) { if (!(args[0].equals("help")) &&
	 * !(args[0].equals("new")) && !(PoliticalManager.isValidCity(args[0]))) {
	 * p.sendMessage(Text.of("You don't belong to a city")); return
	 * CommandResult.success(); } }
	 * 
	 * } } } return false; }
	 */
}
