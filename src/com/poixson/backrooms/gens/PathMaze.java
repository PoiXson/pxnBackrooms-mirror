package com.poixson.backrooms.gens;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.poixson.tools.xJavaPlugin;
import com.poixson.tools.xListener;
import com.poixson.tools.xRand;
import com.poixson.tools.dao.Bab;
import com.poixson.tools.dao.Iab;
import com.poixson.tools.events.SaveEvent;


public class PathMaze extends ConcurrentHashMap<Iab, Bab> {
	private static final long serialVersionUID = 1L;

	protected final xRand random = (new xRand()).seed_time();

	protected final double path_chance;



	public PathMaze(final double path_chance) {
		super();
		this.path_chance = path_chance;
	}



	public void register(final xJavaPlugin plugin) {
		this.listener.register(plugin);
	}
	public void unregister() {
		this.listener.unregister();
	}



	public void load() {
//TODO
	}
	public void save() {
//TODO
	}

	protected final xListener listener = new xListener() {
		@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
		public void onSave(final SaveEvent event) {
			PathMaze.this.save();
		}
	};



	public Bab getMazePart(final int x, final int z) {
		final Iab loc = new Iab(x, z);
		// existing entry
		{
			final Bab dao = super.get(loc);
			if (dao != null)
				return dao;
		}
		// new entry
		{
			int rnd = (int) Math.floor(this.random.nextDbl(0.0, this.path_chance));
			if (rnd > 3) rnd = 0;
			final Bab dao = new Bab(
				(rnd == 1 || rnd == 3),
				(rnd == 2 || rnd == 3)
			);
			final Bab existing = super.putIfAbsent(loc, dao);
			return (existing == null ? dao : existing);
		}
	}

	public boolean getMazeN(final int x, final int z) {
		final Bab dao = this.getMazePart(x, z-1);
		return dao.a;
	}
	public boolean getMazeS(final int x, final int z) {
		final Bab dao = this.getMazePart(x, z+1);
		return dao.a;
	}
	public boolean getMazeE(final int x, final int z) {
		final Bab dao = this.getMazePart(x+1, z);
		return dao.b;
	}
	public boolean getMazeW(final int x, final int z) {
		final Bab dao = this.getMazePart(x-1, z);
		return dao.b;
	}



}
