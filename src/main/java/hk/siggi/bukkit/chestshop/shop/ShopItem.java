package hk.siggi.bukkit.chestshop.shop;

import hk.siggi.bukkit.chestshop.ChestShop;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShopItem {

	public ItemStack base;
	public double buyPrice;
	public double sellPrice;
	public int slot;

	public ItemStack getStack() {
		ItemStack clone = base.clone();
		clone.setAmount(1);
		ItemMeta itemMeta = clone.getItemMeta();
		List<String> lore = itemMeta.getLore();
		if (lore == null) {
			lore = new ArrayList<>();
		}
		lore.add("Cost: $" + ChestShop.doubleToString(buyPrice));
		itemMeta.setLore(lore);
		clone.setItemMeta(itemMeta);
		return clone;
	}

	public ItemStack getBaseStack() {
		return base;
	}
}
