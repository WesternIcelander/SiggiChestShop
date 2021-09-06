package hk.siggi.bukkit.chestshop.pluginlink;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EconomyPluginImplNull extends EconomyPlugin {

	public EconomyPluginImplNull() {
	}

	@Override
	public double balance(UUID player) {
		return 0.0;
	}

	@Override
	public boolean deposit(double amount, long quantity, UUID player, String info) {
		Player p = Bukkit.getPlayer(player);
		if (p != null) {
			p.sendMessage("SiggiChestShop: Simulated deposit: " + moneyToString(amount) + " x " + quantity);
		}
		return true;
	}

	@Override
	public boolean withdraw(double amount, long quantity, UUID player, String info) {
		Player p = Bukkit.getPlayer(player);
		if (p != null) {
			p.sendMessage("SiggiChestShop: Simulated withdrawal: " + moneyToString(amount) + " x " + quantity);
		}
		return true;
	}
	
}
