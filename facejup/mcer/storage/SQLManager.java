package facejup.mcer.storage;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.stream.Collectors;

import facejup.mcer.achievements.Achievement;
import facejup.mcer.users.User;

public class SQLManager {

	private String host;
	private String user;
	private String pass;
	private String database;
	private int port;

	private Connection connection;

	public SQLManager(String host, String user, String pass, String database, int port) {
		this.host = host;
		this.user = user;
		this.pass = pass;
		this.database = database;
		this.port = port;
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
		this.createColumnIfNotExists("uuid", "text");
		this.createColumnIfNotExists("coins", "int(11)");
		this.createColumnIfNotExists("kits", "text");
		this.createColumnIfNotExists("kills", "int(11)");
		this.createColumnIfNotExists("deaths", "int(11)");
		Achievement.getValues().forEach(ach -> this.createColumnIfNotExists(ach.getName().toLowerCase(), "int(11)"));
		return true;
	}

	public String getString(UUID uuid, String column) {
		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery("SELECT * FROM users WHERE UUID = '" + uuid.toString() + "'");
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
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery("SELECT * FROM users WHERE UUID = '" + uuid.toString() + "'");
			while(result.next()){
				return result.getInt(column);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public void saveUser(User user) {
		try {
			Statement statement = connection.createStatement();
			UUID uuid = user.getPlayer().getUniqueId();
			this.updateString(uuid, "kits", user.getUnlockedKits().stream().collect(Collectors.joining(":")));
			this.updateInt(uuid, "coins", user.getCoins());
			this.updateInt(uuid, "kills", user.getKills());
			this.updateInt(uuid, "deaths", user.getDeaths());
			if(!Achievement.getValues().isEmpty()) {
				Achievement.getValues().forEach(ach -> updateInt(uuid, ach.getName(), user.getAchievements().get(ach)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void createSection(UUID uuid) {
		if(getString(uuid, "uuid") != null) { return; }
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate("INSERT INTO users (uuid,kits,coins,kills,deaths) VALUES ('" + uuid.toString() + "','WARRIOR:ARCHER:GUARD',0,0,0)");
			if(!Achievement.getValues().isEmpty()) {
				Achievement.getValues().forEach(ach -> updateInt(uuid, ach.getName(), 0));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateString(UUID uuid, String column, String value) {
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate("INSERT INTO users (" + column + ") VALUES ('" + value + "') WHERE uuid = '" + uuid.toString() + "' ON DUPLICATE KEY UPDATE " + column + "='" + value + "'");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateInt(UUID uuid, String column, int value) {
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate("INSERT INTO users (" + column.toLowerCase() + ") VALUES (" + value + ") WHERE uuid = '" + uuid.toString() + "' ON DUPLICATE KEY UPDATE " + column + "=" + value);
		} catch (SQLException e) {
			e.printStackTrace();
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

}
