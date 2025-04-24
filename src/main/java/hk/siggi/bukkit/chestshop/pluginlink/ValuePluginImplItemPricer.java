package hk.siggi.bukkit.chestshop.pluginlink;

import io.siggi.itempricer.ItemPricer;
import io.siggi.itempricer.config.Amount;
import org.bukkit.inventory.ItemStack;

public class ValuePluginImplItemPricer extends ValuePlugin {

	@Override
	public void setValue(ItemStack item, double value) {
		int oldAmount = item.getAmount();
		item.setAmount(1);
		ItemPricer.getInstance().getConfiguration().setItemPrice(item, new Amount(value));
		item.setAmount(oldAmount);
	}

	@Override
	public double getValue(ItemStack item) {
		return ItemPricer.getPrice(item);
	}
}
