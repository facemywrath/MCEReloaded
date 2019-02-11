package facejup.mcer.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.util.Vector;

import facejup.mcer.kits.MountedTurret;
import facejup.mcer.kits.Turret;
import facejup.mcer.main.Main;
import facejup.mcer.storage.DamageIndicator;
import facejup.mcer.users.User;
import facejup.mcer.util.Animation;
import facejup.mcer.util.Events;

public class DamageListener {

	private List<DamageIndicator> indicators = new ArrayList<>();
	private Animation<DamageIndicator> indicatorMovement;


	public DamageListener(Main main) {
		indicatorMovement = new Animation<DamageIndicator>(main)
				.addFrame(indicator -> {
					indicator.floatAway();
				}, 1L, 9)
				.addFrame(indicator -> {
					indicator.remove();
					indicators.remove(indicator);
				}, 20L);
		Events.listen(main, EntityDamageEvent.class, EventPriority.LOWEST, event -> {
			if(!(event.getEntity() instanceof LivingEntity)) return;
			LivingEntity target = (LivingEntity) event.getEntity();
			main.getServer().getScheduler().runTaskLater(main, () -> {
				if(event.getFinalDamage() <= 0) return;
				if(event.isCancelled()) return;
				if(main.getUserManager().getPlaceableManager().isTurret(target)) {
					Turret turret = main.getUserManager().getPlaceableManager().getTurret(target);
					DamageIndicator indicator = new DamageIndicator(main, event.getEntity().getLocation().add(new Vector(0,2.3,0)), event.getFinalDamage(), turret.getHealth(), 30);
					indicatorMovement.animate(indicator);
					indicators.add(indicator);
					return;
				}
				else if(main.getUserManager().getPlaceableManager().isMountedTurret(target)) {
					MountedTurret turret = main.getUserManager().getPlaceableManager().getMountedTurret(target);
					DamageIndicator indicator = new DamageIndicator(main, event.getEntity().getLocation().add(new Vector(0,2.3,0)), event.getFinalDamage(), turret.getHealth(), 30);
					indicatorMovement.animate(indicator);
					indicators.add(indicator);
					return;
				}
				DamageIndicator indicator = new DamageIndicator(main, event.getEntity().getLocation().add(new Vector(0,2,0)), event.getFinalDamage(), target.getHealth(), target.getMaxHealth());
				indicatorMovement.animate(indicator);
				indicators.add(indicator);
			}, 1L);
		});
		Events.listen(main, EntityDamageByEntityEvent.class, event -> {
			if(event.getEntity().getType() != EntityType.PLAYER) return;
			Player player = (Player) event.getEntity();
			if(!main.getUserManager().getUsers().containsKey(player)) return;
			User user = main.getUserManager().getUsers().get(player);
			Entity damager = event.getDamager();
			if(damager instanceof Projectile) {
				Projectile shot = (Projectile) damager;
				if(shot.getShooter() instanceof Player) damager = (Entity) shot.getShooter();
				else
				{
					if(main.getUserManager().getPlaceableManager().isTurret((Entity) shot.getShooter())) {
						damager = (Entity) main.getUserManager().getPlaceableManager().getTurret((Entity) shot.getShooter()).getOwner().getPlayer();
					}
				}
			}
			user.damageUpdate(main, damager);
		});
		Events.listen(main, EntityDamageByEntityEvent.class, event -> {
			if(main.getUserManager().getPlaceableManager().getMountedTurrets().isEmpty()) return;
			if(!main.getUserManager().getPlaceableManager().isMountedTurret(event.getEntity())) return;
			MountedTurret turret = main.getUserManager().getPlaceableManager().getMountedTurret(event.getEntity());
			if(turret.getPassenger() == null) return;
			if(event.getDamager().equals(turret.getPassenger())) {
				event.setCancelled(true);
				event.setDamage(0);
			}
			else {
				turret.damage(event.getDamage());
			}
		});
		Events.listen(main, VehicleDamageEvent.class, event -> {
			if(main.getUserManager().getPlaceableManager().getMountedTurrets().isEmpty()) return;
			if(!main.getUserManager().getPlaceableManager().isMountedTurret(event.getVehicle())) return;
			MountedTurret turret = main.getUserManager().getPlaceableManager().getMountedTurret(event.getVehicle());
			if(turret.getPassenger() != null && event.getAttacker().equals(turret.getPassenger())) {
				event.setCancelled(true);
				event.setDamage(0);
			}
			else {
				turret.damage(event.getDamage());
			}
		});
		Events.listen(main, PlayerDeathEvent.class, event -> {
			Player player = event.getEntity();
			if(!main.getUserManager().getUsers().containsKey(player)) return;
			User user = main.getUserManager().getUsers().get(player);
			user.incDeaths();
			if(user.lastHitter() != null && user.lastHitter().getType() == EntityType.PLAYER && main.getUserManager().getUsers().containsKey(user.lastHitter())) 
				user.incKills();
		});
		Events.listen(main, EntityDamageEvent.class, event -> {
			if(!main.getUserManager().getPlaceableManager().isTurret(event.getEntity())) return;
			event.setCancelled(true);
			event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_DOOR_WOOD, 0.5f, 0.3f);
			main.getUserManager().getPlaceableManager().getTurret(event.getEntity()).damage(event.getDamage());
		});
	}

	public void spawnIndicator(Main main, Location loc, double damage, double health, double maxHealth) {
		DamageIndicator indicator = new DamageIndicator(main, loc, damage, health, maxHealth);
		indicatorMovement.animate(indicator);
		indicators.add(indicator);
	}

}
