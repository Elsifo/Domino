package it.beyondthecube.domino.data.config;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;

import it.beyondthecube.domino.exceptions.DatabaseException;

import java.sql.Connection;

public class MySQLConfig {
	MySQLConfig msqlc;
	private String host;
	private String user;
	private String pass;
	private String dbname;
	private java.sql.Connection conn;
	private char alias;

	public MySQLConfig() {
		String[] conf = PluginConfig.getMySQLConfiguration();
		host = conf[0];
		user = conf[1];
		pass = conf[2];
		dbname = conf[3];
		alias = conf[4].charAt(0);
	}

	public Connection getConnection() throws DatabaseException {
		if (conn == null) {
			try {
				conn = DriverManager.getConnection("jdbc:mysql://" + host + "/" + dbname, user, pass);
			} catch (SQLException e) {
				throw new DatabaseException(null, e, dbname);
			} 
		}
		return conn;
	}
	
	public Connection getGenericConnection() throws DatabaseException {
		try {
			return DriverManager.getConnection("jdbc:mysql://" + host, user, pass);
		} catch (SQLException e) {
				throw new DatabaseException(null, e, dbname);
		} 
	}
	
	public String getDBName() {
		return dbname;
	}

	public char getChar() {
		return alias;
	}
}
