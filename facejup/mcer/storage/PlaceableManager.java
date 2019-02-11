package facejup.mcer.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import facejup.mcer.kits.MountedTurret;
import facejup.mcer.kits.Shield;
import facejup.mcer.kits.Turret;
import facejup.mcer.main.Main;
import facejup.mcer.users.User;
import facejup.mcer.users.UserManager;

public class PlaceableManager {
	
	private UserManager userManager;

	private List<Shield> shields = new ArrayList<>();
	private List<Turret> turrets = new ArrayList<>();
	private List<MountedTurret> mountedTurrets = new ArrayList<>();
	
	public PlaceableManager(UserManager userManager) {
		this.userManager = userManager;
	}
	public void spawnTurret(Main main, User user, Location location) {
		if(user.getTurrets().size() > 1)
			user.getTurrets().get(0).kill();
		turrets.add(user.placeTurret(main, location.add(new Vector(0.5,0,0.5))));
	}
	
	public boolean isTurret(Entity ent) {
		if(turrets.isEmpty()) return false;
		return turrets.stream().anyMatch(turret -> turret.getTurretBase().equals(ent));
	}
	public Turret getTurret(Entity ent) {
		if(turrets.isEmpty()) return null;
		Optional<Turret> ret = turrets.stream().filter(turret -> turret.getTurretBase().equals(ent)).findFirst();
		return ret.isPresent()?ret.get():null;
	}
	public MountedTurret getMountedTurret(Entity ent) {
		if(mountedTurrets.isEmpty()) return null;
		Optional<MountedTurret> ret = mountedTurrets.stream().filter(turret -> turret.getEntities().contains(ent)).findFirst();
		return ret.isPresent()?ret.get():null;
	}
	public void spawnMountedTurret(Main main, User user, Location location) {
		//if(user.getMountedTurrets().size() > 1)
		//	user.getMountedTurrets().get(0).kill();
		mountedTurrets.add(user.placeMountedTurret(main, location.add(new Vector(0.5,0,0.5))));
	}
	
	public boolean isMountedTurret(Entity ent) {
		if(mountedTurrets.isEmpty()) return false;
		return mountedTurrets.stream().anyMatch(turret -> turret.getEntities().contains(ent));
	}

	public void spawnShield(Main main, User user) {
		if(user.getShields().size() > 0)
			user.getShields().get(0).kill();
		shields.add(user.placeShield(main));
	}
	
	public boolean isShield(Block block) {
		if(shields.isEmpty()) return false;
		return shields.stream().anyMatch(shield -> shield.getBlocks().contains(block));
	}
	public Shield getShield(Block block) {
		if(shields.isEmpty()) return null;
		Optional<Shield> ret = shields.stream().filter(shield -> shield.getBlocks().contains(block)).findFirst();
		return ret.isPresent()?ret.get():null;
	}

	public List<MountedTurret> getMountedTurrets() {
		return this.mountedTurrets;
	}

	public List<Turret> getTurrets() {
		return this.turrets;
	}
	public List<Shield> getShields() {
		return this.shields;
	}

}
