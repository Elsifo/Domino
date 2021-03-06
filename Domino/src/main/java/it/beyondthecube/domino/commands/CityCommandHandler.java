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
import org.spongepowered.api.world.Chunk;

import it.beyondthecube.domino.Domino;
import it.beyondthecube.domino.Utility;
import it.beyondthecube.domino.data.EconomyLinker;
import it.beyondthecube.domino.data.config.ConfigManager;
import it.beyondthecube.domino.data.database.DatabaseManager;
import it.beyondthecube.domino.exceptions.DatabaseException;
import it.beyondthecube.domino.exceptions.InsufficientRankException;
import it.beyondthecube.domino.exceptions.ParseException;
import it.beyondthecube.domino.politicals.City;
import it.beyondthecube.domino.politicals.Nation;
import it.beyondthecube.domino.politicals.PoliticalManager;
import it.beyondthecube.domino.residents.Citizenship;
import it.beyondthecube.domino.residents.Resident;
import it.beyondthecube.domino.residents.ResidentManager;
import it.beyondthecube.domino.tasks.CitizenshipTask;
import it.beyondthecube.domino.tasks.CitySpawnTask;
import it.beyondthecube.domino.tasks.TaskManager;
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
		p.sendMessage(Text.builder("Command ").color(TextColors.GOLD)
				.append(Text.builder("/city").color(TextColors.GREEN)
				.append(Text.builder(" usage").color(TextColors.GOLD).build()).build()).build());
		p.sendMessage(Text.builder("/city [name]").color(TextColors.GREEN).append(Text.builder(
				" - shows own/other city info").color(TextColors.WHITE).build()).build()); 
		p.sendMessage(Text.builder("/city list").color(TextColors.GREEN).append(Text.builder(
				" - list all cities").color(TextColors.WHITE).build()).build()); 
		p.sendMessage(Text.builder("/city leave").color(TextColors.GREEN).append(Text.builder(
				" - leave your current city").color(TextColors.WHITE).build()).build());
		p.sendMessage(Text.builder("/city claim|unclaim").color(TextColors.GREEN).append(Text.builder(
				" - add/remove a plot claim").color(TextColors.WHITE).build()).build());
		p.sendMessage(Text.builder("/city spawn").color(TextColors.GREEN).append(Text.builder(
				" - teleport to your city spawnpoint").color(TextColors.WHITE).build()).build());
		p.sendMessage(Text.builder("/city <add|remove> <player>").color(TextColors.GREEN).append(Text.builder(
				" - add/remove a player from your city").color(TextColors.WHITE).build()).build());
		p.sendMessage(Text.builder("/city <add|remove> assistant <player>").color(TextColors.WHITE)
				.append(Text.builder(" - add/remove assistants from your city")
						.color(TextColors.GREEN).build()).build());
		p.sendMessage(Text.builder("/city <withdraw|deposit> <amount>").color(TextColors.GREEN)
				.append(Text.builder(" - withdraw/deposit amount from/in your city account")
				.color(TextColors.WHITE).build()).build());
		p.sendMessage(Text.builder("/city set tax <amount>").color(TextColors.GREEN).append(Text.builder(
				" - set city tax").color(TextColors.WHITE).build()).build());
		p.sendMessage(Text.builder("/city perm <build|interact|itemuse> " +
				"<owner|friends|citizens|all> <true|false>").color(TextColors.GREEN)
				.append(Text.builder(" - sets a permission").color(TextColors.WHITE).build()).build());
		p.sendMessage(Text.builder("/city toggle <pvp|fire|mobs>").color(TextColors.GREEN).append(Text.builder(
				" - turn pvp, fire or mobs on/off").color(TextColors.WHITE).build()).build());
	}

	public CommandResult executeToggle(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		Resident r = ResidentManager.getResident(p.getUniqueId());
		Optional<City> oc = ResidentManager.getCity(r);
		if(!oc.isPresent()) {
			p.sendMessage(Utility.pluginMessage("Not part of any town"));
			return CommandResult.success();
		}
		City c = oc.get();
		try {
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
						may.getNick() + " is still a citizen of " + ResidentManager.getCity(may).get().getName())));
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
		Optional<City> oc = ResidentManager.getCity(r);
		if(!oc.isPresent()) {
			p.sendMessage(Utility.pluginMessage("Not part of any town"));
			return CommandResult.success();
		}
		City c = oc.get();
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
		TaskManager.getInstance().newTask(added, new CitizenshipTask(new Citizenship(added, r, c, plugin)), 1200);
		return CommandResult.success();
	}

	public CommandResult executeRemove(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		Resident remover = ResidentManager.getResident(p.getUniqueId());
		Optional<City> oc = ResidentManager.getCity(remover);
		if(!oc.isPresent()) {
			p.sendMessage(Utility.pluginMessage("Not part of any town"));
			return CommandResult.success();
		}
		Optional<Resident> or = ResidentManager.getResident((String) args.getOne("player").get());
		if(or.isPresent()) {
			p.sendMessage((Utility.pluginMessage("Player not found")));
			return CommandResult.success();
		}
		Resident removed = or.get();
		try {
			ResidentManager.removeResident(remover, removed, oc.get());
			return CommandResult.success();
		} catch (InsufficientRankException e) {
			p.sendMessage((Utility.pluginMessage("Can't perform this command" + e.getMessage())));
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
		Optional<City> oc = ResidentManager.getCity(r);
		if(!oc.isPresent()) {
			p.sendMessage(Utility.pluginMessage("Not part of any town"));
			return CommandResult.success();
		}
		City c = oc.get();
		try {
			PoliticalManager.setPermissions(r, c, ((String) args.getOne("type").get()),
					((String) args.getOne("target").get()), String.valueOf(args.getOne("mode").get()));
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
		Optional<City> oc = ResidentManager.getCity(r);
		if(!oc.isPresent()) {
			p.sendMessage(Utility.pluginMessage("Not part of any town"));
			return CommandResult.success();
		}
		City c = oc.get();
		try {
			double amount = Double.parseDouble((String) args.getOne("amount").get());
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
		Optional<City> oc = ResidentManager.getCity(r);
		if(!oc.isPresent()) {
			p.sendMessage(Utility.pluginMessage("Not part of any town"));
			return CommandResult.success();
		}
		City c = oc.get();
		try {
			if (c.isMayor(r)) {
				Optional<Resident> my = ResidentManager.getResident((String) args.getOne("mayor").get());
				if(!my.isPresent()) {
					p.sendMessage((Utility.pluginMessage("Player not found")));
					return CommandResult.success();
				}
				Resident mayor = my.get();
				if (!ResidentManager.getCity(mayor).equals(c)) {
					p.sendMessage((Utility.pluginMessage("You can only set your citizens as mayor")));
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
		Optional<City> oc = ResidentManager.getCity(r);
		if(!oc.isPresent()) {
			p.sendMessage(Utility.pluginMessage("Not part of any town"));
			return CommandResult.success();
		}
		Optional<Resident> ass = ResidentManager.getResident((String)args.getOne("player").get());
		if(!ass.isPresent()) {
			p.sendMessage((Utility.pluginMessage("Player not found")));
		}
		try {
			PoliticalManager.addAssistant(r,ass.get());
		} catch (InsufficientRankException e) {
			p.sendMessage((Utility.pluginMessage("Insufficient rank")));
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
		Optional<Resident> rm = ResidentManager.getResident((String) args.getOne("player").get());
		if(!(rm.isPresent())) {
			p.sendMessage((Utility.pluginMessage("Player not found")));
			return CommandResult.success();
		}
		Resident removed = rm.get();
		try {
			if(!PoliticalManager.removeAssistant(r, removed)) {
				p.sendMessage(Utility.pluginMessage("Can't execute this command"));
			}
			p.sendMessage((Utility.pluginMessage(removed.getNick() + " correctly removed from assistants")));
			Optional<Player> op = Sponge.getServer().getPlayer(removed.getPlayer());
			if (op.isPresent())
				op.get().sendMessage((Utility.pluginMessage("You've been removed from assistants")));
		} catch (InsufficientRankException e) {
			p.sendMessage((Utility.pluginMessage("Insufficient rank")));
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
		Optional<City> oc = ResidentManager.getCity(r);
		if(!oc.isPresent()) {
			p.sendMessage(Utility.pluginMessage("Not part of any town"));
			return CommandResult.success();
		}
		City c = oc.get();
		try {
			Double d = Double.valueOf((String) args.getOne("amount").get());
			if (EconomyLinker.canAfford(r.getPlayer(), d)) {
				EconomyLinker.deposit(c, d);
				EconomyLinker.withdrawPlayer(r.getPlayer(), d);
				p.sendMessage((Utility.pluginMessage("You've deposited " + d + " into your town bank")));
			} else
				p.sendMessage((Utility.pluginMessage("You can't deposit this much")));
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
			p.sendMessage(Utility.pluginMessage("Not part of any town"));
			return CommandResult.success();
		}
		City c = oc.get();
		if (c.isAssistant(r) || c.isMayor(r)) {
			Double d = Double.valueOf((String) args.getOne("amount").get());
			if (EconomyLinker.canBankAfford(String.valueOf(c.getID()), d)) {
				EconomyLinker.withdraw(c, d);
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
		Optional<City> oc = ResidentManager.getCity(r);
		if(!oc.isPresent()) {
			p.sendMessage(Utility.pluginMessage("Not part of any town"));
			return CommandResult.success();
		}
		City c = oc.get();
		Collection<Object> cobj = args.getAll("message");
		String msg = "";
		for (Object o : cobj)
			msg += (String) o + " ";
		Text m = Utility.cityChatMessage(p, msg);
		for (Player rec : Sponge.getServer().getOnlinePlayers()) {
			Resident rs = ResidentManager.getResident(rec.getUniqueId());
			if (ResidentManager.hasCity(rs)) {
				if (ResidentManager.getCity(rs).get().equals(c)) {
					rec.sendMessage(m);
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
		Optional<City> oc = ResidentManager.getCity(r);
		Optional<String> os = args.getOne("mode");
		if (os.isPresent()) {
			if(!oc.isPresent()) {
				p.sendMessage(Utility.pluginMessage("Not part of any town"));
				return CommandResult.success();
			}
			City c = oc.get();
			switch (os.get()) {
			case "help": {
				showHelp(p);
				return CommandResult.success();
			}
			case "spawn": {
				double price = ConfigManager.getConfig().getSpawnPrice();
				if (EconomyLinker.canAfford(r.getPlayer(), price)) {
					ComLocation spawn = c.getSpawn();
					if (spawn == null)
						p.sendMessage((Utility.pluginMessage("Your city has no spawn point!")));
					else {
						TaskManager t = TaskManager.getInstance();
						long timer = ConfigManager.getConfig().getSpawnDelay();
						if(p.hasPermission("domino")) t.newTask(r, new CitySpawnTask(c, p), 0);
						else t.newTask(r, new CitySpawnTask(c, p), timer);
						p.sendMessage(Utility.pluginMessage("City spawn request accepted, don't move for" + timer/20 +" seconds..."));
					}
				} else
					p.sendMessage((Utility.pluginMessage("Can't afford city spawn")));
				return CommandResult.success();
			}
			case "claim": {
				if(c.getPlotNumber() == c.getClaimedPlots()) {
					p.sendMessage(Utility.pluginMessage("You've reached city plot limit"));
					return CommandResult.success();
				}
				try {
					if(!EconomyLinker.canBankAfford(c.getName() + "" + c.getID(),ConfigManager.getConfig().getClaimPrice())) {
						p.sendMessage(Utility.pluginMessage("Town can't afford chunk claim"));
						return CommandResult.success();					
					}
					p.sendMessage((Utility.pluginMessage("Processing plot claim")));
					Chunk k = p.getWorld().getChunkAtBlock(p.getLocation().getBlockPosition()).get();
					if (!(PoliticalManager.plotAlreadyClaimed(k, c))) {
						if (PoliticalManager.claim(c, k, r))
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
					if (c.isMayor(r)) {
						p.sendMessage((Utility.pluginMessage("You can't leave the town you're mayor of!")));
						return CommandResult.success();
					}
					ResidentManager.removeResident(r, r, c);
					p.sendMessage((Utility.pluginMessage("You left your town")));
					for(Resident rs : ResidentManager.getResidents(c)) {
						Optional<Player> prs = Sponge.getServer().getPlayer(rs.getPlayer());
						if(prs.isPresent())
							prs.get().sendMessage(Utility.pluginMessage(p.getName() + " left town"));
					}
				} catch (DatabaseException e) {
					p.sendMessage((Utility.errorMessage("ERROR: contact an administrator")));
				} catch (InsufficientRankException e) {
					p.sendMessage((Utility.pluginMessage("Insufficient rank")));
				}
				return CommandResult.success();
			}
			case "online": {
				String ris = "";
				for(Resident rs : ResidentManager.getResidents(c)) {
					Optional<Player> prs = Sponge.getServer().getPlayer(rs.getPlayer());
					if(prs.isPresent())
						ris += prs.get().getName() + ",";
				}
				p.sendMessage(Utility.pluginMessage("Online players:\n"+ris));
			}
			default: {
				Optional<City> ct = PoliticalManager.getCity(os.get());
				if(ct.isPresent()) {
					Utility.cityInfo(p, ct.get());
					return CommandResult.success();
				} else  {
					p.sendMessage((Utility.pluginMessage("City not found")));
					return CommandResult.success();
				}
			}
			}
		} else {
			if (!ResidentManager.hasCity(r)) {
				p.sendMessage((Utility.pluginMessage("Not part of any town")));
				return CommandResult.success();
			}
			Utility.cityInfo(p, oc.get());
			return CommandResult.success();
		}
	}
}
