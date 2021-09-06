package hk.siggi.bukkit.chestshop;

import hk.siggi.bukkit.chestshop.shop.Shop;
import java.util.ArrayList;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ShopInfo {
	public Shop shop = null;
	public ArrayList<SellableItem> sellableItems = new ArrayList<>();
	public Inventory inventory;
	public Inventory topInventory;
	public Chest chest = null;
	public boolean closed;
	public ShopInfo() {
	}
	public SellableItem getItem(ItemStack stack) {
		ItemStack unwrapped = ChestShop.unwrap(stack);
		if (unwrapped != null)
			stack = unwrapped;
		for (SellableItem item : sellableItems) {
			if (item.matches(stack)) {
				return item;
			}
		}
		return null;
	}
	public void addSellableItem(SellableItem item) {
		sellableItems.add(item);
	}
}
