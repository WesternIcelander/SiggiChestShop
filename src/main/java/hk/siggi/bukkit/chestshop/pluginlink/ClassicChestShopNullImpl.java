package hk.siggi.bukkit.chestshop.pluginlink;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class ClassicChestShopNullImpl extends ClassicChestShop {

	@Override
	public boolean isShopItem(Block block) {
		return false;
	}

	@Override
	public boolean isShopItem(Sign sign) {
		return false;
	}

	@Override
	public ConvertedShopItem getShopItem(Block block) {
		return null;
	}

	@Override
	public ConvertedShopItem getShopItem(Sign sign) {
		return null;
	}

}
