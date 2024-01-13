package com.poixson.backrooms.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.gens.Gen_001;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.tools.events.PlayerMoveNormalEvent;
import com.poixson.tools.events.xListener;


// 1 | Basement lights
public class Listener_MoveNormal extends xListener<BackroomsPlugin> {

	public static final int BASEMENT_LIGHT_RADIUS = 20;

	public static final int LAMP_Y = Gen_001.LAMP_Y;

	protected final HashMap<UUID, List<Location>> playerLights = new HashMap<UUID, List<Location>>();



	public Listener_MoveNormal(final BackroomsPlugin plugin) {
		super(plugin);
	}



	@Override
	public void unregister() {
		super.unregister();
		synchronized (this.playerLights) {
			Block blk;
			for (final List<Location> list : this.playerLights.values()) {
				for (final Location loc : list) {
					blk = loc.getBlock();
					if (Material.REDSTONE_TORCH.equals(blk.getType()))
						blk.setType(Material.BEDROCK);
				}
			}
			this.playerLights.clear();
		}
	}



	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onPlayerMoveNormal(final PlayerMoveNormalEvent event) {
		final Player player = event.getPlayer();
		final int level = this.plugin.getLevel(player);
		// basement
		if (level == 1) {
			final Location to = event.getTo();
			final World world = to.getWorld();
			final List<Location> lights = this.getPlayerLightsList(player.getUniqueId());
			// turn off lights
			{
				Location loc;
				Block blk;
				final Iterator<Location> it = lights.iterator();
				while (it.hasNext()) {
					loc = it.next();
					if (to.distance(loc) > BASEMENT_LIGHT_RADIUS) {
						it.remove();
						if (this.canTurnOff(loc)) {
							blk = loc.getBlock();
							if (Material.REDSTONE_TORCH.equals(blk.getType()))
								blk.setType(Material.BEDROCK);
						}
					}
				}
			}
			final int y = Level_000.Y_001 + LAMP_Y + 5;
			final int r = BASEMENT_LIGHT_RADIUS;
			int xx, zz;
			for (int iz=0-r-1; iz<r; iz+=10) {
				zz = Math.floorDiv(iz+to.getBlockZ(), 10) * 10;
				for (int ix=0-r-1; ix<r; ix+=10) {
					xx = Math.floorDiv(ix+to.getBlockX(), 10) * 10;
					final Block blk = world.getBlockAt(xx, y, zz);
					if (to.distance(blk.getLocation()) < BASEMENT_LIGHT_RADIUS) {
						if (Material.BEDROCK.equals(blk.getType())
						||  Material.REDSTONE_TORCH.equals(blk.getType()) ) {
							lights.add(blk.getLocation());
							world.setType(xx, y, zz, Material.REDSTONE_TORCH);
						}
					}
				}
			}
		// not basement
		} else {
			// player left the basement
			final UUID uuid = player.getUniqueId();
			if (this.playerLights.containsKey(uuid)) {
				// turn off all lights for player
				final List<Location> list = this.playerLights.get(uuid);
				this.playerLights.remove(uuid);
				for (final Location loc : list)
					this.lightTurnOff(loc);
				this.playerLights.remove(uuid);
			}
		}
	}



	protected void lightTurnOff(final Location loc) {
		if (this.canTurnOff(loc)) {
			final Block block = loc.getBlock();
			if (Material.REDSTONE_TORCH.equals(block.getType()))
				block.setType(Material.BEDROCK);
		}
	}
	protected boolean canTurnOff(final Location loc) {
		for (final List<Location> list : this.playerLights.values()) {
			if (list.contains(loc))
				return false;
		}
		return true;
	}



	protected List<Location> getPlayerLightsList(final UUID uuid) {
		// existing list
		{
			final List<Location> list = this.playerLights.get(uuid);
			if (list != null)
				return list;
		}
		// new list
		{
			final List<Location> list = new ArrayList<Location>();
			this.playerLights.put(uuid, list);
			return list;
		}
	}



}
