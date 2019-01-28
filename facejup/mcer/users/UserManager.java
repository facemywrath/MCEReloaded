package facejup.mcer.users;

import java.util.NavigableMap;
import java.util.TreeMap;

import org.bukkit.OfflinePlayer;

import facejup.mcer.listeners.UserCacheListener;
import facejup.mcer.main.Main;
import facejup.mcer.storage.SQLManager;

public class UserManager {

	public final int MAX_USERS = 50;

	private SQLManager sqlManager;
	private NavigableMap<OfflinePlayer, User> users = 
			new TreeMap<OfflinePlayer, User>((offlineplayer1, offlineplayer2) -> 
			Long.compare(offlineplayer1.getLastPlayed(), offlineplayer2.getLastPlayed()));

	public UserManager(Main main) {
		this.sqlManager = main.getSqlManager();
	}

	public void addUser(OfflinePlayer player) {
		this.users.put(player, new User(sqlManager, player));
	}
	
	public SQLManager getSqlManager() {
		return sqlManager;
	}

	public NavigableMap<OfflinePlayer, User> getUsers() {
		return this.users;
	}

}
