package hk.siggi.bukkit.chestshop.pluginlink;

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
		return true;
	}

	@Override
	public boolean withdraw(double amount, long quantity, UUID player, String info) {
		return true;
	}
	
}
