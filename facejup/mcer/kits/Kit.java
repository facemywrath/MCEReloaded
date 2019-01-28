package facejup.mcer.kits;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import facejup.mcer.util.ItemCreator;

public interface Kit {

	public static List<Kit> getValues() {
		return Arrays.asList(NONE,WARRIOR, ARCHER, GUARD);
	}

	public static final Kit NONE = new Kit() {
		public ItemStack getMenuItem() { return new ItemCreator(Material.BARRIER).name("None").build(); }
	};
	public static final Kit WARRIOR = new Kit() {
		public ItemStack getMenuItem() { return new ItemCreator(Material.IRON_CHESTPLATE).name("Warrior").build(); }
	};
	public static final Kit ARCHER = new Kit() {
		public ItemStack getMenuItem() { return new ItemCreator(Material.ARROW).name("Archer").build(); }
	};
	public static final Kit GUARD = new Kit() {
		public ItemStack getMenuItem() { return new ItemCreator(Material.SHIELD).name("Guard").build(); }
	};

	public ItemStack getMenuItem();

}
