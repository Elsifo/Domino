package it.beyondthecube.domino.sets.permission;

public class Perm {
	private Boolean[] pset;

	public Perm(boolean owner, boolean friend, boolean citizen, boolean ally, boolean all) {
		pset = new Boolean[5];
		pset[0] = owner;
		pset[1] = friend;
		pset[2] = citizen;
		pset[3] = ally;
		pset[4] = all;
	}

	public Perm(String string) {
		pset = new Boolean[5];
		String[] ps = string.split(",");
		for (int i = 0; i < pset.length; i++) {
			pset[i] = Boolean.parseBoolean(ps[i]);
		}
	}

	public boolean getOwner() {
		return pset[0];
	}

	public boolean getFriend() {
		return pset[1];
	}

	public boolean getCitizen() {
		return pset[2];
	}

	public boolean getAlly() {
		return pset[3];
	}

	public boolean getAll() {
		return pset[4];
	}

	public void setOwner(boolean b) {
		pset[0] = b;
	}

	public void setFriend(boolean b) {
		pset[1] = b;
	}

	public void setCitizen(boolean b) {
		pset[2] = b;
	}

	public void setAlly(boolean b) {
		pset[3] = b;
	}

	public void setAll(boolean b) {
		pset[4] = b;
	}

	@Override
	public String toString() {
		return "[" + pset[0] + "," + pset[1] + "," + pset[2] + "," + pset[3] + "," + pset[4] + "]";
	}
}
