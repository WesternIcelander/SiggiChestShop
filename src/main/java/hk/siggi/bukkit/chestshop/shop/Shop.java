package hk.siggi.bukkit.chestshop.shop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hk.siggi.bukkit.nbt.NBTJsonSerializer;
import hk.siggi.bukkit.nbt.NBTTool;
import java.util.ArrayList;

public class Shop {

	public String storeName;
	public int storeSize;
	public boolean allowSelling;
	public ArrayList<ShopItem> items = new ArrayList<>();

	private static final Gson gson;

	static {
		GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
		NBTJsonSerializer serializer = NBTTool.getSerializer();
		serializer.registerTo(builder);
		gson = builder.create();
	}

	public static Gson getGson() {
		return gson;
	}
}
