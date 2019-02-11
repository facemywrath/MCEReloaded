package facejup.mcer.listeners;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import facejup.mcer.commands.kits.SubMenu;
import facejup.mcer.kits.Kit;
import facejup.mcer.util.Events;
import facejup.mcer.util.Text;

public class MenuListener {
	
	// Stored inside SubMenu.java
	
	private SubMenu command;
	
	public MenuListener(SubMenu command) {
		this.command = command;
		Events.listen(command.getParent().getMain(), InventoryClickEvent.class, event -> {
			if(!(event.getWhoClicked() instanceof Player)) return;
			Player player = (Player) event.getWhoClicked();
			if(event.getClickedInventory() == null) return;
			if(event.getClickedInventory().getType() == InventoryType.PLAYER) return;
			Inventory inventory = event.getClickedInventory();
			if(!ChatColor.stripColor(inventory.getTitle()).equalsIgnoreCase(ChatColor.stripColor(Text.MENU_TITLE))) return;
			event.setCancelled(true);
			if(event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
			ItemStack item = event.getCurrentItem();
			
			if(Kit.getKit(item.getItemMeta().getDisplayName()) == null) return;
			Kit kit = Kit.getKit(item.getItemMeta().getDisplayName()); 
			if(!command.getParent().getMain().getUserManager().getUsers().containsKey(player)) return;
			command.getParent().getMain().getUserManager().getUsers().get(player).setSpawnKit(kit);
			Inventory guiInventory = player.getOpenInventory().getTopInventory();
			guiInventory.clear();
			Arrays.asList(Kit.values()).stream().map(k -> k.getMenuItem(command.getParent().getMain().getUserManager().getUsers().get(player))).forEach(i -> guiInventory.addItem(i));

		});
	}
}
