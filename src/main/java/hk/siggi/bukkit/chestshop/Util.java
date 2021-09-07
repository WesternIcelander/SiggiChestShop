package hk.siggi.bukkit.chestshop;

public class Util {
	private Util() {
	}

	public static boolean parseBoolean(String string) {
		return string.equals("true") || string.equals("yes") || string.equals("1");
	}
}
