package hk.siggi.bukkit.chestshop.pluginlink;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class ClassicChestShop {

	private static ClassicChestShop instance = null;

	public static ClassicChestShop get() {
		if (instance == null) {
			try {
				instance = new ClassicChestShopImpl();
			} catch (Exception e) {
			}
		}
		return instance;
	}

	public abstract boolean isShopItem(Block block);

	public abstract boolean isShopItem(Sign block);

	public abstract ConvertedShopItem getShopItem(Block block);

	public abstract ConvertedShopItem getShopItem(Sign sign);

	public static final class ConvertedShopItem {

		private final ItemStack item;
		private final double buyPrice;
		private final double sellPrice;

		public ConvertedShopItem(ItemStack item, double buyPrice, double sellPrice) {
			this.item = item;
			this.buyPrice = buyPrice;
			this.sellPrice = sellPrice;
		}

		public ItemStack getItem() {
			return item.clone();
		}

		public double getBuyPrice() {
			return buyPrice;
		}

		public double getSellPrice() {
			return sellPrice;
		}

		public ItemStack getAsConfigStack() {
			ItemStack stack = item.clone();
			ItemMeta meta = stack.getItemMeta();

			List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
			if (buyPrice > 0.0) {
				lore.add("BuyCost: $" + buyPrice);
			}
			if (sellPrice > 0.0) {
				lore.add("SellVal: $" + sellPrice);
			}
			meta.setLore(lore);

			stack.setItemMeta(meta);
			return stack;
		}
	}
}
