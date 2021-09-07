package hk.siggi.bukkit.chestshop;

public class Util {
	private Util() {
	}

	public static boolean parseBoolean(String string) {
		return string.equals("true") || string.equals("yes") || string.equals("1");
	}

	public static double buyPrice(double value) {
		if (value < 2.5) {
			return (Math.ceil(value * 10.0) / 10.0) - 0.01;
		} else if (value < 10.0) {
			return (Math.ceil(value * 5.0) / 5.0) - 0.01;
		} else if (value < 35.0) {
			return (Math.ceil(value * 2.0) / 2.0) - 0.01;
		} else if (value < 100.0) {
			return Math.ceil(value) - 0.01;
		} else if (value < 500.0) {
			return (Math.round(value / 5.0) * 5.0) - 0.01;
		} else {
			return (Math.round(value / 10.0) * 10.0) - 0.01;
		}
	}
	public static double sellPrice(double value) {
		value *= ChestShop.getInstance().getSellPortion();
		if (value < 5.0) {
			return Math.round(value * 10.0) / 10.0;
		} else {
			return Math.floor(value);
		}
	}
}
