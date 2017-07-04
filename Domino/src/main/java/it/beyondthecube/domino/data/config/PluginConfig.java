package it.beyondthecube.domino.data.config;

public class PluginConfig {
	private long lasttax;
	private double spawnprice;
	private int bonusplots;
	private double claimprice;
	private long spawndelay;
	
	public PluginConfig() {

	}

	public long getLastTax() {
		return lasttax;
	}
	
	public void setLastTax(long lasttax) {
		this.lasttax = lasttax;
	}
	
	public double getSpawnPrice() {
		return spawnprice;
	}
	
	public void setSpawnPrice(int spawnprice) {
		this.spawnprice = spawnprice;
	}
	
	public int getCitizenPlotBonus() {
		return bonusplots;
	}
	
	public void setCitizenPlotBonus(int bonusplots) {
		this.bonusplots = bonusplots;
	}
	
	public Double getClaimPrice() {
		return claimprice;
	}
	
	public void setClaimPrice(int claimprice) {
		this.claimprice = claimprice;
	}
	
	public void setSpawnDelay(long delay) {
		this.spawndelay = delay;
	}
	
	public long getSpawnDelay() {
		return spawndelay;
	}
}
