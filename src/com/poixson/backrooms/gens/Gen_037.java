package com.poixson.backrooms.gens;

import static com.poixson.utils.BlockUtils.StringToBlockDataDef;

import java.util.LinkedList;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsWorld;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.gens.Gen_000.LobbyData;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.backrooms.worlds.Level_000.Pregen_Level_000;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.dao.Iab;
import com.poixson.tools.noise.FastNoiseLiteD;
import com.poixson.tools.noise.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.tools.noise.FastNoiseLiteD.FractalType;
import com.poixson.tools.noise.FastNoiseLiteD.NoiseType;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.utils.StringUtils;


// 37 | Poolrooms
public class Gen_037 extends BackroomsGen {

	// default params
	public static final int    DEFAULT_LEVEL_H                   = 14;
	public static final int    DEFAULT_WATER_DEPTH               = 4;
	public static final int    DEFAULT_SUBFLOOR                  = 3;
	public static final int    DEFAULT_SUBCEILING                = 3;
	public static final double DEFAULT_NOISE_ROOM_FREQ           = 0.004;
	public static final int    DEFAULT_NOISE_ROOM_OCTAVE         = 2;
	public static final double DEFAULT_NOISE_ROOM_GAIN           = 0.1;
	public static final double DEFAULT_NOISE_ROOM_STRENGTH       = 2.8;
	public static final double DEFAULT_NOISE_TUNNEL_FREQ         = 0.015;
	public static final double DEFAULT_NOISE_TUNNEL_STRENGTH     = 5.0;
	public static final double DEFAULT_NOISE_PORTAL_LOBBY_FREQ   = 0.02;
	public static final int    DEFAULT_NOISE_PORTAL_LOBBY_OCTAVE = 2;
	public static final double DEFAULT_NOISE_PORTAL_HOTEL_FREQ   = 0.01;
	public static final double DEFAULT_THRESH_ROOM               = 0.2;
	public static final double DEFAULT_THRESH_PORTAL             = 0.5;
	public static final double DEFAULT_THRESH_TUNNEL             = 0.95;

	// default blocks
	public static final String DEFAULT_BLOCK_WALL_A     = "minecraft:prismarine_bricks";
	public static final String DEFAULT_BLOCK_WALL_B     = "minecraft:prismarine";
	public static final String DEFAULT_BLOCK_SUBFLOOR   = "minecraft:dark_prismarine";
	public static final String DEFAULT_BLOCK_SUBCEILING = "minecraft:dark_prismarine";
	public static final String DEFAULT_BLOCK_CEILING    = "minecraft:glowstone";

	// params
	public final boolean enable_gen;
	public final boolean enable_top;
	public final int     level_y;
	public final int     level_h;
	public final int     water_depth;
	public final int     subfloor;
	public final int     subceiling;
	public final double  thresh_room;
	public final double  thresh_portal;
	public final double  thresh_tunnel;

	// blocks
	public final String block_wall_a;
	public final String block_wall_b;
	public final String block_subfloor;
	public final String block_subceiling;
	public final String block_ceiling;

	// noise
	public final FastNoiseLiteD noisePoolRooms;
	public final FastNoiseLiteD noiseTunnels;
	public final FastNoiseLiteD noisePortalLobby;
	public final FastNoiseLiteD noisePortalHotel;

	public final Level_000 level_000;



	public Gen_037(final BackroomsWorld backworld, final int seed, final BackroomsGen gen_below) {
		super(backworld, gen_below, seed);
		final int level_number = this.getLevelNumber();
		this.level_000 = (Level_000) backworld;
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(level_number);
		final ConfigurationSection cfgBlocks = this.plugin.getConfigLevelBlocks(level_number);
		// params
		this.enable_gen    = cfgParams.getBoolean("Enable-Gen"   );
		this.enable_top    = cfgParams.getBoolean("Enable-Top"   );
		this.level_y       = cfgParams.getInt(    "Level-Y"      );
		this.level_h       = cfgParams.getInt(    "Level-Height" );
		this.water_depth   = cfgParams.getInt(    "Water-Depth"  );
		this.subfloor      = cfgParams.getInt(    "SubFloor"     );
		this.subceiling    = cfgParams.getInt(    "SubCeiling"   );
		this.thresh_room   = cfgParams.getDouble( "Thresh-Room"  );
		this.thresh_portal = cfgParams.getDouble( "Thresh-Portal");
		this.thresh_tunnel = cfgParams.getDouble( "Thresh-Tunnel");
		// block types
		this.block_wall_a     = cfgBlocks.getString("WallA"     );
		this.block_wall_b     = cfgBlocks.getString("WallB"     );
		this.block_subfloor   = cfgBlocks.getString("SubFloor"  );
		this.block_subceiling = cfgBlocks.getString("SubCeiling");
		this.block_ceiling    = cfgBlocks.getString("Ceiling"   );
		// noise
		this.noisePoolRooms   = this.register(new FastNoiseLiteD());
		this.noiseTunnels     = this.register(new FastNoiseLiteD());
		this.noisePortalLobby = this.register(new FastNoiseLiteD());
		this.noisePortalHotel = this.register(new FastNoiseLiteD());
	}



	@Override
	public int getLevelNumber() {
		return 37;
	}




	@Override
	public int getLevelY() {
		return this.level_y;
	}
	@Override
	public int getOpenY() {
		return this.getMinY() + this.subfloor + 1;
	}

	@Override
	public int getMinY() {
		return this.getLevelY() + this.bedrock_barrier;
	}
	@Override
	public int getMaxY() {
		return this.getMinY() + this.subfloor + this.level_h + this.subceiling + 1;
	}



	public enum RoomType {
		OPEN,
		SOLID,
	};



	public class PoolData {

		public final double value_room, value_portal_hotel, value_portal_lobby;
		public final boolean possible_portal_hotel;
		public final boolean possible_portal_lobby;
		public RoomType type;

		public PoolData(final int x, final int z) {
			this.value_room         = Gen_037.this.noisePoolRooms  .getNoise(x, z);
			this.value_portal_hotel = Gen_037.this.noisePortalHotel.getNoise(x, z);
			this.value_portal_lobby = Gen_037.this.noisePortalLobby.getNoise(x, z);
			if (this.value_room < Gen_037.this.thresh_room) {
				this.type = RoomType.SOLID;
				this.possible_portal_hotel = (this.value_portal_hotel > Gen_037.this.thresh_portal);
				this.possible_portal_lobby = false;
			} else {
				this.type = RoomType.OPEN;
				this.possible_portal_hotel = false;
				this.possible_portal_lobby = (
					this.value_portal_lobby > Gen_037.this.noisePortalLobby.getNoise(x, z-1) &&
					this.value_portal_lobby > Gen_037.this.noisePortalLobby.getNoise(x, z+1) &&
					this.value_portal_lobby > Gen_037.this.noisePortalLobby.getNoise(x+1, z) &&
					this.value_portal_lobby > Gen_037.this.noisePortalLobby.getNoise(x-1, z)
				);
			}
		}

		public boolean isSolid() {
			return RoomType.SOLID.equals(this.type);
		}

	}



	public void pregenerate(final Map<Iab, PoolData> data,
			final int chunkX, final int chunkZ) {
		for (int iz=-1; iz<3; iz++) {
			final int zz = (chunkZ * 16) + (iz * 8) + 4;
			for (int ix=-1; ix<3; ix++) {
				final int xx = (chunkX * 16) + (ix * 8) + 4;
				data.put(
					new Iab(ix, iz),
					new PoolData(xx, zz)
				);
			}
		}
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!this.enable_gen) return;
		final BlockData block_wall_a     = StringToBlockDataDef(this.block_wall_a,     DEFAULT_BLOCK_WALL_A    );
		final BlockData block_wall_b     = StringToBlockDataDef(this.block_wall_b,     DEFAULT_BLOCK_WALL_B    );
		final BlockData block_subfloor   = StringToBlockDataDef(this.block_subfloor,   DEFAULT_BLOCK_SUBFLOOR  );
		final BlockData block_subceiling = StringToBlockDataDef(this.block_subceiling, DEFAULT_BLOCK_SUBCEILING);
		final BlockData block_ceiling    = StringToBlockDataDef(this.block_ceiling,    DEFAULT_BLOCK_CEILING   );
		if (block_wall_a     == null) throw new RuntimeException("Invalid block type for level 37 Wall A"    );
		if (block_wall_b     == null) throw new RuntimeException("Invalid block type for level 37 Wall B"    );
		if (block_subfloor   == null) throw new RuntimeException("Invalid block type for level 37 SubFloor"  );
		if (block_subceiling == null) throw new RuntimeException("Invalid block type for level 37 SubCeiling");
		if (block_ceiling    == null) throw new RuntimeException("Invalid block type for level 37 Ceiling"   );
		final Gen_000 gen_000 = this.level_000.gen_000;
		final int portal_000_037_y = (this.level_000.gen_000.level_y + gen_000.bedrock_barrier + gen_000.subfloor) - 1;
		final int portal_000_037_h = ((this.level_y - gen_000.level_y) + this.bedrock_barrier + this.subfloor) - gen_000.bedrock_barrier - 1;
		final int level_000_h = gen_000.level_h + gen_000.subceiling + 1;
		final Map<Iab, PoolData>  poolData  = ((Pregen_Level_000)pregen).pools;
		final Map<Iab, LobbyData> lobbyData = ((Pregen_Level_000)pregen).lobby;
		final int h_walls = this.level_h + 2;
		final int y_base  = this.level_y + this.bedrock_barrier;
		final int y_floor = y_base + this.subfloor;
		final int y_ceil  = y_floor + this.level_h + 2;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
				// barrier
				for (int iy=0; iy<this.bedrock_barrier; iy++)
					chunk.setBlock(ix, this.level_y+iy, iz, Material.BEDROCK);
				// subfloor
				for (int iy=0; iy<this.subfloor; iy++)
					chunk.setBlock(ix, y_base+iy, iz, block_subfloor);
				// subceiling
				if (this.enable_top) {
					for (int iy=0; iy<this.subceiling; iy++)
						chunk.setBlock(ix, y_ceil+iy, iz, block_subceiling);
				}
			}
		}
		for (int rz=0; rz<2; rz++) {
			for (int rx=0; rx<2; rx++) {
				final BlockPlotter plot =
					(new BlockPlotter())
					.axis("use")
					.xyz(rx*8, y_floor, rz*8)
					.whd(8, h_walls, 8);
				plot.type('#', block_wall_a);
				plot.type('@', block_wall_b);
				plot.type('w', "minecraft:water[level=0]");
				plot.type('g', block_ceiling);
				final PoolData dao_pool = poolData.get(new Iab(rx, rz));
				final boolean solid_n  = poolData.get(new Iab(rx,   rz-1)).isSolid();
				final boolean solid_s  = poolData.get(new Iab(rx,   rz+1)).isSolid();
				final boolean solid_e  = poolData.get(new Iab(rx+1, rz  )).isSolid();
				final boolean solid_w  = poolData.get(new Iab(rx-1, rz  )).isSolid();
				final boolean solid_ne = poolData.get(new Iab(rx+1, rz-1)).isSolid();
				final boolean solid_nw = poolData.get(new Iab(rx-1, rz-1)).isSolid();
				final boolean solid_se = poolData.get(new Iab(rx+1, rz+1)).isSolid();
				final boolean solid_sw = poolData.get(new Iab(rx-1, rz+1)).isSolid();
				final StringBuilder[][] matrix = plot.getMatrix3D();
				switch (dao_pool.type) {
				case SOLID: {
					for (int iz=0; iz<8; iz++) {
						for (int iy=0; iy<h_walls; iy++)
							matrix[iy][iz].append(StringUtils.Repeat(8, '@'));
					}
					// outside-corner
					{
						// north/east outside-corner
						if (!solid_n && !solid_e && !solid_ne) {
							StringUtils.ReplaceInString(matrix[0][0], "####", 4);
							StringUtils.ReplaceInString(matrix[0][1], "##",   6);
							StringUtils.ReplaceInString(matrix[0][2], "#",    7);
							for (int iy=1; iy<h_walls; iy++) {
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
							for (int iy=1; iy<h_walls; iy++) {
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
							for (int iy=1; iy<h_walls; iy++) {
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
							for (int iy=1; iy<h_walls; iy++) {
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
							for (int iy=1; iy<h_walls; iy++) {
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
							for (int iy=1; iy<h_walls; iy++) {
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
							for (int iy=1; iy<h_walls; iy++) {
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
							for (int iy=1; iy<h_walls; iy++) {
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
						for (int iy=1; iy<h_walls; iy++)
							matrix[iy][iz].append(StringUtils.Repeat(8, ' '));
					}
					// inside corner
					{
						// north/east inside corner
						if (solid_n && solid_e && solid_ne) {
							for (int iy=0; iy<h_walls; iy++) {
								StringUtils.ReplaceInString(matrix[iy][0], "@@", 6);
								StringUtils.ReplaceInString(matrix[iy][1], "@",  7);
							}
						}
						// north/west inside corner
						if (solid_n && solid_w && solid_nw) {
							for (int iy=0; iy<h_walls; iy++) {
								StringUtils.ReplaceInString(matrix[iy][0], "@@", 0);
								StringUtils.ReplaceInString(matrix[iy][1], "@",  0);
							}
						}
						// south/east inside corner
						if (solid_s && solid_e && solid_se) {
							for (int iy=0; iy<h_walls; iy++) {
								StringUtils.ReplaceInString(matrix[iy][7], "@@", 6);
								StringUtils.ReplaceInString(matrix[iy][6], "@",  7);
							}
						}
						// south/west inside corner
						if (solid_s && solid_w && solid_sw) {
							for (int iy=0; iy<h_walls; iy++) {
								StringUtils.ReplaceInString(matrix[iy][7], "@@", 0);
								StringUtils.ReplaceInString(matrix[iy][6], "@",  0);
							}
						}
					}
					// portal to lobby
					if (dao_pool.possible_portal_lobby) {
						LobbyData lobby;
						boolean foundWall = false;
						LOOP_Z:
						for (int iz=0; iz<8; iz++) {
							final int zz = (rz * 8) + iz;
							for (int ix=0; ix<8; ix++) {
								final int xx = (rx * 8) + ix;
								lobby = lobbyData.get(new Iab(xx, zz));
								if (lobby.isWall) {
									foundWall = true;
									break LOOP_Z;
								}
							}
						}
						if (!foundWall) {
							final int xx = (chunkX * 16) + (rx * 8);
							final int zz = (chunkZ * 16) + (rz * 8);
							this.level_000.portal_000_to_037.addLocation(xx, zz);
							final int portal_top = portal_000_037_h - 1;
							final BlockPlotter pp =
								(new BlockPlotter())
								.axis("use")
								.xz(rx*8, rz*8)
								.y(portal_000_037_y)
								.whd(6, portal_000_037_h, 6);
							pp.type('#', Material.BEDROCK  );
							pp.type('g', Material.GLOWSTONE);
							pp.type('.', Material.AIR      );
							pp.type(',', "minecraft:water[level=8]");
							final StringBuilder[][] mtx = pp.getMatrix3D();
							// floor
							mtx[0][0].append(" #### "); mtx[1][0].append(" #### ");
							mtx[0][1].append("##gg##"); mtx[1][1].append("##,,##");
							mtx[0][2].append("#gggg#"); mtx[1][2].append("#,,,,#");
							mtx[0][3].append("#gggg#"); mtx[1][3].append("#,,,,#");
							mtx[0][4].append("##gg##"); mtx[1][4].append("##,,##");
							mtx[0][5].append(" #### "); mtx[1][5].append(" #### ");
							for (int yi=2; yi<level_000_h; yi++) {
								mtx[yi][0].append(" .... ");
								mtx[yi][1].append("..,,..");
								mtx[yi][2].append(".,,,,.");
								mtx[yi][3].append(".,,,,.");
								mtx[yi][4].append("..,,..");
								mtx[yi][5].append(" .... ");
							}
							// shaft
							for (int yi=level_000_h; yi<portal_top; yi++) {
								mtx[yi][0].append(" #### ");
								mtx[yi][1].append("##,,##");
								mtx[yi][2].append("#,,,,#");
								mtx[yi][3].append("#,,,,#");
								mtx[yi][4].append("##,,##");
								mtx[yi][5].append(" #### ");
							}
							// top
							mtx[portal_top][0].append("  gg  ");
							mtx[portal_top][1].append(" g,,g ");
							mtx[portal_top][2].append("g,,,,g");
							mtx[portal_top][3].append("g,,,,g");
							mtx[portal_top][4].append(" g,,g ");
							mtx[portal_top][5].append("  gg  ");
							plots.add(new Tuple<BlockPlotter, StringBuilder[][]>(pp, mtx));
						}
					}
					break;
				}
				default: throw new RuntimeException("Unknown pool room type: "+dao_pool.type.toString());
				} // end type switch
				// water
				for (int iz=0; iz<8; iz++) {
					for (int iy=0; iy<=this.water_depth; iy++)
						StringUtils.ReplaceWith(matrix[iy][iz], ' ', 'w');
				}
				// ceiling
				if (this.enable_top) {
					for (int iz=0; iz<8; iz++)
						StringUtils.ReplaceWith(matrix[h_walls-1][iz], ' ', 'g');
				}
				plot.run(chunk, matrix);
			} // end room x
		} // end room z
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void initNoise() {
		super.initNoise();
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(this.getLevelNumber());
		// pool rooms
		this.noisePoolRooms.setFrequency(              cfgParams.getDouble("Noise-Room-Freq"    ));
		this.noisePoolRooms.setFractalOctaves(         cfgParams.getInt(   "Noise-Room-Octave"  ));
		this.noisePoolRooms.setFractalGain(            cfgParams.getDouble("Noise-Room-Gain"    ));
		this.noisePoolRooms.setFractalPingPongStrength(cfgParams.getDouble("Noise-Room-Strength"));
		this.noisePoolRooms.setNoiseType(              NoiseType.OpenSimplex2                    );
		this.noisePoolRooms.setFractalType(            FractalType.PingPong                      );
		// tunnels
		this.noiseTunnels.setAngle(0.25);
		this.noiseTunnels.setFrequency(               cfgParams.getDouble("Noise-Tunnel-Freq"    ));
		this.noiseTunnels.setFractalPingPongStrength( cfgParams.getDouble("Noise-Tunnel-Strength"));
		this.noiseTunnels.setNoiseType(               NoiseType.Cellular                          );
		this.noiseTunnels.setFractalType(             FractalType.PingPong                        );
		this.noiseTunnels.setCellularDistanceFunction(CellularDistanceFunction.Manhattan          );
		// portal to lobby
		this.noisePortalLobby.setFrequency(     cfgParams.getDouble("Noise-Portal-Lobby-Freq"  ));
		this.noisePortalLobby.setFractalOctaves(cfgParams.getInt(   "Noise-Portal-Lobby-Octave"));
		this.noisePortalLobby.setFractalType(   FractalType.FBm                                 );
		// portal to hotel
		this.noisePortalHotel.setFrequency(cfgParams.getDouble("Noise-Portal-Hotel-Freq"));
	}



	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		cfgParams.addDefault("Enable-Gen",                Boolean.TRUE                                      );
		cfgParams.addDefault("Enable-Top",                Boolean.TRUE                                      );
		cfgParams.addDefault("Level-Y",                   Integer.valueOf(this.getDefaultY()               ));
		cfgParams.addDefault("Level-Height",              Integer.valueOf(DEFAULT_LEVEL_H                  ));
		cfgParams.addDefault("Water-Depth",               Integer.valueOf(DEFAULT_WATER_DEPTH              ));
		cfgParams.addDefault("SubFloor",                  Integer.valueOf(DEFAULT_SUBFLOOR                 ));
		cfgParams.addDefault("SubCeiling",                Integer.valueOf(DEFAULT_SUBCEILING               ));
		cfgParams.addDefault("Noise-Room-Freq",           Double .valueOf(DEFAULT_NOISE_ROOM_FREQ          ));
		cfgParams.addDefault("Noise-Room-Octave",         Integer.valueOf(DEFAULT_NOISE_ROOM_OCTAVE        ));
		cfgParams.addDefault("Noise-Room-Gain",           Double .valueOf(DEFAULT_NOISE_ROOM_GAIN          ));
		cfgParams.addDefault("Noise-Room-Strength",       Double .valueOf(DEFAULT_NOISE_ROOM_STRENGTH      ));
		cfgParams.addDefault("Noise-Tunnel-Freq",         Double .valueOf(DEFAULT_NOISE_TUNNEL_FREQ        ));
		cfgParams.addDefault("Noise-Tunnel-Strength",     Double .valueOf(DEFAULT_NOISE_TUNNEL_STRENGTH    ));
		cfgParams.addDefault("Noise-Portal-Lobby-Freq",   Double .valueOf(DEFAULT_NOISE_PORTAL_LOBBY_FREQ  ));
		cfgParams.addDefault("Noise-Portal-Lobby-Octave", Integer.valueOf(DEFAULT_NOISE_PORTAL_LOBBY_OCTAVE));
		cfgParams.addDefault("Noise-Portal-Hotel-Freq",   Double .valueOf(DEFAULT_NOISE_PORTAL_HOTEL_FREQ  ));
		cfgParams.addDefault("Thresh-Room",               Double .valueOf(DEFAULT_THRESH_ROOM              ));
		cfgParams.addDefault("Thresh-Portal",             Double .valueOf(DEFAULT_THRESH_PORTAL            ));
		cfgParams.addDefault("Thresh-Tunnel",             Double .valueOf(DEFAULT_THRESH_TUNNEL            ));
		// block types
		cfgBlocks.addDefault("WallA",      DEFAULT_BLOCK_WALL_A    );
		cfgBlocks.addDefault("WallB",      DEFAULT_BLOCK_WALL_B    );
		cfgBlocks.addDefault("SubFloor",   DEFAULT_BLOCK_SUBFLOOR  );
		cfgBlocks.addDefault("SubCeiling", DEFAULT_BLOCK_SUBCEILING);
		cfgBlocks.addDefault("Ceiling",    DEFAULT_BLOCK_CEILING   );
	}



}
