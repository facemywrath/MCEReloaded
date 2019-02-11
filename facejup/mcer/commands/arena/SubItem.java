package facejup.mcer.commands.arena;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import facejup.mcer.commands.Command;
import facejup.mcer.commands.SubCommand;
import facejup.mcer.storage.ItemSpawn;
import facejup.mcer.util.Events;

public class SubItem extends SubCommand{

	private HashMap<Player, ItemSpawn> placing = new HashMap<>();
	
	public SubItem(Command parent) {
		super(parent);
		Events.listen(parent.getMain(), BlockPlaceEvent.class, event -> {
			if(!placing.containsKey(event.getPlayer())) return;
			Player player = event.getPlayer();
			event.setCancelled(true);
			placing.get(player).addLocation(event.getBlock().getLocation().add(new Vector(0,1,0))).save(parent.getMain());
			placing.remove(player);
			parent.getMain().getMatchManager().getArena().loadArena(parent.getMain());
			player.sendMessage("Item spawn placed");
		});
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		if(player.getInventory().getItemInMainHand() == null) return;
		ItemStack hand = player.getInventory().getItemInMainHand();
		if(args.length == 1) {
			player.sendMessage("Must specify a cooldown time in ticks.");
			return;
		}
		if(!StringUtils.isNumeric(args[1])) {
			player.sendMessage("Must specify a numerical value in ticks for the cooldown.");
			return;
		}
		Long cooldown = Long.parseLong(args[1]);
		placing.put(player, new ItemSpawn(hand, cooldown));
		player.sendMessage("Good! Now place a block where you want it to go");
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Set an item spawn point in the arena. Not finished";
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return "(cooldown-in-seconds)";
	}

	@Override
	public String getPermission() {
		// TODO Auto-generated method stub
		return "mce.arena.items";
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "item";
	}

	@Override
	public boolean consoleUse() {
		// TODO Auto-generated method stub
		return false;
	}

}
