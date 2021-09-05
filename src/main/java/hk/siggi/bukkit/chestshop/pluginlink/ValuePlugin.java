package hk.siggi.bukkit.chestshop.pluginlink;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public abstract class ValuePlugin {

	private static ValuePlugin instance = null;

	public static ValuePlugin get() {
		if (instance == null) {
			if (Bukkit.getPluginManager().getPlugin("ItemPricer") != null) {
				instance = new ValuePluginImplItemPricer();
			} else {
				instance = new ValuePluginImplNull();
			}
		}
		return instance;
	}

	public abstract double getValue(ItemStack item);
}
