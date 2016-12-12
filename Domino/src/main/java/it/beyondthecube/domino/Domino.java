package it.beyondthecube.domino;

import java.io.File;

import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.inject.Inject;

import it.beyondthecube.domino.commands.AreaCommandHandler;
import it.beyondthecube.domino.commands.CityCommandHandler;
import it.beyondthecube.domino.commands.CommandHandler;
import it.beyondthecube.domino.commands.NationCommandHandler;
import it.beyondthecube.domino.commands.ResidentCommandHandler;
import it.beyondthecube.domino.data.EconomyLinker;
import it.beyondthecube.domino.data.config.MySQLConfig;
import it.beyondthecube.domino.data.config.PluginConfig;
import it.beyondthecube.domino.data.database.DatabaseManager;
import it.beyondthecube.domino.exceptions.DatabaseException;
import it.beyondthecube.domino.listeners.AreaListener;
import it.beyondthecube.domino.listeners.LockListener;
import it.beyondthecube.domino.listeners.PlayerListener;
import it.beyondthecube.domino.listeners.TerrainListener;
import it.beyondthecube.domino.tasks.MobTask;
import it.beyondthecube.domino.tasks.TaxesTask;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

@Plugin(id = "domino", name = "Domino", version = "0.2alpha2-SPONGE")
public class Domino {
	private boolean locked;
	
	private Game game;

	@Inject
	private void setGame(Game game) {
	    this.game = game;
	}
	
	public Game getGame() {
		return game;
	}
	
	@Inject
	@ConfigDir(sharedRoot = false)
	private File configDir;

	@Inject
	@DefaultConfig(sharedRoot = false)
	private File defaultConf;

	@Inject
	@DefaultConfig(sharedRoot = false)
	private ConfigurationLoader<CommentedConfigurationNode> loader;
	
	@Listener
	public void onChangeServiceProvider(ChangeServiceProviderEvent event) {
		if (event.getService().equals(EconomyService.class)) {
			EconomyLinker.setEconomy((EconomyService) event.getNewProviderRegistration().getProvider());
		}
	}

	public void load()  {
		switch(PluginConfig.init(defaultConf, loader)) {
		case ERROR: {
			Utility.sendConsole("Could not read config file. Not activating...");
			return;
		}
		case CREATE: {
			Utility.sendConsole("Edit the newly created config. Not actiating...");
			return;
		}
		case SUCCESS: {
			Utility.sendConsole("Config file loaded correctly");
		}
		}
		DatabaseManager.getInstance().setMySQLConfig(new MySQLConfig());
		DatabaseManager.getInstance().setGame(game);
		try {
			if(!DatabaseManager.getInstance().testDB())
				DatabaseManager.getInstance().createDatabase();
		//Utility.sendConsole("Database version: " + vold + ", required: " + vnew);
			/*if (vold != vnew) {
				Utility.sendConsoleing("DATABASE VERSION DIFFERENT, ATTEMPTING UPDATE...");
				DatabaseManager.getInstance().attemptUpdate(vnew);
				PluginConfig.set("database.version", vnew);
				yamlFile.set("database.version", vnew);
				Utility.sendConsole("DATABASE UPDATED SUCCESSFULLY");
			}*/
			DatabaseManager.getInstance().loadNations();
			DatabaseManager.getInstance().loadResidents();
			DatabaseManager.getInstance().loadCities();
			DatabaseManager.getInstance().loadCityResidents();
			DatabaseManager.getInstance().loadAreas();
			DatabaseManager.getInstance().loadFriends();
		} catch (DatabaseException e) {
			Utility.sendConsole("DATABASE ERROR");
			Utility.sendConsole(
					"An error ha occured while reading/writing from database, the plugin will load in failsafe mode!");
			Utility.sendConsole("Error detail: " + e.getMessage());
			Utility.sendConsole("Query that caused this exception: " + e.getQuery().getString());
			Utility.sendConsole("Stack trace: ");
			e.printPreviousStackTrace();
			this.lock();
		}
		new CommandHandler(this);
		new CityCommandHandler(this);
		new NationCommandHandler(this);
		new AreaCommandHandler(this);
		new ResidentCommandHandler(this);
		Sponge.getEventManager().registerListeners(this, new PlayerListener(this));
		Sponge.getEventManager().registerListeners(this, new AreaListener());
		Sponge.getEventManager().registerListeners(this, new TerrainListener());
		(new TaxesTask(this)).run();
		(new MobTask(this)).run();
		Utility.sendConsole("Loaded");
	}
	
	@Listener
    public void onServerStart(GameStartedServerEvent event) {
		load();
	}

	private void lock() {
		this.locked = true;
		Sponge.getServer().getBroadcastChannel().send(Text.of(TextColors.RED + "***SERVER LOCKED***"));
		Sponge.getEventManager().registerListeners(this, new LockListener());
	}

	public boolean isLocked() {
		return locked;
	}

	@Listener
	public void onStopping(GameStoppingServerEvent e) {
		Utility.sendConsole("Disabled");
	}
}