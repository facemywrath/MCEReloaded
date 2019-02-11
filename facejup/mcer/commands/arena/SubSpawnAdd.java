package facejup.mcer.commands.arena;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import facejup.mcer.commands.SubCommand;
import facejup.mcer.util.Text;

public class SubSpawnAdd extends SubCommand {

	public SubSpawnAdd(SubCommand parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(CommandSender player, String[] args) {
		if(args.length == 0) {
			player.sendMessage(Text.trans("Must specify a name."));
			return;
		}
		File arenaFile = this.getParent().getParent().getMain().getArenaFile();
		FileConfiguration config = YamlConfiguration.loadConfiguration(arenaFile);
		int size = 0;
		if(config.contains("Spawns") && !config.getConfigurationSection("Spawns").getKeys(false).isEmpty())
			size = config.getConfigurationSection("Spawns").getKeys(false).size();
		config.set("Spawns." + size + ".x", ((Player)player).getLocation().getX());
		config.set("Spawns." + size + ".y", ((Player)player).getLocation().getY());
		config.set("Spawns." + size + ".z", ((Player)player).getLocation().getZ());
		config.set("Spawns." + size + ".pitch", ((Player)player).getLocation().getPitch());
		config.set("Spawns." + size + ".yaw", ((Player)player).getLocation().getYaw());
		try{
			config.save(arenaFile);
		}catch(Exception e) {
			e.printStackTrace();
		}
		player.sendMessage("Arena spawn added at your location");
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Add a spawn to the arena.";
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String getPermission() {
		// TODO Auto-generated method stub
		return "mce.arena.spawn.add";
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "add";
	}

	@Override
	public boolean consoleUse() {
		// TODO Auto-generated method stub
		return false;
	}

}
