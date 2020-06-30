package nl.thedutchmc.dutchytpa;

import org.bukkit.plugin.java.JavaPlugin;

public class Tpa extends JavaPlugin {

	@Override
	public void onEnable() {
		getCommand("tpa").setExecutor(new CommandHandler(this));
		getCommand("tpaccept").setExecutor(new CommandHandler(this));
		getCommand("tpdeny").setExecutor(new CommandHandler(this));
		getCommand("tpyes").setExecutor(new CommandHandler(this));
		getCommand("tpno").setExecutor(new CommandHandler(this));

	}	
}
