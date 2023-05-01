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
import com.poixson.backrooms.levels.Level_000;
import com.poixson.commonmc.tools.plugin.xListener;
import com.poixson.morefoods.MoreFoodsAPI;


public class Listener_023 extends xListener<BackroomsPlugin> {



	public Listener_023(final BackroomsPlugin plugin) {
		super(plugin);
	}



	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onPlayerConsume(final PlayerItemConsumeEvent event) {
		final Player player = event.getPlayer();
		if (player == null) return;
		final int level = this.plugin.getPlayerLevel(player);
		final ItemStack stack = event.getItem();
		final Material type = stack.getType();
		if (Material.APPLE.equals(type)) {
			switch (level) {
			// lobby to overgrowth
			case 0: {
				final MoreFoodsAPI foodrot = MoreFoodsAPI.GetAPI();
				if (foodrot != null) {
					if (foodrot.isFullyRotten(stack)) {
						final int delta_y = Level_000.Y_023 - Level_000.Y_000;
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
			// overgrowth to lobby
			case 23: {
				final MoreFoodsAPI foodrot = MoreFoodsAPI.GetAPI();
				if (foodrot != null) {
					if (foodrot.isFresh(stack)) {
						final int delta_y = Level_000.Y_000 - Level_000.Y_023;
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
