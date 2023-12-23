package com.poixson.backrooms.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockRedstoneEvent;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.tools.DelayedLever;
import com.poixson.tools.events.xListener;


// 6 | lobby/lights-out teleport
public class Listener_006 extends xListener<BackroomsPlugin> {

	protected final Level_000 level0;



	public Listener_006(final BackroomsPlugin plugin, final Level_000 level0) {
		super(plugin);
		this.level0 = level0;
	}



	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onBlockRedstone(final BlockRedstoneEvent event) {
		final Block block = event.getBlock();
		final World world = block.getWorld();
		final int level = this.plugin.getLevelFromWorld(world);
		if (level == 0) {
			final int y = block.getY();
			final int diff_y = (Level_000.Y_006 - Level_000.Y_000) - 4;
			final int lvl = this.level0.getLevelFromY(y);
			switch (lvl) {
			// lobby
			case 0:
				if (y == Level_000.Y_000 + 6
				&&  Material.LEVER.equals(block.getType())) {
					final Block blk = block.getRelative(BlockFace.UP, diff_y);
					if (Material.LEVER.equals(blk.getType())) {
						this.doLeverTP(6, block.getLocation(), diff_y);
						(new DelayedLever(this.plugin, block.getLocation(), false, 10L))
							.start();
					}
				}
				break;
			// lights out
			case 6:
				if (y == Level_000.Y_006 + 2
				&&  Material.LEVER.equals(block.getType())) {
					final Block blk = block.getRelative(BlockFace.DOWN, diff_y);
					if (Material.LEVER.equals(blk.getType())) {
						this.doLeverTP(0, block.getLocation(), 0-diff_y);
						(new DelayedLever(this.plugin, block.getLocation(), true, 10L))
							.start();
					}
				}
				break;
			default: break;
			}
		}
	}



	protected void doLeverTP(final int to_level, final Location leverLoc, final int y) {
		final World world = leverLoc.getWorld();
		for (final Player player : Bukkit.getOnlinePlayers()) {
			if (world.equals(player.getWorld())) {
				final Location playerLoc = player.getLocation();
				if (playerLoc.distance(leverLoc) < 8.0) {
					if (to_level == 6)
						player.setInvisible(true);
					player.teleport( playerLoc.add(0.0, y, 0.0) );
					if (to_level != 6)
						player.setInvisible(false);
				}
			}
		}
	}



}
