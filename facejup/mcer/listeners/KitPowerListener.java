package facejup.mcer.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType;

import facejup.mcer.kits.Kit;
import facejup.mcer.kits.MountedTurret;
import facejup.mcer.main.Main;
import facejup.mcer.storage.KitPower;
import facejup.mcer.storage.PlaceableManager;
import facejup.mcer.users.User;
import facejup.mcer.util.Animation;
import facejup.mcer.util.Events;
import facejup.mcer.util.ItemCreator;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.PacketPlayOutBlockChange;

public class KitPowerListener {

	private HashMap<ItemStack, KitPower> itemPowers = new HashMap<>();

	private DamageListener damageListener;

	public KitPowerListener(Main main) {
		damageListener = new DamageListener(main);

		Events.listen(main, PlayerRespawnEvent.class, event -> {
			main.getServer().getScheduler().runTaskLater(main, () -> {
				if(main.getUserManager().getPlaceableManager().getShields().isEmpty()) return;
				main.getUserManager().getPlaceableManager().getShields().forEach(shield -> {
					shield.update(event.getPlayer());
				});
			}, 10L);
		});

		Events.listen(main, PlayerArmorStandManipulateEvent.class, event -> {
			if(main.getUserManager().getPlaceableManager().getMountedTurrets().isEmpty()) return;
			if(!main.getUserManager().getPlaceableManager().isMountedTurret(event.getRightClicked())) return;
			event.setCancelled(true);


		});

		Events.listen(main, PlayerInteractEvent.class, event -> {
			Player player = event.getPlayer();
			if(event.getAction().toString().toLowerCase().contains("left_click") && player.getVehicle() != null && main.getUserManager().getPlaceableManager().isMountedTurret(player.getVehicle())) {
				main.getUserManager().getPlaceableManager().getMountedTurret(player.getVehicle()).shoot();
				return;
			}
			ItemStack hand = event.getPlayer().getInventory().getItemInMainHand();
			if(hand == null) return;
			for(ItemStack item : itemPowers.keySet()) {
				if(item.isSimilar(hand)) { 
					if(itemPowers.get(item).hasAnyTrigger() && event.getAction() != Action.PHYSICAL)
						itemPowers.get(item).triggerAny(event);
					if(itemPowers.get(item).hasRightTrigger() && event.getAction().toString().contains("RIGHT_CLICK"))
						itemPowers.get(item).triggerRight(event);
					else if(itemPowers.get(item).hasLeftTrigger() && event.getAction().toString().contains("LEFT_CLICK"))
						itemPowers.get(item).triggerLeft(event);
					break;
				}
			}
		});

		main.getProtocolManager().addPacketListener(
				new PacketAdapter(main, ListenerPriority.NORMAL, 
						PacketType.Play.Server.BLOCK_CHANGE) {
					@Override
					public void onPacketSending(PacketEvent event) {
						if (event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
							com.comphenix.protocol.wrappers.BlockPosition position = event.getPacket().getBlockPositionModifier().read(0);
							PlaceableManager pm = main.getUserManager().getPlaceableManager();
							Block block = position.toLocation(event.getPlayer().getWorld()).getBlock();
							if(pm.isShield(block) && event.getPacket().getBlockData().read(0).getType() == Material.AIR){
								event.setCancelled(true);	
							}
						}
					}

				});
		main.getProtocolManager().addPacketListener(
				new PacketAdapter(main, ListenerPriority.NORMAL, 
						PacketType.Play.Client.BLOCK_DIG) {
					@Override
					public void onPacketReceiving(PacketEvent event) {
						if (event.getPacketType() == PacketType.Play.Client.BLOCK_DIG) {
							com.comphenix.protocol.wrappers.BlockPosition position = event.getPacket().getBlockPositionModifier().read(0);
							PlayerDigType action = event.getPacket().getPlayerDigTypes().read(0);
							if(action != PlayerDigType.START_DESTROY_BLOCK) return;
							PlaceableManager pm = main.getUserManager().getPlaceableManager();
							Block block = position.toLocation(event.getPlayer().getWorld()).getBlock();
							if(pm.isShield(block)){								
								main.getServer().getScheduler().runTask(main, () -> damageListener.spawnIndicator(main, block.getLocation().add(new Vector(0.5,0.5,0.5)), 1, pm.getShield(block).getHealth(), 20));
								main.getServer().getScheduler().runTask(main, () -> pm.getShield(block).damage(main.getProtocolManager(), event.getPlayer(), block, 1));
							}
						}
					}

				});

		itemPowers.put(new ItemCreator(Material.STICK).name("&9Magic Wand").enchantment(Enchantment.DAMAGE_ALL, 2, false).build(), new KitPower().rightClick(event -> {
			Player player = event.getPlayer();
			List<Block> blocks = new ArrayList<>();
			event.setCancelled(true);
			if(player.getCooldown(Material.STICK) > 0) return;
			player.setCooldown(Material.STICK, 200);
			for(int x = -6; x < 6; x++) {
				for(int y = -6; y < 6; y++) {
					for(int z = -6; z < 6; z++) { 
						Block block = player.getLocation().add(new Vector(x,y,z)).getBlock();
						double distance = block.getLocation().distance(player.getLocation());
						double range = 5;
						if(distance >= range-0.45 && distance <= range+0.45)
						{
							blocks.add(block);
						}
					}
				}
			}

			for(Player p : player.getWorld().getPlayers().stream().filter(p -> p.getLocation().distance(player.getLocation()) < 20 && !p.equals(player)).collect(Collectors.toList())) {
				for(Block block : blocks) {
					p.sendBlockChange(block.getLocation(), Material.BEDROCK, (byte) 0); 
				}
			}

			blocks.stream().filter(b -> b.getType() == Material.AIR).forEach(block -> player.sendBlockChange(block.getLocation(), Material.STAINED_GLASS, (byte) 5)); 

			main.getServer().getScheduler().runTaskLater(main, () -> {
				blocks.stream().filter(b -> b.getType() == Material.AIR).forEach(block -> player.sendBlockChange(block.getLocation(), Material.AIR, (byte) 0)); 
			}, 7L);
			main.getServer().getScheduler().runTaskLater(main, () -> {
				blocks.stream().filter(b -> b.getType() == Material.AIR).forEach(block -> player.sendBlockChange(block.getLocation(), Material.STAINED_GLASS, (byte) 5)); 
			}, 10L);
			main.getServer().getScheduler().runTaskLater(main, () -> {
				blocks.stream().filter(b -> b.getType() == Material.AIR).forEach(block -> player.sendBlockChange(block.getLocation(), Material.AIR, (byte) 0)); 
			}, 14L);

			main.getServer().getScheduler().runTaskLater(main, () -> {
				blocks.forEach(block -> block.getState().update());
			}, 60L);
		}));

		itemPowers.put(new ItemCreator(Material.FIREBALL).name("&9Blasting Ball").build(), new KitPower().rightClick(event -> {
			Player player = event.getPlayer();
			event.setCancelled(true);
			if(player.getCooldown(Material.FIREBALL) > 0) return;
			player.getInventory().removeItem(new ItemCreator(Material.FIREBALL).name("&9Blasting Ball").build());
			Fireball  ball = player.launchProjectile(Fireball.class);
			ball.setGlowing(true);
			ball.setIsIncendiary(true);
			ball.setYield(3);
			player.setCooldown(Material.FIREBALL, 100);
		}));

		itemPowers.put(new ItemCreator(Material.SKULL_ITEM).durability((byte)3).name("&9Fake Out").build(), new KitPower().rightClick(event -> {
			Player player = event.getPlayer();
			event.setCancelled(true);
			if(player.getCooldown(Material.SKULL_ITEM) > 0) return;
			Creeper creeper = (Creeper) player.getWorld().spawnEntity(player.getLocation(), EntityType.CREEPER);
			PlayerDisguise disguise = (PlayerDisguise) DisguiseAPI.constructDisguise(player);
			DisguiseAPI.disguiseEntity(creeper, disguise);

			player.setCooldown(Material.SKULL_ITEM, 160);
			Block target = player.getTargetBlock(null, 8);

			Location loc = null;

			if(target == null) loc = player.getLocation().add(player.getLocation().getDirection().multiply(8));
			else loc = target.getLocation().subtract(player.getLocation().getDirection());

			loc.setYaw(player.getLocation().getYaw());
			loc.setPitch(player.getLocation().getPitch());
			player.teleport(loc);
			player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20, 20));
			main.getServer().getScheduler().runTaskLater(main, () -> {
				creeper.remove();
			}, 100L);

		}));

		itemPowers.put(new ItemCreator(Material.CAULDRON_ITEM).name("&9Top Hat").build(), new KitPower().anyClick(event -> {
			Player player = event.getPlayer();
			event.setCancelled(true);
			if(player.getCooldown(Material.CAULDRON_ITEM) > 0) return;
			player.setCooldown(Material.CAULDRON_ITEM, 90);
			Animation<Rabbit> animation = new Animation<Rabbit>(main).addFrame(rabbit -> {
				if(!rabbit.isDead())
					rabbit.getWorld().playSound(rabbit.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 0.6f, 0.7f);
			}, 20L, 3).addFrame(rabbit -> {
				if(rabbit.isDead()) return;
				TNTPrimed tnt = (TNTPrimed) rabbit.getWorld().spawnEntity(rabbit.getLocation(), EntityType.PRIMED_TNT);
				tnt.setFuseTicks(0);
				tnt.setYield(3);

				rabbit.remove();
			}, 30L);
			animation.animate((Rabbit) player.getWorld().spawnEntity(player.getEyeLocation().add(player.getLocation().getDirection()), EntityType.RABBIT));
		}));

		itemPowers.put(new ItemCreator(Material.GLOWSTONE_DUST).name("&9Special Powder").build(), new KitPower().rightClick(event -> {
			Player player = event.getPlayer();
			event.setCancelled(true);
			if(player.getCooldown(Material.GLOWSTONE_DUST) > 0) return;
			player.setCooldown(Material.GLOWSTONE_DUST, 70);
			final Location location = player.getLocation().clone();
			location.getWorld().spawnParticle(Particle.REDSTONE, player.getEyeLocation(), 3);
			new Animation<Player>(main).addFrame(p -> {
				p.teleport(location);
			}, 100L).animate(player);
		}));

		itemPowers.put(new ItemCreator(Material.BREWING_STAND_ITEM).name("&9Turret").build(), new KitPower().rightClick(event -> {
			Player player = event.getPlayer();
			User user = main.getUserManager().getUsers().get(player);
			event.setCancelled(true);
			if(player.getCooldown(Material.BREWING_STAND_ITEM) > 0) return;
			player.setCooldown(Material.BREWING_STAND_ITEM, 900);
			Block target = player.getTargetBlock(null, 3);
			Location loc = null;
			if(target == null || target.getType() == Material.AIR){
				loc = player.getEyeLocation().add(player.getLocation().getDirection().multiply(2));
			}
			else loc = target.getLocation().add(new Vector(0,1,0));
			main.getUserManager().getPlaceableManager().spawnTurret(main, user, loc);
		}));

		itemPowers.put(new ItemCreator(Material.SHIELD).name("&9Placeable Shield").build(), new KitPower().rightClick(event -> {
			Player player = event.getPlayer();
			User user = main.getUserManager().getUsers().get(player);
			event.setCancelled(true);
			if(player.getCooldown(Material.SHIELD) > 0) return;
			player.setCooldown(Material.SHIELD, 900);
			main.getUserManager().getPlaceableManager().spawnShield(main, user);
		}));

		itemPowers.put(new ItemCreator(Material.BREWING_STAND_ITEM).name("&9Mountable Turret").build(), new KitPower().rightClick(event -> {
			Player player = event.getPlayer();
			User user = main.getUserManager().getUsers().get(player);
			event.setCancelled(true);
			if(player.getCooldown(Material.BREWING_STAND_ITEM) > 0) return;
			player.setCooldown(Material.BREWING_STAND_ITEM, 10);
			player.getInventory().removeItem(new ItemCreator(Material.BREWING_STAND_ITEM).name("&9Mountable Turret").build());
			Block target = player.getTargetBlock(null, 3);
			Location loc = null;
			if(target == null || target.getType() == Material.AIR){
				loc = player.getEyeLocation().add(player.getLocation().getDirection().multiply(2));
			}
			else loc = target.getLocation().add(new Vector(0,1,0));
			main.getUserManager().getPlaceableManager().spawnMountedTurret(main, user, loc);
		}));

	}

	public static void callFireFall(Main main, Location loc, int radius)
	{
		for(int x = -1*radius; x <= radius; x++)
		{
			for(int z = -1*radius; z <= radius; z++)
			{
				Location tempLoc = loc.clone().add(new Vector(x,0,z));
				if(tempLoc.distance(loc) < (radius+0.5) && tempLoc.distance(loc) > (radius-0.5))
				{
					if(tempLoc.getBlock().getType() == Material.AIR)
					{
						tempLoc.getWorld().spawnFallingBlock(tempLoc, Material.FIRE, (byte) 0);
					}
				}
			}
		}
		if(radius < 5)
			main.getServer().getScheduler().scheduleSyncDelayedTask(main, new Runnable()
			{
				public void run()
				{
					callFireFall(main, loc, radius+1);
				}
			}, 5L);
	}

	public static void runHarpyFlight(Main main, Player player) {
		if(!player.isSprinting()) return;
		if(!Kit.isUsing(main, "Harpy", player)) return;
		float stamina = player.getLevel();
		if(stamina == 0) return;				
		player.setVelocity(player.getLocation().getDirection().multiply(0.4));
		int red = 2;
		if(player.hasPotionEffect(PotionEffectType.SLOW))
			red += player.getPotionEffect(PotionEffectType.SLOW).getAmplifier();
		if(stamina-red < 0)
			stamina = 0;
		else
			stamina -= red;
		player.setExp((float) (stamina/100.0));
		player.setLevel((int) stamina);
		main.getServer().getScheduler().scheduleSyncDelayedTask(main, new Runnable()
		{
			public void run()
			{
				runHarpyFlight(main, player);
			}
		}, 2L);
	}
	public static void maskBlock(Main main, Player player, Location location, Material material, byte data)
	{
		// As some have pointed out, it could be unwise to use this as a static. Consider using it as an instance var.
		new BukkitRunnable()
		{
			@Override
			public void run()
			{

				BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
				PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(((CraftWorld) location.getWorld()).getHandle(), blockPosition);
				packet.block = net.minecraft.server.v1_12_R1.Block.getByCombinedId(material.getId() + (data << 12));
				((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
			}
		}.runTaskAsynchronously(main);
	}
}
