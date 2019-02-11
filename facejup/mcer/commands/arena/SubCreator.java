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

public class SubCreator extends SubCommand{

	public SubCreator(Command parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(CommandSender player, String[] args) {
		if(args.length == 0) {
			player.sendMessage(Text.trans("Must specify a player name."));
			return;
		}
		File arenaFile = this.getParent().getMain().getArenaFile();
		FileConfiguration config = YamlConfiguration.loadConfiguration(arenaFile);
		config.set("Creator", args[1]);
		try{
			config.save(arenaFile);
		}catch(Exception e) {
			e.printStackTrace();
		}
		player.sendMessage("Arena creator set as " + args[1]);
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Set the creator for this servers arena";
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return "(playername)";
	}

	@Override
	public String getPermission() {
		// TODO Auto-generated method stub
		return "mce.arena.creator";
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "creator";
	}

	@Override
	public boolean consoleUse() {
		// TODO Auto-generated method stub
		return true;
	}

}
