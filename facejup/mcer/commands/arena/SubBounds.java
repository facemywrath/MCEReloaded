package facejup.mcer.commands.arena;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import facejup.mcer.commands.Command;
import facejup.mcer.commands.SubCommand;
import facejup.mcer.util.Events;

public class SubBounds extends SubCommand{

	private Location corner1;
	private Location corner2;
	private Player selector;
	private long cooldown;

	public SubBounds(Command parent) {
		super(parent);
		Events.listen(parent.getMain(), PlayerInteractEvent.class, event -> {
			if(selector == null) return;
			if(!event.getPlayer().equals(selector)) return;
			if(event.getClickedBlock() == null) return;
			if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
			if(corner1 == null && cooldown < System.currentTimeMillis())
			{
				corner1 = event.getClickedBlock().getLocation();
				cooldown = System.currentTimeMillis() + 100L;
				event.getPlayer().sendMessage("First bounds set. Now rightclick the other corner.");
				return;
			}
			if(corner2 == null && cooldown < System.currentTimeMillis()) {
				corner2 = event.getClickedBlock().getLocation();
				cooldown = System.currentTimeMillis() + 100L;
				event.getPlayer().sendMessage("Second bounds set. Saving new bounds.");
			}
			int minx = (corner1.getBlockX() > corner2.getBlockX()?corner2.getBlockX():corner1.getBlockX());
			int miny = (corner1.getBlockY() > corner2.getBlockY()?corner2.getBlockY():corner1.getBlockY());
			int minz = (corner1.getBlockZ() > corner2.getBlockZ()?corner2.getBlockZ():corner1.getBlockZ());
			int maxx = (corner1.getBlockX() < corner2.getBlockX()?corner2.getBlockX():corner1.getBlockX());
			int maxy = (corner1.getBlockY() < corner2.getBlockY()?corner2.getBlockY():corner1.getBlockY());
			int maxz = (corner1.getBlockZ() < corner2.getBlockZ()?corner2.getBlockZ():corner1.getBlockZ());
			corner1 = new Location(corner1.getWorld(), minx, miny, minz);
			corner2 = new Location(corner2.getWorld(), maxx, maxy, maxz);
			File arenaFile = this.getParent().getMain().getArenaFile();
			FileConfiguration config = YamlConfiguration.loadConfiguration(arenaFile);
			config.set("Corner1.x", corner1.getBlockX());
			config.set("Corner1.y", corner1.getBlockY());
			config.set("Corner1.z", corner1.getBlockZ());
			config.set("Corner2.x", corner2.getBlockX());
			config.set("Corner2.y", corner2.getBlockY());
			config.set("Corner2.z", corner2.getBlockZ());
			try{
				config.save(arenaFile);
			}catch(Exception e) {
				e.printStackTrace();
			}
			corner1 = null;
			corner2 = null;
			selector = null;
		});
	}

	@Override
	public void execute(CommandSender player, String[] args) {
		if(selector == null || !player.equals(selector)) {
			selector = (Player) player;
			player.sendMessage("Now selecting arena bounds. Rightclick a block in the corner of the arena.");
			return;
		}
		player.sendMessage("No longer selecting bounds for the arena.");
		selector = null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Set the out of bounds corners for the arena.";
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String getPermission() {
		// TODO Auto-generated method stub
		return "mce.arena.bounds";
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "bounds";
	}

	@Override
	public boolean consoleUse() {
		// TODO Auto-generated method stub
		return false;
	}

}
