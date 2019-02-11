package facejup.mcer.storage;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;

import facejup.mcer.achievements.Achievement;
import facejup.mcer.main.Main;
import facejup.mcer.users.User;

public class SQLManager {

	private Main main;
	
	private String host;
	private String user;
	private String pass;
	private String database;
	private int port;

	private Connection connection;

	public SQLManager(Main main, String host, String user, String pass, String database, int port) {
		this.host = host;
		this.user = user;
		this.pass = pass;
		this.database = database;
		this.port = port;
		this.main = main;
	}

	public boolean openConnection() {
		try {
			if(connection != null && !connection.isClosed()) {
				return false;
			}
			synchronized (this) {
				if(connection != null && !connection.isClosed()) {
					return false;
				}
				connection = DriverManager.getConnection("jdbc:mysql://" + this.host+ ":" + this.port + "/" + this.database, this.user, this.pass);
			}
		} catch (SQLException e) {
			return false;
		}
		this.createTableIfNotExists();
		Achievement.getValues().forEach(ach -> this.createColumnIfNotExists(ach.getName().toLowerCase(), "int(11)"));
		return true;
	}

	public String getString(UUID uuid, String column) {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE UUID = '" + uuid.toString() + "'");
			ResultSet result = statement.executeQuery();
			while(result.next()){
				return result.getString(column);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public int getInt(UUID uuid, String column) {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE UUID = '" + uuid.toString() + "'");
			ResultSet result = statement.executeQuery();
			while(result.next()){
				return result.getInt(column);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public void saveUser(User user) {
		UUID uuid = user.getPlayer().getUniqueId();
		this.update(uuid, "kits", "'" + user.getUnlockedKits().stream().collect(Collectors.joining(":")) + "'");
		this.update(uuid, "coins", user.getCoins() + "");
		this.update(uuid, "kills", user.getKills() + "");
		this.update(uuid, "deaths", user.getDeaths() + "");
		this.update(uuid, "wins", user.getWins() + "");
		this.update(uuid, "runnerups", user.getRunnerups() + "");
		this.update(uuid, "played", user.getPlayed() + "");
		if(!Achievement.getValues().isEmpty()) {
			Achievement.getValues().forEach(ach -> update(uuid, ach.getName().toLowerCase(), user.getAchievements().get(ach) + ""));
		}
	}

	public void createSection(UUID uuid) {
		if(getString(uuid, "uuid") != null) { return; }
		try {
			PreparedStatement statement = connection.prepareStatement("INSERT INTO users (uuid,kits,coins,kills,deaths,runnerups,wins,played) VALUES ('" + uuid.toString() + "','WARRIOR:ARCHER:GUARD',0,0,0,0,0,0)");
			statement.executeUpdate();
			if(!Achievement.getValues().isEmpty()) {
				Achievement.getValues().forEach(ach -> update(uuid, ach.getName().toLowerCase(), "0"));
			}
		} catch (SQLException e) {
			System.out.println("Could not create MySQL section for " + Bukkit.getOfflinePlayer(uuid).getName());
		}
	}
	public void update(UUID uuid, String column, String value) {
		try {
			PreparedStatement statement = connection.prepareStatement("UPDATE users SET " + column + " = " + value + " WHERE uuid = '" + uuid.toString() + "'");
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Could not update MySQL int column " + column + " for " + Bukkit.getOfflinePlayer(uuid).getName());
		}
	}

	private void createColumnIfNotExists(String column, String valueType) {
		try {
			DatabaseMetaData md;
			md = connection.getMetaData();
			ResultSet rs = md.getColumns(null, null, "users", column);
			if (rs.next()) {
				return;
			}
			connection.prepareStatement("ALTER TABLE users ADD " + column.toLowerCase() + " " + valueType).executeUpdate();
		} catch (SQLException e) {
			System.out.println("Could not create column " + column.toLowerCase());
		}
	}

	private void createTableIfNotExists() {
		try {
			DatabaseMetaData md;
			md = connection.getMetaData();
			ResultSet rs = md.getColumns(null, null, "users", null);
			if (rs.next()) {
				return;
			}
			connection.prepareStatement("CREATE TABLE mcer.users (uuid text,coins int(11),kits text,kills int(11),deaths int(11),wins int(11),runnerups int(11),played int(11));").executeUpdate();
		} catch (SQLException e) {
			System.out.println("Could not create users table");
		}
	}

	public Main getMain() {
		return main;
	}

}
