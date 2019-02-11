package facejup.mcer.match.matchtypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import facejup.mcer.kits.Kit;
import facejup.mcer.main.Main;
import facejup.mcer.match.MatchManager;
import facejup.mcer.users.User;
import facejup.mcer.util.Animation;
import facejup.mcer.util.Events;
import facejup.mcer.util.Text;
import io.netty.util.internal.ThreadLocalRandom;
import net.md_5.bungee.api.ChatColor;

public class FFAEliminations extends Match {

	private HashMap<Player, Integer> lives = new HashMap<>();
	private HashMap<Player, Long> feedingCooldown = new HashMap<>();
	private Animation<Player> respawnAnimation;

	public FFAEliminations(MatchManager matchManager) {
		super(matchManager);
		respawnAnimation = new Animation<Player>(matchManager.getMain()).addFrame(player -> {
			if(!matchManager.isRunning()) {
				player.setGameMode(GameMode.SURVIVAL);
				matchManager.getMain().getServer().dispatchCommand(matchManager.getMain().getServer().getConsoleSender(), "spawn " + player.getName());
				respawnAnimation.stop(player);
				return;
			}
			player.sendTitle(ChatColor.RED + "You died!", ChatColor.YELLOW + "Respawning in " + ChatColor.BLUE + "3...", 0, 20, 50);
			player.setGameMode(GameMode.SPECTATOR);
		}, 1L).addFrame(player -> {
			if(!matchManager.isRunning()) {
				player.setGameMode(GameMode.SURVIVAL);
				matchManager.getMain().getServer().dispatchCommand(matchManager.getMain().getServer().getConsoleSender(), "spawn " + player.getName());
				respawnAnimation.stop(player);
				return;
			}
			player.sendTitle(ChatColor.RED + "You died!", ChatColor.YELLOW + "Respawning in " + ChatColor.AQUA + "2...", 0, 20, 50);
		}, 20L).addFrame(player -> {
			if(!matchManager.isRunning()) {
				player.setGameMode(GameMode.SURVIVAL);
				matchManager.getMain().getServer().dispatchCommand(matchManager.getMain().getServer().getConsoleSender(), "spawn " + player.getName());
				respawnAnimation.stop(player);
				return;
			}
			player.sendTitle(ChatColor.RED + "You died!", ChatColor.YELLOW + "Respawning in " + ChatColor.GREEN + "1...", 0, 20, 30);
		}, 20L).addFrame(player -> {
			if(!matchManager.getMain().getUserManager().getUsers().containsKey(player)) return;
			player.setGameMode(GameMode.SURVIVAL);
			User user = matchManager.getMain().getUserManager().getUsers().get(player);
			if(matchManager.isRunning()) 
			matchManager.spawnPlayer(user);
			else matchManager.getMain().getServer().dispatchCommand(matchManager.getMain().getServer().getConsoleSender(), "spawn " + player.getName());

		}, 20L);
	}

	@Override
	public void events(Main main) {
		Events.listen(main, PlayerDeathEvent.class, event -> {
			Player player = event.getEntity();
			if(!main.getMatchManager().isRunning()) {
				matchManager.getMain().getServer().dispatchCommand(matchManager.getMain().getServer().getConsoleSender(), "spawn " + player.getName());
				player.setHealth(player.getMaxHealth());
				return;
			}
			if(!main.getUserManager().getUsers().containsKey(player)) {
				matchManager.getMain().getServer().dispatchCommand(matchManager.getMain().getServer().getConsoleSender(), "spawn " + player.getName());
				player.setHealth(player.getMaxHealth());
				return;
			}
			User user = main.getUserManager().getUsers().get(player);
			event.setKeepInventory(true);
			if(!lives.containsKey(player)){
				matchManager.getMain().getServer().dispatchCommand(matchManager.getMain().getServer().getConsoleSender(), "spawn " + player.getName());
				player.setHealth(player.getMaxHealth());
				return;
			}
			if(lives.get(player) > 1) lives.put(player, lives.get(player)-1);
			else {
				lives.remove(player);
				user.setCurrentKit(Kit.NONE);
				matchManager.addSpectator(user);
				if(lives.keySet().size() == 1) {
					lives.put(player, 0);
					end();
				}
			}
			for(Player p : Bukkit.getOnlinePlayers())
				updateScoreboard(p);
			Location loc = player.getLocation().clone();
			respawnAnimation.animate(player);
		});
		Events.listen(main, PlayerRespawnEvent.class, event -> {
			Player player = event.getPlayer();
			player.getInventory().clear();
			if(!main.getMatchManager().isRunning()) return;
			if(!main.getUserManager().getUsers().containsKey(player)) return;
			User user = main.getUserManager().getUsers().get(player);
			if(!lives.containsKey(player)) return;
			main.getServer().getScheduler().runTaskLater(main, () -> {
				matchManager.spawnPlayer(user);
			}, 10L);
		});
		Events.listen(main, PlayerInteractEvent.class, event -> {
			if(!main.getMatchManager().isRunning()) return;
			Player player = event.getPlayer();
			if(player.getInventory().getItemInMainHand() == null) return;
			ItemStack hand = player.getInventory().getItemInMainHand();
			if(hand.getType() == Material.COOKED_BEEF && (!hand.hasItemMeta() || !hand.getItemMeta().hasDisplayName())) {
				event.setCancelled(true);
				if(player.getHealth() == player.getMaxHealth()) return;
				if(feedingCooldown.containsKey(player) && feedingCooldown.get(player) > System.currentTimeMillis()) return;
				feedingCooldown.put(player, System.currentTimeMillis() + 100L);
				player.getInventory().removeItem(new ItemStack(Material.COOKED_BEEF));
				double health = player.getMaxHealth() - player.getHealth();
				player.setFireTicks(0);
				player.setHealth(health <= 8?player.getMaxHealth():player.getHealth()+8);
			}
		});
		Events.listen(main, FoodLevelChangeEvent.class, event -> {
			if(event.getEntityType() != EntityType.PLAYER) return;
			if(!matchManager.isRunning()) return;
			event.setFoodLevel(20);
		});
		Events.listen(main, EntityDamageEvent.class, event -> {
			Entity target = event.getEntity();
			if(target.getType() != EntityType.PLAYER) return;
			Player player = (Player) target;
			if(player.getGameMode() != GameMode.SURVIVAL) return;
			if(player.isDead()) return;
			if(!main.getUserManager().getUsers().containsKey(player)) return;
			if(event.getCause() == DamageCause.VOID) {	
				event.setCancelled(true);
				event.setDamage(0);
				User user = main.getUserManager().getUsers().get(player);
				Location loc = player.getLocation().clone();
				loc.setY(2);
				user.incMatchesPlayed();
				player.teleport(loc);
				Bukkit.broadcastMessage(Text.TAG + player.getName() + " killed by " + (user.lastHitter() == null? "natural causes." :	(user.lastHitter().getName() == null ? "a(n) " + user.lastHitter().getType().toString().toLowerCase() : user.lastHitter().getName())));
				PlayerDeathEvent pde = new PlayerDeathEvent(player, null, 0, "");
				Bukkit.getPluginManager().callEvent(pde);
				return;
			}
			if(player.getHealth() - event.getDamage() > 0) return;
			event.setCancelled(true);
			User user = main.getUserManager().getUsers().get(player);
			if(user.lastHitter() != null && user.lastHitter() instanceof Player && lives.containsKey(user.lastHitter()) && ThreadLocalRandom.current().nextInt(5) == 4) {
				((Player)user.lastHitter()).sendMessage(ChatColor.GOLD + "You gained an extra life!");
				lives.put((Player) user.lastHitter(), lives.get(user.lastHitter())+1);
			}
			Bukkit.broadcastMessage(Text.TAG + player.getName() + " killed by " + (user.lastHitter() == null? "natural causes." :	(user.lastHitter().getName() == null ? "a(n) " + user.lastHitter().getType().toString().toLowerCase() : user.lastHitter().getName())));
			PlayerDeathEvent pde = new PlayerDeathEvent(player, null, 0, "");
			Bukkit.getPluginManager().callEvent(pde);
		});
	}

	@Override
	public void start() {
		List<User> users = matchManager.getMain().getUserManager().getUsers().values().stream().filter(user -> user.getSpawnKit() != Kit.NONE).filter(user -> user.getPlayer().isOnline()).collect(Collectors.toList());

		users.stream().forEach(user -> {
			lives.put(user.getPlayer().getPlayer(), 5);
			matchManager.spawnPlayer(user);
		});
		users.stream().forEach(user -> {
			updateScoreboard(user.getPlayer().getPlayer());
		});
		Bukkit.broadcastMessage(Text.trans("&b&lMatch Started! \n&6&oYou each start with 5 lives. Be the last player standing to win!"));
	}


	@Override
	public void updateScoreboard(Player player) {
		List<Player> topPlayers = lives.keySet().stream().sorted((player1, player2) -> Integer.compare(lives.get(player1), lives.get(player2))).collect(Collectors.toList());

		Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective objective = scoreboard.registerNewObjective("Lives", "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(Text.trans("&b&lMC&f&lElim"));

		Team mapInfo = scoreboard.registerNewTeam("mapInfo");
		mapInfo.addEntry(Text.trans("&9&o    Map "));
		objective.getScore(Text.trans("&9&o    Map ")).setScore(3);

		Team mapName = scoreboard.registerNewTeam("mapName");
		mapName.addEntry(Text.trans("&6&l" + matchManager.getArena().getName()));
		objective.getScore(Text.trans("&6&l" + matchManager.getArena().getName())).setScore(2);

		Team blank = scoreboard.registerNewTeam("Blank");
		blank.addEntry(" ");
		objective.getScore(" ").setScore(1);

		Team userInfo = scoreboard.registerNewTeam("UserInfo");
		userInfo.addEntry(Text.trans("&1&9&o    Lives "));
		objective.getScore(Text.trans("&1&9&o    Lives ")).setScore(0);

		int livesLeft = 0;
		if(lives.containsKey(player)) livesLeft = lives.get(player);
		Team playerLives = scoreboard.registerNewTeam("PlayerLives");
		playerLives.addEntry(Text.trans("&2" + player.getName() + ": &d"));
		playerLives.setSuffix("" +livesLeft);
		objective.getScore(Text.trans("&2" + player.getName() + ": &d")).setScore(0);

		for(int count = 3; count < 10; count++)
		{
			if(count-3 >= topPlayers.size()) break;
			Player p = topPlayers.get(count-3);
			if(p.equals(player))
				continue;
			livesLeft = 0;
			if(lives.containsKey(p)) livesLeft = lives.get(p);
			playerLives = scoreboard.registerNewTeam(p.getName() + "n");
			playerLives.addEntry(Text.trans("&" + count + "&c" + p.getName() + ": &d"));
			playerLives.setSuffix("" +livesLeft);
			objective.getScore(Text.trans("&" + count + "&c" + p.getName() + ": &d")).setScore(0);

		}
		player.setScoreboard(scoreboard);

	}

	@Override
	public List<Player> getPlayersAlive() {
		if(matchManager.isRunning())
			return lives.keySet().stream().filter(player -> lives.get(player) >= 0).collect(Collectors.toList());
		return matchManager.getMain().getUserManager().getUsers().values().stream().filter(user -> user.getSpawnKit() != Kit.NONE).filter(user -> user.getPlayer().isOnline()).map(user -> user.getPlayer().getPlayer()).collect(Collectors.toList());
	}

	@Override
	public void end() {
		matchManager.stop();
		for(Player p : Bukkit.getOnlinePlayers()) {
			p.setGameMode(GameMode.SURVIVAL);
			matchManager.getMain().getServer().dispatchCommand(matchManager.getMain().getServer().getConsoleSender(), "spawn " + p.getName());
			p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		}
		Bukkit.broadcastMessage("Match ended");
		if(lives.keySet().size() == 0) return;
		String whoWon = "";
		String whoAlmostWon = "";
		if(lives.keySet().size() < 3) {
			Optional<Integer> maxLivesTest = lives.values().stream().max(Integer::compare);
			matchManager.getEndTimer().stop(matchManager);
			matchManager.getStartTimer().animate(matchManager);
			if(!maxLivesTest.isPresent()) return;
			int maxLives = maxLivesTest.get();
			List<Player> winners = lives.keySet().stream().filter(player -> lives.get(player) == maxLives).collect(Collectors.toList());
			if(winners.size() == 1)
			{
				Player winner = lives.keySet().stream().max((p1, p2) -> Integer.compare(lives.get(p1), lives.get(p2))).get();
				Player runnerup = null;
				if(lives.keySet().size() == 2) runnerup = lives.keySet().stream().min((p1, p2) -> Integer.compare(lives.get(p1), lives.get(p2))).get();
				if(runnerup != null && matchManager.getMain().getUserManager().getUsers().containsKey(runnerup)){
					matchManager.getMain().getUserManager().getUsers().get(runnerup).incRunnerups();
					matchManager.getMain().getUserManager().getUsers().get(runnerup).incMatchesPlayed();
					whoAlmostWon = runnerup.getName();
				}
				if(winner != null && matchManager.getMain().getUserManager().getUsers().containsKey(winner)){
					matchManager.getMain().getUserManager().getUsers().get(winner).incWins();
					matchManager.getMain().getUserManager().getUsers().get(winner).incMatchesPlayed();
					whoWon = winner.getName();
				}
				Bukkit.broadcastMessage(ChatColor.AQUA + "" +  ChatColor.BOLD + "Winners: " + whoWon);
				Bukkit.broadcastMessage(ChatColor.AQUA + "" +  ChatColor.BOLD + "Runnerups: " + whoAlmostWon);
				return;
			}
			else {
				Player winner1 = winners.get(0);
				Player winner2 = winners.get(1);
				matchManager.getMain().getUserManager().getUsers().get(winner1).incRunnerups();
				matchManager.getMain().getUserManager().getUsers().get(winner1).incMatchesPlayed();
				matchManager.getMain().getUserManager().getUsers().get(winner2).incRunnerups();
				matchManager.getMain().getUserManager().getUsers().get(winner2).incMatchesPlayed();
				Bukkit.broadcastMessage(ChatColor.AQUA + "" +  ChatColor.BOLD + "Runnerups: " + winner1.getName() + ", " + winner2.getName());
				return;
			}
		}
		Optional<Integer> maxLivesTest = lives.values().stream().max(Integer::compare);
		if(!maxLivesTest.isPresent()) return;
		int maxLives = maxLivesTest.get();
		List<Player> winners = lives.keySet().stream().filter(player -> lives.get(player) == maxLives).collect(Collectors.toList());
		if(winners.isEmpty()) return;
		if(winners.size() == 1) {
			Player winner = winners.get(0);
			if(winner != null && matchManager.getMain().getUserManager().getUsers().containsKey(winner)){
				matchManager.getMain().getUserManager().getUsers().get(winner).incWins();
				whoWon = winner.getName();
			}
			Optional<Integer> secondMaxLivesTest = lives.values().stream().filter(i -> i < maxLives).max(Integer::compare);
			if(!secondMaxLivesTest.isPresent()) return;
			int secondMaxLives = secondMaxLivesTest.get();
			List<Player> runnerups = lives.keySet().stream().filter(player -> lives.get(player) == secondMaxLives).collect(Collectors.toList());
			if(runnerups.isEmpty()) return;
			whoAlmostWon = runnerups.stream().map(Player::getName).collect(Collectors.joining(", "));
			runnerups.forEach(runnerup -> {
				if(runnerup != null && matchManager.getMain().getUserManager().getUsers().containsKey(runnerup)){
					matchManager.getMain().getUserManager().getUsers().get(runnerup).incRunnerups();
				}
			});
			Bukkit.broadcastMessage(ChatColor.AQUA + "" +  ChatColor.BOLD + "Winners: " + whoWon);
			Bukkit.broadcastMessage(ChatColor.AQUA + "" +  ChatColor.BOLD + "Runnerups: " + whoAlmostWon);
			return;
		}
		whoAlmostWon = winners.stream().map(Player::getName).collect(Collectors.joining(", "));
		winners.forEach(winner -> {
			if(winner != null && matchManager.getMain().getUserManager().getUsers().containsKey(winner)){
				matchManager.getMain().getUserManager().getUsers().get(winner).incWins();
			}
		});
		this.matchManager.getEndTimer().stop(matchManager);
		Bukkit.broadcastMessage(ChatColor.AQUA + "" +  ChatColor.BOLD + "Winners: " + whoWon);
		Bukkit.broadcastMessage(ChatColor.AQUA + "" +  ChatColor.BOLD + "Runnerups: " + whoAlmostWon);
	}

}
