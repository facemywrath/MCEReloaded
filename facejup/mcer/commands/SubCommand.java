package facejup.mcer.commands;

import org.bukkit.command.CommandSender;

public abstract class SubCommand {
	
	private Command parent;
	
	public SubCommand(Command parent) {
		this.parent = parent;
	}

	public Command getParent() {
		return this.parent;
	}
	
	public abstract void execute(CommandSender player, String args[]);
	
	public abstract String getDescription();
	
	public abstract String getUsage();
	
	public abstract String getPermission();
	
	public abstract String getName();
	
	public abstract boolean consoleUse();
	
}
