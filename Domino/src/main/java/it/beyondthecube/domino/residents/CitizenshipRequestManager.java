package it.beyondthecube.domino.residents;

import java.io.IOException;
import java.util.HashMap;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import it.beyondthecube.domino.Utility;
import it.beyondthecube.domino.data.database.DatabaseManager;
import it.beyondthecube.domino.exceptions.DatabaseException;
import it.beyondthecube.domino.politicals.City;
import it.beyondthecube.domino.tasks.CitizenshipTask;
import it.beyondthecube.domino.tasks.TaskManager;

public class CitizenshipRequestManager {
	private static HashMap<Resident, Citizenship> requests = new HashMap<Resident, Citizenship>();

	public static void newRequest(Citizenship cs) {
		Resident added = cs.getTarget();
		Resident adder = cs.getSource();
		Player ad = Sponge.getServer().getPlayer(added.getPlayer()).get();
		Player ar = Sponge.getServer().getPlayer(adder.getPlayer()).get();
		City c = ResidentManager.getCity(adder).get();
		ar.sendMessage(Text.of(Utility.pluginMessage("Invitation sent")));
		ad.sendMessage(Text.of(Utility.pluginMessage(
				"You've been invited to city " + c.getName() + ". Type /dom accept to accept or /dom deny to deny")));
		CitizenshipTask task = new CitizenshipTask(cs); 
		requests.put(added, cs);
		TaskManager.getInstance().newTask(added, task, 900L);
	}

	public static void remove(Resident added) {
		requests.remove(added);
	}

	public static void requestAccepted(Resident added) throws IOException {
		TaskManager.getInstance().cancelTask(added);
		City c = requests.get(added).getCity();
		try {
			DatabaseManager.getInstance().addResident(added, c);
			ResidentManager.addToCity(requests.get(added).getSource(), added, c);
		} catch (DatabaseException e) {
			Player p = Sponge.getServer().getPlayer(requests.get(added).getSource().getPlayer()).get();
			if (p.isOnline())
				p.sendMessage(Text.of(Utility.errorMessage("ERROR: contact an admin")));
		}
		Player padded = Sponge.getServer().getPlayer(added.getPlayer()).get();
		if (padded.isOnline())
			padded.sendMessage(Text.of(Utility.pluginMessage("You've been added to " + c.getName())));
		remove(added);
	}

	public static void requestDenied(Resident added) {
	    TaskManager.getInstance().cancelTask(added);
		remove(added);
		Player p = Sponge.getServer().getPlayer(added.getPlayer()).get();
		if (p.isOnline())
			p.sendMessage(Text.of(Utility.pluginMessage("Request denied")));
	}

	public static boolean hasRequest(Resident r) {
		return requests.containsKey(r);
	}
}
