package it.beyondthecube.domino.residents;

import java.util.UUID;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class Resident {
	private UUID player;
	private String nick;
	private Location<World> sel1;
	private Location<World> sel2;
	private boolean selectionMode;

	protected Resident(UUID player, String nick) {
		this.player = player;
		this.nick = nick;
		sel1 = null;
		sel2 = null;
		selectionMode = false;
	}

	public UUID getPlayer() {
		return player;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	protected void setSelection1(Location<World> sel1) {
		this.sel1 = sel1;
	}

	protected void setSelection2(Location<World> sel2) {
		this.sel2 = sel2;
	}

	public Location<World> getSelection1() {
		return sel1;
	}

	public Location<World> getSelection2() {
		return sel2;
	}

	public boolean hasSelections() {
		return sel1 != null && sel2 != null;
	}

	protected void activateSelection() {
		selectionMode = true;
	}

	protected void deactivateSelection() {
		selectionMode = false;
	}

	public boolean isSelecting() {
		return selectionMode;
	}

	@Override
	public boolean equals(Object oth) {
		if (oth instanceof Resident) {
			if (((Resident) (oth)).getPlayer().equals(this.player))
				return true;
		}
		return false;
	}
}