package it.beyondthecube.domino.residents;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import it.beyondthecube.domino.Domino;
import it.beyondthecube.domino.Utility;
import it.beyondthecube.domino.politicals.City;

public class Citizenship {
	private Resident target;
	private Resident source;
	private City c;

	public Citizenship(Resident added, Resident adder, City c, Domino plugin) {
		Player ad = Sponge.getServer().getPlayer(added.getPlayer()).get();
		Player ar = Sponge.getServer().getPlayer(adder.getPlayer()).get();
		ar.sendMessage(Text.of(Utility.pluginMessage("Invitation sent")));
		ad.sendMessage(Text.of(Utility.pluginMessage(
				"You've been invited to city " + c.getName() + ". Type /dom accept to accept or /dom deny to deny")));
		this.target = added;
		this.source = adder;
		this.c = c;
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
}
