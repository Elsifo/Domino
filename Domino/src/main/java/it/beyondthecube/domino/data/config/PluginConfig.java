package it.beyondthecube.domino.data.config;

import java.io.File;
import java.io.IOException;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

public class PluginConfig {

	private static ConfigurationLoader<CommentedConfigurationNode> configManager;
	private static ConfigurationNode config;

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
	}
	
	public static ConfigResult init(File defaultConf, ConfigurationLoader<CommentedConfigurationNode> cfg) {
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
		
		return ConfigResult.SUCCESS;
	}
	
	public static void setValue(Object[] path, Object value) throws IOException {
		config.getNode(path).setValue(value);
		saveConfig();
	}
	public static Object getValue(Object... path) throws IOException {
		return config.getNode(path).getValue();
	}
	
	public static void loadConfig() throws IOException {
		config = configManager.load();
	}

	public static void saveConfig() throws IOException {
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
