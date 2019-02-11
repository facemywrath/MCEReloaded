package facejup.mcer.storage;

import java.io.File;
import java.text.DecimalFormat;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import facejup.mcer.main.Main;
import facejup.mcer.util.Animation;
import net.md_5.bungee.api.ChatColor;

public class ItemSpawn {

	private Location location;
	private ItemStack item;
	private Item droppedItem;
	private long cooldown;
	private Animation<ItemSpawn> spawnAnimation;
	private long currentTime = 0;
	private Hologram hologram;

	public ItemSpawn(Main main, ItemStack item, long cooldown, Location location){
		this.location = location;
		this.item = item;
		this.cooldown = cooldown;
		this.currentTime = cooldown;
		this.spawnAnimation = new Animation<ItemSpawn>(main).addFrame(spawn -> {
			ItemStack itemTest = spawn.getItem();
			String itemName = (item.getItemMeta().hasDisplayName()?item.getItemMeta().getDisplayName():StringUtils.capitaliseAllWords(item.getType().toString().toLowerCase().replaceAll("_", " ")));
			if(hologram == null) {
				hologram = HologramsAPI.createHologram(main, location.add(new Vector(0,2,0)));
				hologram.appendItemLine(item);
				hologram.appendTextLine(itemName);
				hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', "Spawning in " + getTimeLeft()));
				hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', createTimeBar(currentTime, cooldown)));
				return;
			}
			hologram.getLine(2).removeLine();
			if(currentTime > 0 && (droppedItem == null || droppedItem.isDead())) {
				currentTime -= 20L;
				hologram.getLine(2).removeLine();
				hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', ChatColor.AQUA + "Spawning in " + getTimeLeft()));
				hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', createTimeBar(currentTime, cooldown)));
				return;
			}
			hologram.appendTextLine(ChatColor.GREEN + "Ready to pick up.");
			if(droppedItem != null && !droppedItem.isDead()) {
				droppedItem.teleport(spawn.getLocation().clone());
				return;
			}
			droppedItem = spawn.getLocation().getWorld().dropItem(spawn.getLocation().clone().add(new Vector(0,1,0)), spawn.getItem());
			this.currentTime = this.cooldown;
		}, 20L).setLooping(true);
	}

	public ItemSpawn(ItemStack item, long cooldown) {
		this.item = item;
		this.cooldown = cooldown;
	}

	public ItemSpawn addLocation(Location loc) {
		this.location = loc;
		return this;
	}

	public void animate() {
		this.spawnAnimation.animate(this);
	}

	public ItemStack getItem() {
		return this.item;
	}

	public String getTimeLeft() {
		long secondsleft = currentTime/20L;
		long minutesleft = secondsleft/60L;
		long hoursleft = minutesleft/60L;
		long daysleft = hoursleft/24L;
		long weeksleft = daysleft/7L;
		secondsleft -= minutesleft*60L;
		minutesleft -= hoursleft*60L;
		hoursleft -= daysleft*24L;
		daysleft -= weeksleft*7L;
		DecimalFormat format = new DecimalFormat("00");
		boolean usesWeeks = weeksleft != 0;
		boolean usesDays = usesWeeks || daysleft != 0;
		boolean usesHours = usesDays || hoursleft != 0;
		boolean uses[] = {usesWeeks,usesDays,usesHours,true,true};
		ChatColor colors[] = {ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW, ChatColor.BLUE, ChatColor.GREEN};
		long times[] = {weeksleft,daysleft,hoursleft,minutesleft,secondsleft};
		String ret = "";
		int j = 0;
		for(int i = 0; i < uses.length; i++) {
			if(uses[i])
			{
				ret += " " + colors[j] + format.format(times[i]) + " &7:";
				j++;
			}
		}
		return ret.substring(0, ret.length()-1);
	}

	public Location getLocation() {
		return this.location;
	}

	public void save(Main main) {
		File arenaFile = main.getArenaFile();
		FileConfiguration config = YamlConfiguration.loadConfiguration(arenaFile);
		int size = 0;
		if(config.contains("ItemSpawns") && !config.getConfigurationSection("ItemSpawns").getKeys(false).isEmpty())
			size = config.getConfigurationSection("ItemSpawns").getKeys(false).size();
		config.set("ItemSpawns." + size + ".x", getLocation().getX());
		config.set("ItemSpawns." + size + ".y", getLocation().getY());
		config.set("ItemSpawns." + size + ".z", getLocation().getZ());
		config.set("ItemSpawns." + size + ".Item", item);
		config.set("ItemSpawns." + size + ".Cooldown", cooldown);
		try{
			config.save(arenaFile);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static String createTimeBar(double mana, double maxMana) {
		StringBuilder manaBar = new StringBuilder(ChatColor.RED + "[" + ChatColor.BLUE);
		int progress = (int) (mana / maxMana * 50.0D);
		for (int i = 0; i < progress; i++) {
			manaBar.append('|');
		}
		manaBar.append(ChatColor.DARK_RED);
		for (int i = 0; i < 50 - progress; i++) {
			manaBar.append('|');
		}
		manaBar.append(ChatColor.RED).append(']');
		return manaBar.toString();
	}

}
