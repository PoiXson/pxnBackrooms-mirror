/*
package com.poixson.backrooms.gens;

import static com.poixson.utils.LocationUtils.FaceToIxyz;

import org.bukkit.block.BlockFace;

import com.poixson.tools.dao.Iabc;


public class DataHolder_City {

	protected final Gen_011 gen_011;

	protected final double thresh_road;
	protected final double thresh_alley;
	protected final double building_height_base;
	protected final double building_height_factor;

	public final CityData[][] data;



	public DataHolder_City(final Gen_011 gen_011, final int chunkX, final int chunkZ) {
		this.gen_011      = gen_011;
		this.thresh_road  = gen_011.thresh_road;
		this.thresh_alley = gen_011.thresh_alley;
		this.building_height_base   = gen.building_height_base.get();
		this.building_height_factor = gen.building_height_base.get();
		final CityData[][] data = new CityData[48][48];
		int xx, zz;
		for (int iz=0; iz<48; iz++) {
			zz = (chunkZ * 16) + iz;
			for (int ix=0; ix<48; ix++) {
				xx = (chunkX * 16) + ix;
				data[iz][ix] = new CityData(xx, zz);
			}
		}
		this.data = data;
	}



	public void findEdges() {
		final int distance = 6;
		final BlockFace[] dirs = new BlockFace[] {
			BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
		};
		final BlockFace[] dirs8 = new BlockFace[] {
			BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST,
			BlockFace.NORTH_EAST, BlockFace.NORTH_WEST,
			BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST
		};
		int xx, zz;
		// clear alley along road edge
		for (int iz=0; iz<48; iz++) {
			for (int ix=0; ix<48; ix++) {
				boolean isClearing = false;
				final CityData entry = this.data[iz][ix];
				// road edge
				if (entry.isRoad) {
					for (final BlockFace dir : dirs) {
						final Iabc add = FaceToIxyz(dir);
						for (int i=distance-1; i>=1; i--) {
							xx = ix + (add.a * i);
							zz = iz + (add.c * i);
							final CityData entry_near = this.get(xx, zz);
							if (entry_near != null) {
								// distance to building
								if (!entry_near.isRoadOrAlley()) {
									if (entry.edge_road > i)
										entry.edge_road = i;
								}
								// clear alley along road
								if (isClearing)
									entry_near.isAlley = false;
								else
								if (!entry_near.isRoadOrAlley())
									isClearing = true;
							}
						}
					}
				} // end dirs
			} // end ix
		} // end iz
		// road/alley edges
		for (int iz=0; iz<48; iz++) {
			for (int ix=0; ix<48; ix++) {
				final CityData entry = this.data[iz][ix];
				for (final BlockFace dir : dirs8) {
					final Iabc add = FaceToIxyz(dir);
					final CityData entry_near = this.get(ix+add.a, iz+add.c);
					if (entry_near != null) {
						// road edge
						if (entry.isRoad) {
							if (!entry_near.isRoadOrAlley())
								entry_near.isEdgeMain = true;
						} else
						// alley edge
						if (entry.isAlley) {
							if (!entry_near.isRoadOrAlley())
								entry_near.isEdgeBack = true;
						}
					}
				} // end dirs
			} // end ix
		} // end iz
	}



	public CityData get(final int x, final int z) {
		if (x < 0 || x >= 48) return null;
		if (z < 0 || z >= 48) return null;
		return this.data[z][x];
	}



	public class CityData {

		public final double value_road;
		public final double value_alley;

		public boolean isRoad;
		public boolean isAlley;

		public int edge_road = Integer.MAX_VALUE;
		public int edge_low  = Integer.MAX_VALUE;
		public int edge_high = Integer.MAX_VALUE;

		public boolean isEdgeMain = false;
		public boolean isEdgeBack = false;

		public final double building_height_dbl;
		public final int    building_height_int;

		public CityData(final int x, final int z) {
			final Gen_011 gen_011 = DataHolder_City.this.gen_011;
			this.value_road  = gen_011.noiseRoad .getNoiseRot(x, z, 0.25);
			this.value_alley = gen_011.noiseAlley.getNoiseRot(x, z, 0.25);
			this.isRoad  = (this.value_road  > DataHolder_City.this.thresh_road );
			this.isAlley = (this.value_alley > DataHolder_City.this.thresh_alley);
			this.building_height_dbl = this.getBuildingHeight(x, z);
			this.building_height_int = (int) Math.floor(this.building_height_dbl);
		}

		public double getBuildingHeight(final int x, final int z) {
			final double building_height_base   = DataHolder_City.this.building_height_base;
			final double building_height_factor = DataHolder_City.this.building_height_factor;
			final double value_height = this.gen.noiseHeight.getNoise(x, z) + 1.0;
			final int h = (int) ((value_height * building_height_factor) + building_height_base);
			return NumberUtils.MinMax(h, (int)building_height_base, 200);
		}

		public boolean isRoadOrAlley() {
			return this.isRoad || this.isAlley;
		}
		public boolean isEdge() {
			return this.isEdgeMain || this.isEdgeBack;
		}

	}



}
*/
