package it.beyondthecube.domino.listeners;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent.MessageFormatter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import it.beyondthecube.domino.Domino;
import it.beyondthecube.domino.Utility;
import it.beyondthecube.domino.data.database.DatabaseManager;
import it.beyondthecube.domino.exceptions.DatabaseException;
import it.beyondthecube.domino.politicals.City;
import it.beyondthecube.domino.politicals.Nation;
import it.beyondthecube.domino.politicals.PoliticalManager;
import it.beyondthecube.domino.residents.CitizenshipRequestManager;
import it.beyondthecube.domino.residents.Resident;
import it.beyondthecube.domino.residents.ResidentManager;
import it.beyondthecube.domino.terrain.AreaManager;

public class PlayerListener {

	public PlayerListener(Domino plugin) {

	}

	@Listener
	public void onPlayerMove(MoveEntityEvent e, @Root Player p) {
		Text msgFrom = Utility.terrainMessage(e.getFromTransform().getLocation());
		Text msgTo = Utility.terrainMessage(e.getToTransform().getLocation());
		if (!(msgTo.equals(msgFrom))) {
			p.sendMessage(msgTo);
		}
	}

	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Join e) {
		e.getTargetEntity().sendMessage((Utility.pluginMessage(
				"Running version: " + Sponge.getPluginManager().getPlugin("domino").get().getVersion().get())));
	}

	@Listener
	public void onPlayerLogin(ClientConnectionEvent.Login e) {
		try {
			if (ResidentManager.getResident(e.getTargetUser().getUniqueId()) == null)
				ResidentManager.newResident(e.getTargetUser(), null);
			else
				DatabaseManager.getInstance().updateResidentNickname(e.getTargetUser());
		} catch (DatabaseException e1) {
			e.setCancelled(true);
			e.setMessage(Text.of("ERROR: contact an administrator"));
		}
	}

	@Listener
	public void onBlockInteract(InteractBlockEvent e, @Root Player p) {
		if(!e.getTargetBlock().getLocation().isPresent()) return;
		Location<World> l = e.getTargetBlock().getLocation().get();
		if (ResidentManager.getResident(p.getUniqueId()).isSelecting()) {
			if (e instanceof InteractBlockEvent.Primary) {
				ResidentManager.setSelection1(p, l);
				p.sendMessage(Utility.pluginMessage("First position selected"));
			} else if (e instanceof InteractBlockEvent.Secondary) {
				ResidentManager.setSelection2(p, l);
				p.sendMessage(Utility.pluginMessage("Second position selected"));
			}
		}
	}

	@Listener
	public void pvpHandler(DamageEntityEvent e, @First EntityDamageSource eds) {
		if (!(eds.getSource() instanceof Player))
			return;
		if (!(e.getTargetEntity() instanceof Player))
			return;
		Player target = (Player) e.getTargetEntity();
		Player source = (Player) eds.getSource();
		if (!AreaManager.existsArea(target.getLocation()))
			return;
		if (!AreaManager.getCity(AreaManager.getArea(target.getLocation())).getPvp()) {
			e.setCancelled(true);
			source.sendMessage(Utility.pluginMessage("PVP is disabled here"));
		}
	}

	@Listener
	public void onPlayerLogout(ClientConnectionEvent.Disconnect e) {
		CitizenshipRequestManager.remove(ResidentManager.getResident(e.getTargetEntity().getUniqueId()));
	}

	@Listener
	public void onChatMessage(MessageChannelEvent.Chat e, @First Player p) {
		Resident r = ResidentManager.getResident(p.getUniqueId());
		if (ResidentManager.isCitizen(r)) {
			MessageFormatter mf = e.getFormatter();
			City c = ResidentManager.getCity(r).get();
			Nation n = PoliticalManager.getNation(c);
			Text msg = Text.builder("[").append(Text.builder(n.getName()).color(TextColors.LIGHT_PURPLE)
					.append(Text.builder("|").color(TextColors.WHITE)
					.append(Text.builder(c.getName()).color(TextColors.GREEN)
					.append(Text.builder("]").color(TextColors.WHITE)
					.append(mf.getHeader().toText()).build()).build()).build()).build()).build();
			mf.setHeader(msg);
		}
	}
}