package facejup.mcer.commands.arena;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import facejup.mcer.commands.Command;
import facejup.mcer.commands.SubCommand;
import facejup.mcer.util.Text;

public class SubCreate extends SubCommand{

	public SubCreate(Command parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(CommandSender player, String[] args) {
		if(args.length == 1) {
			player.sendMessage(Text.trans("Must specify a name."));
			return;
		}
		File arenaFile = this.getParent().getMain().getArenaFile();
		FileConfiguration config = YamlConfiguration.loadConfiguration(arenaFile);
		String name = StringUtils.capitalize(args[1].toLowerCase().replaceAll("_", " "));
		config.set("Name", name);
		config.set("World", ((Player)player).getWorld().getName());
		try{
			config.save(arenaFile);
		}catch(Exception e) {
			e.printStackTrace();
		}
		player.sendMessage("Arena name set to " + name);
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Set the name for this server's arena";
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return "(name)";
	}

	@Override
	public String getPermission() {
		// TODO Auto-generated method stub
		return "mce.arena.create";
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "create";
	}

	@Override
	public boolean consoleUse() {
		// TODO Auto-generated method stub
		return false;
	}

}
