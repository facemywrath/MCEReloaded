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

public class SubLoad extends SubCommand{

	public SubLoad(Command parent) {
		super(parent);
	}

	@Override
	public void execute(CommandSender player, String[] args) {
		if(!this.getParent().getMain().getMatchManager().getArena().loadArena(this.getParent().getMain())) {
			player.sendMessage("Arena failed to load properly.");
			return;
		}
		player.sendMessage("Arena loaded properly");
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Load the arena specs from file";
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String getPermission() {
		// TODO Auto-generated method stub
		return "mce.arena.load";
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "load";
	}

	@Override
	public boolean consoleUse() {
		// TODO Auto-generated method stub
		return true;
	}

}
