package facejup.mcer.match;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import facejup.mcer.kits.Kit;
import facejup.mcer.kits.Shield;
import facejup.mcer.kits.Turret;
import facejup.mcer.main.Main;
import facejup.mcer.match.matchtypes.FFAEliminations;
import facejup.mcer.match.matchtypes.Match;
import facejup.mcer.storage.ItemSpawn;
import facejup.mcer.users.User;
import facejup.mcer.util.Text;

public class MatchManager {
	
	private int min_players = 5;
	private String matchType = "FFA";
	private Match match;
	private Arena arena;
	private Main main;
	private boolean running = false;
	private StartTimer startTimer;
	private EndTimer endTimer;
	
	private List<OfflinePlayer> spectators;
	
	public MatchManager(Main main) {
		arena = new Arena(this);
		this.main = main;
		startTimer = new StartTimer(main);
		endTimer = new EndTimer(main);
		if(main.getConfig().contains("Users.Min")) this.min_players = main.getConfig().getInt("Users.Min");
		if(main.getConfig().contains("MatchType")) this.matchType = main.getConfig().getString("MatchType");
		switch(matchType.toUpperCase()) {
		case "FFA":
			match = new FFAEliminations(this);
			break;
		}
		if(!arena.loadArena(main))
			System.out.println("ARENA FAILED TO LOAD. TYPE /arena load TO TRY AGAIN");
	}
	
	public boolean isRunning() {
		return running;
	}

	public int getTimeLeft() {
		return 10000;
	}

	public Arena getArena() {
		return arena;
	}

	public Main getMain() {
		return main;
	}

	public Match getMatch() {
		return match;
	}

	public StartTimer getStartTimer() {
		return startTimer;
	}

	public EndTimer getEndTimer() {
		return endTimer;
	}

	public void startMatch() {
		this.running = true;
		endTimer.animate(this);
		match.start();
		if(arena.getItemSpawns().isEmpty()) return;
		arena.getItemSpawns().forEach(ItemSpawn::animate);
	}
	public void endMatch() {
		match.end();
	}
	public void interruptMatch() {
		for(Player player : Bukkit.getOnlinePlayers()) {
			main.getServer().dispatchCommand(main.getServer().getConsoleSender(), "spawn " + player.getName());
		}
		for(Turret turret : main.getUserManager().getPlaceableManager().getTurrets()) {
			turret.kill();
		}
		for(Shield shield : main.getUserManager().getPlaceableManager().getShields()) {
			shield.kill();
		}
		match.interrupt();
	}
	
	public String getSecondsLeft() {
		int secondsLeft = this.getTimeLeft();
		int minutesLeft = (int) (secondsLeft/60.0);
		secondsLeft-=minutesLeft*60;
		boolean useMinutes = minutesLeft != 0;
		return useMinutes?minutesLeft + " minutes":secondsLeft + " seconds";
	}

	public void spawnPlayer(User user) {
		user.setCurrentKit(user.getSpawnKit());
		user.getPlayer().getPlayer().teleport(arena.getFurthestSpawn());
		user.getCurrentKit().spawn(getMain(), user.getPlayer().getPlayer());
		user.getPlayer().getPlayer().getInventory().clear();
		user.getPlayer().getPlayer().getActivePotionEffects().forEach(pot -> user.getPlayer().getPlayer().removePotionEffect(pot.getType()));
		user.getCurrentKit().spawn(main, user.getPlayer().getPlayer());
		user.getPlayer().getPlayer().setHealth(user.getPlayer().getPlayer().getMaxHealth());
		user.getPlayer().getPlayer().setFoodLevel(20);
		user.getCurrentKit().getEquipment().getInventoryContents().forEach(item -> user.getPlayer().getPlayer().getInventory().addItem(item));
		user.getCurrentKit().getEquipment().equipArmor(user.getPlayer().getPlayer());
		user.getPlayer().getPlayer().setFireTicks(0);
		match.updateScoreboard(user.getPlayer().getPlayer());
	}

	public int getMinimumPlayers() {
		return min_players;
	}
	

	public void updateSelected(OfflinePlayer player, Kit kit) {
		Bukkit.broadcastMessage(Text.trans("&e(" + main.getUserManager().getUsers().values().stream().filter(user -> user.getSpawnKit() != Kit.NONE).count() + "/" + this.getMinimumPlayers() + ") &d" + player.getName() + " has " + (kit != Kit.NONE?"joined":"left") + " queue!"));
		if(main.getUserManager().getUsers().values().stream().filter(user -> user.getSpawnKit() != Kit.NONE).count() < min_players) return;
		if(startTimer.isRunning(this)) return;
		startTimer.animate(this);
	}

	public void addSpectator(User user) {
		user.getPlayer().getPlayer().getInventory().clear();
		user.getPlayer().getPlayer().setGameMode(GameMode.SPECTATOR);
	}

	public void stop() {
		this.running = false;
		for(Turret turret : main.getUserManager().getPlaceableManager().getTurrets()) {
			turret.kill();
		}
		for(Shield shield : main.getUserManager().getPlaceableManager().getShields()) {
			shield.kill();
		}
	}

}
