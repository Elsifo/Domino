package it.beyondthecube.domino.data.config;

import java.io.File;
import java.io.IOException;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

public class PluginConfig {

	private static ConfigurationLoader<CommentedConfigurationNode> configManager;
	private static ConfigurationNode config;

	public static ConfigResult init(File defaultConf, ConfigurationLoader<CommentedConfigurationNode> cfg) {
		configManager=cfg; 
		try {		 
			config = cfg.load();
	        if (!defaultConf.exists()) {
	        	config.getNode("database","address").setValue("localhost");
	        	config.getNode("database","user").setValue("user");
	        	config.getNode("database","pass").setValue("pass");
	        	config.getNode("database","alias").setValue("alias");
	        	config.getNode("database","name").setValue("name");
	        	config.getNode("database","version").setValue("4");
	        	config.getNode("domino","lasttaxcollection").setValue("0");
	        	config.getNode("domino","city","spawn","price").setValue("30");
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
