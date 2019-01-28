package facejup.mcer.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import facejup.mcer.commands.kits.SubMenu;
import facejup.mcer.util.Events;

public class MenuListener {
	
	private SubMenu command;
	private final String MENU_TITLE = ChatColor.translateAlternateColorCodes('&', "&2Kits");
	
	public MenuListener(SubMenu command) {
		this.command = command;
		Events.listen(command.getParent().getMain(), InventoryClickEvent.class, event -> {
			if(!(event.getWhoClicked() instanceof Player)) return;
			Player player = (Player) event.getWhoClicked();
			if(event.getClickedInventory() == null) return;
			if(event.getClickedInventory().getType() == InventoryType.PLAYER) return;
			Inventory inventory = event.getClickedInventory();
			if(!ChatColor.stripColor(inventory.getTitle()).equalsIgnoreCase(ChatColor.stripColor(MENU_TITLE))) return;
			player.sendMessage("Valid");
		});
	}

	public String getMenuTitle() {
		return this.MENU_TITLE;
	}
	
}
