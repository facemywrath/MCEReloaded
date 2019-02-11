package facejup.mcer.commands;

import org.bukkit.command.CommandSender;

import facejup.mcer.main.Main;

public abstract class SubCommand {
	
	private SubCommand parent;
	
	public SubCommand(SubCommand parent) {
		this.parent = parent;
	}

	public SubCommand getParent() {
		return this.parent;
	}
	
	public Main getMain() {
		if(this instanceof Command) {
			return ((Command)this).getMain();
		}
		return null;
	}
	
	public abstract void execute(CommandSender player, String args[]);
	
	public abstract String getDescription();
	
	public abstract String getUsage();
	
	public abstract String getPermission();
	
	public abstract String getName();
	
	public abstract boolean consoleUse();
	
}
