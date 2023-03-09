package com.poixson.backrooms.levels;

import static com.poixson.backrooms.levels.Level_000.SUBCEILING;
import static com.poixson.backrooms.levels.Level_000.SUBFLOOR;

import java.util.LinkedList;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.levels.Level_000.PregenLevel0;
import com.poixson.commonmc.tools.plotter.BlockPlotter;
import com.poixson.tools.dao.Iab;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;
import com.poixson.utils.StringUtils;


// 37 | Poolrooms
public class Gen_037 extends GenBackrooms {

	public static final boolean ENABLE_GENERATE = true;
	public static final boolean ENABLE_ROOF     = true;

	public static final int WATER_DEPTH = 3;

	public static final Material POOL_WALL_A  = Material.BLUE_TERRACOTTA;
	public static final Material POOL_WALL_B  = Material.LIGHT_BLUE_TERRACOTTA;
	public static final Material POOL_CEILING = Material.GLOWSTONE;

	public static final Material POOLS_SUBFLOOR   = Material.DARK_PRISMARINE;
	public static final Material POOLS_SUBCEILING = Material.DARK_PRISMARINE;


	// noise
	protected final FastNoiseLiteD noisePoolRooms;
	protected final FastNoiseLiteD noiseTunnels;

	// populators
	public final Pop_037 popPools;



	public Gen_037(final LevelBackrooms backlevel,
			final int level_y, final int level_h) {
		super(backlevel, level_y, level_h);
		// pool rooms
		this.noisePoolRooms = this.register(new FastNoiseLiteD());
		this.noisePoolRooms.setFrequency(0.004);
		this.noisePoolRooms.setFractalOctaves(2);
		this.noisePoolRooms.setNoiseType(NoiseType.OpenSimplex2);
		this.noisePoolRooms.setFractalType(FractalType.PingPong);
		this.noisePoolRooms.setFractalGain(0.1);
		this.noisePoolRooms.setFractalPingPongStrength(2.8);
		// tunnels
		this.noiseTunnels = this.register(new FastNoiseLiteD());
		this.noiseTunnels.setFrequency(0.015);
		this.noiseTunnels.setFractalOctaves(1);
		this.noiseTunnels.setNoiseType(NoiseType.Cellular);
		this.noiseTunnels.setFractalType(FractalType.PingPong);
		this.noiseTunnels.setFractalPingPongStrength(5.0);
		this.noiseTunnels.setCellularDistanceFunction(CellularDistanceFunction.Manhattan);
		// populators
		this.popPools = new Pop_037(this);
	}



	public enum RoomType {
		OPEN,
		SOLID,
	};

	public class PoolData implements PreGenData {

		public final double valueRoom, valuePortalHotel, valuePortalLobby;
		public RoomType type;

		public PoolData(final int x, final int z) {
			this.valueRoom        = Gen_037.this.noisePoolRooms.getNoise(x, z);
			if (this.valueRoom < THRESH_ROOM) {
				this.type = RoomType.SOLID;
			} else {
				this.type = RoomType.OPEN;
			}
		}

		public boolean isSolid() {
			return RoomType.SOLID.equals(this.type);
		}

	}



	public void pregenerate(final Map<Iab, PoolData> data,
			final int chunkX, final int chunkZ) {
		PoolData dao;
		int xx, zz;
		for (int rz=-1; rz<3; rz++) {
			zz = (chunkZ * 16) + (rz * 8) + 4;
			for (int rx=-1; rx<3; rx++) {
				xx = (chunkX * 16) + (rx * 8) + 4;
				dao = new PoolData(xx, zz);
				data.put(new Iab(rx, rz), dao);
			}
		}
	}



	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
		if (!ENABLE_GENERATE) return;
		final Map<Iab, PoolData> poolData = ((PregenLevel0)pregen).pools;
		final int y  = this.level_y + SUBFLOOR + 1;
		final int cy = this.level_h + y + 1;
		final int h  = this.level_h + 2;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
				// subfloor
				chunk.setBlock(ix, this.level_y, iz, Material.BEDROCK);
				for (int iy=0; iy<SUBFLOOR; iy++)
					chunk.setBlock(ix, this.level_y+iy+1, iz, POOLS_SUBFLOOR);
				// subceiling
				if (ENABLE_ROOF) {
					for (int iy=0; iy<SUBCEILING; iy++)
						chunk.setBlock(ix, cy+iy+2, iz, POOLS_SUBCEILING);
				}
			}
		}
		PoolData dao;
		boolean solid_n,  solid_s,  solid_e,  solid_w;
		boolean solid_ne, solid_nw, solid_se, solid_sw;
		for (int rz=0; rz<2; rz++) {
			for (int rx=0; rx<2; rx++) {
				final BlockPlotter plot = new BlockPlotter(chunk, h, 8, 8);
				plot.axis("YZX").location(rx*8, y, rz*8);
				plot.type('#', POOL_WALL_A   );
				plot.type('@', POOL_WALL_B   );
				plot.type('w', Material.WATER);
				plot.type('g', POOL_CEILING  );
				dao = poolData.get(new Iab(rx, rz));
				solid_n  = poolData.get(new Iab(rx,   rz-1)).isSolid();
				solid_s  = poolData.get(new Iab(rx,   rz+1)).isSolid();
				solid_e  = poolData.get(new Iab(rx+1, rz  )).isSolid();
				solid_w  = poolData.get(new Iab(rx-1, rz  )).isSolid();
				solid_ne = poolData.get(new Iab(rx+1, rz-1)).isSolid();
				solid_nw = poolData.get(new Iab(rx-1, rz-1)).isSolid();
				solid_se = poolData.get(new Iab(rx+1, rz+1)).isSolid();
				solid_sw = poolData.get(new Iab(rx-1, rz+1)).isSolid();
				final StringBuilder[][] matrix = plot.getMatrix3D();
				switch (dao.type) {
				case SOLID: {
					for (int iz=0; iz<8; iz++) {
						for (int iy=0; iy<h; iy++)
							matrix[iy][iz].append(StringUtils.Repeat(8, '@'));
					}
					// outside-corner
					{
						// north/east outside-corner
						if (!solid_n && !solid_e && !solid_ne) {
							StringUtils.ReplaceInString(matrix[0][0], "####", 4);
							StringUtils.ReplaceInString(matrix[0][1], "##",   6);
							StringUtils.ReplaceInString(matrix[0][2], "#",    7);
							for (int iy=1; iy<h; iy++) {
								StringUtils.ReplaceInString(matrix[iy][0], "    ", 4);
								StringUtils.ReplaceInString(matrix[iy][1], "  ",   6);
								StringUtils.ReplaceInString(matrix[iy][2], " ",    7);
							}
						}
						// north/west outside-corner
						if (!solid_n && !solid_w && !solid_nw) {
							StringUtils.ReplaceInString(matrix[0][0], "####", 0);
							StringUtils.ReplaceInString(matrix[0][1], "##",   0);
							StringUtils.ReplaceInString(matrix[0][2], "#",    0);
							for (int iy=1; iy<h; iy++) {
								StringUtils.ReplaceInString(matrix[iy][0], "    ", 0);
								StringUtils.ReplaceInString(matrix[iy][1], "  ",   0);
								StringUtils.ReplaceInString(matrix[iy][2], " ",    0);
							}
						}
						// south/east outside-corner
						if (!solid_s && !solid_e && !solid_se) {
							StringUtils.ReplaceInString(matrix[0][7], "####", 4);
							StringUtils.ReplaceInString(matrix[0][6], "##",   6);
							StringUtils.ReplaceInString(matrix[0][5], "#",    7);
							for (int iy=1; iy<h; iy++) {
								StringUtils.ReplaceInString(matrix[iy][7], "    ", 4);
								StringUtils.ReplaceInString(matrix[iy][6], "  ",   6);
								StringUtils.ReplaceInString(matrix[iy][5], " ",    7);
							}
						}
						// south/west outside-corner
						if (!solid_s && !solid_w && !solid_sw) {
							StringUtils.ReplaceInString(matrix[0][7], "####", 0);
							StringUtils.ReplaceInString(matrix[0][6], "##",   0);
							StringUtils.ReplaceInString(matrix[0][5], "#",    0);
							for (int iy=1; iy<h; iy++) {
								StringUtils.ReplaceInString(matrix[iy][7], "    ", 0);
								StringUtils.ReplaceInString(matrix[iy][6], "  ",   0);
								StringUtils.ReplaceInString(matrix[iy][5], " ",    0);
							}
						}
					}
					// cross-corner
					{
						// north/east cross-corner
						if (!solid_n && !solid_e && solid_ne) {
							StringUtils.ReplaceInString(matrix[0][0], "#####", 3);
							StringUtils.ReplaceInString(matrix[0][1], "###",   5);
							StringUtils.ReplaceInString(matrix[0][2], "##",    6);
							StringUtils.ReplaceInString(matrix[0][3], "#",     7);
							StringUtils.ReplaceInString(matrix[0][4], "#",     7);
							for (int iy=1; iy<h; iy++) {
								StringUtils.ReplaceInString(matrix[iy][0], "     ", 3);
								StringUtils.ReplaceInString(matrix[iy][1], "   ",   5);
								StringUtils.ReplaceInString(matrix[iy][2], "  ",    6);
								StringUtils.ReplaceInString(matrix[iy][3], " ",     7);
								StringUtils.ReplaceInString(matrix[iy][4], " ",     7);
							}
						}
						// north/west cross-corner
						if (!solid_n && !solid_w && solid_nw) {
							StringUtils.ReplaceInString(matrix[0][0], "#####", 0);
							StringUtils.ReplaceInString(matrix[0][1], "###",   0);
							StringUtils.ReplaceInString(matrix[0][2], "##",    0);
							StringUtils.ReplaceInString(matrix[0][3], "#",     0);
							StringUtils.ReplaceInString(matrix[0][4], "#",     0);
							for (int iy=1; iy<h; iy++) {
								StringUtils.ReplaceInString(matrix[iy][0], "     ", 0);
								StringUtils.ReplaceInString(matrix[iy][1], "   ",   0);
								StringUtils.ReplaceInString(matrix[iy][2], "  ",    0);
								StringUtils.ReplaceInString(matrix[iy][3], " ",     0);
								StringUtils.ReplaceInString(matrix[iy][4], " ",     0);
							}
						}
						// south/east cross-corner
						if (!solid_s && !solid_e && solid_se) {
							StringUtils.ReplaceInString(matrix[0][7], "#####", 3);
							StringUtils.ReplaceInString(matrix[0][6], "###",   5);
							StringUtils.ReplaceInString(matrix[0][5], "##",    6);
							StringUtils.ReplaceInString(matrix[0][4], "#",     7);
							StringUtils.ReplaceInString(matrix[0][3], "#",     7);
							for (int iy=1; iy<h; iy++) {
								StringUtils.ReplaceInString(matrix[iy][7], "     ", 3);
								StringUtils.ReplaceInString(matrix[iy][6], "   ",   5);
								StringUtils.ReplaceInString(matrix[iy][5], "  ",    6);
								StringUtils.ReplaceInString(matrix[iy][4], " ",     7);
								StringUtils.ReplaceInString(matrix[iy][3], " ",     7);
							}
						}
						// south/west cross-corner
						if (!solid_s && !solid_w && solid_sw) {
							StringUtils.ReplaceInString(matrix[0][7], "#####", 0);
							StringUtils.ReplaceInString(matrix[0][6], "###",   0);
							StringUtils.ReplaceInString(matrix[0][5], "##",    0);
							StringUtils.ReplaceInString(matrix[0][4], "#",     0);
							StringUtils.ReplaceInString(matrix[0][3], "#",     0);
							for (int iy=1; iy<h; iy++) {
								StringUtils.ReplaceInString(matrix[iy][7], "     ", 0);
								StringUtils.ReplaceInString(matrix[iy][6], "   ",   0);
								StringUtils.ReplaceInString(matrix[iy][5], "  ",    0);
								StringUtils.ReplaceInString(matrix[iy][4], " ",     0);
								StringUtils.ReplaceInString(matrix[iy][3], " ",     0);
							}
						}
					}
					break;
				}
				case OPEN: {
					for (int iz=0; iz<8; iz++) {
						matrix[0][iz].append(StringUtils.Repeat(8, '#'));
						for (int iy=1; iy<h; iy++)
							matrix[iy][iz].append(StringUtils.Repeat(8, ' '));
					}
					// inside corner
					{
						// north/east inside corner
						if (solid_n && solid_e && solid_ne) {
							for (int iy=0; iy<h; iy++) {
								StringUtils.ReplaceInString(matrix[iy][0], "@@", 6);
								StringUtils.ReplaceInString(matrix[iy][1], "@",  7);
							}
						}
						// north/west inside corner
						if (solid_n && solid_w && solid_nw) {
							for (int iy=0; iy<h; iy++) {
								StringUtils.ReplaceInString(matrix[iy][0], "@@", 0);
								StringUtils.ReplaceInString(matrix[iy][1], "@",  0);
							}
						}
						// south/east inside corner
						if (solid_s && solid_e && solid_se) {
							for (int iy=0; iy<h; iy++) {
								StringUtils.ReplaceInString(matrix[iy][7], "@@", 6);
								StringUtils.ReplaceInString(matrix[iy][6], "@",  7);
							}
						}
						// south/west inside corner
						if (solid_s && solid_w && solid_sw) {
							for (int iy=0; iy<h; iy++) {
								StringUtils.ReplaceInString(matrix[iy][7], "@@", 0);
								StringUtils.ReplaceInString(matrix[iy][6], "@",  0);
							}
						}
					}
					break;
				}
				default: throw new RuntimeException("Unknown pool room type: " + dao.type.toString());
				} // end type switch
				// water
				for (int iz=0; iz<8; iz++) {
					for (int iy=0; iy<=WATER_DEPTH; iy++)
						StringUtils.ReplaceWith(matrix[iy][iz], ' ', 'w');
				}
				// ceiling
				if (ENABLE_ROOF) {
					for (int iz=0; iz<8; iz++)
						StringUtils.ReplaceWith(matrix[h-1][iz], ' ', 'g');
				}
				plot.run();
			} // end room x
		} // end room z
	}



}
