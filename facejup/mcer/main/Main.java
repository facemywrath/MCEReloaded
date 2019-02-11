package facejup.mcer.main;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import facejup.mcer.commands.arena.CMDArena;
import facejup.mcer.commands.kits.CMDKits;
import facejup.mcer.commands.stats.CMDStats;
import facejup.mcer.kits.Kit;
import facejup.mcer.listeners.KitPowerListener;
import facejup.mcer.listeners.UserCacheListener;
import facejup.mcer.match.MatchManager;
import facejup.mcer.storage.SQLManager;
import facejup.mcer.users.User;
import facejup.mcer.users.UserManager;

public class Main extends JavaPlugin {

	SQLManager sqlManager;
	UserManager userManager;
	MatchManager matchManager;
	ProtocolManager protocolManager;
	
	public void onEnable() {
		this.saveResource("config.yml", false);
		this.saveResource("arena.yml", false);
		if(!loadSql()) {
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		System.out.println("MCER: Successfully loaded SQL Connection.");
		
		
		userManager = new UserManager(this);

		for(Player player : Bukkit.getOnlinePlayers()) {
			sqlManager.createSection(player.getUniqueId());
			userManager.addUser(player);
		}

		this.protocolManager = ProtocolLibrary.getProtocolManager();
		
		registerCommands();
		registerListeners();
		
		this.matchManager = new MatchManager(this);
		for(Kit kit : Kit.values()) {
			if(kit.init() != null) kit.init().accept(this);
		}
	}
	
	public void onDisable() {
		for(User user : userManager.getUsers().values()) {
			sqlManager.saveUser(user);
		}
	}
	
	public File getArenaFile() {
		return new File(this.getDataFolder(), "arena.yml");
	}
	
	public void registerCommands() {
		new CMDKits(this);
		new CMDStats(this);
		new CMDArena(this);
	}
	
	public void registerListeners() {
		new UserCacheListener(this);
		new KitPowerListener(this);
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
		this.sqlManager = new SQLManager(this, host, user, pass, database, port);
		if(!sqlManager.openConnection()) {
			System.out.println("MCER: SQL Connection issue.");
			return false;
		}
		return true;
	}

	public SQLManager getSqlManager() {
		return sqlManager;
	}

	public MatchManager getMatchManager() {
		return matchManager;
	}
	
	public ProtocolManager getProtocolManager() {
		return this.protocolManager;
	}

	public UserManager getUserManager() {
		return userManager;
	}

}
