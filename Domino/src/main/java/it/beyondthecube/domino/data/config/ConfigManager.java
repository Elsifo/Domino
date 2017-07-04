package it.beyondthecube.domino.data.config;

import java.io.File;
import java.io.IOException;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

public class ConfigManager {
	public enum ConfigResult {
		SUCCESS, ERROR, CREATE
	}
	private static ConfigurationLoader<CommentedConfigurationNode> configManager;
	private static ConfigurationNode config;
	private static PluginConfig pc = new PluginConfig();
	
	public static void checkConfigValues() {
		if(config.getNode("database","address").isVirtual()) config.getNode("database","address").setValue("localhost");
    	if(config.getNode("database","user").isVirtual()) config.getNode("database","user").setValue("user");
    	if(config.getNode("database","pass").isVirtual()) config.getNode("database","pass").setValue("pass");
    	if(config.getNode("database","alias").isVirtual()) config.getNode("database","alias").setValue("alias");
    	if(config.getNode("database","name").isVirtual()) config.getNode("database","name").setValue("name");
    	if(config.getNode("database","version").isVirtual()) config.getNode("database","version").setValue("4");
    	if(config.getNode("domino","lasttaxcollection").isVirtual()) config.getNode("domino","lasttaxcollection").setValue("0");
    	if(config.getNode("domino","city","spawn","price").isVirtual()) config.getNode("domino","city","spawn","price").setValue("30");
    	if(config.getNode("domino","city","citizenbonusplots").isVirtual()) config.getNode("domino","city","citizenbonusplots").setValue("10");
    	if(config.getNode("domino","city","baseplots").isVirtual()) config.getNode("domino","city","baseplots").setValue("50");
    	if(config.getNode("domino","city","claim","price").isVirtual()) config.getNode("domino","city","claim","price").setValue("30");
    	if(config.getNode("domino","city","spawn","delay").isVirtual()) config.getNode("domino","city","spawn","delay").setValue("600");
	}
	
	public static ConfigResult init(File defaultConf, ConfigurationLoader<CommentedConfigurationNode> cfg) throws IOException {
		configManager=cfg; 
		try {		 
			config = cfg.load();
			checkConfigValues();
	        if (!defaultConf.exists()) {	        	
	        	saveConfig();
	        	return ConfigResult.CREATE;
	        }
		} catch (IOException e) {
			return ConfigResult.ERROR;
		}
		loadConfig();
		return ConfigResult.SUCCESS;
	}
	
	public static PluginConfig getConfig() {
		return pc;
	}
	
	public static void loadConfig() throws IOException {
		config = configManager.load();
		pc.setCitizenPlotBonus(config.getNode("domino","city","citizenbonusplots").getInt());
		pc.setSpawnPrice(config.getNode("domino","city","spawn","price").getInt());
		pc.setSpawnDelay(config.getNode("domino","city","spawn","delay").getLong());
		pc.setClaimPrice(config.getNode("domino","city","claim","price").getInt());
		pc.setLastTax(config.getNode("domino","lasttaxcollection").getLong());
	}

	public static void saveConfig() throws IOException {
		config.getNode("domino","city","spawn","price").setValue(pc.getSpawnPrice());
    	config.getNode("domino","city","citizenbonusplots").setValue(pc.getCitizenPlotBonus());
    	config.getNode("domino","lasttaxcollection").setValue(pc.getLastTax());
    	config.getNode("domino","city","spawn","delay").setValue(pc.getSpawnDelay());
		configManager.save(config);
	}

	public static String[] getMySQLConfiguration() { 
		String[] ris = new String[5]; 
		ris[0] = config.getNode("database", "address").getString(); 
		ris[1] = config.getNode("database", "user").getString(); 
		ris[2] = config.getNode("database", "pass").getString();
		ris[3] = config.getNode("database", "name").getString(); 
		ris[4] = config.getNode("database", "alias").getString(); 
		return ris; 
	}
}
