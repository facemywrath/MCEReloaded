package facejup.mcer.users;

import java.util.NavigableMap;
import java.util.TreeMap;

import org.bukkit.OfflinePlayer;

import facejup.mcer.main.Main;
import facejup.mcer.storage.PlaceableManager;
import facejup.mcer.storage.SQLManager;

public class UserManager {

	public int max_users = 15;
	
	private PlaceableManager turretManager;
	private SQLManager sqlManager;
	private NavigableMap<OfflinePlayer, User> users = 
			new TreeMap<OfflinePlayer, User>((offlineplayer1, offlineplayer2) -> 
			Long.compare(offlineplayer1.getLastPlayed(), offlineplayer2.getLastPlayed()));

	public UserManager(Main main) {
		this.sqlManager = main.getSqlManager();
		this.turretManager = new PlaceableManager(this);
		if(main.getConfig().contains("Users.Max")) this.max_users = main.getConfig().getInt("Users.Max");
	}
	
	public PlaceableManager getPlaceableManager() {
		return this.turretManager;
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
	
	public void removeUser(OfflinePlayer player) {
		if(this.users.containsKey(player)) this.users.remove(player);
	}
	

}
