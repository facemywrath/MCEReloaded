package facejup.mcer.storage;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import facejup.mcer.main.Main;

public class DamageIndicator {
	
	private Hologram hologram;
	
	public DamageIndicator(Main main, Location loc, double damage, double health, double maxHealth) {
		hologram = HologramsAPI.createHologram(main, loc);
		hologram.appendTextLine(getColor(health-damage, maxHealth) + " -" + (((int)(damage*100.0))/100.0));
	}
	
	public ChatColor getColor(double health, double maxHealth) {
		if(health >= maxHealth * 0.66) return ChatColor.GREEN;
		if(health >= maxHealth * 0.34) return ChatColor.GOLD;
		return ChatColor.RED;
	}

	public void floatAway() {
		if(hologram == null) return;
		hologram.teleport(hologram.getLocation().add(new Vector(0,0.06,0)));
	}
	
	public void remove() {
		this.hologram.delete();
		this.hologram = null;
	}

}
