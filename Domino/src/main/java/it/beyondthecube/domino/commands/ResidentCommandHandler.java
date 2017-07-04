package it.beyondthecube.domino.commands;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import it.beyondthecube.domino.Domino;
import it.beyondthecube.domino.Utility;
import it.beyondthecube.domino.exceptions.DatabaseException;
import it.beyondthecube.domino.residents.Resident;
import it.beyondthecube.domino.residents.ResidentManager;
import it.beyondthecube.domino.tasks.AreaParticleTask;
import it.beyondthecube.domino.terrain.Area;
import it.beyondthecube.domino.terrain.AreaManager;

public class ResidentCommandHandler implements CommandExecutor {
	private Domino plugin;
	
	public ResidentCommandHandler(Domino plugin) {
		this.plugin = plugin;
		CommandSpec toggle = CommandSpec.builder().executor(this::executeToggle)
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("toggle")))).build();
		CommandSpec friends = CommandSpec.builder().executor(this::executeFriends)
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("mode"))),
					GenericArguments.onlyOne(GenericArguments.user(Text.of("player")))).build();
		CommandSpec res = CommandSpec.builder().executor(this)
				.arguments(GenericArguments.optional(GenericArguments.onlyOne(
						GenericArguments.string(Text.of("target"))))).child(toggle, "toggle")
				.child(friends, "friends").build();
		Sponge.getCommandManager().register(plugin, res, "resident", "res");
	}

	private void showHelp(Player p) {
		p.sendMessage(Text.builder("Command ").color(TextColors.GOLD)
				.append(Text.builder("/resident").color(TextColors.GREEN)
				.append(Text.builder(" usage").color(TextColors.GOLD).build()).build()).build());
		p.sendMessage(Text.builder("/resident <player>").color(TextColors.GREEN).append(Text.builder(
				" - shows own/other player info").color(TextColors.WHITE).build()).build());
		p.sendMessage(Text.builder("/resident toggle selection").color(TextColors.GREEN).append(Text.builder(
				" - activate/deactivate selection mode ").color(TextColors.WHITE).build()).build());
		p.sendMessage(Text.builder("/resident toggle borders").color(TextColors.GREEN).append(Text.builder(
				" - activate for 10 seconds 3d areas borders").color(TextColors.WHITE).build()).build());
		p.sendMessage(Text.builder("/resident friends <add|remove> <Player>").color(TextColors.GREEN).append(
				Text.builder(" - add/remove friends").color(TextColors.WHITE).build()).build());
	}

	private CommandResult executeFriends(CommandSource src, CommandContext args) throws CommandException {
		if (src instanceof Player) {
			Player p = (Player) src;
			try {
				User friend = (User) args.getOne("target").get();
				switch ((String) args.getOne("mode").get()) {
				case "add":
					ResidentManager.setFriend(ResidentManager.getResident(p.getUniqueId()),
							ResidentManager.getResident(friend.getUniqueId()), false);

					p.sendMessage((Utility.pluginMessage("Friend added")));
					break;
				case "remove":
					ResidentManager.removeFriend(ResidentManager.getResident(p.getUniqueId()),
							ResidentManager.getResident(friend.getUniqueId()));
					p.sendMessage((Utility.pluginMessage("Friend removed")));
					break;
				default:
					showHelp(p);
				}
			} catch (DatabaseException e) {
				p.sendMessage((Utility.errorMessage("ERROR: contact an administrator")));
			}
		}
		return CommandResult.success();
	}

	private CommandResult executeToggle(CommandSource src, CommandContext args) throws CommandException {
		if (src instanceof Player) {
			Player p = (Player) src;
			Resident r = ResidentManager.getResident(p.getUniqueId());
			Optional<String> o = args.getOne("toggle");
			if (o.isPresent()) {
				switch (o.get()) {
				case "selection": {
					if (r.isSelecting()) {
						ResidentManager.deactivateSelection(p);
						ResidentManager.setSelection1(p, null);
						ResidentManager.setSelection2(p, null);
						p.sendMessage((Utility.pluginMessage("Selection mode deactivated")));
					} else {
						ResidentManager.activateSelection(p);
						p.sendMessage((Utility.pluginMessage("Selection mode activated")));
					}
					break;
				}
				case "borders": {
					Set<Area> areas = AreaManager.getAreas().stream().filter(pr -> !(pr.isPlotClaim()))
							.collect(Collectors.toSet());
					for (Area a : areas) {
						Sponge.getScheduler().createTaskBuilder().execute(new AreaParticleTask(plugin, 0, p, a)).submit(plugin);
					}
					break;
				}
				default:
					showHelp(p);
				}
			}
		}
		return CommandResult.success();
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (src instanceof Player) {
			Player p = (Player) src;
			Optional<String> ot = args.getOne("target");
			if(!ot.isPresent())
				Utility.residentInfo(p, ResidentManager.getResident(p.getUniqueId()));
			else {
				Optional<Resident> target = ResidentManager.getResident(ot.get());
				if (target.isPresent()) 
					Utility.residentInfo(p, target.get());
				else 
					p.sendMessage(Utility.pluginMessage("Not a valid player name"));
			}
			return CommandResult.success();
		}
		return CommandResult.empty();
	}
}