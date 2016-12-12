package it.beyondthecube.domino.data.database;

import java.util.UUID;

import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import it.beyondthecube.domino.Utility;
import it.beyondthecube.domino.data.config.MySQLConfig;
import it.beyondthecube.domino.exceptions.AreaBoundsException;
import it.beyondthecube.domino.exceptions.CityNotFoundException;
import it.beyondthecube.domino.exceptions.DatabaseException;
import it.beyondthecube.domino.politicals.City;
import it.beyondthecube.domino.politicals.Nation;
import it.beyondthecube.domino.politicals.PoliticalManager;
import it.beyondthecube.domino.residents.Resident;
import it.beyondthecube.domino.residents.ResidentManager;
import it.beyondthecube.domino.sets.permission.PermissionSet;
import it.beyondthecube.domino.sets.toggle.ToggleSet;
import it.beyondthecube.domino.terrain.Area;
import it.beyondthecube.domino.terrain.AreaManager;
import it.beyondthecube.domino.terrain.AreaType;
import it.beyondthecube.domino.terrain.ComLocation;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {
	private MySQLConfig msqlc;
	private Game game;
	private static DatabaseManager instance = null;

	private DatabaseManager() {

	}

	public static DatabaseManager getInstance() {
		if (instance == null)
			instance = new DatabaseManager();
		return instance;
	}

	public void setMySQLConfig(MySQLConfig config) {
		msqlc = config;
	}

	public boolean testDB() throws DatabaseException {
		try {
			ResultSet r = msqlc.getGenericConnection().getMetaData().getCatalogs();
			while(r.next()) {
				if(r.getString(1).equals(msqlc.getDBName())) return true;
			}
			return false;
		} catch (SQLException e) {
			throw new DatabaseException(null, e, "SQL Error");
		}
	}
	
	public void attemptUpdate(int vold) throws DatabaseException {
		switch (vold) {
		case 1: {
			return;
		}
		case 2: {
			SQLQuery q = new SQLQuery(
					"CREATE TABLE £.`friend`(`id` int(11) NOT NULL AUTO_INCREMENT, `resident` varchar(36) NOT NULL, `friend` varchar(36) NOT NULL, PRIMARY KEY (`id`), KEY `fkresfriend` (`resident`), KEY `fkfriend` (`friend`), CONSTRAINT `fkfriend` FOREIGN KEY (`friend`) REFERENCES £.`resident` (`idresident`) ON DELETE NO ACTION ON UPDATE CASCADE, CONSTRAINT `fkresfriend` FOREIGN KEY (`resident`) REFERENCES £.`resident`(`idresident`) ON DELETE NO ACTION ON UPDATE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8",
					null, msqlc);
			try {
				q.excecuteUpdate();
			} catch (SQLException e) {
				throw new DatabaseException(q, e, "SQL Error");
			}
			return;
		}
		case 3: {
			SQLQuery q = new SQLQuery(
					"ALTER TABLE £.area ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'NONE' AFTER perms;", null, msqlc);
			try {
				q.excecuteUpdate();
				q = new SQLQuery("ALTER TABLE £.city ADD COLUMN spawn INT DEFAULT NULL", null, msqlc);
				q.excecuteUpdate();
				q = new SQLQuery(
						"ALTER TABLE £.city ADD CONSTRAINT fkspawn FOREIGN KEY(spawn) REFERENCES £.location(idlocation) on update cascade on delete no action",
						null, msqlc);
				q.excecuteUpdate();
			} catch (SQLException e) {
				throw new DatabaseException(q, e, "SQL Error");
			}
			return;
		}
		case 4: {
			SQLQuery q = new SQLQuery("ALTER TABLE £.city CHANGE COLUMN balance tax DOUBLE NOT NULL DEFAULT 0", null,
					msqlc);
			try {
				q.excecuteUpdate();
				q = new SQLQuery("ALTER TABLE £.nation CHANGE COLUMN balance tax DOUBLE NOT NULL DEFAULT 0", null,
						msqlc);
				q.excecuteUpdate();
				q = new SQLQuery("ALTER TABLE £.area ADD COLUMN tax DOUBLE NOT NULL DEFAULT 0", null, msqlc);
				q.excecuteUpdate();
			} catch (SQLException e) {
				throw new DatabaseException(q, e, "SQL Error");
			}
		}
		}
	}

	public int createCity(String name, Resident mayor, Nation n, boolean iscapital, Location<World> spawn)
			throws DatabaseException, CityNotFoundException {
		String[] params0 = { String.valueOf(spawn.getBlockX()), String.valueOf(spawn.getBlockY()),
				String.valueOf(spawn.getBlockZ()), game.getServer().getDefaultWorldName() };
		SQLQuery q = new SQLQuery("insert into £.location(x,y,z,world) values(?,?,?,?);", params0, msqlc);
		try {
			ResultSet r = q.excecuteUpdate();
			if (!r.first())
				throw new DatabaseException(q, null, "SQL exception");
			String[] params = { name, mayor.getPlayer().toString(), String.valueOf(n.getID()), String.valueOf(0),
					(new PermissionSet()).toString(), String.valueOf(r.getInt(1)) };
			q = new SQLQuery("insert into £.city (name,mayor,nation,tax,perms,spawn) values(?,?,?,?,?,?);", params,
					msqlc);
			r = q.excecuteUpdate();
			if (r.first()) {
				int id = r.getInt(1);
				String[] params2 = { String.valueOf(n.getID()), String.valueOf(id), String.valueOf(iscapital ? 1 : 0) };
				q = new SQLQuery("insert into £.citynation(fknation,fkcity,iscapital) values(?,?,?);", params2, msqlc);
				q.excecuteUpdate();
				return id;
			} else {
				throw new CityNotFoundException(null);
			}
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "Syntax error");
		}
	}

	public void createResident(User p) throws DatabaseException {
		SQLQuery q = null;
		try {
			String[] params = { p.getUniqueId().toString(), p.getName() };
			q = new SQLQuery("insert into £.resident(idresident,nick) values(?,?);", params, msqlc);
			q.excecuteUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL exception");
		}

	}

	public boolean isResidentPresent(UUID u) throws DatabaseException {
		String[] params = { u.toString() };
		SQLQuery q = new SQLQuery("select idresident from £.resident where idresident like ?;", params, msqlc);
		ResultSet r;
		try {
			r = q.excecuteQuery();
			return r.first();
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL exception");
		}
	}

	public void loadResidents() throws DatabaseException {
		SQLQuery q = new SQLQuery("select * from £.resident;", null, msqlc);
		try {
			ResultSet r = q.excecuteQuery();
			if (r.first()) {
				do {
					ResidentManager.loadResident(UUID.fromString(r.getString(1)), r.getString(2), null, false);
				} while (r.next());
			}
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL Error");
		}

	}

	public void loadAreas() throws DatabaseException {
		SQLQuery q = new SQLQuery(
				"select idarea,l1.x as a1x,l1.y as a1y, l1.z as a1z, l1.world as a1world, l2.x as a2x, l2.y as a2y, l2.z as a2z, l2.world as a2world,owner,c.idcity,isplotclaim,a.perms,a.type,l1.idlocation,l2.idlocation,a.tax,a.saleprice from (((£.area a join £.location l1 on a.angle1=l1.idlocation) join £.location l2 on a.angle2=l2.idlocation) join £.areacity ac on a.idarea=ac.fkarea) join £.city c on c.idcity=ac.fkcity;",
				null, msqlc);
		ResultSet r;
		try {
			r = q.excecuteQuery();
			if (r.first()) {
				do {
					String owner = r.getString(10);
					ComLocation angle1 = new ComLocation(r.getInt(15),
							Sponge.getServer().getWorld(r.getString(5)).get(), r.getInt(2), r.getInt(3), r.getInt(4));
					ComLocation angle2 = new ComLocation(r.getInt(16),
							Sponge.getServer().getWorld(r.getString(9)).get(), r.getInt(6), r.getInt(7), r.getInt(8));
					PermissionSet pset = Utility.parsePermissions(r.getString(13));
					AreaManager.loadArea(UUID.fromString(r.getString(1)), angle1, angle2,
							(owner == null ? null : ResidentManager.getResident(UUID.fromString(owner))),
							PoliticalManager.getCity(r.getInt(11)), pset, r.getBoolean(12),
							AreaType.valueOf(r.getString(14)), r.getDouble(17), r.getDouble(18));
				} while (r.next());
			}
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL Error");
		} catch (AreaBoundsException e) {
			// TODO Shouldn't happen
			e.printStackTrace();
			Area a = e.getCollidingArea();
			Sponge.getServer().getConsole().sendMessage(Text.of("Area thatt generated the error: " + a.getID()));
		}
	}

	public void loadCities() throws DatabaseException {
		SQLQuery q = null;
		try {
			q = new SQLQuery(
					"select idcity,c.name,mayor,c.tax,idnation,iscapital,perms,spawn,c.toggles from (£.city c join £.citynation cn on c.idcity=cn.fkcity) join £.nation n on n.idnation=cn.fknation;",
					null, msqlc);
			ResultSet r = q.excecuteQuery();
			if (r.first()) {
				do {
					String[] params = { String.valueOf(r.getInt(8)) };
					q = new SQLQuery("select x,y,z,world from £.location where idlocation=?", params, msqlc);
					ResultSet rspawn = q.excecuteQuery();
					Location<World> spawn = null;
					if (rspawn.first()) {
						Utility.sendConsole("Attempting to load spawn for world: "+rspawn.getString("world"));
						spawn = new Location<World>(Sponge.getServer().getWorld(rspawn.getString(4)).get(),
								rspawn.getDouble(1), rspawn.getDouble(2), rspawn.getDouble(3));
					} else
						Sponge.getServer().getBroadcastChannel()
								.send(Text.of(Utility.errorMessage(r.getString(2) + " has no spawn point.")));
					Nation n = PoliticalManager.getNationFromDBID(r.getInt(5));
					PermissionSet pset = Utility.parsePermissions(r.getString(7));
					City c = PoliticalManager.loadCity(r.getInt(1), r.getString(2),
							ResidentManager.getResident(UUID.fromString(r.getString(3))), n, pset,
							(spawn == null) ? null : (new ComLocation(rspawn.getInt(1), spawn)), r.getDouble(4),
							new ToggleSet(r.getString(9)));
					if (n != null) {
						if (r.getBoolean(6))
							n.setCapital(c);
					}
				} while (r.next());
			}
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL Error");
		}
	}

	public void loadCityResidents() throws DatabaseException {
		SQLQuery q = new SQLQuery(
				"SELECT idresident, idcity, isassistant FROM (£.resident r join £.cityresident cr on r.idresident=cr.resident) join £.city c on cr.city=c.idcity;",
				null, msqlc);
		try {
			ResultSet r = q.excecuteQuery();
			if (r.first()) {
				do {
					City c = PoliticalManager.getCity(r.getInt(2));
					Resident res = ResidentManager.getResident(UUID.fromString(r.getString(1)));
					ResidentManager.setCity(res, c);
					if (r.getBoolean(3))
						PoliticalManager.loadAssistant(res, c);
				} while (r.next());
			}
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL error");
		} catch (CityNotFoundException e) {
			e.printStackTrace();
		} // TODO ?!?!?
	}

	public void loadNations() throws DatabaseException {
		SQLQuery q = new SQLQuery("select * from £.nation;", null, msqlc);
		ResultSet r;
		try {
			r = q.excecuteQuery();
			if (r.first()) {
				do {
					PoliticalManager.loadNation(r.getInt(1), r.getString(2), r.getDouble(3));
				} while (r.next());
			}
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL Error");
		}
	}

	public int createNation(String name) throws DatabaseException {
		String[] params = { name };
		SQLQuery q = new SQLQuery("insert into £.nation(name,tax) values(?,0);", params, msqlc);
		try {
			ResultSet r = q.excecuteUpdate();
			if (r.first())
				return r.getInt(1);
			throw new DatabaseException(q, null, "Unknown error during nation creation");
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL Error");
		}
	}

	public ComLocation[] newArea(UUID id, Location<World> angle1, Location<World> angle2, Resident owner, City c,
			PermissionSet pset, boolean ipc, AreaType type, double tax) throws DatabaseException {
		String[] params = { String.valueOf(angle1.getBlockX()), String.valueOf(angle1.getBlockY()),
				String.valueOf(angle1.getBlockZ()), game.getServer().getDefaultWorldName() };
		SQLQuery q = new SQLQuery("insert into £.location(x,y,z,world) values(?,?,?,?);", params, msqlc);
		try {
			ResultSet r1 = q.excecuteUpdate();
			String[] params2 = { String.valueOf(angle2.getBlockX()), String.valueOf(angle2.getBlockY()),
					String.valueOf(angle2.getBlockZ()), angle2.getExtent().getName() };
			q = new SQLQuery("insert into £.location(x,y,z,world) values(?,?,?,?);", params2, msqlc);
			ResultSet r2 = q.excecuteUpdate();
			if (r1.first() && r2.first()) {
				int idl1 = r1.getInt(1);
				int idl2 = r2.getInt(1);
				ComLocation[] angles = { new ComLocation(idl1, angle1), new ComLocation(idl2, angle2) };
				String auid = (id == null) ? UUID.randomUUID().toString() : id.toString();
				String[] params3 = { auid, String.valueOf(idl1), String.valueOf(idl2),
						((owner == null) ? null : owner.getPlayer().toString()), pset.toString(), type.toString() };
				q = new SQLQuery("insert into £.area(idarea,angle1,angle2,owner,perms,type) values(?,?,?,?,?,?)",
						params3, msqlc);
				q.excecuteUpdate();
				String[] params4 = { auid, String.valueOf(c.getID()), String.valueOf((ipc) ? 1 : 0) };
				q = new SQLQuery("insert into £.areacity(fkarea,fkcity,isplotclaim) values(?,?,?)", params4, msqlc);
				q.excecuteUpdate();
				return angles;
			} else
				throw new DatabaseException(q, null, "SQL error");
		} catch (Exception e) {
			throw new DatabaseException(q, e, "SQL error");
		}
	}

	public void addResident(Resident r, City c) throws DatabaseException {
		String[] params = { r.getPlayer().toString(), String.valueOf(c.getID()) };
		SQLQuery q = new SQLQuery("insert into £.cityresident(resident,city) values(?,?);", params, msqlc);
		try {
			q.excecuteUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL Error");
		}
	}

	public void removeResident(Resident r, City c) throws DatabaseException {
		String[] params = { r.getPlayer().toString() };
		SQLQuery q = new SQLQuery("delete from £.cityresident where resident like ?;", params, msqlc);
		try {
			q.excecuteUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL Error");
		}
	}

	public void setOwner(Area a, Resident r) throws DatabaseException {
		String[] params = { (r == null) ? null : r.getPlayer().toString(), a.getID().toString() };
		SQLQuery q = new SQLQuery("update £.area set owner=? where idarea=?;", params, msqlc);
		try {
			q.excecuteUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL Error");
		}
	}

	public void addAssistant(Resident r) throws DatabaseException {
		String[] params = { r.getPlayer().toString() };
		SQLQuery q = new SQLQuery("update £.cityresident set isassistant=1 where resident=?;", params, msqlc);
		try {
			q.excecuteUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL Error");
		}
	}

	public void removeAssistant(Resident r) throws DatabaseException {
		String[] params = { r.getPlayer().toString() };
		SQLQuery q = new SQLQuery("update £.cityresident set isassistant=0 where resident=?;", params, msqlc);
		try {
			q.excecuteUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL Error");
		}
	}

	public void setPermissions(Area a, PermissionSet pset) throws DatabaseException {
		String[] params = { pset.toString(), a.getID().toString() };
		SQLQuery q = new SQLQuery("update £.area set perms=? where idarea=?;", params, msqlc);
		try {
			q.excecuteUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL Error");
		}
	}

	public void setPermissions(City c, PermissionSet pset) throws DatabaseException {
		String[] params = { pset.toString(), String.valueOf(c.getID()) };
		SQLQuery q = new SQLQuery("update £.city set perms=? where idcity=?;", params, msqlc);
		try {
			q.excecuteUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL Error");
		}
	}

	public void removeArea(Area a) throws DatabaseException {
		Location<World> angle1 = a.getAngle1().getLocation();
		Location<World> angle2 = a.getAngle2().getLocation();
		String[] params = { String.valueOf(angle1.getBlockX()), String.valueOf(angle1.getBlockY()),
				String.valueOf(angle1.getBlockZ()), String.valueOf(angle1.getExtent()) };
		String[] params2 = { String.valueOf(angle2.getBlockX()), String.valueOf(angle2.getBlockY()),
				String.valueOf(angle2.getBlockZ()), String.valueOf(angle2.getExtent()) };
		SQLQuery q = new SQLQuery("delete from £.location where x=? and y=? and z=? and world=?;", params, msqlc);
		try {
			q.excecuteUpdate();
			q.setParams(params2);
			q.excecuteUpdate();
			String[] params3 = { a.getID().toString() };
			q = new SQLQuery("delete from £.areacity where fkarea=?", params3, msqlc);
			q.excecuteUpdate();
			q = new SQLQuery("delete from £.area where idarea=?", params3, msqlc);
			q.excecuteUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "");
		}
	}

	public void loadFriends() throws DatabaseException {
		SQLQuery q = new SQLQuery(
				"select r.idresident as res, rf.idresident as fri from (£.friend f join £.resident r on f.resident=r.idresident) join £.resident rf on f.friend=rf.idresident;",
				null, msqlc);
		try {
			ResultSet r = q.excecuteQuery();
			if (r.first()) {
				do {
					ResidentManager.setFriend(ResidentManager.getResident(UUID.fromString(r.getString(1))),
							ResidentManager.getResident(UUID.fromString(r.getString(2))), true);
				} while (r.next());
			}
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL error");
		}
	}

	public void setFriend(Resident r, Resident friend) throws DatabaseException {
		String[] params = { r.getPlayer().toString(), friend.getPlayer().toString() };
		SQLQuery q = new SQLQuery("insert into £.friend(resident,friend) values(?,?);", params, msqlc);
		try {
			q.excecuteUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL Error");
		}
	}

	public void removeFriend(Resident r, Resident friend) throws DatabaseException {
		String[] params = { r.getPlayer().toString(), friend.getPlayer().toString() };
		SQLQuery q = new SQLQuery("delete from £.friend where resident=? and friend=?;", params, msqlc);
		try {
			q.excecuteUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL Error");
		}
	}

	public void setMayor(City city, Resident resident) throws DatabaseException {
		String[] params = { resident.getPlayer().toString(), String.valueOf(city.getID()) };
		SQLQuery q = new SQLQuery("update £.city set mayor=? where idcity=?", params, msqlc);
		try {
			q.excecuteUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL Error");
		}
	}

	public void setAreaType(Area a, AreaType type) throws DatabaseException {
		String[] params = { type.toString(), a.getID().toString() };
		SQLQuery q = new SQLQuery("update £.area set type=? where idarea=?;", params, msqlc);
		try {
			q.excecuteUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL Error");
		}
	}

	public void unclaim(City city, Area a) throws DatabaseException {
		String[] params = { a.getID().toString() };
		SQLQuery q = new SQLQuery("delete from £.areacity where fkarea=?", params, msqlc);
		try {
			q.excecuteUpdate();
			String[] params2 = { a.getID().toString() };
			q = new SQLQuery("delete from £.area where idarea=?", params2, msqlc);
			q.excecuteUpdate();
			String[] params3 = { String.valueOf(a.getAngle1().getID()), String.valueOf(a.getAngle2().getID()) };
			q = new SQLQuery("delete from £.location where idlocation=? and idlocation=?;", params3, msqlc);
			q.excecuteUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL error");
		}
	}

	public void setTax(City c, double d) throws DatabaseException {
		String[] params = { String.valueOf(d), String.valueOf(c.getID()) };
		SQLQuery q = new SQLQuery("update £.city set tax=? where idcity=?", params, msqlc);
		try {
			q.excecuteUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL Error");
		}
	}

	public void setTax(Area a, double d) throws DatabaseException {
		String[] params = { String.valueOf(d), String.valueOf(a.getID()) };
		SQLQuery q = new SQLQuery("update £.area set tax=? where idarea=?", params, msqlc);
		try {
			q.excecuteUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL Error");
		}
	}

	public void setToggles(City c) throws DatabaseException {
		String[] params = { c.getToggles().toString(), String.valueOf(c.getID()) };
		SQLQuery q = new SQLQuery("update £.city set toggles=? where idcity=?;", params, msqlc);
		try {
			q.excecuteUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL error");
		}
	}

	public void setNation(City c, Nation n) throws DatabaseException {
		String[] params = { String.valueOf(c.getID()), String.valueOf(n.getID()) };
		SQLQuery q = new SQLQuery("update £.citynation set fknation=? where fkcity=?", params, msqlc);
		try {
			q.excecuteUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL error");
		}
	}

	public void setSalePrice(Area a, double d) throws DatabaseException {
		String[] params = { String.valueOf(d), String.valueOf(a.getID()) };
		SQLQuery q = new SQLQuery("update £.area set saleprice=? where idarea=?", params, msqlc);
		try {
			q.excecuteUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL error");
		}
	}

	public void setTax(Nation n, double am) throws DatabaseException {
		String[] params = { String.valueOf(am), String.valueOf(n.getID()) };
		SQLQuery q = new SQLQuery("update £.nation set tax=? where idnation=?", params, msqlc);
		try {
			q.excecuteUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(q, e, "SQL error");
		}
	}

	public int getLocationId(Location<World> l) throws DatabaseException {
		String[] params = { String.valueOf(l.getBlockX()), String.valueOf(l.getBlockY()),
				String.valueOf(l.getBlockZ()) };
		SQLQuery q = new SQLQuery("select idlocation from £.location where x=? and y=? and z=?", params, msqlc);
		try {
			ResultSet r = q.excecuteQuery();
			return r.getInt(0);
		} catch (SQLException ex) {
			throw new DatabaseException(q, ex, null);
		}
	}

	public void updateResidentNickname(User u) throws DatabaseException {
		String[] params = { u.getName(), u.getUniqueId().toString() };
		SQLQuery q = new SQLQuery("update £.resident set nick=? where idresident=?", params, msqlc);
		try {
			q.excecuteUpdate();
		} catch (SQLException ex) {
			throw new DatabaseException(q, ex, null);
		}
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public void createDatabase() throws DatabaseException {
		try {
			MySQLCreate.createDB(msqlc);
		} catch (SQLException e) {
			throw new DatabaseException(null, e, null);
		}		
	}
}