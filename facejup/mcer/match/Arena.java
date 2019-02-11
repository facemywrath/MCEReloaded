package facejup.mcer.match;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import facejup.mcer.main.Main;
import facejup.mcer.storage.ItemSpawn;

public class Arena {

	private MatchManager matchManager;
	private String name;
	private String creator;
	private World world;
	private Location corner1;
	private Location corner2;
	private List<Location> playerSpawns = new ArrayList<>();
	private List<ItemSpawn> itemSpawns = new ArrayList<>();

	public Arena(MatchManager manager) {
		this.matchManager = manager;
	}

	public boolean loadArena(Main main) {
		File file = new File(main.getDataFolder(), "arena.yml");
		boolean fail = false;
		if(!file.exists()) {
			System.out.println("Arena file not found.");
			fail = true;
		}
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		try{

			if(!config.contains("World") || Bukkit.getWorld(config.getString("World")) == null) {
				System.out.println("Arena world not found.");
				fail = true;
			}			
			if(!config.contains("Name")) {
				System.out.println("Arena name not found.");
				fail = true;
			}
			if(!config.contains("Creator")) {
				System.out.println("Arena creator not found.");
				fail = true;
			}
			if(!config.contains("Corner1.x") || !config.contains("Corner1.y") || !config.contains("Corner1.z")) {
				System.out.println("Corner 1 not loaded properly.");
				fail = true;
			}
			if(!config.contains("Corner2.x") || !config.contains("Corner2.y") || !config.contains("Corner2.z")) {
				System.out.println("Corner 2 not loaded properly.");
				fail = true;
			}
			if(!config.contains("Spawns") || config.getConfigurationSection("Spawns").getKeys(false).isEmpty()) {
				System.out.println("Player spawns not properly defined.");
				fail = true;
			}
			for(String str : config.getConfigurationSection("Spawns").getKeys(false)) {
				if(!config.contains("Spawns." + str + ".x") || !config.contains("Spawns." + str + ".y") || !config.contains("Spawns." + str + ".z") || !config.contains("Spawns." + str + ".pitch") || !config.contains("Spawns." + str + ".yaw")) {
					System.out.println("Player spawns not properly defined.");
					fail = true;
					break;
				}
			}
		}catch(Exception e) {
			System.out.println("Arena not loaded properly. Matches can not start."); 
			return false;

		}
		if(fail) {
			System.out.println("Arena not loaded properly. Matches can not start."); 
			return false;
		}
		name = config.getString("Name");
		creator = config.getString("Creator");
		world = Bukkit.getWorld(config.getString("World"));
		int x1 = config.getInt("Corner1.x");
		int y1 = config.getInt("Corner1.y");
		int z1 = config.getInt("Corner1.z");
		int x2 = config.getInt("Corner2.x");
		int y2 = config.getInt("Corner2.y");
		int z2 = config.getInt("Corner2.z");
		corner1 = new Location(world, x1, y1, z1);
		corner2 = new Location(world, x2, y2, z2);
		for(String str : config.getConfigurationSection("Spawns").getKeys(false)) {
			Location loc = new Location(world, config.getDouble("Spawns." + str + ".x"), config.getDouble("Spawns." + str + ".y"), config.getDouble("Spawns." + str + ".z"));
			loc.setPitch((float) config.getDouble("Spawns." + str + ".pitch"));
			loc.setYaw((float) config.getDouble("Spawns." + str + ".yaw"));
			playerSpawns.add(loc);
		}
		System.out.println("Arena successfully loaded!!!!!!");
		if(!config.contains("ItemSpawns")) return true;
		for(String str : config.getConfigurationSection("ItemSpawns").getKeys(false)) {
			if(!config.contains("ItemSpawns." + str + ".x") || !config.contains("ItemSpawns." + str + ".y") || !config.contains("ItemSpawns." + str + ".z") || !config.contains("ItemSpawns." + str + ".Item") || !config.contains("ItemSpawns." + str + ".Cooldown")) {
				System.out.println("Item spawn " + str + " not properly defined.");
				continue;
			}
			Location spawn = new Location(world, config.getDouble("ItemSpawns." + str + ".x"), config.getDouble("ItemSpawns." + str + ".y"), config.getDouble("ItemSpawns." + str + ".z"));
			ItemStack item = config.getItemStack("ItemSpawns." + str + ".Item");
			long cooldown = config.getLong("ItemSpawns." + str + ".Cooldown");
			itemSpawns.add(new ItemSpawn(main, item, cooldown, spawn.clone().add(new Vector(0.5,0,0.5))));
		}
		return true;
	}

	public World getWorld() {
		return world;
	}

	public Location getCorner1() {
		return corner1;
	}

	public Location getCorner2() {
		return corner2;
	}

	public List<Location> getPlayerSpawns() {
		return playerSpawns;
	}

	public List<ItemSpawn> getItemSpawns() {
		return itemSpawns;
	}

	public double getDistanceToNearestPlayer(Location loc) {
		Optional<Location> ret = matchManager.getMatch().getPlayersAlive().stream().map(Player::getLocation).min((loc1, loc2) -> Double.compare(loc1.distance(loc), loc2.distance(loc)));
		return ret.isPresent()?ret.get().distance(loc):Double.MAX_VALUE;
	}

	public Location getFurthestSpawn() {
		Optional<Location> spawn = matchManager.getArena().getPlayerSpawns().stream().max((loc1, loc2) -> Double.compare(getDistanceToNearestPlayer(loc1), getDistanceToNearestPlayer(loc2)));
		return spawn.isPresent()?spawn.get():null;
	}

	public String getName() {
		return this.name;
	}

	public String getCreator() {
		return creator;
	}


}
