package facejup.mcer.kits;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType;

import facejup.mcer.main.Main;

public class Shield {

	private World world;
	private Main main;
	private OfflinePlayer owner;
	private List<Block> blocks = new ArrayList<>();
	private double health = 20;
	private boolean alive = true;

	public Shield(Main main, OfflinePlayer owner) {
		this.main = main;
		this.world = owner.getPlayer().getWorld();
		this.owner = owner;
		place();
	}

	public void place() {
		Player player = owner.getPlayer();
		List<Block> blocksList = new ArrayList<>();

		double range = 4;

		for(int x = (int) (range*-1)-1 ; x < (int) range+1 ; x++){
			for (int y = (int) (range*-1)-1 ; y < (int) range+1 ; y++){
				for (int z = (int) (range*-1)-1 ; z < (int) range+1 ; z++){
					Location temp = player.getEyeLocation().add(x, y, z);
					Double distance = temp.distance(player.getEyeLocation());
					if (distance >= range-0.4 && distance <= range+0.4){
						blocksList.add(temp.getBlock());
					}
				}
			}
		}

		blocks = blocksList.stream().filter(block -> {
			Location loc = block.getLocation();
			if(block.getType() != Material.AIR) return false;
			Vector difference = player.getEyeLocation().subtract(loc).toVector().normalize();
			if (difference.dot(player.getLocation().getDirection()) < -0.25) return true;
			return false;
		}).collect(Collectors.toList());

		for(Player p : world.getPlayers()) {
			if(p.getLocation().distance(player.getLocation()) < 60) {
				blocks.forEach(block -> p.sendBlockChange(block.getLocation(), Material.STAINED_GLASS, (byte) 9));
			}
		}

	}

	public void damage(ProtocolManager protocolManager, Player player, Block block, int i) {
		this.health -= i;
		if(health > 1)
		{
			player.sendBlockChange(block.getLocation(), Material.AIR, (byte) 0);
			main.getServer().getScheduler().runTaskLater(main, () -> player.sendBlockChange(block.getLocation(), Material.STAINED_GLASS, (byte) 9), 10L);
		}
		if(health <= 0) kill();
	}

	public void kill() {
		for(Block block : blocks) {
			block.getState().update();
		}
		this.alive = false;
		this.blocks = new ArrayList<>();
		this.main.getUserManager().getUsers().get(this.owner).removeShield(this);
	}

	public List<Block> getBlocks() {
		return blocks;
	}

	public World getWorld() {
		return world;
	}

	public Main getMain() {
		return main;
	}

	public OfflinePlayer getOwner() {
		return owner;
	}

	public double getHealth() {
		return health;
	}

	public boolean isAlive() {
		return alive;
	}

	public void update(Player player) {
		blocks.forEach(block -> player.sendBlockChange(block.getLocation(), Material.STAINED_GLASS, (byte) 9));
	}


}
