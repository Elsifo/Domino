package it.beyondthecube.domino.tasks;

import java.util.HashMap;
import java.util.UUID;

import org.spongepowered.api.Sponge;

import it.beyondthecube.domino.Domino;
import it.beyondthecube.domino.residents.Resident;

public class TaskManager {
	private HashMap<UUID, DominoTask> task;
	private HashMap<Resident, UUID> search;
	private HashMap<Resident, Class<?>> type;
	private Domino plugin;
	private static TaskManager instance = null;

	private TaskManager() {
		task = new HashMap<UUID, DominoTask>();
		search = new HashMap<Resident, UUID>();
		type = new HashMap<Resident, Class<?>>();
		plugin = null;
	}

	public static TaskManager getInstance() {
		if (instance == null)
			instance = new TaskManager();
		return instance;
	}

	public enum TaskSignal {
		CANCEL, SUCCESS
	}
	
	public void setPlugin(Domino plugin) {
		this.plugin = plugin;
	}

	public void newTask(Resident res, DominoTask r, long delay) {
		UUID u = UUID.randomUUID();
		r.setUUID(u);
		task.put(u, r);
		search.put(res, u);
		type.put(res, r.getClass());
		Sponge.getScheduler().createTaskBuilder().delayTicks(delay).execute((Runnable) r).submit(plugin);
	}

	public void cancelTask(Resident r) {
		signalTask(r, TaskSignal.CANCEL);
		task.remove(search.get(r));
		search.remove(r);
		type.remove(r);
	}

	public void signalTask(Resident r, TaskSignal s) {
		if(!search.containsKey(r)) return;
		task.get(search.get(r)).signal(s);
		switch (s) {
		case CANCEL: {
			task.remove(search.get(r));
			search.remove(r);
			type.remove(r);
			break;
		}
		default:
			break;
		}
	}

	public boolean hasTask(Resident r) {
		return search.containsKey(r);
	}

	public Class<?> getTaskType(Resident r) {
		return type.get(r);
	}

	public boolean isStopped(Resident r) {
		if(search.get(r) == null) return true;
		return task.get(search.get(r)).isStopped();
	}
}
