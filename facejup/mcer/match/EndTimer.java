package facejup.mcer.match;

import org.bukkit.Bukkit;

import facejup.mcer.main.Main;
import facejup.mcer.util.Animation;

public class EndTimer extends Animation<MatchManager>{

	private final int ROUND_TIME = 1200;

	int timeUntil = ROUND_TIME;

	public EndTimer(Main main) {
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
				if(getTimeLeft().equals("-1")) return;
				Bukkit.broadcastMessage(getTimeLeft() + " until match ends.");
				timeUntil--;
				return;
			}
			matchManager.getMatch().end();
			this.timeUntil = ROUND_TIME;
			stop(matchManager);
		}, 20L).setLooping(true, 0L);
	}

	private String getTimeLeft() {
		int time = timeUntil;
		if(time >= 60 && time%60 == 0) {
			return time/60 + " minutes";
		}
		if(time < 60 && (time%5==0 || time < 6))
			return time + " seconds";
		return -1 + "";
	}

}
