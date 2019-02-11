package facejup.mcer.commands.kits;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import facejup.mcer.commands.Command;
import facejup.mcer.commands.SubCommand;
import facejup.mcer.kits.Kit;
import facejup.mcer.listeners.MenuListener;
import facejup.mcer.util.Text;

public class SubMenu extends SubCommand {

	private MenuListener menuListener;
	
	public SubMenu(Command parent) {
		super(parent);
		menuListener = new MenuListener(this);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		Inventory guiInventory = Bukkit.createInventory(player, 54, Text.MENU_TITLE);
		Arrays.asList(Kit.values()).stream().map(kit -> kit.getMenuItem(this.getParent().getMain().getUserManager().getUsers().get(player))).forEach(item -> guiInventory.addItem(item));
		player.openInventory(guiInventory);
	}

	@Override
	public String getDescription() {
		return "Open the kits menu to choose or purchase kits.";
	}

	@Override
	public String getUsage() {
		return "";
	}

	@Override
	public String getPermission() {
		return "kits.menu";
	}

	@Override
	public String getName() {
		return "menu";
	}

	@Override
	public boolean consoleUse() {
		return false;
	}

}
