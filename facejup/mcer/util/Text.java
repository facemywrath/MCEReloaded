package facejup.mcer.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

public interface Text {
	
	public static final String MENU_TITLE = trans("&2Kits");
	public static final String TAG = trans("&9&l(&b&l&oMC&f&l&oElim&9&l) &a&o");
	
	public static String trans(String str) { return ChatColor.translateAlternateColorCodes('&', str); }
	
	public static List<String> splitByWords(String string, int wordCount) {
		List<String> ret = new ArrayList<>();
		int i = 0;
		String retAdditive = "&7";
		for(String str : string.split(" ")) {
			if(i < wordCount) {
				i++;
				retAdditive += str + " ";
				continue;
			}
			ret.add(retAdditive);
			i = 0;
			retAdditive = "&7" + str + " ";
		}
		ret.add(retAdditive);
		return ret;
	}

}
