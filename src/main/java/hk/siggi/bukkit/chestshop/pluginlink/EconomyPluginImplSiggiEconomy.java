package hk.siggi.bukkit.chestshop.pluginlink;

import io.siggi.economy.SiggiEconomy;
import java.util.UUID;

public class EconomyPluginImplSiggiEconomy extends EconomyPlugin {

	public EconomyPluginImplSiggiEconomy() {
	}

	@Override
	public double balance(UUID player) {
		return SiggiEconomy.getUser(player).getBalance();
	}

	@Override
	public boolean deposit(double amount, long quantity, UUID player, String info) {
		if (amount < 0.0) {
			return false;
		}
		if (amount == 0.0) {
			return true;
		}
		return SiggiEconomy.getUser(player).deposit(amount, quantity, info).isSuccessful();
	}

	@Override
	public boolean withdraw(double amount, long quantity, UUID player, String info) {
		if (amount < 0.0) {
			return false;
		}
		if (amount == 0.0) {
			return true;
		}
		return SiggiEconomy.getUser(player).withdraw(amount, quantity, info).isSuccessful();
	}

	@Override
	public String moneyToString(double amount) {
		return SiggiEconomy.moneyToString(amount);
	}

}
