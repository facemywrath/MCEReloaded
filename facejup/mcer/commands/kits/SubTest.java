package facejup.mcer.commands.kits;

import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import facejup.mcer.commands.Command;
import facejup.mcer.commands.SubCommand;
import facejup.mcer.kits.Kit;
import facejup.mcer.users.User;
import facejup.mcer.users.UserManager;
import facejup.mcer.util.ItemCreator;

public class SubTest extends SubCommand {

	public SubTest(Command parent) {
		super(parent);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		UserManager userManager = this.getParent().getMain().getUserManager();
		if(!userManager.getUsers().containsKey(player)) {
			player.sendMessage("You have no kits");
			return;
		}
		User user = userManager.getUsers().get(player);
		if(args.length == 1) {
			player.getInventory().addItem(new ItemCreator(Material.BREWING_STAND_ITEM).name("&9Mountable Turret").build());
			return;
		}
		if(Kit.getKit(args[1]) == null) {
			player.sendMessage("A kit doesn't exist by that name");
			return;
		}
		player.getInventory().clear();
		player.getActivePotionEffects().forEach(pot -> player.removePotionEffect(pot.getType()));
		Kit kit = Kit.getKit(args[1]);
		user.setCurrentKit(kit);
		user.getCurrentKit().spawn(this.getParent().getMain(), player);
		user.getCurrentKit().getEquipment().getInventoryContents().forEach(item -> player.getInventory().addItem(item));
		user.getCurrentKit().getEquipment().equipArmor(player);
		player.sendMessage("Kit changed to " + user.getCurrentKit().getName());
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
