package it.beyondthecube.domino.residents;

import org.spongepowered.api.Sponge;

import it.beyondthecube.domino.Domino;
import it.beyondthecube.domino.politicals.City;
import it.beyondthecube.domino.tasks.CitizenshipTask;

public class Citizenship {
	private Resident target;
	private Resident source;
	private City c;
	private CitizenshipTask task;

	public Citizenship(Resident target, Resident source, City c, Domino plugin) {
		this.target = target;
		this.source = source;
		this.c = c;
		this.task = new CitizenshipTask(target, source);
	}

	public Resident getTarget() {
		return target;
	}

	public City getCity() {
		return c;
	}

	public Resident getSource() {
		return source;
	}

	public CitizenshipTask getTask() {
		return task;
	}

	public void runTask() {
		Sponge.getScheduler().createTaskBuilder().delayTicks(1200).execute(new CitizenshipTask(target, source));
	}

	public void stopTask() {
		task.cancel();
	}
}
