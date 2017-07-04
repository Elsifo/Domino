package it.beyondthecube.domino.data.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

import it.beyondthecube.domino.data.config.MySQLConfig;
import it.beyondthecube.domino.exceptions.DatabaseException;

public class SQLQuery {
	private MySQLConfig msqlc;
	private String query;
	private String[] params;

	public SQLQuery(String query, String[] params, MySQLConfig msqlc) {
		this.msqlc = msqlc;
		this.query = query.replace("£", msqlc.getDBName());
		this.params = params;

	}

	public SQLQuery(String query, MySQLConfig msqlc) {
		this.msqlc = msqlc;
		this.query = query.replace("£", msqlc.getDBName());
		this.params = null;
	}

	private void applyParameters(PreparedStatement ps, String[] params) throws SQLException {
		if (params != null) {
			if (params.length != StringUtils.countMatches(query, "?"))
				throw new SQLException("Insufficient params");
			for (int i = 0; i < params.length; i++) {
				ps.setString(i + 1, params[i]);
			}
		}
	}

	public ResultSet excecuteQuery() throws DatabaseException, SQLException {
		PreparedStatement ps = null;
		if (params == null) {
			ps = (PreparedStatement) msqlc.getGenericConnection().prepareStatement(query);
		} else {
			ps = (PreparedStatement) msqlc.getConnection().prepareStatement(query);
			if (params.length != 0)
				applyParameters(ps, params);
		}
		ResultSet r = ps.executeQuery();
		return r;
	}

	public ResultSet excecuteUpdate() throws SQLException, DatabaseException {
		PreparedStatement ps = null;
		if (params == null) {
			ps = (PreparedStatement) msqlc.getGenericConnection().prepareStatement(query,
					PreparedStatement.RETURN_GENERATED_KEYS);
		} else {
			ps = (PreparedStatement) msqlc.getConnection().prepareStatement(query,
					PreparedStatement.RETURN_GENERATED_KEYS);
			if (params.length != 0)
				applyParameters(ps, params);
		}
		ps.executeUpdate();
		return ps.getGeneratedKeys();
	}

	public String getString() {
		return query;
	}

	public void setParams(String[] params) {
		this.params = params;
	}
}