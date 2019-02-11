package facejup.mcer.kits;

import java.lang.reflect.Method;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import facejup.mcer.main.Main;

public class Turret {
	private Main main;
	private OfflinePlayer owner;
	private ArmorStand turretBase;
	private LivingEntity target;
	private double health = 30.0;
	private double bullets = 7.0;
	private boolean alive = true;
	private final double RANGE = 10.0;
	private final long RELOAD = 50L;
	private final long FIRERATE = 10L;
	private final long TARGETSPEED = 30L;
	private final Method[] methods = ((Supplier<Method[]>)() -> {
		try {
			Method getHandle = Class.forName(String.valueOf(Bukkit.getServer().getClass().getPackage().getName()) + ".entity.CraftEntity").getDeclaredMethod("getHandle", new Class[0]);
			return new Method[]{getHandle, getHandle.getReturnType().getDeclaredMethod("setPositionRotation", Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE)};
		}
		catch (Exception ex) {
			return null;
		}
	}).get();

	public Turret(Main main, OfflinePlayer owner, Location location) {
		this.main = main;
		this.owner = owner;
		this.turretBase = (ArmorStand)location.getWorld().spawnEntity(location.clone().add(new Vector(0.0, 0.25, 0.0)), EntityType.ARMOR_STAND);
		this.turretBase.setHelmet(new ItemStack(Material.SKULL_ITEM, 1, (short) 1));
		this.turretBase.setCustomName(String.valueOf(owner.getName()) + "'s Turret");
		this.turretBase.setCustomNameVisible(true);
		this.turretBase.setCollidable(true);
		this.turretBase.setBasePlate(false);
		this.findTarget();
	}

	private void findTarget() {
		if (!this.owner.isOnline()) {
			return;
		}
		if (this.turretBase.isDead()) {
			return;
		}
		if (!this.alive) {
			return;
		}
		Location location = this.turretBase.getLocation();
		block0 : for (Entity ent : this.turretBase.getLocation().getWorld().getNearbyEntities(location, 10.0, 10.0, 10.0)) {
			if (!(ent instanceof LivingEntity) || ent.equals((Object)this.turretBase) || ent.equals((Object)this.owner.getPlayer()) || (this.main.getUserManager().getPlaceableManager().isTurret(ent) && this.main.getUserManager().getPlaceableManager().getTurret(ent).getOwner().equals((Object)this.owner))) continue;
			Location tempLoc = location.clone().add(new Vector(0.5, 0.5, 0.5));
			Location targetLoc = ent.getLocation().add(new Vector(0.0, 0.75, 0.0));
			Vector direction = targetLoc.clone().subtract(tempLoc).toVector().normalize();
			int i = 0;
			while ((double)i < tempLoc.distance(targetLoc)) {
				if (tempLoc.add(direction).getBlock().getType() != Material.AIR) {
					this.target = null;
					continue block0;
				}
				++i;
			}
			this.target = (LivingEntity)ent;
			break;
		}
		location.getWorld().playSound(location, Sound.ENTITY_ITEM_BREAK, 0.2f, 0.3f);
		if (this.target == null || this.target.isDead() || this.target.getLocation().distance(this.turretBase.getLocation()) > 10.0) {
			if(this.bullets < 7) this.bullets++;
			this.heal(2.0);
			this.main.getServer().getScheduler().runTaskLater((Plugin)this.main, () -> this.findTarget(), 30L);
		} else {
			this.main.getServer().getScheduler().runTaskLater((Plugin)this.main, () -> this.shoot(), 30L);
		}
	}

	public OfflinePlayer getOwner() {
		return this.owner;
	}

	private void shoot() {
		block8 : {
		block7 : {
		if (!this.alive) {
			return;
		}
		Location location = this.turretBase.getLocation();
		if (this.bullets == 0.0) {
			this.main.getServer().getScheduler().runTaskLater((Plugin)this.main, () -> this.reload(0), 25L);
			return;
		}
		Vector direction = this.target.getLocation().subtract(location).toVector().normalize();
		Location loc = location.clone();
		loc.setDirection(this.target.getLocation().subtract(loc).toVector().normalize());
		try {
			this.methods[1].invoke(this.methods[0].invoke((Object)this.turretBase, new Object[0]), loc.getX(), loc.getY(), loc.getZ(), Float.valueOf(loc.getYaw()), Float.valueOf(loc.getPitch()));
		}
		catch (Exception exception) {
			// empty catch block
		}
		location.getWorld().spawnArrow(location.clone().add(direction.multiply(0.8f)).add(new Vector(0.0, 1.25, 0.0)), direction, 1.25f, 1.0f).setShooter(this.turretBase);;
		location.getWorld().playSound(location, Sound.BLOCK_ANVIL_FALL, 1.0f, 1.0f);
		this.bullets -= 1.0;
		if (this.target == null || this.target.isDead()) break block7;
		if (this.target.getLocation().distance(this.turretBase.getLocation()) <= 10.0) break block8;
	}
	this.main.getServer().getScheduler().runTask((Plugin)this.main, () -> this.findTarget());
	return;
	}
	this.main.getServer().getScheduler().runTaskLater((Plugin)this.main, () -> this.shoot(), 10L);
	}

	private void reload(int i) {
		if (!this.alive) {
			return;
		}
		Location location = this.turretBase.getLocation();
		this.bullets = 7.0;
		location.getWorld().playSound(location, Sound.BLOCK_TRIPWIRE_CLICK_ON, 0.3f, 1.0f);
		if (i < 7) {
			this.main.getServer().getScheduler().runTaskLater((Plugin)this.main, () -> this.reload(i + 1), 7L);
			return;
		}
		this.main.getServer().getScheduler().runTaskLater((Plugin)this.main, () -> this.findTarget(), 7L);
	}

	public ArmorStand getTurretBase() {
		return this.turretBase;
	}

	public LivingEntity getTarget() {
		return this.target;
	}

	public void damage(double damage) {
		this.health -= damage;
		if (this.health <= 0.0) {
			this.kill();
		}
	}
	public void kill() {
		Location location = this.turretBase.getLocation();
		location.getWorld().spawnParticle(Particle.FLAME, location.clone().add(new Vector(0.0, 0.5, 0.0)), 5, 0.5, 0.2, 0.2, 0.2);
		this.turretBase.remove();
		this.alive = false;
		this.main.getUserManager().getUsers().get(this.owner).removeTurret(this);
	}

	public void heal(double d) {
		Location location = this.turretBase.getLocation();
		if (this.health <= 30.0 - d) {
			location.getWorld().spawnParticle(Particle.HEART, location.clone().add(new Vector(0, 1, 0)), 5, 0.5, 0.2, 0.2, 0.2);
			this.health += d;
		}
	}

	public double getHealth() {
		return this.health;
	}
}
