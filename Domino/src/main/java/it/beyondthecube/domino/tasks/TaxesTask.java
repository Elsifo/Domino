package it.beyondthecube.domino.tasks;

import java.io.IOException;
import java.util.Calendar;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

import it.beyondthecube.domino.Domino;
import it.beyondthecube.domino.Utility;
import it.beyondthecube.domino.data.config.ConfigManager;
import it.beyondthecube.domino.politicals.PoliticalManager;
import it.beyondthecube.domino.residents.ResidentManager;
import it.beyondthecube.domino.terrain.AreaManager;

public class TaxesTask implements Runnable {
	private static boolean lock = false;
	private Domino plugin;

	public TaxesTask(Domino plugin) {
		this.plugin = plugin;
	}

	private static synchronized boolean attemptLock() {
		if (lock)
			return false;
		else {
			lock = true;
			return true;
		}
	}

	private static synchronized void unlock() {
		lock = false;
	}

	@Override
	public void run() {
		if (attemptLock()) {
			try {
				if (Utility.isTimeToCollect()) {
					Calendar collectionDate = Calendar.getInstance();
					collectionDate.set(Calendar.HOUR_OF_DAY, 12);
					collectionDate.set(Calendar.MINUTE, 00);
					collectionDate.set(Calendar.SECOND, 00);			
					ConfigManager.getConfig().setLastTax(collectionDate.getTimeInMillis());
					AreaManager.collectTaxes();
					ResidentManager.collectResidentTaxes();
					PoliticalManager.collectTaxes();
					Sponge.getServer().getBroadcastChannel()
						.send(Text.of(Utility.pluginMessage("Taxes have been collected")));
					unlock();
				}
			} catch (IOException e) {
				Utility.sendConsole("Error reading config file");
			}	
		}
		Sponge.getScheduler().createTaskBuilder().delayTicks(1200L).execute(new TaxesTask(plugin)).submit(plugin);
	}
}
