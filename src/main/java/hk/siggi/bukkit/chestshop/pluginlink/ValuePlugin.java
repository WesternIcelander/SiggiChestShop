package hk.siggi.bukkit.chestshop.pluginlink;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.List;

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

	public abstract void setValue(ItemStack item, double amount);

	public abstract double getValue(ItemStack item);

	public abstract boolean supportsSearch();

	public abstract List<ItemStack> search(String searchString);
}
