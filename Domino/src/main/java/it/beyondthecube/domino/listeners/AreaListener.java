package it.beyondthecube.domino.listeners;

import java.util.Map.Entry;
import java.util.Optional;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;

import it.beyondthecube.domino.Utility;
import it.beyondthecube.domino.residents.Resident;
import it.beyondthecube.domino.residents.ResidentManager;
import it.beyondthecube.domino.terrain.AreaManager;
import it.beyondthecube.domino.terrain.AreaManager.ActionType;

public class AreaListener {
	public AreaListener() {

	}

	@Listener
	public void onBlockBreak(ChangeBlockEvent.Break e, @Root Player p) {
		if(p.hasPermission("domino")) return;
		Location<World> l = e.getTransactions().get(0).getDefault().getLocation().get();
		if (!(AreaManager.canPerformAction(ResidentManager.getResident(p.getUniqueId()), l, ActionType.BUILD))) {
			p.sendMessage(Text.of(Utility.pluginMessage("You aren't allowed to build here")));
			e.setCancelled(true);
		}
	}

	@Listener
	public void onBlockPlace(ChangeBlockEvent.Place e, @Root Player p) {
		if(p.hasPermission("domino")) return;
		Location<World> l = e.getTransactions().get(0).getDefault().getLocation().get();
		if (!(AreaManager.canPerformAction(ResidentManager.getResident(p.getUniqueId()), l, ActionType.BUILD))) {
			p.sendMessage(Text.of(Utility.pluginMessage("You aren't allowed to build here")));
			e.setCancelled(true);
		}
	}

	private boolean isSwitchableRightClick(Location<World> l) {
		switch (l.getBlock().getType().getId().substring(10)) {
		case "acacia_door":
		case "spruce_door":
		case "birch_door":
		case "dark_oak_door":
		case "jungle_door":
		case "trapdoor":
		case "wooden_door":
		case "stone_button":
		case "wooden_button":
		case "fence_gate":
		case "acacia_fence_gate":
		case "spruce_fence_gate":
		case "birch_fence_gate":
		case "dark_oak_fence_gate":
		case "jungle_fence_gate":
		case "bed":
		case "lever":
		case "unpowered_comparator":
		case "powered_comparator":
		case "unpowered_repeater":
		case "powered_repeater":
		case "chest":
		case "furnace":
		case "lit_furnace":
		case "crafting_table":
		case "hopper":
		case "dropper":
		case "dispenser":
		case "end_portal_frame":
		case "enchanting_table":
		case "anvil":
		case "beacon":
		case "jukebox":
			return true;
		default:
			return false;
		}
	}

	private boolean isSwitchableRedstone(BlockState b) {
		switch (b.getType().getId().substring(10)) {
		case "tripwire":
		case "heavy_weighted_pressure_plate":
		case "light_weighted_pressure_plate":
		case "wooden_pressure_plate":
		case "stone_pressure_plate":
			return true;
		default:
			return false;
		}
	}

	@Listener
	public void onBlockInteract(InteractBlockEvent.Secondary e, @Root Player p) {
		if(p.hasPermission("domino")) return;
		if(!e.getTargetBlock().getLocation().isPresent()) return;
		Location<World> l = e.getTargetBlock().getLocation().get();
		Optional<ItemStack> is = p.getItemInHand(HandTypes.MAIN_HAND);
		if (is.isPresent() && isUsable(is.get())) {
			if (!(AreaManager.canPerformAction(ResidentManager.getResident(p.getUniqueId()), l, ActionType.ITEMUSE))) {
				e.setCancelled(true);
				p.sendMessage(Text.of(Utility.pluginMessage("You can't use this item here")));
			}
		}
		if (isSwitchableRightClick(l)) {				
			if (!(AreaManager.canPerformAction(ResidentManager.getResident(p.getUniqueId()), l,ActionType.INTERACT))) {
				e.setCancelled(true);
				p.sendMessage(Text.of(Utility.pluginMessage("You can't interact with this block")));
			}
		} 
	}

	private boolean isUsable(ItemStack itemInHand) {
		switch (itemInHand.getItem().getId().toUpperCase()) {
		case "WATER_BUCKET":
		case "BUCKET":
		case "LAVA_BUCKET":
		case "BONE_MEAL":
		case "FISHING_ROD":
		case "LEASH":
			return true;
		default:
			return false;
		}
	}
	@Listener
	public void onBlockRedstone(NotifyNeighborBlockEvent e, @First Player p, @Root BlockSnapshot blockSource) {
		if(p.hasPermission("domino")) return;
		Resident r = ResidentManager.getResident(p.getUniqueId());
		for(Entry<Direction,BlockState> entry : e.getNeighbors().entrySet()) {
			Vector3i v3i = entry.getKey().asBlockOffset();
			Optional<Location<World>> onewl = blockSource.getLocation();
			if(onewl.isPresent()) {
				Location<World> newl = onewl.get().add(v3i);
				if(!AreaManager.canPerformAction(r, newl, ActionType.INTERACT) && isSwitchableRedstone(entry.getValue())) {
					p.sendMessage(Utility.pluginMessage("Can't interact with this block"));
					e.setCancelled(true);
					return;
				}
			}
		}
	}
}
