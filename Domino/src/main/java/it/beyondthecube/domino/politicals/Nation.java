package it.beyondthecube.domino.politicals;

public class Nation {
	private String name;
	private City capital;
	private int dbid;
	private double tax;

	public Nation(int dbid, String name, double tax) {
		this.dbid = dbid;
		this.name = name;
		this.tax = tax;
		this.capital = null;
	}

	public void setCapital(City capital) {
		this.capital = capital;
	}

	public City getCapital() {
		return capital;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public int getID() {
		return dbid;
	}

	public double getTax() {
		return tax;
	}

	protected void setTax(double tax) {
		this.tax = tax;
	}
}
