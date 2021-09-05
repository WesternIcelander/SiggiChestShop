package hk.siggi.bukkit.chestshop.pluginlink;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.Breeze.Utils.PriceUtil;
import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

public class ClassicChestShopImpl extends ClassicChestShop {

	private ChestShop cs = null;

	public ClassicChestShopImpl() {
		cs = ChestShop.getPlugin();
	}
	
	@Override
	public boolean isShopItem(Block block){
		try {
			return isShopItem((Sign) block.getState());
		} catch (Exception e) {
		}
		return false;
	}
	
	@Override
	public boolean isShopItem(Sign sign){
		return ChestShopSign.isValid(sign);
	}

	@Override
	public ConvertedShopItem getShopItem(Block block) {
		try {
			return getShopItem((Sign) block.getState());
		} catch (Exception e) {
		}
		return null;
	}

	@Override
	public ConvertedShopItem getShopItem(Sign sign) {
		try {
			String[] lines = sign.getLines();
			String name = lines[0];
			String quantity = lines[1];
			String prices = lines[2];
			String material = lines[3];
			
			ItemStack item = MaterialUtil.getItem(material);
			item.setAmount(Integer.parseInt(quantity));
			
			double buyPrice = PriceUtil.getBuyPrice(prices);
			double sellPrice = PriceUtil.getSellPrice(prices);
			
			return new ConvertedShopItem(item, buyPrice, sellPrice);
		} catch (Exception e) {
		}
		return null;
	}

}
