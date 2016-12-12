package it.beyondthecube.domino.commands;

import java.util.Optional;
import java.util.UUID;

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
import it.beyondthecube.domino.data.database.DatabaseManager;
import it.beyondthecube.domino.exceptions.AreaBoundsException;
import it.beyondthecube.domino.exceptions.CityNotFoundException;
import it.beyondthecube.domino.exceptions.DatabaseException;
import it.beyondthecube.domino.exceptions.InsufficientRankException;
import it.beyondthecube.domino.exceptions.ParseException;
import it.beyondthecube.domino.politicals.City;
import it.beyondthecube.domino.politicals.PoliticalManager;
import it.beyondthecube.domino.residents.Resident;
import it.beyondthecube.domino.residents.ResidentManager;
import it.beyondthecube.domino.sets.permission.PermissionSet;
import it.beyondthecube.domino.terrain.Area;
import it.beyondthecube.domino.terrain.AreaManager;
import it.beyondthecube.domino.terrain.AreaType;
import it.beyondthecube.domino.terrain.ComLocation;

public class AreaCommandHandler implements CommandExecutor {
	private void showHelp(Player p) {
		p.sendMessage(Text.of(TextColors.GOLD + "Command " + TextColors.GREEN + "/area" + TextColors.GOLD + " usage:"));
		p.sendMessage(Text.of(TextColors.GREEN + "/area" + TextColors.WHITE + " - shows area info"));
		p.sendMessage(
				Text.of(TextColors.GREEN + "/area claim" + TextColors.WHITE + " - claim an area from 2 selections"));
		p.sendMessage(Text.of(TextColors.GREEN + "/area <forsale|notforsale>" + TextColors.WHITE
				+ " - sets an area for sale or not for sale"));
		p.sendMessage(Text.of(TextColors.GREEN + "/area buy" + TextColors.WHITE + " - buy an area for sale"));
		p.sendMessage(Text.of(TextColors.GREEN + "/area set type <type>" + TextColors.WHITE + " - set area type"));
		p.sendMessage(Text.of(TextColors.GREEN + "/area set tax <tax>" + TextColors.WHITE + " - set area tax"));
		p.sendMessage(Text
				.of(TextColors.GREEN + "/area perm <build|interact|itemuse> <owner|friends|citizens|all> <true|false>"
						+ TextColors.WHITE + " - sets a permission"));
		p.sendMessage(Text
				.of(TextColors.GREEN + "/area unclaim " + TextColors.WHITE + " - remove your ownership of an area"));
		p.sendMessage(Text.of(TextColors.GREEN + "/area delete " + TextColors.WHITE + " - delete an area"));
	}

	public AreaCommandHandler(Domino plugin) {
		CommandSpec perm = CommandSpec.builder().executor(this::executePerm)
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("type"))),
						GenericArguments.onlyOne(GenericArguments.string(Text.of("target"))),
						GenericArguments.onlyOne(GenericArguments.bool(Text.of("mode"))))
				.build();
		CommandSpec type = CommandSpec.builder().executor(this::executeType)
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("type")))).build();
		CommandSpec tax = CommandSpec.builder().executor(this::executeTax)
				.arguments(GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("amount")))).build();
		CommandSpec set = CommandSpec.builder().child(type, "type").child(tax, "tax").build();
		CommandSpec forsale = CommandSpec.builder().executor(this::executeForsale).arguments(
				GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("amount")))))
				.build();
		CommandSpec area = CommandSpec.builder().executor(this)
				.arguments(GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.string(Text.of("mode")))))
				.child(forsale, "forsale").child(perm, "perm").child(set, "set").build();
		Sponge.getCommandManager().register(plugin, area, "area");
	}

	public CommandResult executeForsale(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		Optional<Object> oo = args.getOne("amount");
		double saleprice = 0;
		if (oo.isPresent()) {
			try {
				saleprice = Double.parseDouble(oo.get().toString());
			} catch (NumberFormatException e) {
				p.sendMessage((Utility.pluginMessage("Price not valid")));
				return CommandResult.success();
			}
		}
		Area a = AreaManager.getArea(p.getLocation());
		if (AreaManager.canSell(p, a)) {
			p.sendMessage((Utility.pluginMessage("Area set for sale")));
			AreaManager.setForSale(a, saleprice);
		} else
			p.sendMessage((Utility.pluginMessage("Can't set this area for sale")));
		return CommandResult.success();
	}

	public CommandResult executePerm(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		try {
			AreaManager.setPermissions(ResidentManager.getResident(p.getUniqueId()), p.getLocation(),
					(String) args.getOne("type").get(), (String) args.getOne("target").get(),
					(String) args.getOne("mode").get());
		} catch (InsufficientRankException e) {
			p.sendMessage((Utility.pluginMessage(e.getMessage())));
		} catch (ParseException e) {
			p.sendMessage(Text.of(Utility.pluginMessage("Syntax error: " + e.getMessage() + " not recognized")));
		} catch (DatabaseException e) {
			p.sendMessage((Utility.errorMessage("ERROR: contact an administrator")));
		} catch (CityNotFoundException e) {
			p.sendMessage((Utility.pluginMessage("You don't belong to this city")));
		}
		return CommandResult.success();
	}

	public CommandResult executeType(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		Resident r = ResidentManager.getResident(p.getUniqueId());
		try {
			Area a = AreaManager.getArea(p.getLocation());
			City c = AreaManager.getCity(a);
			if (!(c.getAssistants().contains(r) || c.getMayor().equals(r))) {
				p.sendMessage((Utility.pluginMessage("Insufficient rank")));
			}
			if (!a.isPlotClaim()) {
				p.sendMessage((Utility.pluginMessage("Can't set type of 3D area. Only plots are allowed to settype")));
				return CommandResult.success();
			}
			AreaType type = AreaType.valueOf(((String) args.getOne("type").get()).toUpperCase());
			DatabaseManager.getInstance().setAreaType(a, type);
			AreaManager.setAreaType(a, type);
			if (type.equals(AreaType.TOWNHALL)) {
				PoliticalManager.changeTownHall(a, c, false, false);
				PoliticalManager.setSpawn(ComLocation.getComLocation(p.getLocation()),
						ResidentManager.getCity(ResidentManager.getResident(p.getUniqueId())));
			}
		} catch (DatabaseException e) {
			p.sendMessage((Utility.errorMessage("ERROR: contact an administrator")));
		}
		return CommandResult.success();
	}

	public CommandResult executeTax(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		try {
			double d = Double.parseDouble(args.getOne("amount").get().toString());
			AreaManager.setTax(AreaManager.getArea(p.getLocation()), d);
			p.sendMessage((Utility.pluginMessage(
					"Area tax has been set to " + TextColors.GOLD + d + " " + EconomyLinker.getCurrencyNamePlural())));
		} catch (NumberFormatException e) {
			p.sendMessage((Utility.pluginMessage("Not a valid amount")));
		} catch (DatabaseException e) {
			p.sendMessage((Utility.errorMessage("ERROR: contact an administrator")));
		}
		return CommandResult.success();
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player))
			return CommandResult.empty();
		Player p = (Player) src;
		Resident r = ResidentManager.getResident(p.getUniqueId());
		if (!args.getOne("mode").isPresent()) {
			Utility.areaInfo(p);
			return CommandResult.success();
		}
		switch ((String) args.getOne("mode").get()) {
		case "help": {
			showHelp(p);
			return CommandResult.success();
		}

		case "notforsale": {
			Area a = AreaManager.getArea(p.getLocation());
			if (AreaManager.canSell(p, a)) {
				p.sendMessage((Utility.pluginMessage("Area set not for sale")));
				a.notForSale();
			} else
				p.sendMessage((Utility.pluginMessage("Can't set this area not for sale")));
			return CommandResult.success();
		}
		case "claim": {
			if (r == null)
				return CommandResult.success();
			try {
				if (ResidentManager.hasSelections(r)) {
					City c = ResidentManager.getCity(r);
					if (c.isAssistant(r) || c.isMayor(r)) {
						AreaManager.newArea(UUID.randomUUID(), r.getSelection1(), r.getSelection2(), r, c, false,
								new PermissionSet(), false, false, AreaType.NONE, 0, 0);
						p.sendMessage((Utility.pluginMessage("Area claimed")));
					} else
						p.sendMessage((Utility.pluginMessage("Insufficient rank")));
					ResidentManager.deactivateSelection(p);
				} else
					p.sendMessage((Utility.pluginMessage("You didn't select the area limits")));
			} catch (DatabaseException e) {
				p.sendMessage((Utility.errorMessage("ERROR: contact ad administrator")));
			} catch (AreaBoundsException e) {
				p.sendMessage((Utility.pluginMessage("Portion of your selection is already claimed")));
			}
			return CommandResult.success();
		}
		case "unclaim": {
			Area a;
			try {
				a = AreaManager.getArea(p.getLocation());
				if (AreaManager.canSell(p, a)) {
					DatabaseManager.getInstance().setOwner(a, null);
					AreaManager.setOwner(a, null);
					AreaManager.setForSale(a, 0);
					p.sendMessage((Utility.pluginMessage("Area unclaimed successfully")));
				} else
					p.sendMessage((Utility.pluginMessage("Can't unclaim this area")));
			} catch (DatabaseException e) {
				p.sendMessage((Utility.errorMessage("ERROR: contact an administrator")));
			}
			return CommandResult.success();
		}
		case "delete": {
			if (r == null)
				return CommandResult.success();
			try {
				Area a = AreaManager.getArea(p.getLocation());
				if (AreaManager.getCity(a).isAssistant(r) || AreaManager.getCity(a).isMayor(r)) {
					if (a.getAreaType().equals(AreaType.TOWNHALL)) {
						p.sendMessage((Utility.pluginMessage("Townhall is not deletable")));
						return CommandResult.success();
					}
					AreaManager.unclaimArea(a);
					p.sendMessage((Utility.pluginMessage("Area successfully deleted")));
				} else
					p.sendMessage((Utility.pluginMessage("Insufficient rank")));
			} catch (DatabaseException e) {
				p.sendMessage((Utility.errorMessage("ERROR: contact an administrator")));
			}
			return CommandResult.success();
		}
		case "buy": {
			if (r == null)
				return CommandResult.success();
			try {
				if (!ResidentManager.hasCity(r))
					p.sendMessage((Utility.pluginMessage("You don't belong to a city")));
				if (ResidentManager.getCity(r).equals(AreaManager.getCity(AreaManager.getArea(p.getLocation())))) {
					if (AreaManager.getArea(p.getLocation()).isForSale()) {
						if (AreaManager.acquireArea(p, p.getLocation()))
							p.sendMessage((Utility.pluginMessage("Area bought")));
						else
							p.sendMessage((Utility.pluginMessage("Can't buy this area")));
					} else
						p.sendMessage((Utility.pluginMessage("This area is not for sale")));

				} else
					p.sendMessage((Utility.pluginMessage("You don't belong to this city")));
			} catch (DatabaseException e) {
				p.sendMessage((Utility.errorMessage("ERROR: contact an administrator")));
			}
			return CommandResult.success();
		}

		default:
			showHelp(p);
		}
		return CommandResult.success();
	}

}