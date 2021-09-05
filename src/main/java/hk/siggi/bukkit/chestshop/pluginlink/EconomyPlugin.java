package hk.siggi.bukkit.chestshop.pluginlink;

import java.util.UUID;
import org.bukkit.Bukkit;

public abstract class EconomyPlugin {

	private static EconomyPlugin instance = null;

	public static EconomyPlugin get() {
		if (instance == null) {
			if (Bukkit.getPluginManager().getPlugin("SiggiEconomy")!=null) {
				instance = new SiggiEconomyPluginImpl();
			} else {
				try {
					instance = new VaultPluginImpl();
				} catch (Exception e) {
					instance = new VaultPluginNullImpl();
				}
			}
		}
		return instance;
	}

	EconomyPlugin() {
	}

	public abstract double balance(UUID player);

	public abstract boolean deposit(double amount, long quantity, UUID player, String info);

	public abstract boolean withdraw(double amount, long quantity, UUID player, String info);

	public String moneyToString(double amount) {
		String moneyAsString = Double.toString(Math.round(amount * 100.0) / 100.0);
		int dotPosition = moneyAsString.indexOf(".");
		if (dotPosition == -1) {
			return moneyAsString;
		}
		String beforeDot = moneyAsString.substring(0, dotPosition);
		String afterDot = moneyAsString.substring(dotPosition + 1);
		if (afterDot.length() == 0) {
			afterDot = afterDot + "00";
		}
		if (afterDot.length() == 1) {
			afterDot = afterDot + "0";
		}
		if (afterDot.equals("00")) {
			return beforeDot;
		}
		return "$" + beforeDot + "." + afterDot;
	}

}
