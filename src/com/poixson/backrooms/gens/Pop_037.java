package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_000.ENABLE_GEN_037;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.generator.LimitedRegion;

import com.poixson.backrooms.BackroomsPop;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.pluginlib.tools.LineTracer;
import com.poixson.pluginlib.tools.plotter.BlockPlotter;
import com.poixson.tools.dao.Iab;


// 37 | Poolrooms
public class Pop_037 implements BackroomsPop {

	public static final int SUBFLOOR = Level_000.SUBFLOOR;

	public static final double THRESH_TUNNEL = 0.95;

	protected final Gen_037 gen;

	protected final Iab[] starting_points;



	public Pop_037(final Level_000 level0) {
		super();
		this.gen = level0.gen_037;
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
	public void populate(final int chunkX, final int chunkZ,
	final LimitedRegion region, final LinkedList<BlockPlotter> plots) {
		if (!ENABLE_GEN_037) return;
		final Material block_wall_a = Material.matchMaterial(this.gen.block_wall_a.get());
		final Material block_wall_b = Material.matchMaterial(this.gen.block_wall_b.get());
		if (block_wall_a == null) throw new RuntimeException("Invalid block type for level 37 WallA");
		if (block_wall_b == null) throw new RuntimeException("Invalid block type for level 37 WallB");
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
		for (final TunnelTracer tracer : tunnelTracers) {
			int x_low  = Integer.MAX_VALUE;
			int z_low  = Integer.MAX_VALUE;
			int x_high = Integer.MIN_VALUE;
			int z_high = Integer.MIN_VALUE;
			for (final Iab loc : tracer.points) {
				if (x_low  > loc.a) x_low  = loc.a;
				if (z_low  > loc.b) z_low  = loc.b;
				if (x_high < loc.a) x_high = loc.a;
				if (z_high < loc.b) z_high = loc.b;
			}
			if (x_low == Integer.MAX_VALUE
			||  z_low == Integer.MAX_VALUE
			||  x_high == Integer.MIN_VALUE
			||  z_high == Integer.MIN_VALUE)
				continue;
			x_low -= 3; x_high += 4;
			z_low -= 3; z_high += 4;
			int w = Math.abs(x_high - x_low);
			int d = Math.abs(z_high - z_low);
			int xx, zz;
			final int yy = this.gen.level_y + 7;
			for (int iz=0; iz<d; iz++) {
				zz = z_low + iz;
				for (int ix=0; ix<w; ix++) {
					xx = x_low + ix;
					int distance = Integer.MAX_VALUE;
					int dist;
					for (final Iab loc : tracer.points) {
						dist = ShortestDistance(xx, zz, loc.a, loc.b);
						if (distance > dist)
							distance = dist;
						if (distance == 0)
							break;
					}
					if (region.isInRegion(xx, 0, zz)) {
						if (distance < 2) {
							if (block_wall_b.equals(region.getType(xx, yy, zz)))
								region.setType(xx, yy, zz, block_wall_a);
							for (int iy=1; iy<5; iy++) {
								if (block_wall_b.equals(region.getType(xx, yy+iy, zz)))
									region.setType(xx, yy+iy, zz, Material.AIR);
							}
							if (block_wall_b.equals(region.getType(xx, yy+5, zz)))
								region.setType(xx, yy+5, zz, block_wall_a);
						} else
						if (distance == 2) {
							if (block_wall_b.equals(region.getType(xx, yy, zz)))
								region.setType(xx, yy, zz, block_wall_a);
							for (int iy=1; iy<4; iy++) {
								if (block_wall_b.equals(region.getType(xx, yy+iy, zz)))
									region.setType(xx, yy+iy, zz, Material.AIR);
							}
							if (block_wall_b.equals(region.getType(xx, yy+4, zz)))
								region.setType(xx, yy+4, zz, block_wall_a);
						} else
						if (distance == 3) {
							for (int iy=0; iy<4; iy++) {
								if (block_wall_b.equals(region.getType(xx, yy+iy, zz)))
									region.setType(xx, yy+iy, zz, block_wall_a);
							}
						}
					}
				} // end for ix
			} // end for iz
		} // end tracers loop
	}



	public static int ShortestDistance(final int x1, final int z1, final int x2, final int z2) {
		return Math.max(
			Math.min( Math.abs(x1-x2), Math.abs(x2-x1) ),
			Math.min( Math.abs(z1-z2), Math.abs(z2-z1) )
		);
	}



}
