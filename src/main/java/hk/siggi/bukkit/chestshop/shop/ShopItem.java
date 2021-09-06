package hk.siggi.bukkit.chestshop.shop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import hk.siggi.bukkit.chestshop.pluginlink.EconomyPlugin;
import hk.siggi.bukkit.nbt.NBTCompound;
import hk.siggi.bukkit.nbt.NBTTool;
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
		NBTCompound originalItem = NBTTool.getUtil().itemToNBT(clone);
		ItemMeta itemMeta = clone.getItemMeta();
		List<String> lore = itemMeta.getLore();
		String link = null;
		if (lore == null) {
			lore = new ArrayList<>();
		}
		if (buyPrice > 0.0) {
			lore.add("Cost: " + EconomyPlugin.get().moneyToString(buyPrice));
		}
		for (Iterator<String> it = lore.iterator(); it.hasNext();) {
			String line = it.next();
			if (line.startsWith(">>")) {
				it.remove();
				link = line.substring(2);
			}
		}
		if (lore.isEmpty())
			lore = null;
		itemMeta.setLore(lore);
		clone.setItemMeta(itemMeta);
		NBTCompound tag = NBTTool.getUtil().getTag(clone);
		NBTCompound siggiChestShopTag = NBTTool.getUtil().newCompound();
		if (buyPrice > 0.0) {
			siggiChestShopTag.setInt("allowBuying", 1);
			siggiChestShopTag.setDouble("cost", buyPrice);
		}
		if (link != null) {
			siggiChestShopTag.setString("link", link);
		}
		siggiChestShopTag.setCompound("originalItem", originalItem);
		tag.setCompound("SiggiChestShop", siggiChestShopTag);
		clone = NBTTool.getUtil().setTag(clone, tag);
		return clone;
	}

	public ItemStack getBaseStack() {
		return base;
	}
}
