package facejup.mcer.listeners;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import facejup.mcer.main.Main;
import facejup.mcer.util.Events;

public class UserCacheListener {
	
	
	// Stored inside Main.java

	public UserCacheListener(Main main) {
		
		
		Events.listen(main, PlayerJoinEvent.class, event -> {
			Player player = event.getPlayer();
			main.getSqlManager().createSection(player.getUniqueId());
			if(Bukkit.getOnlinePlayers().size() > main.getUserManager().max_users) {
				for(OfflinePlayer offlineplayer : main.getUserManager().getUsers().descendingKeySet()) {
					if(!offlineplayer.isOnline()){
						main.getUserManager().getUsers().get(offlineplayer).remove();
						main.getUserManager().getUsers().remove(offlineplayer);
						break;
					}
				};
			}
			if(main.getUserManager().getUsers().size() > main.getUserManager().max_users) {
				player.kickPlayer("This lobby is full.");
				return;
			}
			main.getUserManager().addUser(player);
		});
		
		
		Events.listen(main, PlayerQuitEvent.class, event -> {
			Player player = event.getPlayer();
			if(main.getUserManager().getUsers().containsKey(player)) {
				main.getSqlManager().saveUser(main.getUserManager().getUsers().get(player));
				main.getUserManager().removeUser(player);
				if(main.getMatchManager().isRunning()) {
					for(Player p : Bukkit.getOnlinePlayers())
						main.getMatchManager().getMatch().updateScoreboard(p);
				}
			}
		});
	}

}
