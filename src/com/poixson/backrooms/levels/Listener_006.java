package com.poixson.backrooms.levels;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockRedstoneEvent;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.commonmc.tools.DelayedLever;
import com.poixson.commonmc.utils.BukkitUtils;


// lobby/lights-out teleport
public class Listener_006 {

	protected final BackroomsPlugin plugin;

	protected final Level_000 level0;



	public Listener_006(final BackroomsPlugin plugin) {
		this.plugin = plugin;
		this.level0 = (Level_000) plugin.getBackroomsLevel(0);
		if (this.level0 == null) throw new NullPointerException("Failed to get Level0");
	}



	public void onBlockRedstone(final BlockRedstoneEvent event) {
		final Block block = event.getBlock();
		final World world = block.getWorld();
		final String worldName = world.getName();
		if ("level0".equals(worldName)) {
			final int y = block.getY();
			final int lvl = this.level0.getLevelFromY(y);
			switch (lvl) {
			case 0: // lobby
				if (y == Level_000.Y_000 + 6
				&&  Material.LEVER.equals(block.getType())) {
					// sign above
					final Block blk = block.getRelative(BlockFace.UP);
					if (BukkitUtils.isSign(blk.getType())) {
						final int diff_y = (Level_000.Y_006 - Level_000.Y_000) - 4;
						this.doLeverTP(block.getLocation(), diff_y);
						(new DelayedLever(this.level0.plugin, block.getLocation(), false, 10L))
							.start();
					}
				}
				break;
			case 6: // lights out
				if (y == Level_000.Y_006 + 2
				&&  Material.LEVER.equals(block.getType())) {
					// sign above
					final Block blk = block.getRelative(BlockFace.UP);
					if (BukkitUtils.isSign(blk.getType())) {
						final int diff_y = (Level_000.Y_000 - Level_000.Y_006) + 4;
						this.doLeverTP(block.getLocation(), diff_y);
						(new DelayedLever(this.level0.plugin, block.getLocation(), true, 10L))
							.start();
					}
				}
				break;
			default: break;
			}
		}
	}



	protected void doLeverTP(final Location leverLoc, final int y) {
		final World world = leverLoc.getWorld();
		for (final Player player : Bukkit.getOnlinePlayers()) {
			if (world.equals(player.getWorld())) {
				final Location playerLoc = player.getLocation();
				if (playerLoc.distance(leverLoc) < 8.0) {
					player.teleport( playerLoc.add(0.0, y, 0.0) );
				}
			}
		}
	}



}
