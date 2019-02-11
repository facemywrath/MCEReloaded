package facejup.mcer.match.matchtypes;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import facejup.mcer.main.Main;
import facejup.mcer.match.MatchManager;
import facejup.mcer.util.Marker;

public abstract class Match {

	MatchManager matchManager;
	private HashMap<Player, Marker<Location>> afkTimer;
	
	public Match(MatchManager matchManager) {
		this.matchManager = matchManager;
		events(matchManager.getMain());
	}
	
	public abstract List<Player> getPlayersAlive();
	
	public abstract void events(Main main);
	
	public abstract void start();

	public abstract void updateScoreboard(Player player);

	public abstract void end();

	public void interrupt() {
		// TODO Auto-generated method stub
		
	}
	
}
