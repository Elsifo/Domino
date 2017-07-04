package it.beyondthecube.domino.listeners;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.cause.First;

import it.beyondthecube.domino.Utility;
import it.beyondthecube.domino.terrain.Area;
import it.beyondthecube.domino.terrain.AreaManager;

public class TerrainListener {
	public TerrainListener() {

	}

	public boolean verifyEntity(Entity e) {
		if (!AreaManager.existsArea(e.getLocation()))
			return false;
		if (Utility.isMonster(e)) {
			Area a = AreaManager.getArea(e.getLocation());
			return AreaManager.getCity(a).getToggleMobs();
		}
		return false;
	}

	@Listener
	public void onMobSpawn(SpawnEntityEvent e) {
		if(e.getEntities().isEmpty()) return;
		Entity entity=e.getEntities().get(0);
		if(!(e instanceof Living) || e instanceof Player) return;
		if (!AreaManager.existsArea(entity.getLocation()))
			return;
		if (Utility.isMonster(entity)
				&& AreaManager.getCity(AreaManager.getArea(entity.getLocation())).getToggleMobs() == false)
			e.setCancelled(true);
	}

	/*
	private boolean stopFire(BlockEvent e) {
		if (!AreaManager.existsArea(e.getBlock().getLocation()))
			return false;
		Area a = AreaManager.getArea(e.getBlock().getLocation());
		return !AreaManager.getCity(a).getToggleFire();
	}*/

	/**TODO
	@Listener
	public void onBlockIgnite(ChangeBlockEvent.Decay e) {
		if (e.getCause() == IgniteCause.SPREAD) {
			if (stopFire(e))
				e.setCancelled(true);
			;
		}
	}*/
	
	/** TODO
	@Listener
	public void onBlockBurn(BlockBurnEvent e) {
		if (stopFire(e))
			e.setCancelled(true);
	}*/
}
