package facejup.mcer.commands;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import facejup.mcer.main.Main;
import net.md_5.bungee.api.ChatColor;

public abstract class Command extends SubCommand implements CommandExecutor {

	private HashMap<String, SubCommand> subCommands = new HashMap<>();
	
	private String name;
	
	private Main main;
	
	public Command(Main main, String name) {
		super(null);
		this.name = name;
		this.main = main;
		main.getCommand(name).setExecutor(this);
	}
	
	public Command registerSubCommand(String name, SubCommand subCommand) {
		this.subCommands.put(name, subCommand);
		return this;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
			if(!this.anyConsoleUse()) {
				sender.sendMessage("There are no usages of this command for console.");
				return true;
			}
			if(args.length == 0) {
				execute(sender, args);
				return true;
			}
			if(!subCommands.containsKey(args[0].toLowerCase())){
				sender.sendMessage("Sorry! " + args[0].toLowerCase() + " is not a given subcommand for that command.");
				return true;
			}
			SubCommand sub = this.subCommands.get(args[0].toLowerCase());
			if(!sub.consoleUse()) {
				sender.sendMessage("Sorry! " + args[0].toLowerCase() + " does not permit console use.");
				return true;
			}
			sub.execute(sender, args);
			return true;
		}
		Player player = (Player) sender;
		if(args.length == 0) {
			if(!player.hasPermission(getPermission())){
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cSorry! You don't have permission for that!"));
				return true;
			}
			execute(player, args);
			return true;
		}
		if(!subCommands.containsKey(args[0].toLowerCase())){
			player.sendMessage("Sorry! " + args[0].toLowerCase() + " is not a given subcommand for that command.");
			return true;
		}
		SubCommand sub = this.subCommands.get(args[0].toLowerCase());
		if(!player.hasPermission(sub.getPermission()))
		{
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cSorry! You don't have permission for that!"));
			return true;
		}
		sub.execute(player, args);
		return true;
	}
	
	public boolean anyConsoleUse() {
		if(subCommands.isEmpty()) return false;
		if(subCommands.values().stream().anyMatch(SubCommand::consoleUse)) return true;
		return false;
	}

	public void execute(CommandSender player, String[] args) {
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l--- &r&2" + StringUtils.capitalize(name) + " &bSubCommands &7&l---"));
		subCommands.values().forEach(sub -> {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2/" + name + " " + sub.getName() + (sub.getUsage().length() == 0?"":" &b" + sub.getUsage()) + " &e&l- &7" + sub.getDescription()));
		});
	}

	public Main getMain() {
		return main;
	}

	

}