package facejup.mcer.commands.kits;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import facejup.mcer.commands.Command;
import facejup.mcer.commands.SubCommand;
import facejup.mcer.kits.Kit;
import facejup.mcer.listeners.MenuListener;

public class SubMenu extends SubCommand {

	private MenuListener menuListener;
	
	public SubMenu(Command parent) {
		super(parent);
		menuListener = new MenuListener(this);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		Inventory guiInventory = Bukkit.createInventory(player, 54, menuListener.getMenuTitle());
		Kit.getValues().stream().map(Kit::getMenuItem).forEach(item -> guiInventory.addItem(item));
		player.openInventory(guiInventory);
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Open the kits menu to choose or purchase kits.";
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String getPermission() {
		// TODO Auto-generated method stub
		return "kits.menu";
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "menu";
	}

	@Override
	public boolean consoleUse() {
		// TODO Auto-generated method stub
		return false;
	}

}
