package hk.siggi.bukkit.chestshop.pluginlink;

import java.util.UUID;

public class VaultPluginNullImpl extends EconomyPlugin {

	public VaultPluginNullImpl() {
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
