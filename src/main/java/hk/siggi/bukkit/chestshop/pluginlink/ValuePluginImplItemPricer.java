package hk.siggi.bukkit.chestshop.pluginlink;

import io.siggi.itempricer.ItemPricer;
import org.bukkit.inventory.ItemStack;

public class ValuePluginImplItemPricer extends ValuePlugin {

	@Override
	public double getValue(ItemStack item) {
		return ItemPricer.getPrice(item);
	}
}
