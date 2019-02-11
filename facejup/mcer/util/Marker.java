package facejup.mcer.util;

public class Marker<T> {

	private long time = System.currentTimeMillis();
	private T value;
	
	public Marker(T value) {
		this.value = value;
	}
	
	public long getTimeSince() {
		return System.currentTimeMillis() - time;
	}
	
	public T getValue() {
		return this.value;
	}
	
}
