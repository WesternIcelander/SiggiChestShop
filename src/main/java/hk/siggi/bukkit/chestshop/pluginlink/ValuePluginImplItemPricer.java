package hk.siggi.bukkit.chestshop.pluginlink;

import io.siggi.itempricer.ItemPricer;
import io.siggi.itempricer.config.Amount;
import io.siggi.itempricer.itemdatabase.ItemInfo;
import io.siggi.nbt.NBTTool;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

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

	public boolean supportsSearch() {
		return true;
	}

	public List<ItemStack> search(String searchString) {
		List<ItemStack> items = new ArrayList<>();
		searchString = searchString.toLowerCase(Locale.ROOT);
		Collection<ItemInfo> itemInfos = ItemPricer.getInstance().getItemDatabase().getItemInfos();
		for (ItemInfo itemInfo : itemInfos) {
			ItemStack itemStack = itemInfo.getItemStack();
			String itemName = NBTTool.getUtil().getItemName(itemStack).toLowerCase(Locale.ROOT);
			if (itemName.contains(searchString)) {
				items.add(itemStack);
			}
		}
		return items;
	}
}
