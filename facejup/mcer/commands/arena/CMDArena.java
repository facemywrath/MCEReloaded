package facejup.mcer.commands.arena;

import facejup.mcer.commands.Command;
import facejup.mcer.main.Main;

public class CMDArena extends Command {

	public CMDArena(Main main) {
		super(main, "arena");
		this.registerSubCommand("bounds", new SubBounds(this))
		.registerSubCommand("create", new SubCreate(this))
		.registerSubCommand("item", new SubItem(this))
		.registerSubCommand("creator", new SubCreator(this))
		.registerSubCommand("spawn", new SubSpawn(this))
		.registerSubCommand("load", new SubLoad(this));
	}
	
	@Override
	public String getDescription() {
		return "Base arena command";
	}

	@Override
	public String getUsage() {
		return "";
	}

	@Override
	public String getPermission() {
		return "mce.arena";
	}

	@Override
	public String getName() {
		return "arena";
	}

	@Override
	public boolean consoleUse() {
		return false;
	}

}
