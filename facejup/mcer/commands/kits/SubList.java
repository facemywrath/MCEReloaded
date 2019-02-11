package facejup.mcer.commands.kits;

import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import facejup.mcer.commands.Command;
import facejup.mcer.commands.SubCommand;
import facejup.mcer.users.User;
import facejup.mcer.users.UserManager;

public class SubList extends SubCommand {

	public SubList(Command parent) {
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
		player.sendMessage("Your unlocked kits: " + user.getUnlockedKits().stream().map(str -> StringUtils.capitalize(str.toLowerCase())).collect(Collectors.joining(", ")));
	}

	@Override
	public String getDescription() {
		return "View your currently owned kits.";
	}

	@Override
	public String getUsage() {
		return "";
	}

	@Override
	public String getPermission() {
		return "kits.list";
	}

	@Override
	public String getName() {
		return "list";
	}

	@Override
	public boolean consoleUse() {
		return false;
	}

}
