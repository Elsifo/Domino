package it.beyondthecube.domino.exceptions;

import it.beyondthecube.domino.terrain.Area;

public class AreaBoundsException extends Exception {
	private static final long serialVersionUID = 11L;
	private Area a;

	public AreaBoundsException(Area a) {
		super("");
		this.a = a;
	}

	public Area getCollidingArea() {
		return a;
	}
}
