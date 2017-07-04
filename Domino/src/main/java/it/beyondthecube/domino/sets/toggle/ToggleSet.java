package it.beyondthecube.domino.sets.toggle;

public class ToggleSet {
	private boolean mobs;
	private boolean firespread;
	private boolean pvp;

	public ToggleSet(String set) {
		String[] toggles = set.substring(1, set.length() - 2).split("-");
		mobs = Boolean.parseBoolean(toggles[0]);
		firespread = Boolean.parseBoolean(toggles[1]);
		pvp = Boolean.parseBoolean(toggles[2]);
	}

	public ToggleSet() {
		mobs = false;
		firespread = false;
		pvp = false;
	}

	public String toString() {
		return "{" + Boolean.toString(mobs) + "-" + Boolean.toString(firespread) + "-" + Boolean.toString(pvp) + "}";
	}

	public boolean getMobs() {
		return mobs;
	}

	public boolean getFire() {
		return firespread;
	}

	public boolean getPvp() {
		return pvp;
	}

	public void toggleMobs() {
		mobs = !mobs;
	}

	public void toggleFire() {
		firespread = !firespread;
	}

	public void togglePvp() {
		pvp = !pvp;
	}
}
