package com.poixson.backrooms.listeners;

import static com.poixson.backrooms.worlds.Level_771.LEVEL_H;
import static com.poixson.backrooms.worlds.Level_771.LEVEL_Y;
import static com.poixson.utils.BlockUtils.IsButton;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.tools.SeriesBlockChanger;
import com.poixson.tools.events.xListener;


public class Listener_771 implements xListener {

	public static final long DEFAULT_TICKS_PER_BLOCK = 6L;

	protected final BackroomsPlugin plugin;

	protected final AtomicReference<SeriesBlockChanger> block_changer = new AtomicReference<SeriesBlockChanger>(null);



	public Listener_771(final BackroomsPlugin plugin) {
		this.plugin = plugin;
	}



	public void register() {
		xListener.super.register(this.plugin);
	}



	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onPlayerInteract(final PlayerInteractEvent event) {
		final Block block = event.getClickedBlock();
		final int x = block.getX();
		final int y = block.getY();
		final int z = block.getZ();
		if (y == LEVEL_Y + LEVEL_H + 7
		&& (x == 0 || x == -1)
		&& (z == 0 || z == -1)) {
			if (IsButton(block.getType())) {
				final int level = this.plugin.getLevel(block.getLocation());
				if (level == 771)
					this.triggerSecretDoor();
			}
		}
	}



	public void triggerSecretDoor() {
		if (this.block_changer.get() == null) {
			final int y = LEVEL_Y + LEVEL_H;
			final BlockData block_air   = Bukkit.createBlockData("minecraft:air"                         );
			final BlockData block_solid = Bukkit.createBlockData("minecraft:chiseled_polished_blackstone");
			final BlockData block_slab  = Bukkit.createBlockData("minecraft:polished_blackstone_slab"    );
			final World world = this.plugin.getWorldFromLevel(771);
			final LinkedList<Location> locs = new LinkedList<Location>();
			locs.addLast(world.getBlockAt( 0, y,  0).getLocation());
			locs.addLast(world.getBlockAt(-1, y,  0).getLocation());
			locs.addLast(world.getBlockAt(-1, y, -1).getLocation());
			locs.addLast(world.getBlockAt( 0, y, -1).getLocation());
			final SeriesBlockChanger changer =
				new SeriesBlockChanger(this.plugin, DEFAULT_TICKS_PER_BLOCK) {
					@Override
					public void stop() {
						super.stop();
						Listener_771.this.block_changer.set(null);
					}
				};
			for (final Location loc : locs) changer.add(loc, block_slab );
			for (final Location loc : locs) changer.add(loc, block_air  );
			for (final Location loc : locs) changer.add(loc, block_slab );
			for (final Location loc : locs) changer.add(loc, block_solid);
			if (this.block_changer.compareAndSet(null, changer)) {
				changer.start();
				this.plugin.log().info("Crossroads center door opened");
			}
		}
	}



}
