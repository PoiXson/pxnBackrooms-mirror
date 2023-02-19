package com.poixson.backrooms.listeners;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.poixson.backrooms.BackroomsPlugin;


// level 78 - space
public class Listener_078 implements Listener {

	public static final int GRAVITY_REACH = 10;

	protected final BackroomsPlugin plugin;

	protected final ArrayList<UUID> floating = new ArrayList<UUID>();



	public Listener_078(final BackroomsPlugin plugin) {
		this.plugin = plugin;
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
			final UUID uuid = player.getUniqueId();
			final World world = player.getWorld();
			final int level = this.plugin.getLevelFromWorld(world.getName());
			if (level == 78) {
				// teleport top/bottom of the world
				if (toY < -64) {
					while (to.getBlockY() < -64) {
						to.add(0, 384, 0);
					}
				} else
				if (toY > 319) {
					while (to.getBlockY() > 319) {
						to.subtract(0, 384, 0);
					}
				}
				// fly in space
				switch (player.getGameMode()) {
				case CREATIVE:
				case SPECTATOR:
					player.setAllowFlight(true);
					player.setGravity(true);
					this.floating.remove(uuid);
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
					// gravity
					if (grounded) {
						if (this.floating.remove(uuid)) {
							player.setFlying(false);
							player.setAllowFlight(false);
							player.setGravity(true);
							player.setGlowing(false);
						}
					// floating
					} else {
						if (!this.floating.contains(uuid)) {
							this.floating.add(uuid);
							player.setAllowFlight(true);
							player.setGravity(false);
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
							}).init(player).runTaskLater(this.plugin, 2L);
						}
					}
					break;
				}
				default: throw new RuntimeException("Unknown game mode: "+player.getGameMode().toString());
				}
			} else {
				this.floating.remove(uuid);
			}
		}
	}



}
