package it.beyondthecube.domino.terrain;

import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import it.beyondthecube.domino.data.database.DatabaseManager;
import it.beyondthecube.domino.exceptions.DatabaseException;

public class ComLocation {
	private int dbid;
	private Location<World> l;

	public static ComLocation getComLocation(Location<World> l) throws DatabaseException {
		return new ComLocation(DatabaseManager.getInstance().getLocationId(l), l.getExtent(), l.getX(), l.getY(), l.getZ());
	}

	public ComLocation(int dbid, World w, double x, double y, double z) {
		l = new Location<World>(w, x, y, z);
		this.dbid = dbid;
	}

	public ComLocation(int idl1, Location<World> l) {
		this.dbid = idl1;
		this.l = l;
	}

	public int getID() {
		return dbid;
	}

	public Location<World> getLocation() {
		return l;
	}

	public int getBlockX() {
		return l.getBlockX();
	}

	public int getBlockY() {
		return l.getBlockY();
	}

	public int getBlockZ() {
		return l.getBlockZ();
	}

	public World getWorld() {
		return l.getExtent();
	}

	public Chunk getChunk() {
		return l.getExtent().getChunkAtBlock(l.getBlockPosition()).get();
	}
}
