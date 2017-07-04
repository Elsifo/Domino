package it.beyondthecube.domino.terrain;

import java.util.ArrayList;
import java.util.UUID;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import it.beyondthecube.domino.Utility;
import it.beyondthecube.domino.residents.Resident;
import it.beyondthecube.domino.sets.permission.PermissionSet;

public class Area {
	private UUID id;
	private Resident owner;
	private ComLocation angle1;
	private ComLocation angle2;
	private int dimension;
	private boolean forsale;
	private double saleprice;
	private PermissionSet pset;
	private boolean isplotclaim;
	private AreaType type;
	private double tax;

	public Area(UUID iddb, ComLocation angle1, ComLocation angle2, Resident owner, PermissionSet pset,
			boolean isplotclaim, AreaType type, double tax, double saleprice) {
		if (iddb != null)
			id = iddb;
		else
			id = UUID.randomUUID();
		this.angle1 = angle1;
		this.angle2 = angle2;
		this.owner = owner;
		this.dimension = Utility.getDimension(angle1, angle2);
		if (pset == null)
			this.pset = new PermissionSet();
		else
			this.pset = pset;
		this.isplotclaim = isplotclaim;
		this.type = type;
		this.tax = tax;
		this.saleprice = saleprice;
	}

	public boolean isInArea(Location<World> l) {
		int a1x = angle1.getBlockX();
		int a2x = angle2.getBlockX();
		int a1y = angle1.getBlockY();
		int a2y = angle2.getBlockY();
		int a1z = angle1.getBlockZ();
		int a2z = angle2.getBlockZ();
		int lx = l.getBlockX();
		int ly = l.getBlockY();
		int lz = l.getBlockZ();
		if (a1x < a2x) {
			if (lx < a1x || lx > a2x)
				return false;
		} else if (lx < a2x || lx > a1x)
			return false;
		if (a1y < a2y) {
			if (ly < a1y || ly > a2y)
				return false;
		} else if (ly < a2y || ly > a1y)
			return false;
		if (a1z < a2z) {
			if (lz < a1z || lz > a2z)
				return false;
		} else if (lz < a2z || lz > a1z)
			return false;
		return true;
	}

	public UUID getID() {
		return id;
	}

	protected void setOwner(Resident player) {
		this.owner = player;
	}

	public Resident getOwner() {
		return owner;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Area) {
			return ((Area) o).getID().equals(this.id);
		}
		return false;
	}

	public int getDimension() {
		return dimension;
	}

	protected void forSale() {
		forsale = true;
	}

	public void notForSale() {
		forsale = false;
	}

	public boolean isForSale() {
		return forsale;
	}

	public PermissionSet getPermissionSet() {
		return pset;
	}

	protected void setPermissionSet(PermissionSet pset) {
		this.pset = pset;
	}

	public boolean collides(Location<World> l1, Location<World> l2) {
		if (!(Utility.isBetween(l1.getBlockX(), angle1.getBlockX(), angle2.getBlockX())))
			return false;
		if (!(Utility.isBetween(l1.getBlockY(), angle1.getBlockY(), angle2.getBlockY())))
			return false;
		if (!(Utility.isBetween(l1.getBlockZ(), angle1.getBlockZ(), angle2.getBlockZ())))
			return false;
		return true;
	}

	public boolean isPlotClaim() {
		return isplotclaim;
	}

	public ComLocation getAngle1() {
		return angle1;
	}

	public ComLocation getAngle2() {
		return angle2;
	}

	public AreaType getAreaType() {
		return type;
	}

	protected void setAreaType(AreaType type) {
		this.type = type;
	}

	public ArrayList<Location<World>> getBorderBlocks() {
		ArrayList<Location<World>> blocks = new ArrayList<Location<World>>();
		int topBlockX = 1+(angle1.getBlockX() < angle2.getBlockX() ? angle2.getBlockX() : angle1.getBlockX());
		int bottomBlockX = (angle1.getBlockX() > angle2.getBlockX() ? angle2.getBlockX() : angle1.getBlockX());
		int topBlockY = 1+(angle1.getBlockY() < angle2.getBlockY() ? angle2.getBlockY() : angle1.getBlockY());
		int bottomBlockY = (angle1.getBlockY() > angle2.getBlockY() ? angle2.getBlockY() : angle1.getBlockY());
		int topBlockZ = 1+(angle1.getBlockZ() < angle2.getBlockZ() ? angle2.getBlockZ() : angle1.getBlockZ());
		int bottomBlockZ = (angle1.getBlockZ() > angle2.getBlockZ() ? angle2.getBlockZ() : angle1.getBlockZ());
		for (int x = bottomBlockX; x <= topBlockX; x++) {
			for (int y = bottomBlockY; y <= topBlockY; y++) {
				for (int z = bottomBlockZ; z <= topBlockZ; z++) {
					int vx = (x == bottomBlockX || x == topBlockX ) ? 1 : 0;
					int vy = (y == bottomBlockY || y == topBlockY ) ? 1 : 0;
					int vz = (z == bottomBlockZ || z == topBlockZ ) ? 1 : 0;
					if (vx + vy + vz > 1) {
						Location<World> block = new Location<World>(angle1.getWorld(), x, y, z);
						blocks.add(block);
					}
				}
			}
		}
		return blocks;
	}

	public boolean isChunkLoaded() {
		return angle1.getChunk().isLoaded();
	}

	protected void setTax(double tax) {
		this.tax = tax;
	}

	public double getTax() {
		return tax;
	}

	public boolean hasOwner() {
		return owner != null;
	}

	public void setSalePrice(double saleprice) {
		this.saleprice = saleprice;
	}

	public double salePrice() {
		return saleprice;
	}
}
