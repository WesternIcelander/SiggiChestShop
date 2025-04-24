package hk.siggi.bukkit.chestshop.pluginlink;

import org.bukkit.inventory.ItemStack;

public class ValuePluginImplNull extends ValuePlugin {

	@Override
	public void setValue(ItemStack item, double amount) {
	}

	@Override
	public double getValue(ItemStack item) {
		return 0.0;
	}
}
