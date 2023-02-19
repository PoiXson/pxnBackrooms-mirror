package com.poixson.backrooms.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.levels.Gen_001;
import com.poixson.backrooms.levels.Level_000;


// level 1 - basement
public class Listener_001 implements Listener {

	public static final int BASEMENT_LIGHT_RADIUS = 20;

	public static final int LAMP_Y = Gen_001.LAMP_Y;

	protected final BackroomsPlugin plugin;

	protected final int level_y;

	protected final HashMap<String, ArrayList<Location>> playerLights =
			new HashMap<String, ArrayList<Location>>();



	public Listener_001(final BackroomsPlugin plugin) {
		this.plugin  = plugin;
		this.level_y = Level_000.Y_001;
	}



	public void register() {
		Bukkit.getPluginManager()
			.registerEvents(this, this.plugin);
	}
	public void unregister() {
		HandlerList.unregisterAll(this);
		synchronized (this.playerLights) {
			Block blk;
			for (final ArrayList<Location> list : this.playerLights.values()) {
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
			final World world = player.getWorld();
			final int level = this.plugin.getLevelFromWorld(world.getName());
			int lvl = (level==0 ? this.plugin.getPlayerLevel(player) : Integer.MIN_VALUE);
			// basement
			if (lvl == 1) {
				final ArrayList<Location> lights = this.getPlayerLightsList(player);
				// turn off lights
				{
//TODO: check other players
					Location loc;
					Block blk;
					final Iterator<Location> it = lights.iterator();
					while (it.hasNext()) {
						loc = it.next();
						if (to.distance(loc) > BASEMENT_LIGHT_RADIUS) {
							it.remove();
							blk = loc.getBlock();
							if (Material.REDSTONE_TORCH.equals(blk.getType()))
								blk.setType(Material.BEDROCK);
						}
					}
				}
				final int y = this.level_y + LAMP_Y + 5;
				final int r = BASEMENT_LIGHT_RADIUS;
				int xx, zz;
				for (int iz=0-r-1; iz<r; iz+=10) {
					zz = Math.floorDiv(toZ+iz, 10) * 10;
					for (int ix=0-r-1; ix<r; ix+=10) {
						xx = Math.floorDiv(toX+ix, 10) * 10;
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
				final String uuid = player.getUniqueId().toString();
				if (this.playerLights.containsKey(uuid)) {
					Block blk;
					for (final Location loc : this.playerLights.get(uuid)) {
						blk = loc.getBlock();
						if (Material.REDSTONE_TORCH.equals(blk.getType()))
							blk.setType(Material.BEDROCK);
					}
					this.playerLights.remove(uuid);
				}
			}
		}
	}



	public ArrayList<Location> getPlayerLightsList(final Player player) {
		return this.getPlayerLightsList(player.getUniqueId().toString());
	}
	public ArrayList<Location> getPlayerLightsList(final String uuid) {
		// existing list
		{
			final ArrayList<Location> list = this.playerLights.get(uuid);
			if (list != null)
				return list;
		}
		// cleanup
//TODO: improve this
		if (this.playerLights.size() % 5 == 0) {
			String key;
			ArrayList<Location> list;
			final Iterator<String> it = this.playerLights.keySet().iterator();
			while (it.hasNext()) {
				key = it.next();
				list = this.playerLights.get(key);
				if (list == null || list.isEmpty())
					this.playerLights.remove(key);
			}
		}
		// new list
		{
			final ArrayList<Location> list = new ArrayList<Location>();
			this.playerLights.put(uuid, list);
			return list;
		}
	}



}
