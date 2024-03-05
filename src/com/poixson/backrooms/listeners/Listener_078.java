/*
package com.poixson.backrooms.listeners;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.pluginlib.events.PlayerMoveNormalEvent;
import com.poixson.pluginlib.tools.plugin.xListener;


// 78 | Space
public class Listener_078 implements xListener {

	public static final int GRAVITY_REACH = 10;

	protected final BackroomsPlugin plugin;

	protected final CopyOnWriteArraySet<UUID> flying = new CopyOnWriteArraySet<UUID>();



	public Listener_078(final BackroomsPlugin plugin) {
		this.plugin = plugin;
	}



	public void register() {
		xListener.super.register(this.plugin);
	}
	@Override
	public void unregister() {
		xListener.super.unregister();
		for (final UUID uuid : this.flying) {
			final Player player = Bukkit.getPlayer(uuid);
			if (player != null)
				this.unfly(player);
		}
	}



	public boolean fly(final Player player) {
		final UUID uuid = player.getUniqueId();
		if (this.flying.add(uuid)) {
			player.setAllowFlight(true);
			player.setFlying(true);
			player.setGravity(false);
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
			}).init(player).runTaskLater(this.plugin, 2L);
			return true;
		}
		return false;
	}
	public boolean unfly(final Player player) {
		final UUID uuid = player.getUniqueId();
		if (this.flying.remove(uuid)) {
			player.setFlying(false);
			player.setAllowFlight(false);
			player.setGravity(true);
			player.setGlowing(false);
			return true;
		}
		return false;
	}



	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onPlayerMoveNormal(final PlayerMoveNormalEvent event) {
		final Location to = event.getTo();
		final int toX = to.getBlockX();
		final int toY = to.getBlockY();
		final int toZ = to.getBlockZ();
		final Player player = event.getPlayer();
		final World world = player.getWorld();
		final int level = this.plugin.getLevelFromWorld(world.getName());
		if (level == 78) {
			// teleport top/bottom of the world
			if (toY < -64) {
				while (to.getBlockY() < -64)
					to.add(0, 384, 0);
			} else
			if (toY > 319) {
				while (to.getBlockY() > 319)
					to.subtract(0, 384, 0);
			}
			// fly in space
			switch (player.getGameMode()) {
			case CREATIVE:
			case SPECTATOR:
				this.unfly(player);
				break;
			case ADVENTURE:
			case SURVIVAL: {
				boolean grounded = false;
				int yy;
				Block block;
				for (int iy=0; iy<GRAVITY_REACH; iy++) {
					yy = toY - iy;
					if (yy < -64) break;
					block = world.getBlockAt(toX, yy, toZ);
					if (!block.isPassable()) {
						grounded = true;
						break;
					}
				}
				if (grounded) this.unfly(player);
				else          this.fly(player);
				break;
			}
			default: throw new RuntimeException("Unknown game mode: " + player.getGameMode().toString());
			}
		} else {
//			this.unfly(player);
		}
	}



}
*/
