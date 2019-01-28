package facejup.mcer.achievements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface Achievement {
	
	public static List<Achievement> getValues() {
		return Arrays.asList(SLAYER);
	}
	
	public static final Achievement SLAYER = () -> { return "Slayer"; };
	
	public String getName();

}
