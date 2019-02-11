package facejup.mcer.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Equipment {

	private HashMap<Integer, ItemStack> armorContents = new HashMap<>();
	private List<ItemStack> inventoryContents = new ArrayList<>();
	
	 
	public void clear() {
		 this.armorContents = new HashMap<>();
		 this.inventoryContents = new ArrayList<>();
	}

	 
	public HashMap<Integer, ItemStack> getArmorContents() {
		return this.armorContents;
	}
	 
	public ItemStack getItemInOffHand() {
		return armorContents.get(0);
	}
	
	public void equipArmor(Player player) {
		if(this.armorContents.containsKey(0))
			player.getInventory().setItemInOffHand(this.armorContents.get(0));
		if(this.armorContents.containsKey(1))
			player.getInventory().setBoots(this.armorContents.get(1));
		if(this.armorContents.containsKey(2))
			player.getInventory().setLeggings(this.armorContents.get(2));
		if(this.armorContents.containsKey(3))
			player.getInventory().setChestplate(this.armorContents.get(3));
		if(this.armorContents.containsKey(4))
			player.getInventory().setHelmet(this.armorContents.get(4));
	}

	 
	public ItemStack getBoots() {
		return armorContents.get(1);
	}

	public ItemStack getLeggings() {
		return armorContents.get(2);
	}
	 
	public ItemStack getChestplate() {
		return armorContents.get(3);
	}

	public ItemStack getHelmet() {
		return armorContents.get(4);
	}
	
	public Equipment addItem(ItemStack... items) {
		this.inventoryContents.addAll(Arrays.asList(items));
		return this;
	}
	
	public List<ItemStack> getInventoryContents() {
		return this.inventoryContents;
	}

	public Equipment addArmorPiece(int i, ItemStack item) {
		 this.armorContents.put(i, item);
		 return this;
	}
	public Equipment setItemInOffHand(ItemStack item) {
		 this.armorContents.put(0, item);
		 return this;
	}

	public Equipment setBoots(ItemStack item) {
		 this.armorContents.put(1, item);
		 return this;
	}
	 
	 
	public Equipment setLeggings(ItemStack item) {
		 this.armorContents.put(2, item);
		 return this;
	}
	
	public Equipment setChestplate(ItemStack item) {
		 this.armorContents.put(3, item);
		 return this;
	}

	public Equipment setHelmet(ItemStack item) {
		 this.armorContents.put(4, item);
		 return this;
	}
	public Equipment addArmorPiece(int i, Material item) {
		 this.armorContents.put(i, new ItemStack(item));
		 return this;
	}
	public Equipment setItemInOffHand(Material item) {
		 this.armorContents.put(0, new ItemStack(item));
		 return this;
	}

	public Equipment setBoots(Material item) {
		 this.armorContents.put(1, new ItemStack(item));
		 return this;
	}
	 
	public Equipment addItem(Material... items) {
		Arrays.asList(items).stream().map(mat -> new ItemStack(mat)).forEach(item -> this.inventoryContents.add(item));
		return this;
	}
	
	public Equipment setLeggings(Material item) {
		 this.armorContents.put(2, new ItemStack(item));
		 return this;
	}
	
	public Equipment setChestplate(Material item) {
		 this.armorContents.put(3, new ItemStack(item));
		 return this;
	}

	public Equipment setHelmet(Material item) {
		 this.armorContents.put(4, new ItemStack(item));
		 return this;
	}

}
