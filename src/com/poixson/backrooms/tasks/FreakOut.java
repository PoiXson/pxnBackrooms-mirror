package com.poixson.backrooms.tasks;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.tools.abstractions.xStartStop;


public class FreakOut extends BukkitRunnable implements xStartStop {

	public static final int LEVEL_DEST   = 771;

	public static final int EFFECT_TICKS = 60;
	public static final int TP_TICKS     = 160;

	protected final BackroomsPlugin plugin;

	protected final Player player;

	protected int index = 0;
	protected int index_total = 0;



	public FreakOut(final BackroomsPlugin plugin, final Player player) {
		this.plugin = plugin;
		this.player = player;
	}



	@Override
	public void start() {
		this.runTaskTimer(this.plugin, 1L, 1L);
	}
	@Override
	public void stop() {
		try {
			this.cancel();
		} catch (Exception ignore) {}
		this.player.setSneaking(false);
		this.player.setSprinting(false);
		this.player.setVisualFire(false);
		this.plugin.removeFreakOut(this.player);
	}
	public void reset() {
		this.index = this.index % 10;
	}



	@Override
	public void run() {
		final int index       = this.index++;
		final int index_total = this.index_total++;
		if (index_total >= TP_TICKS) {
			this.plugin.noclip(this.player, LEVEL_DEST);
			this.stop();
			return;
		}
		if (index >= EFFECT_TICKS) {
			this.stop();
			return;
		}
		this.player.setSneaking(  index % 2 == 0);
		this.player.setSprinting( index % 3 == 1);
		this.player.setVisualFire(index % 5 <  4);
	}



}
