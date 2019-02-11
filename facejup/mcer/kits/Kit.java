package facejup.mcer.kits;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import facejup.mcer.listeners.KitPowerListener;
import facejup.mcer.main.Main;
import facejup.mcer.storage.Equipment;
import facejup.mcer.users.User;
import facejup.mcer.util.Animation;
import facejup.mcer.util.Events;
import facejup.mcer.util.ItemCreator;
import facejup.mcer.util.Text;

public enum Kit {

	NONE(Material.BARRIER, 0, null, "Deselect your class and leave queue.", null, null),


	WARRIOR(Material.IRON_SWORD,
			0,
			new Equipment()
			.addArmorPiece(2, Material.LEATHER_LEGGINGS)
			.addArmorPiece(3, Material.IRON_CHESTPLATE)
			.addArmorPiece(4, Material.IRON_HELMET)
			.addItem(Material.IRON_SWORD)
			.addItem(new ItemStack(Material.COOKED_BEEF, 4))
			, "A balanced melee fighter with decent armor and damage."
			, null
			, null),


	@SuppressWarnings("unchecked")
	ARCHER(Material.BOW,
			0,
			new Equipment()
			.addArmorPiece(2, Material.LEATHER_LEGGINGS)
			.addArmorPiece(3, Material.CHAINMAIL_CHESTPLATE)
			.addArmorPiece(4, Material.LEATHER_HELMET)
			.addItem(Material.BOW)
			.addItem(new ItemCreator(Material.WOOD_SWORD).enchantment(Enchantment.KNOCKBACK, 1, true).build())
			.addItem(new ItemStack(Material.COOKED_BEEF, 4))
			, "Shoot enemies from a range before they can get to you."
			, (main) -> { // Add a cooldown for bow usage
				Events.listen(main, ProjectileLaunchEvent.class, event -> {
					if(!(event.getEntity().getShooter() instanceof ProjectileSource)) return;
					if(!Kit.isUsing(main, "Archer", (Entity) event.getEntity().getShooter())) return;
					Player player = (Player) event.getEntity().getShooter();
					player.setCooldown(Material.BOW, 10);
				});
			}
			, (main, player) -> { // Animate a player on spawn to give them arrows until they die.
				final int lives = main.getUserManager().getUsers().get(player).getLives();
				Animation<Player> animation = new Animation<Player>(main);
				animation.addFrame(p -> {
					if(!Kit.isUsing(main, "Archer", p) || main.getUserManager().getUsers().get(p).getLives() != lives) {
						animation.stop(p);
						return;
					}
					if(p.getInventory().contains(Material.ARROW, 5)) return;
					p.getInventory().addItem(new ItemStack(Material.ARROW));
				}, 30L).setLooping(true, 20L).animate(player);
			}),


	GUARD(Material.SHIELD,
			0,
			new Equipment()
			.addArmorPiece(0, Material.SHIELD)
			.addArmorPiece(2, Material.IRON_LEGGINGS)
			.addArmorPiece(3, Material.DIAMOND_CHESTPLATE)
			.addItem(Material.STONE_SWORD)
			.addItem(new ItemStack(Material.COOKED_BEEF, 4))
			, "A slow brute of a class."
			, null
			, (main, player) -> {
				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, main.getMatchManager().getTimeLeft(), 1));
			}),


	NINJA(Material.ENDER_PEARL,
			2500,
			new Equipment()
			.addArmorPiece(2, Material.GOLD_LEGGINGS)
			.addArmorPiece(3, Material.GOLD_CHESTPLATE)
			.addItem(new ItemCreator(Material.GOLD_SWORD).enchantment(Enchantment.DAMAGE_ALL, 2, false).build())
			.addItem(new ItemStack(Material.ENDER_PEARL, 4))
			.addItem(new ItemStack(Material.COOKED_BEEF, 4))
			, "A speedy fighter with little armor and teleportation."
			, (main) -> {
				Events.listen(main, EntityDamageEvent.class, event -> {
					if(!isUsing(main, "Ninja", event.getEntity())) return;
					if(event.getCause() != DamageCause.FALL) return;
					event.setDamage(event.getDamage()/2.0);
				});
				Events.listen(main, PlayerTeleportEvent.class, event -> {
					if(event.getCause() != TeleportCause.ENDER_PEARL) return;
					event.setCancelled(true);
					event.getPlayer().teleport(event.getTo());
				});
			}
			, (main, player) -> {
				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, main.getMatchManager().getTimeLeft(), 1));
			}),


	FISHERMAN(Material.FISHING_ROD, 
			2000,
			new Equipment()
			.addArmorPiece(2, Material.LEATHER_LEGGINGS)
			.addArmorPiece(3, Material.LEATHER_CHESTPLATE)
			.addArmorPiece(4, Material.LEATHER_HELMET)
			.addItem(new ItemStack(Material.FISHING_ROD))
			.addItem(new ItemCreator(Material.WOOD_SWORD).enchantment(Enchantment.KNOCKBACK, 2, true).build())
			.addItem(new ItemStack(Material.COOKED_BEEF, 4))
			, "Pull people towards you and whisk them away."
			, (main) -> {
				Events.listen(main, PlayerFishEvent.class, event -> {
					Player player = event.getPlayer();
					if(!isUsing(main, "Fisherman", player)) return;
					if(event.getState() != PlayerFishEvent.State.CAUGHT_ENTITY) return;
					if(!(event.getCaught() instanceof LivingEntity)) return;
					player.setCooldown(Material.FISHING_ROD, 80);
					event.getCaught().teleport(player.getLocation().add(new Vector(player.getLocation().getDirection().getX(), 0, player.getLocation().getDirection().getZ())));
				});
				Events.listen(main, ProjectileLaunchEvent.class, event -> {
					if(event.getEntity().getType() != EntityType.FISHING_HOOK) return;
					if(!Kit.isUsing(main, "Fisherman", (Entity) event.getEntity().getShooter())) return;
					event.getEntity().setVelocity(event.getEntity().getVelocity().multiply(2));
				});
			}
			, null),

	@SuppressWarnings({ "deprecation", "unchecked" })
	DEMON(Material.FIREBALL,
			0,
			new Equipment()
			.addArmorPiece(2, Material.LEATHER_LEGGINGS)
			.addArmorPiece(3, Material.LEATHER_CHESTPLATE)
			.addArmorPiece(4, Material.LEATHER_HELMET)
			.addItem(new ItemCreator(Material.GOLD_SWORD).enchantment(Enchantment.FIRE_ASPECT, 2, true).build())
			, "The fire-wielding demon class leaps to devour its foes in flame."
			, (main) -> {
				Events.listen(main, EntityDamageEvent.class, event -> {
					if(!isUsing(main, "Demon", event.getEntity())) return;
					DamageCause cause = event.getCause();
					if(cause != DamageCause.FIRE_TICK && cause != DamageCause.LAVA && cause != DamageCause.FIRE) return;
					((Player) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, 1));
					event.setCancelled(true);
				});
				Events.listen(main, PlayerMoveEvent.class, event -> {
					if(!Kit.isUsing(main, "Demon", (Entity) event.getPlayer())) return;
					if(event.getTo().getBlock().equals(event.getFrom().getBlock())) return;
					event.getPlayer().getWorld().spawnFallingBlock(event.getFrom().add(new Vector(0,0.5,0)), Material.FIRE, (byte) 0);
				});
				Events.listen(main, EntityChangeBlockEvent.class, event -> {
					main.getServer().getScheduler().runTaskLater(main, () -> {
						if(event.getBlock().getType() == Material.FIRE) event.getBlock().setType(Material.AIR);
					}, 30L);
				});
				Events.listen(main, PlayerStatisticIncrementEvent.class, event -> {
					if(event.getStatistic() != Statistic.JUMP) return;
					Player player = event.getPlayer();
					if(!player.isSneaking()) return;
					if(!Kit.isUsing(main, "Demon", player)) return;
					if(player.getLevel() < 100) return;
					player.setVelocity(player.getLocation().getDirection().multiply(2));
					player.setLevel(0);
					KitPowerListener.callFireFall(main, player.getLocation(), 1);
				});
			}
			, (main, player) -> {
				player.setLevel(100);
				player.setExp(1.0f);	
				final int lives = main.getUserManager().getUsers().get(player).getLives();
				Animation<Player> animation = new Animation<Player>(main);
				animation.addFrame(p -> {
					if(!Kit.isUsing(main, "Demon", p) || main.getUserManager().getUsers().get(p).getLives() != lives) {
						animation.stop(p);
						return;
					}
					player.setLevel(player.getLevel()+5 > 100?100:player.getLevel()+5);
					player.setExp(player.getLevel()/100.0f);
				}, 5L).setLooping(true, 3L).animate(player);
			}),


	@SuppressWarnings("unchecked")
	HARPY(Material.ELYTRA, 
			0,
			new Equipment()
			.addArmorPiece(1, new ItemCreator(Material.LEATHER_BOOTS).dye(Color.RED).build())
			.addArmorPiece(2, new ItemCreator(Material.LEATHER_LEGGINGS).dye(Color.RED).build())
			.addArmorPiece(3, new ItemCreator(Material.ELYTRA).build())
			.addArmorPiece(4, new ItemCreator(Material.LEATHER_HELMET).dye(Color.RED).build())
			.addItem(Material.STONE_SWORD)
			.addItem(new ItemStack(Material.COOKED_BEEF, 4))
			, "A flying beast which can swoop down and attack foes"
			, (main) -> {
				Events.listen(main, EntityDamageEvent.class, event -> {
					if(!isUsing(main, "Harpy", event.getEntity())) return;
					if(event.getCause() != DamageCause.FALL) return;
					event.setDamage(event.getDamage()/4.0);
				});
				Events.listen(main, PlayerToggleSprintEvent.class, event -> {
					main.getServer().getScheduler().runTask(main, () -> KitPowerListener.runHarpyFlight(main, event.getPlayer()));
				});
			}
			, (main, player) -> {
				player.setLevel(100);
				player.setExp(1.0f);	
				final int lives = main.getUserManager().getUsers().get(player).getLives();
				Animation<Player> animation = new Animation<Player>(main);
				animation.addFrame(p -> {
					if(!Kit.isUsing(main, "Harpy", p) || main.getUserManager().getUsers().get(p).getLives() != lives) {
						animation.stop(p);
						return;
					}
					if(!player.isOnGround()) return;
					player.setLevel(player.getLevel()+6 > 100?100:player.getLevel()+6);
					player.setExp(player.getLevel()/100.0f);
				}, 5L).setLooping(true, 3L).animate(player);
			}),	



	ENGINEER(Material.IRON_INGOT,
			4500,
			new Equipment()
			.addArmorPiece(1, Material.CHAINMAIL_BOOTS)
			.addArmorPiece(2, new ItemCreator(Material.LEATHER_LEGGINGS).dye(Color.GRAY).build())
			.addArmorPiece(3, Material.CHAINMAIL_CHESTPLATE)
			.addArmorPiece(4, new ItemCreator(Material.LEATHER_HELMET).dye(Color.GRAY).build())
			.addItem(Material.STONE_SPADE)
			.addItem(new ItemCreator(Material.BREWING_STAND_ITEM).name("&9Turret").build())
			.addItem(new ItemCreator(Material.SHIELD).name("&9Placeable Shield").build())
			.addItem(new ItemStack(Material.COOKED_BEEF, 4))
			, "A builder class. Build turrets and place shields."
			, (main) -> {
				Events.listen(main, EntityDamageEvent.class, event -> {
					if(!main.getUserManager().getPlaceableManager().isTurret(event.getEntity())) return;
					event.setCancelled(true);
					event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_DOOR_WOOD, 0.5f, 0.3f);
					main.getUserManager().getPlaceableManager().getTurret(event.getEntity()).damage(event.getDamage());
				});
				Events.listen(main, EntityDamageByEntityEvent.class, event -> {
					if(!main.getUserManager().getPlaceableManager().isTurret(event.getEntity())) return;
					Turret turret = main.getUserManager().getPlaceableManager().getTurret(event.getEntity());
					event.setCancelled(true);
					if(!turret.getOwner().equals(event.getDamager())) return;
					Player player = (Player) event.getDamager();
					if(player.getInventory().getItemInMainHand().getType() != Material.STONE_SPADE) return;
					turret.kill();
					player.setCooldown(Material.BREWING_STAND_ITEM, player.getCooldown(Material.BREWING_STAND_ITEM)-25);
					event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 0.7f);

				});
				Events.listen(main, ProjectileLaunchEvent.class, event -> {
					if((Entity)event.getEntity().getShooter() != null && ((Entity)event.getEntity().getShooter()).getType() == EntityType.SHULKER)
						event.getEntity().remove();
				});
			}		
			, null),

	TRICKSTER(Material.GOLD_HOE,
			4500,
			new Equipment()
			.addArmorPiece(1, new ItemCreator(Material.LEATHER_BOOTS).dye(Color.MAROON).build())
			.addArmorPiece(2, new ItemCreator(Material.LEATHER_LEGGINGS).dye(Color.MAROON).build())
			.addArmorPiece(4, new ItemCreator(Material.CHAINMAIL_HELMET).build())
			.addItem(new ItemCreator(Material.GOLD_HOE).enchantment(Enchantment.KNOCKBACK, 1, true).enchantment(Enchantment.DAMAGE_ALL, 1, true).build())
			.addItem(Material.BOW)
			.addItem(new ItemCreator(Material.SKULL_ITEM).durability((byte)3).name("&9Fake Out").build())
			.addItem(new ItemStack(Material.COOKED_BEEF, 4))
			.addItem(Material.ARROW)
			, "A tricky escapist fighter class."
			, (main) -> { // Add a cooldown for bow usage
				Events.listen(main, ProjectileLaunchEvent.class, event -> {
					if(!(event.getEntity().getShooter() instanceof ProjectileSource)) return;
					if(!Kit.isUsing(main, "Trickster", (Entity) event.getEntity().getShooter())) return;
					Player player = (Player) event.getEntity().getShooter();
					player.setCooldown(Material.BOW, 100);
				});
				Events.listen(main, EntityDamageByEntityEvent.class, event -> {
					if(!(event.getDamager() instanceof Projectile)) return;
					Projectile shot = (Projectile) event.getDamager();
					if(!(shot.getShooter() instanceof Player)) return;
					Player damager = (Player) shot.getShooter();
					if(Kit.isUsing(main, "Trickster", damager)) {
						Location loc = damager.getLocation();
						damager.teleport(event.getEntity());
						event.getEntity().teleport(loc);
					}
				});
			}
			, (main, player) -> { // Animate a player on spawn to give them arrows until they die.
				final int lives = main.getUserManager().getUsers().get(player).getLives();
				Animation<Player> animation = new Animation<Player>(main);
				animation.addFrame(p -> {
					if(!Kit.isUsing(main, "Trickster", p) || main.getUserManager().getUsers().get(p).getLives() != lives) {
						animation.stop(p);
						return;
					}
					if(p.getInventory().contains(Material.ARROW, 2)) return;
					p.getInventory().addItem(new ItemStack(Material.ARROW));
				}, 40L).setLooping(true, 20L).animate(player);
			}),

	MAGICIAN(Material.GLOWSTONE_DUST,
			4500,
			new Equipment()
			.addArmorPiece(1, new ItemCreator(Material.LEATHER_BOOTS).dye(Color.BLACK).build())
			.addArmorPiece(2, new ItemCreator(Material.LEATHER_LEGGINGS).dye(Color.BLACK).build())
			.addArmorPiece(3, new ItemCreator(Material.LEATHER_CHESTPLATE).dye(Color.WHITE).build())
			.addArmorPiece(4, new ItemCreator(Material.LEATHER_HELMET).dye(Color.BLACK).build())
			.addItem(new ItemCreator(Material.STICK).name("&9Magic Wand").enchantment(Enchantment.DAMAGE_ALL, 2, false).build())
			.addItem(new ItemCreator(Material.GLOWSTONE_DUST).name("&9Special Powder").build())
			.addItem(new ItemCreator(Material.CAULDRON_ITEM).name("&9Top Hat").build())
			.addItem(new ItemStack(Material.COOKED_BEEF, 4))
			, "An annoying fighter which utilizes magic and trickery to confuse the opponent."
			, null
			, null);


	Material displayItem;
	Equipment equipment;
	Consumer<Main> eventListener;
	String description;
	int cost;
	BiConsumer<Main, Player> spawn;

	Kit(Material displayItem, int cost, Equipment equipment, String description, Consumer<Main> eventListener, BiConsumer<Main,Player> spawn) {
		this.displayItem = displayItem;
		this.equipment = equipment;
		this.cost = cost;
		this.eventListener = eventListener;
		this.spawn = spawn;
		this.description = description;
	}

	public String getName() {
		return StringUtils.capitalize(this.toString().toLowerCase());
	}

	public Consumer<Main> init() {
		return this.eventListener;
	}

	public void spawn(Main main, Player player) {
		if(spawn == null) return;
		spawn.accept(main, player);
	}

	public ItemStack getMenuItem(User user) {
		boolean has = user.hasKit(this);
		boolean is = user.getSpawnKit().equals(this);
		boolean canAfford = user.getCoins() >= this.cost;
		String costLine = "&aCost: " + this.cost;
		String purchaseLine = has?is?"&2You are using this kit":"&6Click to select":canAfford?"&aClick to purchase":"&cYou can't afford this";
		return new ItemCreator(this.displayItem).name(this.getName()).lore(Text.splitByWords(this.description, 5)).addLore((!has?costLine:""), purchaseLine).build();
	}

	public int getCost() {
		return this.cost;
	}

	public Equipment getEquipment() {
		return this.equipment;
	}

	public static Kit getKit(String name) {
		return Kit.valueOf(name.toUpperCase());
	}

	public static boolean isUsing(Main main, String name, Entity entity) {
		if(entity.getType() != EntityType.PLAYER) return false;
		Player player = (Player) entity;
		if(!main.getUserManager().getUsers().containsKey(player)) return false;
		User user = main.getUserManager().getUsers().get(player);
		if(!player.isOnline() || !user.isInMatch() || !user.getCurrentKit().getName().equals(name)) return false;
		return true;
	}

}
