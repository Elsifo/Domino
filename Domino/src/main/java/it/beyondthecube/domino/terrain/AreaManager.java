package it.beyondthecube.domino.terrain;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;

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
import it.beyondthecube.domino.sets.permission.Perm;
import it.beyondthecube.domino.sets.permission.PermissionSet;

public class AreaManager {
	public static HashMap<Area, City> areas = new HashMap<>();

	public enum ActionType {
		BUILD, INTERACT, ITEMUSE
	}
	
	public static void loadArea(UUID id, ComLocation l1, ComLocation l2, Resident owner, City c,
			PermissionSet pset, boolean isplotclaim, AreaType type, double tax,
			double saleprice) throws AreaBoundsException, DatabaseException {
		Area a = new Area(id, l1, l2,
				owner, pset, isplotclaim, (type == null) ? (AreaType.NONE) : type, tax, saleprice);
		areas.put(a, c);
		if (isplotclaim)
			PoliticalManager.loadClaim(c, l1.getLocation().getExtent().getChunkAtBlock(l1.getLocation().getBlockPosition()).get());
	}

	public static void newArea(UUID id, Location<World> l1, Location<World> l2, Resident owner, City c, boolean iscreating,
			PermissionSet pset, boolean isplotclaim, boolean isnewcity, AreaType type, double tax,
			double saleprice) throws AreaBoundsException, DatabaseException {
		if (!(isnewcity)) {
			for (Area a : areas.keySet()) {
				if (!(a.isPlotClaim())) {
					if (a.collides(l1, l2))
						throw new AreaBoundsException(a);
				}
			}
		}
		ComLocation[] angles = DatabaseManager.getInstance().newArea(id, l1, l2, owner, c, pset, isplotclaim,
					(type == null) ? AreaType.NONE : type, tax);
		Area a = new Area(id, angles[0], angles[1],
				owner, pset, isplotclaim, (type == null) ? (AreaType.NONE) : type, tax, saleprice);
		areas.put(a, c);
		if (type.equals(AreaType.TOWNHALL))
			PoliticalManager.changeTownHall(a, c, false, iscreating);
		if (isplotclaim)
			PoliticalManager.loadClaim(c, l1.getExtent().getChunkAtBlock(l1.getBlockPosition()).get());
	}

	public static Area getArea(Location<World> l) {
		Area ris = null;
		for (Area a : areas.keySet()) {
			if (a.isInArea(l)) {
				if (ris == null)
					ris = a;
				else if (ris.getDimension() > a.getDimension())
					ris = a;
			}
		}
		return ris;
	}

	public static boolean acquireArea(Player p, Location<World> l) throws DatabaseException {
		Resident r = ResidentManager.getResident(p.getUniqueId());
		Area a = getArea(l);
		double am = a.salePrice();
		if (EconomyLinker.canAfford(p.getUniqueId(), am)) {
			DatabaseManager.getInstance().setOwner(a, r);
			EconomyLinker.withdrawPlayer(p.getUniqueId(), am);
			if (a.hasOwner())
				EconomyLinker.deposit(a.getOwner().getPlayer(), am);
			else
				EconomyLinker.deposit(ResidentManager.getCity(r), a.salePrice());
			a.setOwner(r);
			a.notForSale();
			a.setSalePrice(0);
			return true;
		} else
			return false;
	}

	public static Resident getOwner(Location<World> l) {
		return getArea(l).getOwner();
	}

	public static boolean canSell(Player p, Area a) {
		City c = areas.get(a);
		Resident r = ResidentManager.getResident(p.getUniqueId());
		if (ResidentManager.getCity(r).equals(c)) {
			boolean hasRank = (c.isMayor(r) || c.isAssistant(r));
			if (hasRank)
				return true;
		} else
			return false;
		return false;
	}

	public static boolean canPerformAction(Resident r, Location<World> l, ActionType at) {
		if (!existsArea(l))
			return true;
		Area a = getArea(l);
		Perm aperms = null;
		switch (at) {
		case BUILD:
			aperms = a.getPermissionSet().getBuild();
			break;
		case INTERACT:
			aperms = a.getPermissionSet().getInteract();
			break;
		case ITEMUSE:
			aperms = a.getPermissionSet().getItemUse();
			break;
		}
		if (!ResidentManager.hasCity(r))
			return aperms.getAll();
		City c = ResidentManager.getCity(r);
		if (c.isMayor(r) || c.isAssistant(r))
			return true;
		if (a.hasOwner()) {
			if (a.getOwner().equals(r) && aperms.getOwner() == true)
				return true;
			if (ResidentManager.getFriendsList(a.getOwner()).contains(r) && aperms.getFriend())
				return true;
			if (ResidentManager.getCity(a.getOwner()).equals(c) && aperms.getCitizen() == true)
				return true;
			if (PoliticalManager.getNation(c).equals(PoliticalManager.getNation(AreaManager.getCity(a)))
					&& aperms.getAlly() == true)
				return true;
		} else {
			return PoliticalManager.canPerformAction(r, areas.get(a), at);
		}
		return false;
	}

	public static void setPermissions(Resident r, Location<World> l, String string, String string2, String string3)
			throws InsufficientRankException, ParseException, DatabaseException, CityNotFoundException {
		Area a = getArea(l);
		City c = AreaManager.getCity(a);
		if (c.isAssistant(r) || c.isMayor(r) || a.getOwner().equals(r)) {
			PermissionSet pset = Utility.setNewPermission(a.getPermissionSet(), string, string2, string3);
			DatabaseManager.getInstance().setPermissions(a, pset);
			a.setPermissionSet(pset);
		} else
			throw new InsufficientRankException(r, "Insufficient rank");
	}

	public static City getCity(Area a) {
		return areas.get(a);
	}

	public static boolean isClaimable(Location<Chunk> l, Resident r) {
		if (!ResidentManager.hasCity(r))
			return false;
		City c = ResidentManager.getCity(r);
		World world = l.getExtent().getWorld();
		boolean owncity = false;
		Vector3i chunk = l.getChunkPosition();
		Chunk n = world.getChunk(chunk.getX() + 1, 0, chunk.getZ()).get();
		Chunk s = world.getChunk(chunk.getX() - 1, 0, chunk.getZ()).get();
		Chunk w = world.getChunk(chunk.getX(), 0, chunk.getZ() - 1).get();
		Chunk e = world.getChunk(chunk.getX(), 0, chunk.getZ() + 1).get();
		Area[] areas = { getArea(new Location<World>(world,n.getBlockMin())), getArea(new Location<World>(world,s.getBlockMin())),
				getArea(new Location<World>(world,e.getBlockMin())), getArea(new Location<World>(world,w.getBlockMin())) };
		for (int i = 0; i < 4; i++) {
			if (areas[i] != null) {
				City o = AreaManager.getCity(areas[i]);
				if (c.equals(o))
					owncity = true;
				else if (o != null)
					return false;
			}
		}
		return owncity;
	}

	public static void unclaimArea(Area a) throws DatabaseException {
		DatabaseManager.getInstance().removeArea(a);
		areas.remove(a);
	}

	public static void setForSale(Area a, double saleprice) {
		a.forSale();
		a.setSalePrice(saleprice);
	}

	public static void setOwner(Area a, Resident r) {
		a.setOwner(r);
	}

	public static void setAreaType(Area a, AreaType t) {
		a.setAreaType(t);
	}

	public static void removeArea(Area a) {
		areas.remove(a);
	}

	public static Set<Area> getAreas() {
		return areas.keySet();
	}

	public static void collectTaxes() {
		for (Entry<Area, City> s : areas.entrySet()) {
			Area a = s.getKey();
			City c = s.getValue();
			if (!a.hasOwner())
				return;
			Resident r = a.getOwner();
			if (!c.isAssistant(r) && !c.isMayor(r)) {
				double tax = a.getTax();
				if (tax != 0 && EconomyLinker.canAfford(r.getPlayer(), a.getTax())) {
					EconomyLinker.withdrawPlayer(r.getPlayer(), tax);
					EconomyLinker.deposit(c, tax);
				}
			}
		}
	}

	public static void setTax(Area a, double d) throws DatabaseException {
		DatabaseManager.getInstance().setTax(a, d);
		a.setTax(d);
	}

	public static boolean existsArea(Location<World> location) {
		return getArea(location) != null;
	}

	public static void setSalePrice(Area a, double d) throws DatabaseException {
		DatabaseManager.getInstance().setSalePrice(a, d);
		a.setSalePrice(d);
	}
}