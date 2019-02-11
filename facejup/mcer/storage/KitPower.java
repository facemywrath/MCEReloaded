package facejup.mcer.storage;

import java.util.function.Consumer;

import org.bukkit.event.player.PlayerInteractEvent;

public class KitPower {

	private Consumer<PlayerInteractEvent> leftClick;
	private Consumer<PlayerInteractEvent> rightClick;
	private Consumer<PlayerInteractEvent> anyClick;
	
	public KitPower leftClick(Consumer<PlayerInteractEvent> event) {
		this.leftClick = event;
		return this;
	}
	
	public KitPower rightClick(Consumer<PlayerInteractEvent> event) {
		this.rightClick = event;
		return this;
	}
	
	public KitPower anyClick(Consumer<PlayerInteractEvent> event) {
		this.rightClick = event;
		return this;
	}
	
	public boolean hasAnyTrigger() {
		return this.anyClick != null;
	}
	
	public boolean hasLeftTrigger() {
		return this.leftClick != null;
	}
	
	public boolean hasRightTrigger() {
		return this.rightClick != null;
	}
	
	public void triggerAny(PlayerInteractEvent event) {
		this.anyClick.accept(event);
	}
	
	public void triggerLeft(PlayerInteractEvent event) {
		this.leftClick.accept(event);
	}
	
	public void triggerRight(PlayerInteractEvent event) {
		this.rightClick.accept(event);
	}
	
}
