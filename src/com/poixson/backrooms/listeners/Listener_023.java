package com.poixson.backrooms.listeners;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.morefoods.MoreFoodsAPI;
import com.poixson.tools.events.xListener;


// 23 | Overgrowth
public class Listener_023 implements xListener {

	protected final BackroomsPlugin plugin;



	public Listener_023(final BackroomsPlugin plugin) {
		this.plugin = plugin;
	}



	public void register() {
		xListener.super.register(this.plugin);
	}



	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onPlayerConsume(final PlayerItemConsumeEvent event) {
		final Player player = event.getPlayer();
		final int level = this.plugin.getLevel(player);
		if (level == 0
		||  level == 1
		||  level == 23) {
			final Level_000 level_000 = (Level_000) this.plugin.getBackroomsWorld(0);
			final int level_000_y = level_000.gen_000.level_y;
			final int level_023_y = level_000.gen_023.level_y;
			final ItemStack stack = event.getItem();
			final Material type = stack.getType();
			final MoreFoodsAPI morefoods = MoreFoodsAPI.GetAPI();
			boolean aged = true;
			if (morefoods != null) {
				final Boolean result = morefoods.isFullyAged(stack);
				if (result != null) aged = result.booleanValue();
			}
			switch (level) {
			// rotten apple - lobby/basement to overgrowth
			case 0:
			case 1: {
				if (Material.APPLE.equals(type)) {
					if (aged) {
						final int delta_y = level_023_y - level_000_y;
						final Location loc = player.getLocation();
						loc.add(0.0, (double)delta_y, 0.0);
						player.teleport(loc);
						player.playEffect(EntityEffect.TELEPORT_ENDER);
						player.playSound(player, Sound.AMBIENT_CRIMSON_FOREST_ADDITIONS, 1.0f, 0.7f);
						player.playSound(player, Sound.AMBIENT_CRIMSON_FOREST_MOOD,      0.7f, 2.0f);
					}
				}
				break;
			}
			// corn - overgrowth to lobby
			case 23: {
				if (Material.CARROT.equals(type)) {
					if (aged) {
						final int delta_y = level_000_y - level_023_y;
						final Location loc = player.getLocation();
						loc.add(0.0, (double)delta_y, 0.0);
						player.teleport(loc);
						player.playEffect(EntityEffect.TELEPORT_ENDER);
						player.playSound(player, Sound.AMBIENT_BASALT_DELTAS_ADDITIONS, 1.0f, 1.85f);
						player.playSound(player, Sound.AMBIENT_BASALT_DELTAS_MOOD,     0.85f, 0.65f);
					}
				}
				break;
			}
			default: break;
			}
		}
	}



}
