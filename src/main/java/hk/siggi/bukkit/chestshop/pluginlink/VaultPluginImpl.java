package hk.siggi.bukkit.chestshop.pluginlink;

import hk.siggi.bukkit.chestshop.ChestShop;
import java.util.UUID;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultPluginImpl extends EconomyPlugin {

	VaultPluginImpl() {
		economy = null;
		setupEconomy();
	}
	private Economy economy = null;

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = ChestShop.getInstance().getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}

	@Override
	public double balance(UUID player) {
		if (economy == null) {
			return 0.0;
		}
		OfflinePlayer p = Bukkit.getOfflinePlayer(player);
		return economy.getBalance(p);
	}

	@Override
	public boolean deposit(double amount, long quantity, UUID player, String info) {
		if (economy == null) {
			return true;
		}
		OfflinePlayer p = Bukkit.getOfflinePlayer(player);
		return economy.depositPlayer(p, amount * ((double) quantity)).transactionSuccess();
	}

	@Override
	public boolean withdraw(double amount, long quantity, UUID player, String info) {
		if (economy == null) {
			return true;
		}
		OfflinePlayer p = Bukkit.getOfflinePlayer(player);
		return economy.withdrawPlayer(p, amount * ((double) quantity)).transactionSuccess();
	}
}
