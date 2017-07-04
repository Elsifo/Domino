package it.beyondthecube.domino;

import java.io.IOException;
import java.util.Calendar;

import org.apache.commons.lang3.text.WordUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Builder;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import it.beyondthecube.domino.data.EconomyLinker;
import it.beyondthecube.domino.data.config.ConfigManager;
import it.beyondthecube.domino.exceptions.ParseException;
import it.beyondthecube.domino.politicals.City;
import it.beyondthecube.domino.politicals.Nation;
import it.beyondthecube.domino.residents.Resident;
import it.beyondthecube.domino.residents.ResidentManager;
import it.beyondthecube.domino.sets.permission.Perm;
import it.beyondthecube.domino.sets.permission.PermissionSet;
import it.beyondthecube.domino.sets.toggle.ToggleSet;
import it.beyondthecube.domino.terrain.Area;
import it.beyondthecube.domino.terrain.AreaManager;
import it.beyondthecube.domino.terrain.AreaType;
import it.beyondthecube.domino.terrain.ComLocation;

public class Utility {
	private static String decor = "      ~°~°~°~°~°";
	private static String roced = (new StringBuilder(decor)).reverse().toString();

	public static void sendConsole(String msg) {
		Sponge.getServer().getConsole().sendMessage(Text.of("[Domino]: " + msg));
	}

	public static int getDimension(ComLocation l1, ComLocation l2) {
		int dimension = (l1.getBlockX() - l2.getBlockX() + 1) * (l1.getBlockY() - l2.getBlockY() + 1)
				* (l1.getBlockZ() - l2.getBlockZ() + 1);
		return (dimension < 0) ? (-1 * dimension) : dimension;
	}

	public static boolean isTimeToCollect() throws IOException{
		long last = ConfigManager.getConfig().getLastTax();
		long now = Calendar.getInstance().getTimeInMillis();
		return (now - last > 86400000);	
	}

	public static Text pluginMessage(String msg) {
		Text t = Text.builder(msg).color(TextColors.GREEN).build();
		return Text.builder("[Domino] ").color(TextColors.GOLD).append(t).build();
	}

	public static Text terrainMessage(Location<World> l) {
		Builder s = null;
		Area a = null;
		if (!AreaManager.existsArea(l))
			return Text.builder("Wilderness").color(TextColors.GREEN).build();
		a = AreaManager.getArea(l);
		AreaType type = a.getAreaType();
		s = Text.builder("~").color(TextColors.GOLD)
				.append(Text.builder(AreaManager.getCity(a).getName()).color(TextColors.GREEN).build());
		if (!type.equals(AreaType.NONE))
			s.append(Text.builder("[")
					.color(TextColors.GOLD).append(Text.builder(WordUtils.capitalize(type.toString().toLowerCase()))
							.color(TextColors.AQUA).append(Text.builder("]").color(TextColors.GOLD).build()).build())
					.build());
		if (a.hasOwner())
			s.append(Text.builder("[")
					.color(TextColors.GOLD).append(Text.builder(a.getOwner().getNick())
							.color(TextColors.AQUA).append(Text.builder("]").color(TextColors.GOLD).build()).build())
					.build());
		if (a.isForSale())
			s.append(Text.builder(" for sale").color(TextColors.GOLD).build());
		if (a.salePrice() > 0)
			s.append(Text.builder(": " + a.salePrice() + " " + EconomyLinker.getCurrencyNamePlural())
					.color(TextColors.GOLD).build());
		return Text.of(s.build());
	}

	public static Text errorMessage(String string) {
		Text ris = Text.builder("[Domino] ").color(TextColors.DARK_RED)
				.append(Text.builder(string).color(TextColors.RED).build()).build();
		return ris;
	}

	public static String errorMessageRaw(String string) {
		String ris = "";
		ris += "[Domino] " + string;
		return ris;
	}

	public static PermissionSet parsePermissions(String perm) {
		perm = perm.substring(1, perm.length() - 1);
		String[] perms = perm.split(";");
		Perm build;
		Perm interact;
		Perm itemuse;
		String tempB = perms[0].substring(6);
		build = new Perm(tempB.substring(1, tempB.length() - 1));
		String tempI = perms[1].substring(9);
		interact = new Perm(tempI.substring(1, tempI.length() - 1));
		String tempU = perms[2].substring(8);
		itemuse = new Perm(tempU.substring(1, tempU.length() - 1));
		return new PermissionSet(build, interact, itemuse);
	}

	public static void cityInfo(Player p, City c) {
		p.sendMessage(Text.builder(decor).color(TextColors.GOLD)
				.append(Text.builder("" + c.getName() + "[" + c.getClaimedPlots() + "/" + c.getPlotNumber() + "]")
				.color(TextColors.GREEN).style(TextStyles.BOLD)
				.append(Text.builder(roced).color(TextColors.GOLD).style(TextStyles.RESET).build()).build()).build());
		p.sendMessage(Text.builder("Mayor: ").color(TextColors.GOLD).style(TextStyles.BOLD)
				.append(Text.builder(c.getMayor().getNick()).color(TextColors.GREEN)
				.style(TextStyles.RESET).build()).build());
		Builder asst = Text.builder("Assistants: ").color(TextColors.GOLD).style(TextStyles.BOLD);
		for (Resident r : c.getAssistants()) {
			asst.append(Text.builder(r.getNick() + ";").color(TextColors.GREEN).style(TextStyles.RESET).build());
		}
		p.sendMessage(asst.build());
		Builder res = Text.builder("Residents: ").color(TextColors.GOLD).style(TextStyles.BOLD);
		for (Resident r : ResidentManager.getResidents(c)) {
			res.append(Text.builder(r.getNick() + ";").color(TextColors.GREEN).style(TextStyles.RESET).build());
		}
		p.sendMessage(res.build());
		p.sendMessage(c.getPermissionSet().print());
		p.sendMessage(Text.builder("Balance: ").color(TextColors.GOLD).style(TextStyles.BOLD)
				.append(Text.builder(EconomyLinker.getBalance(c) + EconomyLinker.getCurrencyNamePlural())
						.color(TextColors.GREEN).style(TextStyles.RESET).build())
				.build());
		p.sendMessage(Text.builder("Tax: ").color(TextColors.GOLD).style(TextStyles.BOLD)
				.append(Text.builder(new String("" + c.getTax())).color(TextColors.GREEN).style(TextStyles.RESET).build()).build());
		ToggleSet toggles = c.getToggles();
		Builder toggle = Text.builder("Mobs: ").color(TextColors.GOLD).style(TextStyles.BOLD)
				.append(Text.builder(String.valueOf(toggles.getMobs() + "  ")).color(TextColors.GREEN).style(TextStyles.RESET)
				.build());
		toggle.append(Text.builder("Fire: ").style(TextStyles.BOLD).color(TextColors.GOLD)
				.append(Text.builder(String.valueOf(toggles.getFire()) + "  ").color(TextColors.GREEN).style(TextStyles.RESET)
				.build()).build()); 
		toggle.append(Text.builder("PVP: ").color(TextColors.GOLD).style(TextStyles.BOLD)
				.append(Text.builder(String.valueOf(toggles.getPvp()) + "  ").color(TextColors.GREEN).build()).style(TextStyles.RESET)
				.build());
		p.sendMessage(toggle.build());
	}

	public static void residentInfo(Player p, Resident target) {
		p.sendMessage(Text.builder(decor).color(TextColors.GOLD).append(Text.builder(target.getNick()).color(TextColors.GREEN)
				.style(TextStyles.BOLD).append(Text.builder(roced).color(TextColors.GOLD).style(TextStyles.RESET).build()).build())
				.build());		
		Builder s = Text.builder("City: ").color(TextColors.GOLD).style(TextStyles.BOLD);
		if (ResidentManager.hasCity(target))
			s.append(Text.builder(ResidentManager.getCity(target).get().getName()).color(TextColors.GREEN).style(TextStyles.RESET).build());
		p.sendMessage(s.build());
		String balance = String.valueOf(EconomyLinker.getBalance(target));
		p.sendMessage(Text.builder("Balance: ").color(TextColors.GOLD).style(TextStyles.BOLD)
				.append(Text.builder(balance).color(TextColors.GREEN).style(TextStyles.RESET).build()).build());
		String friends = "";
		for (Resident res : ResidentManager.getFriendsList(target)) {
			friends += res.getNick() + ";";
		}
		p.sendMessage(Text.builder("Friends: ").color(TextColors.GOLD).style(TextStyles.BOLD)
		.append(Text.builder(friends).color(TextColors.GREEN).style(TextStyles.RESET).build()).build());
	}

	public static void areaInfo(Player p) {
		p.sendMessage(Text.builder(decor).color(TextColors.GOLD).append(Text.builder("Area").color(TextColors.GREEN)
				.style(TextStyles.BOLD).append(Text.builder(roced).color(TextColors.GOLD).style(TextStyles.RESET).build()).build())
				.build());
		if (AreaManager.existsArea(p.getLocation())) {
			Builder s = Text.builder("City: ").color(TextColors.GOLD).style(TextStyles.BOLD);
			Area a = AreaManager.getArea(p.getLocation());
			p.sendMessage(s.append(Text.builder(AreaManager.getCity(a).getName()).color(TextColors.GREEN).style(TextStyles.RESET)
					.build()).build());
			p.sendMessage(a.getPermissionSet().print());
			p.sendMessage(Text.builder("Tax: ").color(TextColors.GOLD).style(TextStyles.BOLD)
					.append(Text.builder(new String("" + a.getTax())).color(TextColors.GREEN).style(TextStyles.RESET).build())
					.build());
			s = Text.builder("Owner: ").color(TextColors.GOLD).style(TextStyles.BOLD);
			if (a.hasOwner()) {
				Resident r = a.getOwner();
				p.sendMessage(s.append(Text.builder(r.getNick()).color(TextColors.GREEN).style(TextStyles.RESET).build()).build());
			}
		} else {
			p.sendMessage(Text.builder("City: ").color(TextColors.GOLD).style(TextStyles.BOLD).build());
			p.sendMessage(Text.builder("Tax: ").color(TextColors.GOLD).style(TextStyles.BOLD).build());
			p.sendMessage(Text.builder("Owner: ").color(TextColors.GOLD).style(TextStyles.BOLD).build());
		}
	}

	public static PermissionSet setNewPermission(PermissionSet oldpset, String string, String string2, String string3)
			throws ParseException {
		if (!(string3.equals("true") || string3.equals("false")))
			throw new ParseException(string3);
		boolean value = Boolean.valueOf(string3);
		Perm perm;
		switch (string) {
		case "build":
			perm = oldpset.getBuild();
			break;
		case "interact":
			perm = oldpset.getInteract();
			break;
		case "itemuse":
			perm = oldpset.getItemUse();
			break;
		default:
			throw new ParseException(string);
		}
		switch (string2) {
		case "owner":
			perm.setOwner(value);
		case "friends":
			perm.setFriend(value);
			break;
		case "citizens":
			perm.setCitizen(value);
			break;
		case "ally":
			perm.setAlly(value);
			break;
		case "all":
			perm.setAll(value);
			break;
		default:
			throw new ParseException(string2);
		}

		return oldpset;
	}

	public static boolean isBetween(int block, int block2, int block3) {
		if (block2 < block3) {
			return (block > block2) && (block < block3);
		} else
			return (block > block3) && (block < block2);
	}

	public static Text cityChatMessage(Player sender, String msg) {
		return Text.builder("[").append(Text.builder("CC").color(TextColors.GREEN)
				.append(Text.builder("]").color(TextColors.WHITE)
				.append(Text.builder(" " + sender.getName()).color(TextColors.GREEN)
				.append(Text.builder(": ").color(TextColors.WHITE)
				.append(Text.builder(msg).color(TextColors.GREEN).build()).build()).build()).build()).build()).build();
	}

	public static void nationInfo(Nation n, Player p) {
		p.sendMessage(Text.of(TextColors.GOLD + decor + TextColors.GREEN + "" + TextStyles.BOLD + n.getName()
				+ TextColors.RESET + TextColors.GOLD + roced));
		p.sendMessage(Text.of(TextColors.GOLD + "" + TextStyles.BOLD + "Capital: " + TextColors.RESET + TextColors.GREEN
				+ n.getCapital().getName()));
		p.sendMessage(Text.of(TextColors.GOLD + "" + TextStyles.BOLD + "Balance: " + TextColors.RESET + TextColors.GREEN
				+ EconomyLinker.getBalance(n) + EconomyLinker.getCurrencyNamePlural()));
		p.sendMessage(Text.of(
				TextColors.GOLD + "" + TextStyles.BOLD + "Tax: " + TextColors.RESET + TextColors.GREEN + n.getTax()));
	}

	public static boolean isMonster(Entity e) {
		if (!(e instanceof Living))
			return false;
		switch (e.getType().getName().toUpperCase()) {
		case "CREEPER":
		case "ENDERMAN":
		case "SKELETON":
		case "SPIDER":
		case "ZOMBIE":
		case "BLAZE":
		case "GHAST":
		case "PIG_ZOMBIE":
		case "ZOMBIE_VILLAGER":
		case "MAGMA_CUBE":
		case "WITHER_SKELETON":
		case "SLIME":
		case "VINDICATOR":
		case "ELDER_GUARDIAN§":
		case "CAVE_SPIDER":
		case "ENDERMITE":
		case "WITCH":
			return true;
		default:
			return false;
		}
	}

	public static Text nationChatMessage(Player sender, String msg) {
		return Text.builder("[").append(Text.builder("NC").color(TextColors.LIGHT_PURPLE)
				.append(Text.builder("] ").color(TextColors.WHITE )
				.append(Text.builder(sender.getName()).color(TextColors.LIGHT_PURPLE)
				.append(Text.builder(": ").color(TextColors.WHITE)
				.append(Text.builder(msg).color(TextColors.LIGHT_PURPLE).build()).build()).build()).build()).build()).build(); 
	}
}
