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
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.tools.DelayedLever;
import com.poixson.tools.events.xListener;


// 6 | Lobby <-> Lights-Out - teleports
//   |           and button to 111
public class Listener_Interact implements xListener {

	protected final BackroomsPlugin plugin;

	protected final double tp_range;



	public Listener_Interact(final BackroomsPlugin plugin) {
		this.plugin = plugin;
		final Level_000 level_000 = (Level_000) plugin.getBackroomsWorld(0);
		this.tp_range = level_000.gen_006.tp_range;
	}



	public void register() {
		xListener.super.register(this.plugin);
	}



	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onPlayerChangedWorld(final PlayerChangedWorldEvent event) {
		this.plugin.getInvisiblePlayersTask()
			.update(event.getPlayer());
	}



	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onPlayerInteract(final PlayerInteractEvent event) {
		final Level_000 level_000 = (Level_000) this.plugin.getBackroomsWorld(0);
		final int level_000_lever_y = level_000.gen_000.level_y + level_000.gen_000.bedrock_barrier + level_000.gen_000.subfloor + 2;
		final int level_006_lever_y = level_000.gen_006.level_y + level_000.gen_006.bedrock_barrier + 1;
		final Block block = event.getClickedBlock();
		final int y = block.getY();
		if (y == level_000_lever_y
		||  y == level_006_lever_y) {
			final Player player = event.getPlayer();
			final int diff_y = level_006_lever_y - level_000_lever_y;
			final int level = this.plugin.getLevel(block.getLocation());
			SWITCH_TYPE:
			switch (block.getType()) {
			case LEVER: {
				SWITCH_LEVEL:
				switch (level) {
				// lobby
				case 0: {
					final Block blk = block.getRelative(BlockFace.UP, diff_y);
					if (Material.LEVER.equals(blk.getType())) {
						this.doLeverTP(6, block.getLocation(), diff_y);
						(new DelayedLever(this.plugin, block.getLocation(), false, 10L))
							.start();
						this.plugin.getInvisiblePlayersTask()
							.update(player);
					}
					break SWITCH_LEVEL;
				}
				// lights out
				case 6: {
					final Block blk = block.getRelative(BlockFace.DOWN, diff_y);
					if (Material.LEVER.equals(blk.getType())) {
						this.doLeverTP(0, block.getLocation(), 0-diff_y);
						(new DelayedLever(this.plugin, block.getLocation(), true, 10L))
							.start();
						this.plugin.getInvisiblePlayersTask()
							.update(player);
					}
					break SWITCH_LEVEL;
				}
				default: break SWITCH_LEVEL;
				}
				break SWITCH_TYPE;
			}
			case DARK_OAK_BUTTON: {
				if (level == 6) {
					if (player.hasPermission("backrooms.level_111.button"))
						this.plugin.noclip(player, 111); // level 111 - run for your life
				}
				break SWITCH_TYPE;
			}
			}
		}
	}



	protected void doLeverTP(final int to_level, final Location lever_loc, final int y) {
		final World world = lever_loc.getWorld();
		for (final Player player : Bukkit.getOnlinePlayers()) {
			if (world.equals(player.getWorld())) {
				final Location player_loc = player.getLocation();
				if (player_loc.distance(lever_loc) < this.tp_range)
					player.teleport( player_loc.add(0.0, y, 0.0) );
			}
		}
	}



}
