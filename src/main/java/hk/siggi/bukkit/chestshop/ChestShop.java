package hk.siggi.bukkit.chestshop;

import static hk.siggi.bukkit.chestshop.ReflectionUtil.getMethod;
import static hk.siggi.bukkit.chestshop.ReflectionUtil.setInt;
import hk.siggi.bukkit.chestshop.pluginlink.ClassicChestShop;
import hk.siggi.bukkit.chestshop.pluginlink.EconomyPlugin;
import hk.siggi.bukkit.chestshop.shop.Shop;
import hk.siggi.bukkit.chestshop.shop.ShopItem;
import hk.siggi.bukkit.nbt.NBTCompound;
import hk.siggi.bukkit.nbt.NBTTool;
import hk.siggi.bukkit.nbt.NBTUtil;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Sign;
import org.bukkit.command.PluginCommand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ChestShop extends JavaPlugin implements Listener {

	private static ChestShop instance;

	public static ChestShop getInstance() {
		return instance;
	}
	private final ArrayList inventories = new ArrayList();
	private final Map<Inventory, Shop> shopEditors = new WeakHashMap<>();
	private final Map<Inventory, String> shopEditorNames = new WeakHashMap<>();
	private final WeakHashMap<Player, ShopInfo> currentlyOpenShops = new WeakHashMap<>();
	private boolean checkedCTShop = false;
	private boolean hasCubeTokensShop = false;

	@Override
	public void onLoad() {
		instance = this;
	}

	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
		PluginCommand command = getCommand("scs");
		ChestShopCommand csc = new ChestShopCommand(this);
		command.setExecutor(csc);
		command.setTabCompleter(csc);
		new BukkitRunnable() {
			@Override
			public void run() {
				for (Map.Entry<Player, ShopInfo> entry : currentlyOpenShops.entrySet()) {
					Player p = entry.getKey();
					ShopInfo info = entry.getValue();
					Inventory inv = info.topInventory;
					removeSoldItems(p, info, inv);
				}
			}
		}.runTaskTimer(this, 1L, 1L);
	}

	@Override
	public void onDisable() {
		// Clear the CubeTokens inventories so people can't steal from them
		// in the event that the plugin gets disabled while they're in the
		// CubeTokens shop.
		Inventory[] inventory = (Inventory[]) inventories.toArray(new Inventory[inventories.size()]);
		for (Inventory inv : inventory) {
			inv.clear();
		}
		inventories.clear();
	}

	public double getBalance(UUID uuid) {
		return EconomyPlugin.get().balance(uuid);
	}

	public boolean giveMoney(final UUID uuid, final double moneyToAdd, long quantity, String info) {
		try {
			return EconomyPlugin.get().deposit(moneyToAdd, quantity, uuid, info);
		} catch (Exception e) {
			return false;
		}
	}

	public boolean chargeMoney(final UUID uuid, final double moneyToTake, long quantity, String info) {
		double oldBalance = EconomyPlugin.get().balance(uuid);
		if (oldBalance < moneyToTake) {
			return false;
		}
		return EconomyPlugin.get().withdraw(moneyToTake, quantity, uuid, info);
	}

	public ItemStack createMoneyLeftItemStack(double money, boolean allowSelling) {
		ItemStack stack = new ItemStack(Material.DIAMOND, 1);
		ItemMeta meta = stack.getItemMeta();
		String colourCode = new String(new char[]{(char) 0xA7});
		meta.setDisplayName(colourCode + "rYou currently have: " + colourCode + "6" + EconomyPlugin.get().moneyToString(money) + "");
		List<String> lore = new ArrayList<>();
		lore.add(colourCode + "r");
		lore.add(colourCode + "rTo buy an item, move it to your inventory.");
		if (allowSelling) {
			lore.add(colourCode + "rTo sell an item, move it to the shop.");
		}
		meta.setLore(lore);
		meta.addEnchant(Enchantment.LOOT_BONUS_MOBS, 3, true);
		stack.setItemMeta(meta);
		NBTUtil util = NBTTool.getUtil();
		NBTCompound tag = util.getTag(stack);
		tag.setByte("HideFlags", (byte) 63);
		util.setTag(stack, tag);
		return stack;
	}

	public Shop loadShop(String shop) {
		try {
			return Shop.getGson().fromJson(new FileReader(getShopFile(shop)), Shop.class);
		} catch (Exception e) {
			return null;
		}
	}

	public boolean saveShop(String shopName, Shop shop) {
		try (FileWriter writer = new FileWriter(getShopFile(shopName))) {
			Shop.getGson().toJson(shop, writer);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public void openShop(Player player, String shop) {
		openShop(player, shop, null);
	}

	public void openShop(final Player player, String shop, Chest chest) {
		String colourCode = new String(new char[]{(char) 0xA7});
		File tokenShop = getShopFile(shop);
		if (!tokenShop.exists()) {
			player.sendMessage(ChatColor.RED + "An error has occurred. (shop file missing)");
			return;
		}
		Inventory inventory = null;
		ShopInfo info = null;
		try {
			Shop theShopInfo = loadShop(shop);
			inventory = getServer().createInventory(player, 9 * theShopInfo.storeSize, theShopInfo.storeName);
			inventory.setItem(0, createMoneyLeftItemStack(getBalance(player.getUniqueId()), theShopInfo.allowSelling));
			info = new ShopInfo();
			info.shop = theShopInfo;
			for (ShopItem item : theShopInfo.items) {
				try {
					ItemStack stack = item.getStack();
					if (item.slot > 0) {
						inventory.setItem(item.slot, stack);
					} else {
						inventory.addItem(stack);
					}
					if (theShopInfo.allowSelling && item.sellPrice > 0.0) {
						info.addSellableItem(new SellableItem(item.getBaseStack(), item.sellPrice));
					}
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		final ShopInfo finalShopInfo = info;
		if (inventory != null && info != null) {
			ShopInfo oldShopInfo = currentlyOpenShops.get(player);
			if (oldShopInfo != null) {
				if (chest == null) {
					chest = oldShopInfo.chest;
				}
				oldShopInfo.closed = true;
			}
			info.inventory = inventory;
			info.chest = chest;
			InventoryView view = player.openInventory(inventory);
			inventories.add(view.getTopInventory());
			info.topInventory = inventory;
			currentlyOpenShops.put(player, info);
			addLores(player, info);
			if (chest != null) {
				openChest(chest, player);
			}
			if (info.chest != null) {
				new BukkitRunnable() {
					@Override
					public void run() {
						if (finalShopInfo.closed || currentlyOpenShops.get(player) != finalShopInfo) {
							cancel();
							return;
						}
						try {
							Object tileEntity = getTileEntity(finalShopInfo.chest);
							setInt(tileEntity, "viewingCount", 1);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}.runTaskTimer(this, 1L, 1L);
			}
		}
	}

	public boolean openShopEditor(Player player, String shopName) {
		try {
			Shop shop = Shop.getGson().fromJson(new FileReader(getShopFile(shopName)), Shop.class);
			Inventory inventory = getServer().createInventory(player, 9 * shop.storeSize, shop.storeName);
			inventory.setItem(0, new ItemStack(Material.BARRIER));
			for (ShopItem item : shop.items) {
				try {
					ItemStack stack = item.getBaseStack();
					ItemMeta meta = stack.getItemMeta();
					List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
					lore.add("BuyCost: " + EconomyPlugin.get().moneyToString(item.buyPrice));
					if (shop.allowSelling && item.sellPrice > 0.0) {
						lore.add("SellVal: " + EconomyPlugin.get().moneyToString(item.sellPrice));
					}
					meta.setLore(lore);
					stack.setItemMeta(meta);
					if (item.slot > 0) {
						inventory.setItem(item.slot, stack);
					} else {
						inventory.addItem(stack);
					}
				} catch (Exception e) {
				}
			}
			InventoryView view = player.openInventory(inventory);
			Inventory topInventory = view.getTopInventory();
			shopEditors.put(topInventory, shop);
			shopEditorNames.put(topInventory, shopName);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void closedShopEditor(Player player, Inventory inventory, String shopName, Shop shop) {
		shop.items.clear();
		shop.allowSelling = false;
		for (int i = 1; i < shop.storeSize * 9; i++) {
			ItemStack item = inventory.getItem(i);
			if (item == null || item.getType() == Material.AIR) {
				continue;
			}
			item.setAmount(1);
			ShopItem shopItem = new ShopItem();
			double buy = 0.0;
			double sell = 0.0;
			ItemMeta meta = item.getItemMeta();
			if (meta.hasLore()) {
				List<String> lore = meta.getLore();
				for (String l : lore) {
					try {
						if (l.startsWith("BuyCost: $")) {
							buy = Double.parseDouble(l.substring(10));
						} else if (l.startsWith("SellVal: $")) {
							sell = Double.parseDouble(l.substring(10));
						}
					} catch (Exception e) {
					}
				}
			}
			if (sell > 0.0) {
				shop.allowSelling = true;
			}
			shopItem.base = cleanItem(item, true);
			shopItem.buyPrice = buy;
			shopItem.sellPrice = sell;
			shopItem.slot = i;
			shop.items.add(shopItem);
		}
		if (saveShop(shopName, shop)) {
			player.sendMessage("Shop saved");
		} else {
			player.sendMessage("Shop failed to save!");
		}
	}

	File getShopFile(String shopName) {
		return new File(getDataFolder(), shopName + ".json");
	}

	public void openChest(Chest chest, Player p) {
		call(chest, p, "startOpen");
	}

	public void closeChest(Chest chest, Player p) {
		call(chest, p, "closeContainer");
	}

	private void call(Chest chest, Player p, String method) {
		try {
			Object obj = getTileEntity(chest);
			if (obj == null) {
				return;
			}
			Object nmsPlayer = getNMSPlayer(p);
			Class nmsHumanEntity = nmsPlayer.getClass().getSuperclass();
			Method methodToCall = getMethod(obj.getClass(), method, nmsHumanEntity);
			methodToCall.setAccessible(true);
			methodToCall.invoke(obj, nmsPlayer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Object getTileEntity(Chest c) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Method getTileEntity = getMethod(c.getClass(), "getTileEntity");
		getTileEntity.setAccessible(true);
		return getTileEntity.invoke(c);
	}

	private Object getNMSPlayer(Player p) throws IllegalAccessException {
		try {
			Field entityField = p.getClass().getSuperclass().getSuperclass().getSuperclass().getDeclaredField("entity");
			entityField.setAccessible(true);
			return entityField.get(p);
		} catch (NoSuchFieldException ex) {
			throw new RuntimeException(ex);
		}
	}

	public ShopInfo getCurrentShop(Player player) {
		return currentlyOpenShops.get(player);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void clickInventory(InventoryClickEvent event) {
		try {
			Inventory inventory = event.getInventory();
			if (!inventories.contains(inventory)) {
				return;
			}
			HumanEntity he = event.getWhoClicked();
			Player p = null;
			ShopInfo shop = null;
			if (he instanceof Player) {
				p = (Player) he;
				shop = currentlyOpenShops.get(p);
			}
			if (p == null || shop == null) {
				return;
			}

			int rawSlot = event.getRawSlot();
			if (rawSlot == 0) {
				event.setCancelled(true);
				try {
					updateInventory(p);
				} catch (Exception e) {
				}
				return;
			}
			int firstSlotOfPlayerInventory = inventory.getSize();
			InventoryAction action = event.getAction();
			if (action == InventoryAction.DROP_ALL_CURSOR || action == InventoryAction.DROP_ONE_CURSOR
					|| action == InventoryAction.DROP_ALL_SLOT || action == InventoryAction.DROP_ONE_SLOT) {
				event.setCancelled(true);
				try {
					updateInventory(p);
				} catch (Exception e) {
				}
			} else {
				if (rawSlot < firstSlotOfPlayerInventory) { // clicking on shop inventory
					if (action == InventoryAction.PICKUP_ALL || action == InventoryAction.PICKUP_HALF || action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
						// buying by left-click, right-click or shift-click
						int amount = 1;
						ItemStack stack = event.getCurrentItem();
						boolean rightClickWorkaround = false;
						if (action == InventoryAction.PICKUP_HALF || action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
							amount = stack.getType().getMaxStackSize();
							if (action == InventoryAction.PICKUP_HALF) {
								rightClickWorkaround = true;
							}
						}
						if (stack != null) {
							ItemStack originalItem = stack.clone();
							boolean allowBuying = true;
							double money = 0;
							ItemMeta meta = stack.getItemMeta();
							String linkStore = null;
							String itemDisplayName = meta.getDisplayName();
							if (itemDisplayName != null) {
								if (ChatColor.stripColor(itemDisplayName).equalsIgnoreCase("Information")) {
									allowBuying = false;
								}
							}
							List lore = meta.getLore();
							if (lore != null) {
								String[] loreStrings = (String[]) lore.toArray(new String[lore.size()]);
								for (int i = 0; i < loreStrings.length; i++) {
									if (loreStrings[i].startsWith("Cost: $")) {
										money = Double.parseDouble(loreStrings[i].substring(7));
										lore.remove(loreStrings[i]);
									}
									if (loreStrings[i].startsWith(">>")) {
										linkStore = loreStrings[i].substring(2);
										lore.remove(loreStrings[i]);
									}
									if (loreStrings[i].startsWith("Sell Value: ")) {
										allowBuying = false;
									}
								}
								if (lore.isEmpty()) {
									lore = null;
								}
								meta.setLore(lore);
							}
							String itemName = NBTTool.getUtil().getItemName(stack);
							if (itemDisplayName != null) {
								itemName = itemDisplayName + " (" + itemName + ")";
							}
							if (linkStore != null) {
								event.setCancelled(true);
								try {
									updateInventory(p);
								} catch (Exception e) {
								}
								openShop(p, linkStore);
							} else if (!allowBuying) {
								event.setCancelled(true);
								try {
									updateInventory(p);
								} catch (Exception e) {
								}
							} else if (chargeMoney(p.getUniqueId(), money, amount, "Buy " + itemName)) {
								stack.setItemMeta(meta);
								if (money > 0.0) {
									stack.setAmount(amount);
								}
								event.setCurrentItem(stack);
								double sellPrice = -1.0;
								SellableItem sellableItem = shop.getItem(stack);
								if (sellableItem != null) {
									sellPrice = sellableItem.value;
								}
								if (sellPrice > 0.0) {
									addSellLore(stack, sellPrice);
								}
								event.setCurrentItem(stack);
								if (rightClickWorkaround) {
									event.setCancelled(true);
									event.getView().setCursor(stack);
								}
								inventory.setItem(0, createMoneyLeftItemStack(getBalance(p.getUniqueId()), shop.shop.allowSelling));
								if (money > 0.0) {
									readdBoughtItem(originalItem, inventory, rawSlot);
								}
								try {
									updateInventory(p);
								} catch (Exception e) {
								}
							} else {
								event.setCancelled(true);
								try {
									p.sendMessage(ChatColor.RED + "You don't have enough money to buy this!");
									updateInventory(p);
								} catch (Exception e) {
								}
							}
						}
					} else if (action == InventoryAction.PLACE_ONE
							|| action == InventoryAction.PLACE_ALL) {
						ItemStack stack = event.getCursor();
						int amount = ((action == InventoryAction.PLACE_ONE) ? 1 : stack.getAmount());
						sellItem(p, stack, amount, shop, event, inventory, firstSlotOfPlayerInventory, rawSlot);
					} else {
						event.setCancelled(true);
						try {
							updateInventory(p);
						} catch (Exception e) {
						}
						return;
					}
				} else {
					if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
						ItemStack stack = event.getCurrentItem();
						int amount = stack.getAmount();
						sellItem(p, stack, amount, shop, event, inventory, firstSlotOfPlayerInventory, rawSlot);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sellItem(Player p, ItemStack item, int amount, ShopInfo shop, InventoryClickEvent event,
			Inventory inventory, int firstSlotOfPlayerInventory, int rawSlot) {
		try {
			if (shop.shop.allowSelling) {
				SellableItem sellableItem = shop.getItem(item);
				if (sellableItem == null) {
					p.sendMessage(ChatColor.RED + "You cannot sell this item to this shop.");
					event.setCancelled(true);
					try {
						updateInventory(p);
					} catch (Exception e) {
					}
					return;
				}
				//double payout = amount * sellableItem.value;
				//giveMoney(p.getUniqueId(), payout);
				removeSoldItems(p, shop, inventory);
			} else {
				p.sendMessage(ChatColor.RED + "This shop does not buy items.");
				event.setCancelled(true);
				try {
					updateInventory(p);
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			p.sendMessage(ChatColor.RED + "Something went wrong. :/");
			event.setCancelled(true);
			try {
				updateInventory(p);
			} catch (Exception e2) {
			}
		}
	}

	private void readdBoughtItem(final ItemStack stack, final Inventory inventory, final int slot) {
		new BukkitRunnable() {
			@Override
			public void run() {
				inventory.setItem(slot, stack);
			}
		}.runTask(this);
	}

	private void removeSoldItems(final Player p, final ShopInfo shop, final Inventory inventory) {
		new BukkitRunnable() {
			@Override
			public void run() {
				boolean changed = false;
				ItemStack[] contents = inventory.getContents();
				for (int i = 0; i < contents.length; i++) {
					ItemStack stack = contents[i];
					if (stack == null || stack.getType() == Material.AIR) {
						continue;
					}
					ItemMeta meta = stack.getItemMeta();
					List<String> lores = meta.getLore();
					double itemValue = 0.0;
					if (lores != null) {
						for (String lore : lores) {
							if (lore.startsWith("Sell Value: $")) {
								itemValue = (Double.parseDouble(lore.substring(13)));
							}
						}
					} else {
						SellableItem item = shop.getItem(stack);
						if (item != null) {
							itemValue = item.value;
						}
					}
					if (itemValue <= 0.0) {
						continue;
					}
					contents[i] = null;
					changed = true;
					try {
						String itemDisplayName = meta.getDisplayName();
						String itemName = NBTTool.getUtil().getItemName(stack);
						if (itemDisplayName != null) {
							itemName = itemDisplayName + " (" + itemName + ")";
						}
						giveMoney(p.getUniqueId(), itemValue, stack.getAmount(), "Sold " + itemName);
					} catch (Exception e) {
					}
				}
				if (changed) {
					inventory.setContents(contents);
				}
			}
		}.runTask(this);
	}

	public void updateInventory(final Player player) {
		new BukkitRunnable() {
			@Override
			public void run() {
				player.updateInventory();
			}
		}.runTask(this);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void closeInventory(InventoryCloseEvent event) {
		Inventory inventory = event.getInventory();
		if (inventories.contains(inventory)) {
			inventories.remove(inventory);
		}
		HumanEntity he = event.getPlayer();
		if (he instanceof Player) {
			Player p = (Player) he;
			ShopInfo shop = currentlyOpenShops.get(p);
			if (shop != null) {
				if (shop.chest != null) {
					closeChest(shop.chest, p);
					shop.chest = null;
				}
			}
			ShopInfo removedShop = currentlyOpenShops.remove(p);
			if (removedShop != null) {
				removedShop.closed = true;
			}
			if (shop != null) {
				removeSoldItems(p, shop, event.getInventory());
				clearLores(p);
			}
			if (shopEditors.containsKey(inventory)) {
				Shop editedShop = shopEditors.get(inventory);
				shopEditors.remove(inventory);
				if (shopEditorNames.containsKey(inventory)) {
					String shopName = shopEditorNames.get(inventory);
					shopEditorNames.remove(inventory);
					closedShopEditor(p, inventory, shopName, editedShop);
				}
			} else if (shopEditorNames.containsKey(inventory)) {
				shopEditorNames.remove(inventory);
			}
		}
	}

	public void addLores(Player p, ShopInfo info) {
		if (!info.shop.allowSelling) {
			return;
		}
		Inventory inv = p.getInventory();
		ItemStack[] items = inv.getContents();
		boolean didChange = false;
		for (int i = 0; i < items.length; i++) {
			ItemStack stack = items[i];
			if (stack == null) {
				continue;
			}
			if (stack.getType() == Material.AIR) {
				continue;
			}
			SellableItem sellableItem = info.getItem(stack);
			if (sellableItem != null) {
				double value = sellableItem.value;
				addSellLore(stack, value);
				items[i] = stack;
				didChange = true;
			}
		}
		if (didChange) {
			inv.setContents(items);
		}
	}

	public void clearLores(Player p) {
		Inventory inv = p.getInventory();
		ItemStack[] items = inv.getContents();
		boolean didChange = false;
		for (int i = 0; i < inv.getSize(); i++) {
			ItemStack stack = inv.getItem(i);
			if (stack == null) {
				continue;
			}
			if (stack.getType() == Material.AIR) {
				continue;
			}
			ItemMeta meta = stack.getItemMeta();
			List<String> lore = meta.getLore();
			if (lore == null) {
				continue;
			}
			boolean didChangeLore = false;
			for (int j = 0; j < lore.size(); j++) {
				String loreLine = lore.get(j);
				if (loreLine.startsWith("Sell Value: ")) {
					lore.remove(j);
					j -= 1;
					didChangeLore = true;
				}
			}
			if (didChangeLore) {
				if (lore.isEmpty()) {
					lore = null;
				}
				meta.setLore(lore);
				stack.setItemMeta(meta);
				items[i] = stack;
				didChange = true;
			}
		}
		if (didChange) {
			inv.setContents(items);
		}
	}

	public void addSellLore(ItemStack stack, double value) {
		ItemMeta meta = stack.getItemMeta();
		List<String> lore = meta.getLore();
		if (lore == null) {
			lore = new ArrayList<String>();
		}
		lore.add("Sell Value: " + EconomyPlugin.get().moneyToString(value));
		meta.setLore(lore);
		stack.setItemMeta(meta);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void interact(PlayerInteractEvent event) {
		Action action = event.getAction();
		if (action != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		Block block = event.getClickedBlock();
		BlockState state = block.getState();
		String shopToOpen = null;
		Player p = event.getPlayer();
		if (state instanceof Chest) {
			Chest chest = (Chest) state;
			Inventory chestInventory = chest.getBlockInventory();
			for (ItemStack stack : chestInventory.getContents()) {
				if (stack == null) {
					continue;
				}
				if (stack.getType() == Material.PAPER) {
					ItemMeta itemMeta = stack.getItemMeta();
					if (itemMeta == null) {
						continue;
					}
					List<String> lores = itemMeta.getLore();
					if (lores == null) {
						continue;
					}
					for (String lore : lores) {
						if (lore.startsWith("ShopID:")) {
							shopToOpen = lore.substring(7);
						}
					}
				}
				if (shopToOpen != null) {
					break;
				}
			}
			if (shopToOpen != null) {
				event.setCancelled(true);
				if (p.getGameMode() == GameMode.CREATIVE && p.hasPermission("hk.siggi.bukkit.scs.admin")) {
					ChestShop.getInstance().openShopEditor(p, shopToOpen);
				} else {
					openShop(p, shopToOpen, chest);
				}
			}
		} else if (state instanceof Sign) {
			if (p.getGameMode() == GameMode.CREATIVE) {
				Sign sign = (Sign) state;
				ClassicChestShop ccs = ClassicChestShop.get();
				if (ccs.isShopItem(sign)) {
					ClassicChestShop.ConvertedShopItem convertedItem = ccs.getShopItem(sign);
					ItemStack stack = convertedItem.getAsConfigStack();
					p.getInventory().addItem(stack);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void placeBlock(BlockPlaceEvent event) {
		if (!checkedCTShop) {
			checkedCTShop = true;
			hasCubeTokensShop = (getServer().getPluginManager().getPlugin("CubeTokens")) != null;
		}
		if (hasCubeTokensShop) {
			return;
		}
		if (event.isCancelled()) {
			return;
		}
		ItemStack inHand = event.getItemInHand();
		if (inHand.getType() == getSpawnerMaterial()) {
			ItemMeta meta = inHand.getItemMeta();
			String displayName = meta.getDisplayName();
			if (displayName != null) {
				String name = ChatColor.stripColor(displayName);
				if (!name.equals(displayName)) {
					if (name.toLowerCase().endsWith(" spawner")) {
						String creatureName = name.substring(0, name.length() - 8);
						creatureName = creatureName.toUpperCase().replaceAll(" ", "_");
						EntityType type = EntityType.valueOf(creatureName);
						Block block = event.getBlockPlaced();
						block.setType(getSpawnerMaterial());
						CreatureSpawner spawner = (CreatureSpawner) block.getState();
						spawner.setSpawnedType(type);
						spawner.update();
					}
				}
			}
		}
	}

	private Material spawnerMaterial;

	private Material getSpawnerMaterial() {
		if (spawnerMaterial == null) {
			try {
				spawnerMaterial = Material.SPAWNER;
			} catch (Throwable t) {
				spawnerMaterial = Material.getMaterial("MOB_SPAWNER");
			}
		}
		return spawnerMaterial;
	}

	@EventHandler
	public void pickUpItem(EntityPickupItemEvent event) {
		LivingEntity entity = event.getEntity();
		if (!(entity instanceof Player)) {
			return;
		}
		Player p = (Player) entity;
		ShopInfo shop = getCurrentShop(p);
		if (shop != null) {
			event.setCancelled(true);
		}
	}

	public ItemStack cleanItem(ItemStack stack, boolean removeEditorStuff) {
		ItemMeta meta = stack.getItemMeta();
		List<String> lore = meta.getLore();
		if (lore == null) {
			return stack;
		}
		for (Iterator<String> it = lore.iterator(); it.hasNext();) {
			String s = it.next();
			if (s.equalsIgnoreCase("Spawned Item")
					|| s.equalsIgnoreCase("Modified Item")
					|| s.startsWith(">>")
					|| s.startsWith("Sell Value: ")
					|| s.startsWith("Price: ")
					|| (removeEditorStuff && (s.startsWith("BuyCost: ") || s.startsWith("SellVal: ")))) {
				it.remove();
			}
		}
		if (lore.isEmpty()) {
			lore = null;
		}
		meta.setLore(lore);
		stack.setItemMeta(meta);
		return stack;
	}

	public void givePaper(Player player, String shopN) {
		ItemStack stack = new ItemStack(Material.PAPER);
		ItemMeta itemMeta = stack.getItemMeta();
		List<String> lore = new ArrayList<>();
		lore.add("ShopID:" + shopN);
		itemMeta.setLore(lore);
		stack.setItemMeta(itemMeta);
		player.getInventory().addItem(stack);
	}
}
