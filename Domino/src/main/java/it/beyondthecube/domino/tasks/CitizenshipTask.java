package it.beyondthecube.domino.tasks;

import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import it.beyondthecube.domino.Utility;
import it.beyondthecube.domino.data.database.DatabaseManager;
import it.beyondthecube.domino.exceptions.DatabaseException;
import it.beyondthecube.domino.politicals.City;
import it.beyondthecube.domino.residents.Citizenship;
import it.beyondthecube.domino.residents.ResidentManager;
import it.beyondthecube.domino.tasks.TaskManager.TaskSignal;

public class CitizenshipTask implements DominoTask, Runnable {
	private Citizenship c;
	private UUID taskid;
	private boolean stopped;

	public CitizenshipTask(Citizenship c) {
		this.c = c;
		this.stopped = false;
	}
	
	public void signal(TaskSignal ts) {
		switch(ts) {
		case SUCCESS: cancel();return;
		case CANCEL: denied();return;
		}
	}

	private void cancel() {
		City ct = c.getCity();
		try {
			DatabaseManager.getInstance().addResident(c.getTarget(), ct);
			ResidentManager.addToCity(c.getSource(), c.getTarget(), ct);
		} catch (DatabaseException e) {
			Player p = Sponge.getServer().getPlayer(c.getSource().getPlayer()).get();
			if (p.isOnline())
				p.sendMessage(Text.of(Utility.errorMessage("ERROR: contact an admin")));
		}
		Player padded = Sponge.getServer().getPlayer(c.getTarget().getPlayer()).get();
		if (padded.isOnline())
			padded.sendMessage(Text.of(Utility.pluginMessage("You've been added to " + ct.getName())));
	}
	
	private void denied() {
		Player p = Sponge.getServer().getPlayer(c.getTarget().getPlayer()).get();
		Player s = Sponge.getServer().getPlayer(c.getSource().getPlayer()).get();
		if (p.isOnline())
			p.sendMessage(Text.of(Utility.pluginMessage("Request denied")));
		if(s.isOnline()) {
			s.sendMessage(Text.of(Utility.pluginMessage(p.getName()+ " has denied your request")));
		}
	}
	
	public UUID getTaskId() {
		return taskid;
	}

	@Override
	public void run() {
		if (!stopped)
			TaskManager.getInstance().cancelTask(c.getTarget());
	}

	@Override
	public void setUUID(UUID u) {
		this.taskid = u;		
	}

	@Override
	public boolean isStopped() {
		return stopped;
	}
}