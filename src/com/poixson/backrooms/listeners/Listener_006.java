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
import org.bukkit.event.player.PlayerInteractEvent;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.tools.DelayedLever;
import com.poixson.tools.events.xListener;


// 6 | Lobby/Lights-Out teleport
public class Listener_006 extends xListener<BackroomsPlugin> {

	protected final Level_000 level0;



	public Listener_006(final BackroomsPlugin plugin, final Level_000 level0) {
		super(plugin);
		this.level0 = level0;
	}



	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onPlayerInteract(final PlayerInteractEvent event) {
		final Block block = event.getClickedBlock();
		final int y = block.getY();
		if (y == Level_000.Y_000+6
		||  y == Level_000.Y_006+2) {
			final Player player = event.getPlayer();
			final int diff_y = (Level_000.Y_006 - Level_000.Y_000) - 4;
			final int level = this.plugin.getLevel(block.getLocation());
			TYPE_SWITCH:
			switch (block.getType()) {
			case LEVER: {
				LEVEL_SWITCH:
				switch (level) {
				// lobby
				case 0: {
					final Block blk = block.getRelative(BlockFace.UP, diff_y);
					if (Material.LEVER.equals(blk.getType())) {
						this.doLeverTP(6, block.getLocation(), diff_y);
						(new DelayedLever(this.plugin, block.getLocation(), false, 10L))
							.start();
					}
					break LEVEL_SWITCH;
				}
				// lights out
				case 6: {
					final Block blk = block.getRelative(BlockFace.DOWN, diff_y);
					if (Material.LEVER.equals(blk.getType())) {
						this.doLeverTP(0, block.getLocation(), 0-diff_y);
						(new DelayedLever(this.plugin, block.getLocation(), true, 10L))
							.start();
					}
					break LEVEL_SWITCH;
				}
				default: break LEVEL_SWITCH;
				}
				break TYPE_SWITCH;
			}
			case DARK_OAK_BUTTON: {
				if (level == 6)
					this.plugin.noclip(player, 33); // level 33 - run for your life
				break TYPE_SWITCH;
			}
			default: break TYPE_SWITCH;
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
