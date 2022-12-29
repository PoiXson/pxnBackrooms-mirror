package com.poixson.backrooms.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.levels.Gen_001;
import com.poixson.backrooms.levels.Gen_078;
import com.poixson.backrooms.levels.Level_000;
import com.poixson.backrooms.levels.Level_078;
import com.poixson.utils.NumberUtils;


public class PlayerMoveListener implements Listener {

	protected final BackroomsPlugin plugin;

	protected final Gen_001 gen_001;
	protected final Gen_078 gen_078;



	public PlayerMoveListener(final BackroomsPlugin plugin) {
		this.plugin = plugin;
		final Level_000 lvl_000 = (Level_000) plugin.getBackroomsLevel(0);
		final Level_078 lvl_078 = (Level_078) plugin.getBackroomsLevel(78);
		if (lvl_000 == null) throw new RuntimeException("Failed to get backrooms level 0");
		if (lvl_078 == null) throw new RuntimeException("Failed to get backrooms level 78");
		this.gen_001 = lvl_000.gen_001;
		this.gen_078 = lvl_078.gen_078;
	}



	public void register() {
		Bukkit.getPluginManager()
			.registerEvents(this, this.plugin);
	}
	public void unregister() {
		HandlerList.unregisterAll(this);
	}



	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onPlayerMove(final PlayerMoveEvent event) {
		final Location from = event.getFrom();
		final Location to   = event.getTo();
		final int toX = to.getBlockX();
		final int toY = to.getBlockY();
		final int toZ = to.getBlockZ();
		// location changed
		if (from.getBlockX() != toX
		||  from.getBlockY() != toY
		||  from.getBlockZ() != toZ) {
			final Player player = event.getPlayer();
			final String world_name = player.getWorld().getName();
			if (world_name.startsWith("level")) {
				final int level;
				final String str = world_name.substring(5);
				if (NumberUtils.IsNumeric(str)) {
					level = Integer.parseInt(str);
					// level 1 - basement
					this.gen_001.onPlayerMove(event, level);
					// level 78 - space
					this.gen_078.onPlayerMove(event, level);
				}
			}
		}
	}



}
