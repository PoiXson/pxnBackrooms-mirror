package com.poixson.backrooms.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.poixson.backrooms.levels.Gen_001;
import com.poixson.backrooms.levels.Gen_078;
import com.poixson.utils.NumberUtils;


public class PlayerMoveListener implements Listener {

	protected final Gen_001 gen_001;
	protected final Gen_078 gen_078;



	public PlayerMoveListener(final Gen_001 gen_001, final Gen_078 gen_078) {
		this.gen_001 = gen_001;
		this.gen_078 = gen_078;
	}



	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onPlayerMove(final PlayerMoveEvent event) {
		final Location from = event.getFrom();
		final Location to   = event.getTo();
		final int toX = to.getBlockX();
		final int toY = to.getBlockY();
		final int toZ = to.getBlockZ();
		// location changed
		if (from.getBlockX() == toX
		&&  from.getBlockY() == toY
		&&  from.getBlockZ() == toZ)
			return;
		final Player player = event.getPlayer();
		final String world_name = player.getWorld().getName();
		if (!world_name.startsWith("level"))
			return;
		final int level;
		{
			final String str = world_name.substring(5);
			if (!NumberUtils.IsNumeric(str))
				return;
			level = Integer.parseInt(str);
		}
		// level 1 - basement
		this.gen_001.onPlayerMove(event, level);
		// level 78 - space
		this.gen_078.onPlayerMove(event, level);
	}



}
