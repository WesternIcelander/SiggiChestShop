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
		ItemMeta meta = stack.getItemMeta();
		if (meta.hasLore()) {
			boolean didRemove = false;
			List<String> lore = meta.getLore();
			for (Iterator<String> it = lore.iterator(); it.hasNext();) {
				String str = it.next();
				if (str.startsWith("Sell Value: ")) {
					it.remove();
					didRemove = true;
				}
			}
			if (didRemove) {
				if (lore.isEmpty()) {
					lore = null;
				}
				meta.setLore(lore);
				stack.setItemMeta(meta);
			}
		}
		return item.isSimilar(stack);
	}
}
