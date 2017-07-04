package it.beyondthecube.domino.data.database;

import java.sql.SQLException;

import it.beyondthecube.domino.data.config.MySQLConfig;
import it.beyondthecube.domino.exceptions.DatabaseException;

public class MySQLCreate {
	public static void createDB(MySQLConfig msqlc) throws SQLException, DatabaseException {
		SQLQuery q = new SQLQuery("create database `" + msqlc.getDBName() + "`;", msqlc);
		q.excecuteUpdate();
		String query = "CREATE TABLE `nation` ( " +
						"`idnation` int(11) NOT NULL AUTO_INCREMENT," +
						"`name` varchar(45) NOT NULL," +
						"`tax` int(11) DEFAULT NULL," +
						"PRIMARY KEY (`idnation`)" +
						") ENGINE=InnoDB";
		q = new SQLQuery(query, new String[]{}, msqlc);
		q.excecuteUpdate();
		query = "CREATE TABLE `location` (" +
				"`idlocation` int(11) NOT NULL AUTO_INCREMENT," +
				"`x` int(11) NOT NULL," +
				"`y` int(11) NOT NULL," +
				"`z` int(11) NOT NULL," +
				"`world` varchar(45) NOT NULL," +
				"PRIMARY KEY (`idlocation`)" +
				") ENGINE=InnoDB";
		q = new SQLQuery(query, new String[]{}, msqlc);
		q.excecuteUpdate();
		query = "CREATE TABLE `resident` (" +
				 "`idresident` varchar(45) NOT NULL," +
				 "`nick` varchar(45) NOT NULL," +
				 "PRIMARY KEY (`idresident`)" +
				 ") ENGINE=InnoDB";
		q = new SQLQuery(query, new String[]{}, msqlc);
		q.excecuteUpdate();
		query = "CREATE TABLE `area` (" +
				"`idarea` varchar(45) NOT NULL," +
				"`owner` varchar(45) DEFAULT NULL," +
				"`perms` varchar(256) NOT NULL," +
				"`type` varchar(45) NOT NULL," +
				"`tax` int(11) DEFAULT NULL," +
				"`angle1` int(11) DEFAULT NULL," +
				"`angle2` int(11) DEFAULT NULL," +
				"`saleprice` varchar(45) NOT NULL DEFAULT '0'," +
				"PRIMARY KEY (`idarea`)," +
				"KEY `fkowner_idx` (`owner`)," +
				"KEY `fkloca1_idx` (`angle1`)," +
				"KEY `fkloca2_idx` (`angle2`)," +
				"CONSTRAINT `fkloca1` FOREIGN KEY (`angle1`) REFERENCES `location` (`idlocation`) ON DELETE NO ACTION ON UPDATE NO ACTION," +
				"CONSTRAINT `fkloca2` FOREIGN KEY (`angle2`) REFERENCES `location` (`idlocation`) ON DELETE NO ACTION ON UPDATE NO ACTION," +
				"CONSTRAINT `fkowner` FOREIGN KEY (`owner`) REFERENCES `resident` (`idresident`) ON DELETE NO ACTION ON UPDATE NO ACTION" +
				") ENGINE=InnoDB";
		q = new SQLQuery(query, new String[]{}, msqlc);
		q.excecuteUpdate();
		query = "CREATE TABLE `city` (" +
				"`idcity` int(11) NOT NULL AUTO_INCREMENT," +
				"`name` varchar(45) NOT NULL," +
				"`mayor` varchar(45) NOT NULL," +
				"`tax` varchar(45) DEFAULT NULL," +
				"`perms` varchar(256) NOT NULL," +
				"`spawn` int(11) NOT NULL," +
				"`balance` int(11) NOT NULL DEFAULT '0'," +
				"`nation` varchar(45) DEFAULT NULL," +
				"`toggles` varchar(45) NOT NULL DEFAULT '{false-false-false}'," +
				"`plotnum` int(11) NOT NULL DEFAULT 50" +
				"PRIMARY KEY (`idcity`)," +
				"KEY `fkspawn_idx` (`spawn`)," +
				"CONSTRAINT `fkspawn` FOREIGN KEY (`spawn`) REFERENCES `location` (`idlocation`) ON DELETE NO ACTION ON UPDATE NO ACTION" +
				") ENGINE=InnoDB";
		q = new SQLQuery(query, new String[]{}, msqlc);
		q.excecuteUpdate();
		query = "CREATE TABLE `areacity` (" +
				"`fkarea` varchar(45) NOT NULL," +
				"`fkcity` int(11) NOT NULL," +
				"`isplotclaim` varchar(45) NOT NULL," +
				"PRIMARY KEY (`fkarea`,`fkcity`)," +
				"KEY `fk_city_idx` (`fkcity`)," +
				"CONSTRAINT `fk_area` FOREIGN KEY (`fkarea`) REFERENCES `area` (`idarea`) ON DELETE NO ACTION ON UPDATE NO ACTION," +
				"CONSTRAINT `fk_city` FOREIGN KEY (`fkcity`) REFERENCES `city` (`idcity`) ON DELETE NO ACTION ON UPDATE NO ACTION" +
				") ENGINE=InnoDB";
		q = new SQLQuery(query, new String[]{}, msqlc);
		q.excecuteUpdate();
		query = "CREATE TABLE `citynation` (" +
				"`fkcity` int(11) NOT NULL," +
				"`fknation` int(11) NOT NULL," +
				"`iscapital` varchar(45) DEFAULT NULL," +
				"PRIMARY KEY (`fkcity`,`fknation`)," +
				"KEY `fknation_idx` (`fknation`)," +
				"CONSTRAINT `fkcity` FOREIGN KEY (`fkcity`) REFERENCES `city` (`idcity`) ON DELETE NO ACTION ON UPDATE NO ACTION," +
				"CONSTRAINT `fknation` FOREIGN KEY (`fknation`) REFERENCES `nation` (`idnation`) ON DELETE NO ACTION ON UPDATE NO ACTION" +
				") ENGINE=InnoDB";
		q = new SQLQuery(query, new String[]{}, msqlc);
		q.excecuteUpdate();
		query = "CREATE TABLE `cityresident` (" +
				"`resident` varchar(45) NOT NULL," +
				"`city` int(11) NOT NULL," +
				"`isassistant` varchar(45) NOT NULL DEFAULT 'false'," +
				"PRIMARY KEY (`resident`,`city`)," +
				"KEY `fk_city_idx` (`city`)," +
				"KEY `fk_cityres_idx` (`city`)," +
				"CONSTRAINT `fk_cityres` FOREIGN KEY (`city`) REFERENCES `city` (`idcity`) ON DELETE NO ACTION ON UPDATE NO ACTION," +
				"CONSTRAINT `fk_resident` FOREIGN KEY (`resident`) REFERENCES `resident` (`idresident`) ON DELETE NO ACTION ON UPDATE NO ACTION" +
				") ENGINE=InnoDB";
		q = new SQLQuery(query, new String[]{}, msqlc);
		q.excecuteUpdate();
		query = "CREATE TABLE `friend` (" +
				"`id` int(11) NOT NULL AUTO_INCREMENT," +
				"`resident` varchar(45) NOT NULL," +
				"`friend` varchar(45) NOT NULL," +
				"PRIMARY KEY (`id`)," +
				"KEY `fkresident_idx` (`resident`)," +
				"KEY `fkfriend_idx` (`friend`)," +
				"CONSTRAINT `fkfriend` FOREIGN KEY (`friend`) REFERENCES `resident` (`idresident`) ON DELETE NO ACTION ON UPDATE NO ACTION," +
				"CONSTRAINT `fkresident` FOREIGN KEY (`resident`) REFERENCES `resident` (`idresident`) ON DELETE NO ACTION ON UPDATE NO ACTION" +
				") ENGINE=InnoDB";
		q = new SQLQuery(query, new String[]{}, msqlc);
		q.excecuteUpdate();
		query = "CREATE TABLE `domino` (" +
				"`dbver` int(11) NOT NULL," +
				"PRIMARY KEY (`dbver`)" +
				") ENGINE=InnoDB";
		q = new SQLQuery(query, new String[]{}, msqlc);
		q.excecuteUpdate();
		return;
	}
}
