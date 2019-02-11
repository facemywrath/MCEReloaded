package facejup.mcer.commands.arena;

import org.bukkit.command.CommandSender;

import facejup.mcer.commands.Command;
import facejup.mcer.commands.SubCommand;

public class SubSpawn extends SubCommand{

	private SubSpawnAdd add;
	private SubSpawnRemove remove;
	private SubSpawnList list;
	
	public SubSpawn(SubCommand parent) {
		super(parent);
		add = new SubSpawnAdd(this);
		remove = new SubSpawnRemove(this);
		list = new SubSpawnList(this);
	}

	@Override
	public void execute(CommandSender player, String[] args) {
		if(args.length == 1) {
			player.sendMessage("Must specify add/remove/list");
			return;
		}
		switch(args[1].toLowerCase()) {
		case "add":
			add.execute(player, args);
			break;
		case "remove":
			remove.execute(player, args);
			break;
		case "list":
			list.execute(player, args);
			break;
		}
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Control arena spawns";
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return "(add/remove/list)";
	}

	@Override
	public String getPermission() {
		// TODO Auto-generated method stub
		return "mce.arena.spawn";
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "spawn";
	}

	@Override
	public boolean consoleUse() {
		// TODO Auto-generated method stub
		return false;
	}

}
