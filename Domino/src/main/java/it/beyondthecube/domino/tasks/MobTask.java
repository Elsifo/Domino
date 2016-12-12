package it.beyondthecube.domino.tasks;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import it.beyondthecube.domino.Domino;
import it.beyondthecube.domino.Utility;
import it.beyondthecube.domino.terrain.AreaManager;

public class MobTask implements Runnable {
	private Domino plugin;
	
	public MobTask(Domino plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		for (World w : Sponge.getServer().getWorlds()) {
			for (Entity e : w.getEntities()) {
				Location<World> l = e.getLocation();
				if (AreaManager.existsArea(l)) {
					if (Utility.isMonster(e) && AreaManager.getCity(AreaManager.getArea(l)).getToggleMobs() == false) {
						e.remove();
					}
				}
			}

		}
		Sponge.getScheduler().createTaskBuilder().execute(new MobTask(plugin)).delayTicks(40).submit(plugin);
	}
}
