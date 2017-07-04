package it.beyondthecube.domino.tasks;

import java.util.ArrayList;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;

import it.beyondthecube.domino.Domino;
import it.beyondthecube.domino.terrain.Area;

public class AreaParticleTask implements Runnable {
	private int ai;
	private Player p;
	private Area a;
	private Domino plugin;

	public AreaParticleTask(Domino plugin, int ai, Player p, Area a) {
		this.plugin = plugin;
		this.ai = ai;
		this.p = p;
		this.a = a;
	}

	@Override
	public void run() {
		ArrayList<Location<World>> blocks = a.getBorderBlocks();
		if (ai < 10) {
			ParticleEffect blockParticle = ParticleEffect.builder().type(ParticleTypes.DRAGON_BREATH).quantity(10)
					.velocity(new Vector3d(0, 0, 0)).offset(new Vector3d(0.5, 0.5, 0.5)).build();
			for (Location<World> l : blocks) {
				p.spawnParticles(blockParticle, l.getPosition());
			}
			p.spawnParticles(blockParticle, new Vector3d(0, 60, 0));
			Sponge.getScheduler().createTaskBuilder().delayTicks(20).execute(new AreaParticleTask(plugin, ++ai, p, a))
					.submit(plugin);
		} else
			return;
	}
}
