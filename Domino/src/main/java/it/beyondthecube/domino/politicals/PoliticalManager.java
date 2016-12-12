package it.beyondthecube.domino.politicals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import it.beyondthecube.domino.Utility;
import it.beyondthecube.domino.data.EconomyLinker;
import it.beyondthecube.domino.data.database.DatabaseManager;
import it.beyondthecube.domino.exceptions.CityNotFoundException;
import it.beyondthecube.domino.exceptions.DatabaseException;
import it.beyondthecube.domino.exceptions.InsufficientRankException;
import it.beyondthecube.domino.exceptions.ParseException;
import it.beyondthecube.domino.residents.Resident;
import it.beyondthecube.domino.residents.ResidentManager;
import it.beyondthecube.domino.sets.permission.Perm;
import it.beyondthecube.domino.sets.permission.PermissionSet;
import it.beyondthecube.domino.sets.toggle.ToggleSet;
import it.beyondthecube.domino.terrain.Area;
import it.beyondthecube.domino.terrain.AreaManager;
import it.beyondthecube.domino.terrain.AreaManager.ActionType;
import it.beyondthecube.domino.terrain.AreaType;
import it.beyondthecube.domino.terrain.ComLocation;

public class PoliticalManager {
	private static ArrayList<Nation> nations = new ArrayList<>();
	private static HashMap<City, Nation> cities = new HashMap<>();
	private static HashMap<String, City> search = new HashMap<>();

	public static void createCity(Player p, String name, Resident mayor, Nation n, boolean iscapital, Location<World> spawn,
			double tax) throws DatabaseException {
		int iddb;
		try {
			iddb = DatabaseManager.getInstance().createCity(name, mayor, n, iscapital, spawn);
			City c = new City(iddb, name, null, null, mayor, null, new PermissionSet(), new ComLocation(iddb,spawn), tax, new ToggleSet());
			DatabaseManager.getInstance().addResident(mayor, c);
			ResidentManager.setCity(mayor, c);
			c.claim(Sponge.getServer().getPlayer(mayor.getPlayer()).get().getLocation().getExtent()
					.getChunkAtBlock(p.getLocation().getBlockPosition()).get(), true);
			if (!cities.containsKey(c)) {
				cities.put(c, n);
				search.put(name, c);
			}
			EconomyLinker.createCityBank(c);
		} catch (CityNotFoundException e1) {
			e1.printStackTrace();
		}
	}

	public static City loadCity(int dbid, String name, Resident mayor, Nation n, PermissionSet pset, ComLocation spawn,
			double tax, ToggleSet toggles) {
		City c = new City(dbid, name, null, null, mayor, null, pset, spawn, tax, toggles);
		cities.put(c, n);
		search.put(name, c);
		return c;
	}

	public static void createNation(String name) throws DatabaseException {
		int dbid = DatabaseManager.getInstance().createNation(name);
		Nation n = new Nation(dbid, name, 0);
		nations.add(n);
		EconomyLinker.createNationBank(n);
	}

	public static Nation getNation(String c) {
		Iterator<Nation> i = nations.iterator();
		while (i.hasNext()) {
			Nation n = i.next();
			if (n.getName().equals(c))
				return n;
		}
		return null;
	}

	public static ArrayList<String> getNationsList() {
		ArrayList<String> s = new ArrayList<>();
		for (Nation n : nations) {
			s.add(n.getName());
		}
		return s;
	}

	public static ArrayList<City> getCitiesList() {
		ArrayList<City> s = new ArrayList<>();
		for (City c : cities.keySet()) {
			s.add(c);
		}
		return s;
	}

	public static City getCity(String string) throws CityNotFoundException {
		City c = search.get(string);
		if (c == null)
			throw new CityNotFoundException();
		return c;
	}

	public static Nation getNation(City c) {
		return cities.get(c);
	}

	public static void addAssistant(Resident adder, Resident added)
			throws InsufficientRankException, CityNotFoundException, DatabaseException {
		City c = ResidentManager.getCity(adder);
		if (c.isMayor(adder)) {
			DatabaseManager.getInstance().addAssistant(added);
			c.addAssistant(added);
		} else
			throw new InsufficientRankException(adder, "Insufficient rank");
	}

	public static void removeAssistant(Resident remover, Resident removed)
			throws InsufficientRankException, CityNotFoundException, DatabaseException {
		City c = ResidentManager.getCity(remover);
		if (c.isMayor(remover)) {
			DatabaseManager.getInstance().removeAssistant(removed);
			c.removeAssistant(removed);
		} else
			throw new InsufficientRankException(remover, "Insufficient rank");
	}

	public static void loadNation(int i, String name, double tax) {
		Nation n = new Nation(i, name, tax);
		nations.add(n);
	}

	public static Nation getNationFromDBID(int dbid) {
		for (Nation n : nations) {
			if (n.getID() == dbid)
				return n;
		}
		return null;
	}

	public static City getCity(int iddb) {
		for (City c : cities.keySet()) {
			if (c.getID() == iddb)
				return c;
		}
		return null;
	}

	public static void loadClaim(City c, Chunk chunk) {
		c.loadClaim(chunk);
	}

	public static boolean claim(City city, Chunk chunk, Resident r) throws DatabaseException {
		if (AreaManager.isClaimable(chunk.getLocation(chunk.getBlockMin()), r)) {
			city.claim(chunk, false);
			return true;
		} else
			return false;
	}

	public static boolean canPerformAction(Resident r, City c, ActionType at) {
		if (c.isMayor(r) || c.isAssistant(r))
			return true;
		Perm cperms = null;
		switch (at) {
		case BUILD:
			cperms = c.getPermissionSet().getBuild();
			break;
		case INTERACT:
			cperms = c.getPermissionSet().getInteract();
			break;
		case ITEMUSE:
			cperms = c.getPermissionSet().getItemUse();
			break;
		}
		if (!ResidentManager.hasCity(r))
			return cperms.getAll();
		City cres = ResidentManager.getCity(r);
		if (cres.equals(c) && cperms.getCitizen() == true)
			return true;
		if (cities.get(cres).equals(cities.get(c)) && cperms.getAlly() == true)
			return true;
		else
			return cperms.getAll();
	}

	public static void loadAssistant(Resident res, City c) {
		c.addAssistant(res);
	}

	public static void setPermissions(Resident r, City c, String string, String string2, String string3)
			throws ParseException, DatabaseException, InsufficientRankException {
		if (c.isAssistant(r) || c.isMayor(r)) {
			PermissionSet pset = Utility.setNewPermission(c.getPermissionSet(), string, string2, string3);
			DatabaseManager.getInstance().setPermissions(c, pset);
			c.setPermissionSet(pset);
		} else
			throw new InsufficientRankException(r, "Insufficient rank");
	}

	public static void setMayor(City city, Resident resident) throws DatabaseException {
		DatabaseManager.getInstance().setMayor(city, resident);
		city.setMayor(resident);
	}

	public static void setSpawn(ComLocation l, City c) {
		c.setSpawn(l);
	}

	public static void unclaim(Resident r, Location<World> l)
			throws InsufficientRankException, CityNotFoundException, DatabaseException {
		City c = ResidentManager.getCity(r);
		if (!c.isMayor(r) && !c.isAssistant(r))
			throw new InsufficientRankException(r, "");
		Area a = AreaManager.getArea(l);
		if (!a.isPlotClaim())
			return;
		c.unclaim(a);
		AreaManager.removeArea(a);
	}

	public static void changeTownHall(Area a, City c, boolean isloading, boolean iscreating) {
		if (!isloading)
			if (!iscreating)
				AreaManager.setAreaType(c.getTownHall(), AreaType.NONE);
		c.setTownHall(a);
	}

	public static void collectTaxes() {
		for (City c : cities.keySet()) {
			Nation n = cities.get(c);
			if (n != null) {
				double tax = n.getTax();
				EconomyLinker.withdraw(c, tax);
				EconomyLinker.deposit(n, tax);
			}
		}
	}

	public static void setTax(City c, double d) throws DatabaseException {
		DatabaseManager.getInstance().setTax(c, d);
		c.setTax(d);
	}

	public static void setNation(City c, Nation n) {
		cities.put(c, n);
	}

	public static boolean plotAlreadyClaimed(Chunk chunk, City c) {
		return c.ownedChunk(chunk);
	}

	public static boolean isValidCity(String string) {
		return search.containsKey(string);
	}

	public static void setTax(Nation n, double am) throws DatabaseException {
		DatabaseManager.getInstance().setTax(n, am);
		n.setTax(am);
	}
}