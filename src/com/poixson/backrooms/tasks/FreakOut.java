package com.poixson.backrooms.tasks;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.tools.abstractions.xStartStop;


public class FreakOut extends BukkitRunnable implements xStartStop {

	protected final BackroomsPlugin plugin;

	protected final Player player;

	protected int index = 0;



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
		final int index = this.index++;
		if (index >= 60) {
			this.stop();
			return;
		}
		final int mod2 = index % 2;
		this.player.setSneaking( mod2 == 0);
		this.player.setSprinting(mod2 == 1);
		this.player.setVisualFire(index % 5 < 4);
	}



}
