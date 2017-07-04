package it.beyondthecube.domino.tasks;

import java.util.UUID;

import it.beyondthecube.domino.tasks.TaskManager.TaskSignal;

public interface DominoTask {
	
	public void signal(TaskSignal ts);

	public void setUUID(UUID u);
	
	public UUID getTaskId();

	public boolean isStopped();
}
