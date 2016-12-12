package it.beyondthecube.domino.tasks;

import it.beyondthecube.domino.residents.CitizenshipRequestManager;
import it.beyondthecube.domino.residents.Resident;

public class CitizenshipTask implements Runnable {
	private Resident added;
	private boolean stopped;

	public CitizenshipTask(Resident added, Resident adder) {
		this.added = added;
		this.stopped = false;
	}

	public void cancel() {
		this.stopped = true;
	}

	@Override
	public void run() {
		if (!stopped)
			CitizenshipRequestManager.requestDenied(added);
	}
}