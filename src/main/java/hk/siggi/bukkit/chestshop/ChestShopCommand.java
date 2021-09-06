package hk.siggi.bukkit.chestshop;

import hk.siggi.bukkit.chestshop.pluginlink.EconomyPlugin;
import hk.siggi.bukkit.chestshop.shop.Shop;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class ChestShopCommand implements CommandExecutor, TabExecutor {

	private final ChestShop plugin;

	public ChestShopCommand(ChestShop plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		} else {
			sender.sendMessage("This command can only be run by in-game players.");
			return true;
		}
		if (!player.hasPermission("hk.siggi.bukkit.scs.admin")) {
			sender.sendMessage("You are not allowed to use this command.");
			return true;
		}
		if (split.length == 0) {
			sender.sendMessage("SiggiChestShop Admin:");
			sender.sendMessage("/scs createshop [shopname] - Create a new blank shop");
			sender.sendMessage("/scs setname [shopname] [new name] - Set the name of a shop");
			sender.sendMessage("/scs setsize [shopname] [newsize] - Set the number of rows in a shop");
			sender.sendMessage("/scs open [shopname] - Open a shop as customer");
			sender.sendMessage("/scs item [shopname] - Get Shop paper item to put in a chest");
			sender.sendMessage("/scs edit [shopname] - Edit a shop");
			sender.sendMessage("/scs setprice [buy] [sell] - Set buy and sell values of item in your hand");
			sender.sendMessage("/scs setpriceall [buy] [sell] - Set buy and sell values of all items in your inventory");
			return true;
		}
		if (split[0].equalsIgnoreCase("createshop")) {
			if (ChestShop.getInstance().loadShop(split[0]) != null) {
				sender.sendMessage("Shop already exists!");
			} else {
				String shopN = split[1];
				Shop shop = new Shop();
				shop.storeName = shopN;
				shop.storeSize = 6;
				ChestShop.getInstance().saveShop(shopN, shop);
				sender.sendMessage("Shop " + shopN + " created!");
				sender.sendMessage("Set it's display name with /scs setname " + shopN + " [newname]");
				sender.sendMessage("Set it's size with /scs setsize " + shopN + " [rowcount]");
				sender.sendMessage("Put it in a chest to make that chest the chest shop!");
				sender.sendMessage("Right click the chest while in creative mode to edit it!");
				ChestShop.getInstance().givePaper(player, shopN);
			}
		}
		if (split[0].equalsIgnoreCase("setname")) {
			String shopN = split[1];
			String newName = split[2];
			for (int i = 3; i < split.length; i++) {
				newName += " " + split[i];
			}
			Shop shop = ChestShop.getInstance().loadShop(shopN);
			shop.storeName = newName;
			ChestShop.getInstance().saveShop(shopN, shop);
		}
		if (split[0].equalsIgnoreCase("setsize")) {
			String shopN = split[1];
			Shop shop = ChestShop.getInstance().loadShop(shopN);
			shop.storeSize = Integer.parseInt(split[2]);
			if (shop.storeSize < 1) {
				sender.sendMessage("Minimum size is 1!");
				return true;
			}
			ChestShop.getInstance().saveShop(shopN, shop);
		}
		if (split[0].equalsIgnoreCase("open")) {
			String shopN = split[1];
			ChestShop.getInstance().openShop(player, shopN);
		}
		if (split[0].equalsIgnoreCase("item")) {
			String shopN = split[1];
			ChestShop.getInstance().givePaper(player, shopN);
		}
		if (split[0].equalsIgnoreCase("edit")) {
			String shopN = split[1];
			ChestShop.getInstance().openShopEditor(player, shopN);
		}
		if (split[0].equalsIgnoreCase("setprice")) {
			ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
			if (itemInMainHand == null || itemInMainHand.getType() == Material.AIR) {
				return true;
			}
			double buy = 0.0;
			double sell = 0.0;
			if (split.length >= 2) {
				buy = Double.parseDouble(split[1]);
			}
			if (split.length >= 3) {
				sell = Double.parseDouble(split[2]);
			}
			setPrices(itemInMainHand, buy, sell);
		}
		if (split[0].equalsIgnoreCase("setpriceall")) {
			double buy = 0.0;
			double sell = 0.0;
			if (split.length >= 2) {
				buy = Double.parseDouble(split[1]);
			}
			if (split.length >= 3) {
				sell = Double.parseDouble(split[2]);
			}
			PlayerInventory inventory = player.getInventory();
			for (int i = 0; i < inventory.getSize(); i++) {
				ItemStack item = inventory.getItem(i);
				if (item == null || item.getType() == Material.AIR) {
					continue;
				}
				setPrices(item, buy, sell);
				inventory.setItem(i, item);
			}
		}
		if (split[0].equalsIgnoreCase("list")) {
			File f = ChestShop.getInstance().getDataFolder();
			File[] listFiles = f.listFiles();
			for (File ff : listFiles) {
				String n = ff.getName();
				if (n.toLowerCase().endsWith(".json")) {
					String shopName = n.substring(0, n.length() - 5);
					try {
						shopName += " " + ChestShop.getInstance().loadShop(shopName).storeName;
					} catch (Exception e) {
					}
					player.sendMessage(shopName);
				}
			}
		}
		if (split[0].equalsIgnoreCase("deleteshop")) {
			String shopN = split[1];
			ChestShop.getInstance().getShopFile(shopN).delete();
		}
		return true;
	}

	public ItemStack setPrices(ItemStack stack, double buy, double sell) {
		ItemMeta meta = stack.getItemMeta();
		List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
		for (Iterator<String> it = lore.iterator(); it.hasNext();) {
			String l = it.next();
			if (l.startsWith("BuyCost: ") || l.startsWith("SellVal: ")) {
				it.remove();
			}
		}
		if (buy > 0.0) {
			lore.add("BuyCost: " + Double.toString(buy));
		}
		if (sell > 0.0) {
			lore.add("SellVal: " + Double.toString(sell));
		}
		meta.setLore(lore);
		stack.setItemMeta(meta);
		return stack;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> result = new LinkedList<>();
		Consumer<String> addSuggestion = (suggestion) -> {
			if (suggestion.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
				result.add(suggestion);
			}
		};
		if (args.length == 1) {
			addSuggestion.accept("createshop");
			addSuggestion.accept("setname");
			addSuggestion.accept("setsize");
			addSuggestion.accept("open");
			addSuggestion.accept("item");
			addSuggestion.accept("edit");
			addSuggestion.accept("setprice");
			addSuggestion.accept("setpriceall");
		} else if (args.length == 2) {
			switch (args[0]) {
				case "setname":
				case "setsize":
				case "open":
				case "item":
				case "edit": {
					for (File f : plugin.getDataFolder().listFiles()) {
						String n = f.getName();
						if (!n.endsWith(".json")) {
							continue;
						}
						n = n.substring(0, n.length() - 5);
						addSuggestion.accept(n);
					}
				}
				break;
			}
		} else if (args.length == 3) {
			switch (args[0]) {
				case "setsize": {
					addSuggestion.accept("1");
					addSuggestion.accept("2");
					addSuggestion.accept("3");
					addSuggestion.accept("4");
					addSuggestion.accept("5");
					addSuggestion.accept("6");
				}
				break;
			}
		}
		return result;
	}
}
