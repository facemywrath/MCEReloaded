package facejup.mcer.commands.kits;

import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import facejup.mcer.commands.Command;
import facejup.mcer.commands.SubCommand;
import facejup.mcer.kits.Kit;
import facejup.mcer.users.User;
import facejup.mcer.users.UserManager;
import facejup.mcer.util.ItemCreator;

public class SubForce extends SubCommand {

	public SubForce(Command parent) {
		super(parent);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		if(args.length < 3) {
			player.sendMessage("Specify a player please");
			return;
		}
		if(!Bukkit.getOfflinePlayer(args[1]).hasPlayedBefore() && !Bukkit.getOfflinePlayer(args[1]).isOnline()) {
			player.sendMessage("That player doesn't exist");
			return;
		}
		if(!Bukkit.getOfflinePlayer(args[1]).isOnline()) {
			player.sendMessage("That player is not online.");
			return;
		}
		Player target = Bukkit.getPlayer(args[1]);
		if(!getParent().getMain().getUserManager().getUsers().containsKey(target))
		{
			player.sendMessage("Player is not loaded in.");
			return;
		}
		User user = getParent().getMain().getUserManager().getUsers().get(target);
		if(Kit.getKit(args[2]) == null) {
			player.sendMessage("A kit doesn't exist by that name");
			return;
		}
		target.getInventory().clear();
		target.getActivePotionEffects().forEach(pot -> player.removePotionEffect(pot.getType()));
		Kit kit = Kit.getKit(args[2]);
		user.setSpawnKit(kit);
		user.setCurrentKit(kit);
		user.getCurrentKit().spawn(this.getParent().getMain(), player);
		user.getCurrentKit().getEquipment().getInventoryContents().forEach(item -> player.getInventory().addItem(item));
		user.getCurrentKit().getEquipment().equipArmor(player);
		player.sendMessage(target.getName() + "'s kit changed to " + user.getCurrentKit().getName());
		target.sendMessage("Kit changed to " + user.getCurrentKit().getName());
	}

	@Override
	public String getDescription() {
		return "Admin Test Commands.";
	}

	@Override
	public String getUsage() {
		return "";
	}

	@Override
	public String getPermission() {
		return "kits.admin";
	}

	@Override
	public String getName() {
		return "test";
	}

	@Override
	public boolean consoleUse() {
		return false;
	}

}
