package facejup.mcer.match;

import java.awt.TextComponent;

import org.bukkit.Bukkit;

import facejup.mcer.main.Main;
import facejup.mcer.util.Animation;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;

public class StartTimer extends Animation<MatchManager> {

	private final int BETWEEN_ROUND_TIME = 20;

	int timeUntil = BETWEEN_ROUND_TIME;

	public StartTimer(Main main) {
		super(main);
		this.addFrame(matchManager -> {
			int playersAlive = matchManager.getMatch().getPlayersAlive().size();
			if(playersAlive < matchManager.getMinimumPlayers()) {
				Bukkit.broadcastMessage("Player left, match start aborted.");
				this.stop(matchManager);
				return;
			}
			if(timeUntil > 0)
			{
				if(timeUntil%5==0 && timeUntil > 5)
					Bukkit.broadcastMessage(ChatColor.YELLOW + "" + timeUntil + " seconds until match starts.");
				if(timeUntil < 6 && timeUntil > 1)
				{
					Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle(timeUntil + "", ""));
					Bukkit.broadcastMessage(ChatColor.GOLD + "" + timeUntil + " seconds until match starts.");
				}
				if(timeUntil == 1)
					Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle(main.getMatchManager().getArena().getName(), "Map Built By: " + main.getMatchManager().getArena().getCreator()));
				timeUntil--;
				return;
			}
			matchManager.startMatch();
			this.timeUntil = BETWEEN_ROUND_TIME;
			stop(matchManager);
		}, 20L).setLooping(true, 0L);
	}


}
