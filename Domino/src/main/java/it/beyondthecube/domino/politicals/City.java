package it.beyondthecube.domino.politicals;

import java.util.ArrayList;

import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import it.beyondthecube.domino.data.database.DatabaseManager;
import it.beyondthecube.domino.exceptions.AreaBoundsException;
import it.beyondthecube.domino.exceptions.DatabaseException;
import it.beyondthecube.domino.residents.Resident;
import it.beyondthecube.domino.sets.permission.PermissionSet;
import it.beyondthecube.domino.sets.toggle.ToggleSet;
import it.beyondthecube.domino.terrain.Area;
import it.beyondthecube.domino.terrain.AreaManager;
import it.beyondthecube.domino.terrain.AreaType;
import it.beyondthecube.domino.terrain.ComLocation;

public class City {
	private int dbid;
	private String name;
	private Resident mayor;
	private ArrayList<Resident> assistants;
	private ArrayList<Area> plots;
	private PermissionSet pset;
	private ComLocation spawn;
	private Area townhall;
	private double tax;
	private ToggleSet toggles;
	private int plotnum;

	protected City(int dbid, String name, ArrayList<Resident> residents, ArrayList<Resident> assistants, Resident mayor,
			ArrayList<Area> plots, PermissionSet pset, ComLocation spawn2, double tax, ToggleSet toggles, int plotnum) {
		this.dbid = dbid;
		this.name = name;
		this.assistants = (assistants == null) ? new ArrayList<>() : assistants;
		this.mayor = mayor;
		this.plots = (plots == null) ? new ArrayList<>() : plots;
		if (pset == null)
			this.pset = new PermissionSet();
		else
			this.pset = pset;
		this.spawn = spawn2;
		this.tax = tax;
		this.toggles = toggles;
		this.plotnum = plotnum;
	}

	public int getID() {
		return dbid;
	}

	public String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}

	public boolean isAssistant(Resident r) {
		return assistants.contains(r);
	}

	public boolean isMayor(Resident r) {
		return mayor.equals(r);
	}

	protected void addAssistant(Resident added) {
		this.assistants.add(added);
	}

	protected void removeAssistant(Resident r) {
		this.assistants.remove(r);
	}

	protected void setMayor(Resident r) {
		this.mayor = r;
	}

	public Resident getMayor() {
		return mayor;
	}
	
	public int getPlotNumber() {
		return plotnum;
	}

	protected void claim(Chunk k, boolean isfirstclaim) throws DatabaseException {
		Location<World> angle1 = new Location<World>(k.getWorld(), k.getBlockMax());
		Location<World> angle2 = new Location<World>(k.getWorld(), k.getBlockMin());
		try {
			AreaManager.newArea(null, angle1, angle2, null, this, true, new PermissionSet(), true, isfirstclaim,
					(isfirstclaim) ? AreaType.TOWNHALL : AreaType.NONE, 0, 0);
		} catch (AreaBoundsException e) {
			// TODO shouldn't happen
			e.printStackTrace();
		}
	}

	protected void unclaim(Area a) throws DatabaseException {
		DatabaseManager.getInstance().unclaim(this, a);
		this.plots.remove(a.getAngle1().getChunk());
	}

	public boolean ownedChunk(Chunk chunk) {
		for (Area a : plots) {
			if (a.collides(new Location<World>(chunk.getWorld(), chunk.getBlockMin()),
					new Location<World>(chunk.getWorld(), chunk.getBlockMax())))
				return true;
		}
		return false;
	}

	public ArrayList<Resident> getAssistants() {
		return assistants;
	}

	protected void loadClaim(Area c) {
		plots.add(c);
	}

	public PermissionSet getPermissionSet() {
		return pset;
	}

	protected void setPermissionSet(PermissionSet pset) {
		this.pset = pset;
	}

	protected void setSpawn(ComLocation spawn) {
		this.spawn = spawn;
	}

	public ComLocation getSpawn() {
		return spawn;
	}

	protected void setTownHall(Area townhall) {
		this.townhall = townhall;
	}

	public Area getTownHall() {
		return townhall;
	}

	protected void setTax(double tax) {
		this.tax = tax;
	}

	public double getTax() {
		return tax;
	}

	public boolean getToggleMobs() {
		return toggles.getMobs();
	}

	public boolean getToggleFire() {
		return toggles.getFire();
	}

	public boolean getPvp() {
		return toggles.getPvp();
	}

	public ToggleSet getToggles() {
		return toggles;
	}

	public void toggleFire() {
		toggles.toggleFire();
	}

	public void togglePvp() {
		toggles.togglePvp();
	}

	public void toggleMobs() {
		toggles.toggleMobs();
	}

	public int getClaimedPlots() {
		return plots.size();
	}

	public void addPlotBonus(int bonus) {
		this.plotnum += bonus;		
	}
}