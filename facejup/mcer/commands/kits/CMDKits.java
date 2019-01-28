package facejup.mcer.commands.kits;

import facejup.mcer.commands.Command;
import facejup.mcer.main.Main;

public class CMDKits extends Command {

	public CMDKits(Main main) {
		super(main, "kits");
		this.registerSubCommand("list", new SubList(this));
		this.registerSubCommand("menu", new SubMenu(this));
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "The main kits command.";
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String getPermission() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "kits";
	}

	@Override
	public boolean consoleUse() {
		// TODO Auto-generated method stub
		return true;
	}

}
