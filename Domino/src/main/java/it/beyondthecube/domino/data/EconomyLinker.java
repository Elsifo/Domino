package it.beyondthecube.domino.data;

import java.math.BigDecimal;
import java.util.UUID;

import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;

import it.beyondthecube.domino.politicals.City;
import it.beyondthecube.domino.politicals.Nation;
import it.beyondthecube.domino.residents.Resident;

public class EconomyLinker {
	private static EconomyService economy;
	private static Currency def;

	public static void setEconomy(EconomyService econ) {
		economy = econ;
		def = econ.getDefaultCurrency();
	}

	public static double getBalance(Resident target) {
		return economy.getOrCreateAccount(target.getPlayer()).get().getBalance(def).doubleValue();
	}

	public static Currency getDefaultCurrency() {
		return def;
	}

	public static double getBalance(City c) {
		return economy.getOrCreateAccount(c.getName() + "" + c.getID()).get().getBalance(def).doubleValue();
	}

	public static void deposit(City c, Double value) {
		economy.getOrCreateAccount(c.getName() + "" + c.getID()).get().deposit(def, new BigDecimal(value), null);
	}

	public static void withdraw(City c, Double d) {
		economy.getOrCreateAccount(c.getName() + "" + c.getID()).get().withdraw(def, new BigDecimal(d), null);
	}

	public static void createCityBank(City c) {
		economy.getOrCreateAccount(c.getName() + "" + c.getID());
	}

	public static void withdrawPlayer(UUID p, Double d) {
		economy.getOrCreateAccount(p).get().withdraw(def, new BigDecimal(d), null);
	}

	public static boolean canAfford(UUID p, Double d) {
		return economy.getOrCreateAccount(p).get().getBalance(def).doubleValue() >= d;
	}

	public static boolean canBankAfford(String b, double d) {
		return economy.getOrCreateAccount(b).get().getBalance(def).doubleValue() >= d;
	}

	public static double getBalance(Nation n) {
		return economy.getOrCreateAccount(n.getName() + "" + n.getID()).get().getBalance(def).doubleValue();
	}

	public static void createNationBank(Nation n) {
		economy.getOrCreateAccount(n.getName() + "" + n.getID());
	}

	public static void deposit(Nation n, double am) {
		economy.getOrCreateAccount(n.getName() + "" + n.getID()).get().deposit(def, new BigDecimal(am), null);

	}

	public static String getCurrencyNamePlural() {
		return def.getName();
	}

	public static void deposit(UUID p, double am) {
		economy.getOrCreateAccount(p).get().deposit(def, new BigDecimal(am), null);
	}

	public static void withdraw(Nation n, Double d) {
		economy.getOrCreateAccount(n.getName() + "" + n.getID());		
	}
}