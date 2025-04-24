package hk.siggi.bukkit.chestshop.shop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.siggi.nbt.NBTTool;
import java.util.ArrayList;

public class Shop {

	public String storeName;
	public int storeSize;
	public boolean allowSelling;
	public ArrayList<ShopItem> items = new ArrayList<>();

	private static final Gson gson;

	static {
		GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
		NBTTool.registerTo(builder);
		gson = builder.create();
	}

	public static Gson getGson() {
		return gson;
	}
}
