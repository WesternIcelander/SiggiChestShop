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

}
