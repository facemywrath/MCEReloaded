package facejup.mcer.kits;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import facejup.mcer.main.Main;
import facejup.mcer.storage.RelEntity;
import facejup.mcer.util.Animation;

public class MountedTurret {

	private Main main;

	private double health = 50;
	private double heat = 0;
	private Location loc;
	private List<RelEntity> entities;
	private Animation<Location> turning;
	private Animation<MountedTurret> cooldown;
	private Animation<MountedTurret> shoot;
	private OfflinePlayer owner;
	private boolean coolingDown = false;
	private boolean shooting = false;

	public MountedTurret(Main main, OfflinePlayer owner, Location location) {
		entities = new ArrayList<>();
		this.owner = owner;
		this.main = main;
		turning = new Animation<Location>(main).addFrame(loc -> {
			if(getPassenger()==null) {
				updateLocations(loc);
				return;
			}
			updateLocations(getPassenger().getLocation());
		}, 1L).setLooping(true);
		cooldown = new Animation<MountedTurret>(main).addFrame(t -> {
			this.coolingDown = true;
			int sub = 2;
			if(heat-sub <= 0) {
				heat = 0;
				this.coolingDown = false;
				cooldown.stop(t);
				return;
			}
			if(getPassenger() != null) {
				getPassenger().setExp((float) (heat/100.0));
				getPassenger().setLevel((int) heat);
			}
			heat -= sub;
			loc.getWorld().spawnParticle(Particle.FLAME, loc, 5);
		}, 1L).setLooping(true);
		shoot = new Animation<MountedTurret>(main).addFrame(t -> {
			int add = 3;
			shooting = true;
			if(heat + add >= 100 || coolingDown) {
				if(heat + add >= 100)
					heat = 100;
				shoot.stop(t);
				shooting = false;
				cooldown.animate(t);
				return;
			}
			heat+=add;
			if(getPassenger() == null) {
				shoot.stop(t);
				shooting = false;
				cooldown.animate(t);
				return;
			}
			getPassenger().setExp((float) (heat/100.0));
			getPassenger().setLevel((int) heat);
			loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SHOOT, 1.0f, 1.6f);
			loc.getWorld().spawnArrow(loc.clone().add(new Vector(0,2,0)).add(new Vector(loc.getDirection().getX(), 0, loc.getDirection().getZ())), loc.getDirection(), 2, 5);
		}, 2L, 7).setLooping(true).setLoopDelay(10L);
		this.loc = location;
		this.loc.setDirection(new Vector(1,0,0));

		Location locMine = loc.clone().add(new Vector(-1.5, 1, 1.5));
		Minecart minecart = (Minecart) locMine.getWorld().spawnEntity(locMine, EntityType.MINECART);
		minecart.setGravity(false);
		entities.add(new RelEntity(minecart, new Vector(-1.5, 1, 1.5), 0, 0));

		addPillar(loc.clone().add(new Vector(0, 0.75, 0)), new Vector(0,0.75,0), 0, 0);
		locMine = loc.clone().add(new Vector(0, 1.25, 0));
		addPillar(locMine, new Vector(0, 1.25, 0), 0, 0);
		turning.animate(loc);

		ArmorStand ent = (ArmorStand) loc.getWorld().spawnEntity(loc.clone().subtract(new Vector(0,0.5,0)), EntityType.ARMOR_STAND);
		ent.setGravity(false);
		ent.setCollidable(false);
		ent.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
		ent.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
		//	ent.setVisible(false);
		ent.setArms(true);
		ent.setHeadPose(new EulerAngle(-Math.PI/2, -Math.PI/2, -Math.PI/2));
		ent.setBasePlate(false);
		ent.setLeftArmPose(new EulerAngle(-Math.PI/2.0,0,-2));
		ent.setRightArmPose(new EulerAngle(-Math.PI/2.0,0,2));
		entities.add(new RelEntity(ent, new Vector(0,0.5,0), 0, 0));

		ArmorStand connector = (ArmorStand) loc.getWorld().spawnEntity(loc.clone().subtract(new Vector(0,0.3,0)), EntityType.ARMOR_STAND);
		connector.setGravity(false);
		connector.setCollidable(false);
		connector.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
		connector.setVisible(false);
		connector.setBodyPose(new EulerAngle(Math.PI/2,0,0));
		entities.add(new RelEntity(connector, new Vector(0,-0.3,0), 0, 0));
		connector = (ArmorStand) loc.getWorld().spawnEntity(loc.clone().subtract(new Vector(0.25,0.3,0)), EntityType.ARMOR_STAND);
		connector.setGravity(false);
		connector.setCollidable(false);
		connector.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
		connector.setVisible(false);
		connector.setBodyPose(new EulerAngle(Math.PI/2,0,0));
		entities.add(new RelEntity(connector, new Vector(-0.25,-0.3,0), 0, 0));
		connector = (ArmorStand) loc.getWorld().spawnEntity(loc.clone().subtract(new Vector(0.5,0.3,0)), EntityType.ARMOR_STAND);
		connector.setGravity(false);
		connector.setCollidable(false);
		connector.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
		connector.setVisible(false);
		connector.setBodyPose(new EulerAngle(Math.PI/2,0,0));
		entities.add(new RelEntity(connector, new Vector(-0.5,-0.3,0), 0, 0));
		connector = (ArmorStand) loc.getWorld().spawnEntity(loc.clone().subtract(new Vector(0.75,0.3,0)), EntityType.ARMOR_STAND);
		connector.setGravity(false);
		connector.setCollidable(false);
		connector.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
		connector.setVisible(false);
		connector.setBodyPose(new EulerAngle(Math.PI/2,0,0));
		entities.add(new RelEntity(connector, new Vector(-0.75,-0.3,0), 0, 0));
		connector = (ArmorStand) loc.getWorld().spawnEntity(loc.clone().subtract(new Vector(-.25,0.3,0)), EntityType.ARMOR_STAND);
		connector.setGravity(false);
		connector.setCollidable(false);
		connector.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
		connector.setVisible(false);
		connector.setBodyPose(new EulerAngle(Math.PI/2,0,0));
		entities.add(new RelEntity(connector, new Vector(.25,-0.3,0), 0, 0));
	}

	public ArmorStand addPillar(Location loc, Vector position, float yaw, float pitch) {
		ArmorStand ent = (ArmorStand) loc.getWorld().spawnEntity(loc.clone().subtract(new Vector(0,1,0)), EntityType.ARMOR_STAND);
		ent.setGravity(false);
		ent.setCollidable(false);
		ent.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
		ent.setVisible(false);
		entities.add(new RelEntity(ent, position.clone().subtract(new Vector(0,1,0)), yaw, pitch));
		return ent;
	}

	public void updateLocations(Location loc) {
		this.loc.setYaw(loc.getYaw());
		this.loc.setPitch(loc.getPitch());
		for (RelEntity ent : entities) {
			Vector vec = ent.relativeLocation.clone();
			double x = -Math.sin(Math.toRadians(loc.getYaw()));
			double z = -Math.cos(Math.toRadians(loc.getYaw()));
			DecimalFormat format = new DecimalFormat("##.######");
			Vector dir = new Vector(x,1,z);
			vec.multiply(dir);
			Location temploc = this.loc.clone().add(vec);
			temploc.setYaw(temploc.getYaw()+ent.yaw);
			temploc.setPitch(temploc.getPitch()+ent.pitch);
			move(ent.entity, temploc);
		}
	}

	private void move(Entity entity, Location location) {
		main.getServer().getScheduler().runTask(main, () -> {
			try {
				Class<?> entityClass = net.minecraft.server.v1_12_R1.Entity.class;
				net.minecraft.server.v1_12_R1.Entity target = ((CraftEntity) entity).getHandle();
				Method method = entityClass.getDeclaredMethod("setPositionRotation", double.class, double.class, double.class, float.class, float.class);
				method.invoke(target, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
			} catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
				e.printStackTrace();
			}
		});
	}

	public Player getPassenger() {
		return (Player) entities.get(0).entity.getPassenger();
	}

	public List<Entity> getEntities() {
		return this.entities.stream().map(RelEntity::getEntity).collect(Collectors.toList());
	}

	public void shoot() {
		if(!coolingDown && !shooting)
			shoot.animate(this);
		else if(!coolingDown){
			shoot.stop(this);
			shooting = false;
			cooldown.animate(this);
		}
	}

	public void damage(double damage) { 
		if(this.health - damage <= 0) {
			this.kill();
			return;
		}
		this.health -= damage;
	}

	public OfflinePlayer getOwner() {
		return owner;
	}

	public void kill() {
		this.loc.getWorld().playSound(this.loc.add(new Vector(0,2,0)), Sound.BLOCK_ANVIL_LAND, 1, 1);
		for(Entity ent : entities.stream().map(RelEntity::getEntity).collect(Collectors.toList())) {
			ent.remove();
		}
		entities.clear();
		main.getUserManager().getUsers().get(owner).removeMountedTurret(this);
	}

	public double getHealth() {
		return this.health;
	}

}
