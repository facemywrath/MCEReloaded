package facejup.mcer.users;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;

import facejup.mcer.achievements.Achievement;
import facejup.mcer.kits.Kit;
import facejup.mcer.kits.MountedTurret;
import facejup.mcer.kits.Shield;
import facejup.mcer.kits.Turret;
import facejup.mcer.main.Main;
import facejup.mcer.storage.SQLManager;
import facejup.mcer.util.Marker;
import net.md_5.bungee.api.ChatColor;

public class User {

	//Match variables
	private int lives = 5;
	private Kit currentKit = Kit.NONE;
	private Kit spawnKit = Kit.NONE;
	private Marker<Entity> lastDamageCause; 

	//Kit specific variables
	private List<Turret> turrets = new ArrayList<>(); // Engineer turret.
	private List<Shield> shields = new ArrayList<>();
	private List<MountedTurret> mountedTurrets = new ArrayList<>();

	//Account variables
	private List<String> unlockedKits = new ArrayList<>();
	private HashMap<Achievement, Integer> achievements = new HashMap<>();
	private int coins;
	private int kills;
	private int deaths;
	private OfflinePlayer player;
	private int runnerups;
	private int wins;
	private int played;

	SQLManager sqlManager;

	public User(SQLManager sqlManager, OfflinePlayer player) {
		this.player = player;
		UUID uuid = player.getUniqueId();
		this.unlockedKits = Arrays.asList(sqlManager.getString(uuid, "kits").split(":"));
		this.coins = sqlManager.getInt(uuid, "coins");
		this.runnerups = sqlManager.getInt(uuid, "runnerups");
		this.wins = sqlManager.getInt(uuid, "wins");
		this.played = sqlManager.getInt(uuid, "played");
		for(Achievement achievement : Achievement.getValues()) {
			achievements.put(achievement, sqlManager.getInt(uuid, achievement.getName()));
		}
		this.sqlManager = sqlManager;
	}

	public void remove() {
		this.lives = 0;
		this.currentKit = Kit.NONE;
		this.spawnKit = Kit.NONE;
		this.unlockedKits = new ArrayList<>();
		this.achievements = new HashMap<>();
		this.coins = 0;
		this.kills = 0;
		this.deaths = 0;
		this.wins = 0;
		this.runnerups = 0;
		this.played = 0;
		this.player = null;

	}

	public int getLives() {
		return lives;
	}

	public boolean isInMatch() {
		if(!sqlManager.getMain().getMatchManager().isRunning()) return false;
		if(lives <= 0) return false;
		if(currentKit == Kit.NONE) return false;
		return true;
	}

	public Kit getCurrentKit() {
		return currentKit==null?Kit.NONE:currentKit;
	}

	public Kit getSpawnKit() {
		return spawnKit==null?getCurrentKit():spawnKit;
	}

	public List<String> getUnlockedKits() {
		return unlockedKits;
	}

	public HashMap<Achievement, Integer> getAchievements() {
		return achievements;
	}
	
	public boolean hasKit(Kit kit) {
		for(String kitName : unlockedKits) {
			if(kitName.equalsIgnoreCase(kit.getName())) return true;
		}
		return false;
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

	public void setCurrentKit(Kit kit) {
		this.currentKit = kit;
	}

	public List<Turret> getTurrets() {
		return turrets;
	}

	public List<MountedTurret> getMountedTurrets() {
		return mountedTurrets;
	}

	public List<Shield> getShields() {
		return shields;
	}

	public Turret placeTurret(Main main, Location location) {
		Turret turret = new Turret(main, player, location);
		this.turrets.add(turret);
		return turret;
	}

	public MountedTurret placeMountedTurret(Main main, Location add) {
		MountedTurret turret = new MountedTurret(main, player, add);
		this.mountedTurrets.add(turret);
		return turret;
	}
	public Shield placeShield(Main main) {
		Shield shield = new Shield(main, player);
		this.shields.add(shield);
		return shield;
	}

	public void removeTurret(Turret turret) {
		this.turrets.remove(turret);
	}

	public void removeMountedTurret(MountedTurret mountedTurret) {
		this.mountedTurrets.remove(mountedTurret);
	}

	public void removeShield(Shield shield) {
		this.shields.remove(shield);
	}

	public void damageUpdate(Main main, Entity damager) {
		Marker<Entity> temp = new Marker<Entity>(damager);
		this.lastDamageCause = temp;
		main.getServer().getScheduler().runTaskLater(main, () -> {
			if(this.lastDamageCause.equals(temp))
				this.lastDamageCause = null;
		}, 160L);
	}

	public void setSpawnKit(Kit kit) {
		Kit tempKit = this.spawnKit;
		this.spawnKit = kit;
		this.getPlayer().getPlayer().sendMessage(ChatColor.GOLD + "You will spawn with kit " + kit.getName());
		if((kit == Kit.NONE && tempKit != Kit.NONE) || (tempKit == Kit.NONE && kit != Kit.NONE))
			this.
			sqlManager
			.getMain()
			.getMatchManager()
			.updateSelected(this.player, kit);
	}

	public Entity lastHitter() {
		return this.lastDamageCause != null ? this.lastDamageCause.getValue() : null;
	}

	public void incDeaths() {
		this.deaths++;
	}

	public void incKills() {
		this.kills++;
	}

	public void incWins() {
		this.wins++;
	}

	public void incMatchesPlayed() {
		this.played++;
	}

	public void incRunnerups() {
		this.runnerups++;
	}

	public void incCoins(int i) {
		this.coins += i;
	}

	public int getRunnerups() {
		return runnerups;
	}

	public int getWins() {
		return wins;
	}

	public int getPlayed() {
		return played;
	}
}
