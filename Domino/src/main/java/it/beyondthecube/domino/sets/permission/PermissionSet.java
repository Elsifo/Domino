package it.beyondthecube.domino.sets.permission;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Builder;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

public class PermissionSet {
	private Perm build;
	private Perm interact;
	private Perm itemuse;

	public PermissionSet(Perm build, Perm interact, Perm itemuse) {
		this.build = build;
		this.interact = interact;
		this.itemuse = itemuse;
	}

	public PermissionSet() {
		this.build = new Perm(false, false, false, false, false);
		this.interact = new Perm(false, false, false, false, false);
		this.itemuse = new Perm(false, false, false, false, false);
	}

	public void setBuild(Perm build) {
		this.build = build;
	}

	public void setInteract(Perm interact) {
		this.interact = interact;
	}

	public void setItemUse(Perm itemuse) {
		this.itemuse = itemuse;
	}

	public Perm getBuild() {
		return build;
	}

	public Perm getInteract() {
		return interact;
	}

	public Perm getItemUse() {
		return itemuse;
	}

	public Text print() {
		Builder res = Text.builder("build:").color(TextColors.GOLD).style(TextStyles.BOLD).append(Text.builder(new String(
				build.getOwner() ? "o" : "_") + (build.getFriend() ? "f" : "_") + (build.getCitizen() ? "c" : "_") + 
				(build.getAlly() ? "y" : "_") + (build.getAlly() ? "a" : "_")).color(TextColors.GREEN).style(TextStyles.RESET)
				.build());
		res.append(Text.builder(" interact:").style(TextStyles.BOLD).color(TextColors.GOLD).append(Text.builder(new String(
				(interact.getOwner() ? "o" : "_") + (interact.getFriend() ? "f" : "_") + (interact.getCitizen() ? "c" : "_")
				+ (interact.getAlly() ? "y" : "_") + (interact.getAll() ? "a" : "_"))).color(TextColors.GREEN).style(TextStyles.RESET)
				.build()).build());
		res.append(Text.builder(" itemuse:").style(TextStyles.BOLD).color(TextColors.GOLD).append(Text.builder(new String(
				(itemuse.getOwner() ? "o" : "_") + (itemuse.getFriend() ? "f" : "_") + (itemuse.getCitizen() ? "c" : "_")
				+ (itemuse.getAlly() ? "y" : "_") + (itemuse.getAll() ? "a" : "_"))).color(TextColors.GREEN).style(TextStyles.RESET)
				.build()).build());
		return res.build();
	}

	@Override
	public String toString() {
		String ris = "";
		ris += "{build:"+build.toString()+";interact:" + interact.toString() + "];itemuse:" + itemuse.toString() + "}";
		return ris;
	}
}