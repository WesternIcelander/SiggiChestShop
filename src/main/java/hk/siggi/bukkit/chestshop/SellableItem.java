package hk.siggi.bukkit.chestshop;

import java.util.Iterator;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SellableItem {

	public final ItemStack item;
	public final double value;

	public SellableItem(ItemStack item, double value) {
		this.item = item;
		this.value = value;
	}

	public boolean matches(ItemStack stack) {
		return item.isSimilar(stack);
	}
}
