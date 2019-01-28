package facejup.mcer.main;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import facejup.mcer.commands.kits.CMDKits;
import facejup.mcer.listeners.UserCacheListener;
import facejup.mcer.storage.SQLManager;
import facejup.mcer.users.UserManager;

public class Main extends JavaPlugin {

	SQLManager sqlManager;
	UserManager userManager;

	public void onEnable() {
		this.saveResource("config.yml", false);
		if(!loadSql()) {
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		System.out.println("MCER: Successfully loaded SQL Connection.");
		
		for(Player player : Bukkit.getOnlinePlayers()) {
			sqlManager.createSection(player.getUniqueId());
		}
		
		userManager = new UserManager(this);
		
		registerCommands();
		registerListeners();
	}
	
	public void registerCommands() {
		new CMDKits(this);
	}
	
	public void registerListeners() {
		new UserCacheListener(this);
	}

	public boolean loadSql() {
		FileConfiguration config = this.getConfig();
		if(!config.contains("SQL.Host") || !config.contains("SQL.Port") || !config.contains("SQL.User") || !config.contains("SQL.Pass") || !config.contains("SQL.Database"))
		{
			System.out.println("MCE Reloaded can not connect to MYSQL. Error in config. Plugin shutting down.");
			return false;
		}
		String host = config.getString("SQL.Host");
		int port = config.getInt("SQL.Port");
		String user = config.getString("SQL.User");
		String pass = config.getString("SQL.Pass");
		String database = config.getString("SQL.Database");
		this.sqlManager = new SQLManager(host, user, pass, database, port);
		if(!sqlManager.openConnection()) {
			System.out.println("MCER: SQL Connection issue.");
			return false;
		}
		return true;
	}

	public SQLManager getSqlManager() {
		return sqlManager;
	}

	public UserManager getUserManager() {
		return userManager;
	}

}
