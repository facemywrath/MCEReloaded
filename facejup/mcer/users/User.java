package facejup.mcer.users;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.OfflinePlayer;

import facejup.mcer.achievements.Achievement;
import facejup.mcer.kits.Kit;
import facejup.mcer.storage.SQLManager;

public class User {
	
	//Match variables
	private int lives;
	private boolean inMatch;
	private Kit currentKit;
	private Kit spawnKit;
	
	//Account variables
	private List<String> unlockedKits = new ArrayList<>();
	private HashMap<Achievement, Integer> achievements = new HashMap<>();
	private int coins;
	private int kills;
	private int deaths;
	private OfflinePlayer player;
	
	public User(SQLManager sqlManager, OfflinePlayer player) {
		this.player = player;
		UUID uuid = player.getUniqueId();
		this.unlockedKits = Arrays.asList(sqlManager.getString(uuid, "kits").split(":"));
		this.coins = sqlManager.getInt(uuid, "coins")+10;
		for(Achievement achievement : Achievement.getValues()) {
			achievements.put(achievement, sqlManager.getInt(uuid, achievement.getName()));
		}
	}

	public void remove() {
		// TODO Auto-generated method stub
		
	}

	public int getLives() {
		return lives;
	}

	public boolean isInMatch() {
		return inMatch;
	}

	public Kit getCurrentKit() {
		return currentKit;
	}

	public Kit getSpawnKit() {
		return spawnKit;
	}

	public List<String> getUnlockedKits() {
		return unlockedKits;
	}

	public HashMap<Achievement, Integer> getAchievements() {
		return achievements;
	}

	public int getCoins() {
		return coins;
	}

	public int getKills() {
		return kills;
	}

	public int getDeaths() {
		return deaths;
	}

	public OfflinePlayer getPlayer() {
		return player;
	}
	
	
}
