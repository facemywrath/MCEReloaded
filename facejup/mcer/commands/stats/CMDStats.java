package facejup.mcer.commands.stats;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import facejup.mcer.commands.Command;
import facejup.mcer.main.Main;
import facejup.mcer.users.User;
import net.md_5.bungee.api.ChatColor;

public class CMDStats extends Command {

	public CMDStats(Main main) {
		super(main, "stats");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof Player)) {
			if(args.length == 0) {
				sender.sendMessage("Must input a player name");
				return;
			}
			if(!Bukkit.getOfflinePlayer(args[0]).hasPlayedBefore()) {
				sender.sendMessage("Unknown player: " + args[0]);
				return;
			}
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
			if(getMain().getUserManager().getUsers().containsKey(offlinePlayer)) {
				sendStatsMessage(sender, getMain().getUserManager().getUsers().get(offlinePlayer));
				return;
			}
			sendStatsMessage(sender, new User(getMain().getSqlManager(), offlinePlayer));
		}
		if(args.length == 0) {
			OfflinePlayer offlinePlayer = (OfflinePlayer) sender;
			if(getMain().getUserManager().getUsers().containsKey(offlinePlayer)) {
				sendStatsMessage(sender, getMain().getUserManager().getUsers().get(offlinePlayer));
				return;
			}
			return;
		}
		if(!Bukkit.getOfflinePlayer(args[0]).hasPlayedBefore()) {
			sender.sendMessage("Unknown player: " + args[0]);
			return;
		}
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
		if(getMain().getUserManager().getUsers().containsKey(offlinePlayer)) {
			sendStatsMessage(sender, getMain().getUserManager().getUsers().get(offlinePlayer));
			return;
		}
		sender.sendMessage("GAY");
		sendStatsMessage(sender, new User(getMain().getSqlManager(), offlinePlayer));
	
	}
	
	public void sendStatsMessage(CommandSender sender, User user) {
		sender.sendMessage(ChatColor.GOLD + user.getPlayer().getName() + "'s Stats");
		sender.sendMessage(ChatColor.AQUA + "Coins: " + ChatColor.GREEN + user.getCoins());
		sender.sendMessage(ChatColor.AQUA + "Kills: " + ChatColor.GREEN + user.getKills());
		sender.sendMessage(ChatColor.AQUA + "Deaths: " + ChatColor.GREEN + user.getDeaths());
		sender.sendMessage(ChatColor.AQUA + "Wins: " + ChatColor.GREEN + user.getWins());
		sender.sendMessage(ChatColor.AQUA + "Runnerups: " + ChatColor.GREEN + user.getRunnerups());
		sender.sendMessage(ChatColor.AQUA + "Matches Played: " + ChatColor.GREEN + user.getPlayed());
	}

	@Override
	public String getDescription() {
		return "View a players stats";
	}

	@Override
	public String getUsage() {
		return "(player)";
	}

	@Override
	public String getPermission() {
		return "mce.stats";
	}

	@Override
	public String getName() {
		return "stats";
	}

	@Override
	public boolean consoleUse() {
		return true;
	}

}
