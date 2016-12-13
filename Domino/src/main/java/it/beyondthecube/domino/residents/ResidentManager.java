package it.beyondthecube.domino.residents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import it.beyondthecube.domino.Utility;
import it.beyondthecube.domino.data.EconomyLinker;
import it.beyondthecube.domino.data.config.PluginConfig;
import it.beyondthecube.domino.data.database.DatabaseManager;
import it.beyondthecube.domino.exceptions.CityNotFoundException;
import it.beyondthecube.domino.exceptions.DatabaseException;
import it.beyondthecube.domino.exceptions.InsufficientRankException;
import it.beyondthecube.domino.politicals.City;

public class ResidentManager {
	private static HashMap<UUID, Resident> index = new HashMap<>();
	private static HashMap<String, Resident> search = new HashMap<>();
	private static HashMap<Resident, City> residents = new HashMap<>();
	private static HashMap<Resident, Resident> friends = new HashMap<>();

	public static Collection<Resident> getResidents(City c) {
		ArrayList<Resident> ris = new ArrayList<>();
		for (Entry<Resident, City> e : residents.entrySet()) {
			if (e.getValue() != null) {
				if (e.getValue().equals(c))
					ris.add(e.getKey());
			}
		}
		return ris;
	}

	public static Resident getResident(UUID p) {
		return index.get(p);
	}

	public static Resident newResident(User p, City c) throws DatabaseException {
		DatabaseManager.getInstance().createResident(p);
		Resident r = new Resident(p.getUniqueId(), p.getName());
		residents.put(r, c);
		index.put(p.getUniqueId(), r);
		search.put(p.getName(), r);
		return r;
	}

	public static void setCity(Resident added, City c) throws CityNotFoundException {
		if (c == null)
			throw new CityNotFoundException();
		residents.put(added, c);
	}

	public static void addToCity(Resident adder, Resident added, City c) throws IOException {
		residents.put(added, c);
		c.addPlotBonus(Integer.parseInt((String) PluginConfig.getValue("domino","city","citizenbonusplot")));
	}

	public static void removeResident(Resident remover, Resident removed, City c)
			throws DatabaseException, CityNotFoundException, InsufficientRankException {
		City crer = residents.get(remover);
		if (crer == null)
			throw new CityNotFoundException();
		if (crer.isMayor(remover) || crer.isAssistant(remover) || remover.equals(removed)) {
			City cred = residents.get(removed);
			if (crer.equals(cred)) {
				DatabaseManager.getInstance().removeResident(removed, cred);
				residents.put(removed, null);
			} else {
				throw new InsufficientRankException(remover, "Insufficient rank");
			}
		} else
			throw new InsufficientRankException(remover, "" + "Insufficient rank");
	}

	public static boolean hasSelections(Resident r) {
		return r.hasSelections();
	}

	public static City getCity(Resident added) {
		return residents.get(added);
	}

	public static void setSelection1(Player p, Location<World> location) {
		index.get(p.getUniqueId()).setSelection1(location);
	}

	public static void setSelection2(Player p, Location<World> location) {
		index.get(p.getUniqueId()).setSelection2(location);
	}

	public static void deactivateSelection(Player p) {
		index.get(p.getUniqueId()).deactivateSelection();
	}

	public static void activateSelection(Player p) {
		index.get(p.getUniqueId()).activateSelection();
	}

	public static void setFriend(Resident r, Resident friend, boolean isloading) throws DatabaseException {
		if (getFriendsList(r).contains(friend)) {
			Sponge.getServer().getPlayer(r.getPlayer()).get()
					.sendMessage(Text.of(Utility.pluginMessage("You are already friends")));
			return;
		}
		if (!isloading)
			DatabaseManager.getInstance().setFriend(r, friend);
		friends.put(r, friend);
	}

	public static void removeFriend(Resident r, Resident friend) throws DatabaseException {
		DatabaseManager.getInstance().removeFriend(r, friend);
		friends.remove(r, friend);
	}

	public static boolean isFriend(Resident r, Resident friend) {
		Stream<Entry<Resident, Resident>> filter = friends.entrySet().stream().filter(p -> p.getKey().equals(r))
				.filter(p -> p.getValue().equals(friend));
		return filter.collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue())).size() > 0;
	}

	public static Collection<Resident> getFriendsList(Resident r) {
		return friends.entrySet().stream().filter(p -> p.getKey().equals(r))
				.collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue())).values();
	}

	public static void collectResidentTaxes() {
		for (Resident r : residents.keySet()) {
			City c = residents.get(r);
			if (c != null) {
				if (c.isAssistant(r) || c.isMayor(r)) {
					Optional<Player> p = Sponge.getServer().getPlayer(r.getPlayer());
						if (p.isPresent())
							p.get().sendMessage(Text.of(Utility.pluginMessage("You don't pay taxes!")));
				} else {
					double tax = c.getTax();
					EconomyLinker.withdrawPlayer(r.getPlayer(), tax);
					EconomyLinker.deposit(c, tax);
				}
			}
		}
	}

	public static boolean isCitizen(Resident added) {
		return residents.get(added) != null;
	}

	public static boolean hasCity(Resident r) {
		return residents.get(r) != null;
	}

	public static void loadResident(UUID p, String nick, City c, boolean isnew) {
		Resident r = new Resident(p, nick);
		residents.put(r, c);
		index.put(p, r);
		search.put(nick, r);
	}

	public static Optional<Resident> getResident(String nick) {
		Optional<Resident> res = Optional.empty();
		if(search.containsKey(nick)) res = Optional.of(search.get(nick));
		return res;
	}
}