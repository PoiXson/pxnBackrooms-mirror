package com.poixson.backrooms.levels;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.poixson.backrooms.BackroomsPlugin;


// 78 | Space
public class Gen_078 extends BackroomsGenerator {

	public static final int GRAVITY_REACH = 10;

	protected final BackroomsPlugin plugin;

	protected final ArrayList<UUID> floating = new ArrayList<UUID>();



	public Gen_078(final BackroomsPlugin plugin) {
		super();
		this.plugin = plugin;
	}
	@Override
	public void unload() {
	}



	@Override
	public void setSeed(final int seed) {
	}



	public void onPlayerMove(final PlayerMoveEvent event, final int level) {
		final Player player = event.getPlayer();
		final UUID uuid = player.getUniqueId();
		if (level != 78) {
			this.floating.remove(uuid);
			return;
		}
		final Location to = event.getTo();
		final World world = to.getWorld();
		final int toX = to.getBlockX();
		final int toY = to.getBlockY();
		final int toZ = to.getBlockZ();
		if (toY < -64) {
			while (to.getBlockY() < -64)
				to.add(0, 384, 0);
		} else
		if (toY > 319) {
			while (to.getBlockY() > 319)
				to.subtract(0, 384, 0);
		}
		boolean grounded = false;
//TODO: search below 0 y
		final int h = Math.min(toY, GRAVITY_REACH);
		for (int iy=0; iy<h; iy++) {
			final Block block = world.getBlockAt(toX, toY-iy, toZ);
			if (!block.isPassable()) {
				grounded = true;
				break;
			}
		}
		// gravity
		if (grounded) {
			if (this.floating.remove(uuid)) {
				player.setGravity(true);
				player.setFlying(false);
				player.setAllowFlight(false);
				player.setGlowing(false);
			}
		// floating
		} else {
			if (!this.floating.contains(uuid)) {
				this.floating.add(uuid);
				player.setGravity(false);
				player.setAllowFlight(true);
				player.setFlying(true);
				player.setGlowing(true);
				// ensure player is flying
				(new BukkitRunnable() {
					private Player player = null;
					public BukkitRunnable init(final Player player) {
						this.player = player;
						return this;
					}
					@Override
					public void run() {
						this.player.setFlying(true);
					}
				}).init(player).runTaskLater(this.plugin, 1L);
			}
		}
	}



}
