package com.poixson.backrooms.levels;

import java.util.LinkedList;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.poixson.commonmc.tools.LineTracer;
import com.poixson.tools.dao.Iab;


// 37 | Poolrooms
public class Pop_037 extends BlockPopulator {

	public static final int SUBFLOOR = Level_000.SUBFLOOR;

	public static final double THRESH_TUNNEL = 0.95;

	public static final Material POOL_WALL_A = Gen_037.POOL_WALL_A;
	public static final Material POOL_WALL_B = Gen_037.POOL_WALL_B;

	protected final Gen_037 gen;

	protected final Iab[] starting_points;



	public Pop_037(final Gen_037 gen) {
		super();
		this.gen = gen;
		// find starting points
		{
			final LinkedList<Iab> list = new LinkedList<Iab>();
			int i = 0;
			int x, y;
			while (true) {
				i += 21;
				x = (i % 16);
				y = Math.floorDiv(i, 16);
				if (y >= 16) break;
				list.addLast(new Iab(x, y));
			}
			this.starting_points = list.toArray(new Iab[0]);
		}
	}



	public class TunnelTracer extends LineTracer {
		public final LinkedList<TunnelTracer> otherTracers;
		public final LimitedRegion region;
		public final int y;
		public int ends = 0;

		public TunnelTracer(final LimitedRegion region,
				final LinkedList<TunnelTracer> otherTracers,
				final int x, final int z) {
			super(x, z, false);
			this.region = region;
			this.otherTracers = otherTracers;
			this.y = Pop_037.this.gen.level_y + 9;
			final Material type = region.getType(x, this.y, z);
			if (Material.AIR.equals(type))
				this.ok = false;
		}

		@Override
		public void check(final Iab from) {
			this.checkone(from.a, this.y ,from.b-1); // north
			this.checkone(from.a, this.y ,from.b+1); // south
			this.checkone(from.a+1, this.y ,from.b); // east
			this.checkone(from.a-1, this.y ,from.b); // west
		}
		protected void checkone(final int x, final int y, final int z) {
			if (!this.ok) return;
			final Iab loc = new Iab(x, z);
			if (this.checked.add(loc)) {
				if (this.isValidPoint(x, z)) {
					// already used
					if (this.contains(loc)) return;
					for (final TunnelTracer tracer : this.otherTracers) {
						if (tracer.contains(loc)) {
							this.ok = false;
							return;
						}
					}
					if (!this.region.isInRegion(x, y, z)) {
						this.ok = false;
						return;
					}
					// end of tunnel
					final Material type = this.region.getType(x, y, z);
					if (Material.AIR.equals(type)) {
						this.ends++;
						return;
					}
					if (this.add(loc))
						this.queued.add(loc);
				}
			}
		}

		@Override
		public boolean isValidPoint(final int x, final int y) {
			final double value = Pop_037.this.gen.noiseTunnels.getNoiseRot(x, y, 0.25);
			return (value > THRESH_TUNNEL);
		}

	}



	@Override
	public void populate(final WorldInfo world, final Random rnd,
	final int chunkX, final int chunkZ, final LimitedRegion region) {
		if (!Gen_037.ENABLE_GENERATE) return;
		// trace tunnels
		final LinkedList<TunnelTracer> tunnelTracers = new LinkedList<TunnelTracer>();
		{
			int x, y;
			STARTING_POINTS_LOOP:
			for (final Iab loc : this.starting_points) {
				// part of an existing trace
				for (final TunnelTracer trace : tunnelTracers) {
					if (trace.contains(loc))
						continue STARTING_POINTS_LOOP;
				}
				// trace a tunnel
				x = (chunkX * 16) + loc.a;
				y = (chunkZ * 16) + loc.b;
				final TunnelTracer tracer = new TunnelTracer(region, tunnelTracers, x, y);
				tracer.run();
				if (tracer.ok && tracer.ends == 2) {
					tunnelTracers.addLast(tracer);
					break STARTING_POINTS_LOOP;
				}
			}
		}
		// place blocks for tunnels
		Iab last;
		for (final TunnelTracer tracer : tunnelTracers) {
			final BlockPlotter plotter = new BlockPlotter(region, 6, 7);
			plot.y(this.gen.level_y + 7);
			plot.type('.', Material.AIR);
			plot.type('#', POOL_WALL_A );
			final StringBuilder[] matrix = plot.getMatrix2D();
			matrix[5].append(" ##### ");
			matrix[4].append("##...##");
			matrix[3].append("#.....#");
			matrix[2].append("#.....#");
			matrix[1].append("#.....#");
			matrix[0].append("#######");
			int dirX;
			last = null;
			boolean first = true;
			String axis;
			POINTS_LOOP:
			for (final Iab loc : tracer.points) {
//TODO
if (region.isInRegion(loc.x, 73, loc.y)) region.setType(loc.x, 73, loc.y, Material.GLOWSTONE);
				if (last == null) {
					last = loc;
					continue POINTS_LOOP;
				}
				plot.x(loc.x);
				plot.z(loc.y);
				// find direction
				dirX = loc.x - last.x;
				if (dirX == 0) { plot.axis("YX"); plotter.x(plotter.x() - 3);
				} else {         plot.axis("YZ"); plotter.z(plotter.z() - 3); }
				plot.run();
				if (first) {
					first = false;
					if (dirX == 0) plot.z(last.y);
					else           plot.x(last.x);
					plot.run();
				}
				last = loc;
			}
		}
	}



}
