package it.beyondthecube.domino.tasks;

import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import it.beyondthecube.domino.Utility;
import it.beyondthecube.domino.data.EconomyLinker;
import it.beyondthecube.domino.data.config.ConfigManager;
import it.beyondthecube.domino.politicals.City;
import it.beyondthecube.domino.residents.ResidentManager;
import it.beyondthecube.domino.tasks.TaskManager.TaskSignal;

public class CitySpawnTask implements Runnable, DominoTask {
	private Player p;
	private City c;
	private UUID u;
	private boolean stopped;
	
	public CitySpawnTask(City c, Player p) {
		this.p = p;
		this.c = c;
		this.stopped = false;
	}
	
	@Override
	public void run() {
		if(stopped) {
			Sponge.getServer().getBroadcastChannel().send(Text.of("cancel"));
			return;
		}
		synchronized(TaskManager.getInstance()) {
			TaskManager.getInstance().signalTask(ResidentManager.getResident(p.getUniqueId()),TaskSignal.SUCCESS);
			double price = ConfigManager.getConfig().getSpawnPrice();
			Location<World> sp = c.getSpawn().getLocation();
			p.transferToWorld(sp.getExtent(), sp.getPosition());
			EconomyLinker.withdrawPlayer(p.getUniqueId(), price);
			EconomyLinker.deposit(c, price);
			p.sendMessage(
				(Utility.pluginMessage("You've been charged " + price + " for city spawn")));
		}
	}

	@Override
	public void signal(TaskSignal ts) {
		switch(ts) {
		    case CANCEL: {
		    	p.sendMessage(Utility.pluginMessage("You moved! City spawn cancelled"));
		    	stopped = true;
		    	break;
		    }
		    case SUCCESS: {
		    	stopped = true;
		    	break;
		    }
		}
	}

	@Override
	public void setUUID(UUID u) {
		this.u = u;		
	}

	@Override
	public UUID getTaskId() {
		return u;
	}

	@Override
	public boolean isStopped() {
		return stopped;
	}
}
