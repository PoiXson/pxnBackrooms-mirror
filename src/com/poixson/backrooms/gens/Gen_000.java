package com.poixson.backrooms.gens;

import static com.poixson.utils.BlockUtils.StringToBlockData;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.gens.Gen_001.BasementData;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.backrooms.worlds.Level_000.PregenLevel0;
import com.poixson.tools.DelayedChestFiller;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.dao.Iab;
import com.poixson.tools.dao.Iabc;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.CellularReturnType;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;


// 0 | Lobby
public class Gen_000 extends BackroomsGen {

	// default params
	public static final int    DEFAULT_LEVEL_H             = 5;
	public static final int    DEFAULT_SUBFLOOR            = 3;
	public static final int    DEFAULT_SUBCEILING          = 3;
	public static final double DEFAULT_NOISE_WALL_FREQ     = 0.022;
	public static final int    DEFAULT_NOISE_WALL_OCTAVE   = 2;
	public static final double DEFAULT_NOISE_WALL_GAIN     = 0.1;
	public static final double DEFAULT_NOISE_WALL_LACUN    = 0.4;
	public static final double DEFAULT_NOISE_WALL_STRENGTH = 2.28;
	public static final double DEFAULT_NOISE_LOOT_FREQ     = 0.1;
	public static final double DEFAULT_THRESH_WALL_L       = 0.38;
	public static final double DEFAULT_THRESH_WALL_H       = 0.5;
	public static final double DEFAULT_THRESH_LOOT         = 0.65;

	public static final int WALL_SEARCH_DIST = 6;

	// default blocks
	public static final String DEFAULT_BLOCK_WALL        = "minecraft:yellow_terracotta";
	public static final String DEFAULT_BLOCK_WALL_BASE   = "minecraft:orange_terracotta";
	public static final String DEFAULT_BLOCK_SUBFLOOR    = "minecraft:oak_planks";
	public static final String DEFAULT_BLOCK_SUBCEILING  = "minecraft:oak_planks";
	public static final String DEFAULT_BLOCK_CARPET      = "minecraft:light_gray_wool";
	public static final String DEFAULT_BLOCK_CEILING     = "minecraft:smooth_stone_slab[type=top]";

	// noise
	public final FastNoiseLiteD noiseLobbyWalls;
	public final FastNoiseLiteD noiseLoot;

	// params
	public final boolean enable_gen;
	public final boolean enable_top;
	public final int     level_y;
	public final int     level_h;
	public final int     subfloor;
	public final int     subceiling;
	public final double  thresh_wall_L;
	public final double  thresh_wall_H;
	public final double  thresh_loot;

	// blocks
	public final AtomicReference<String> block_wall        = new AtomicReference<String>(null);
	public final AtomicReference<String> block_wall_base   = new AtomicReference<String>(null);
	public final AtomicReference<String> block_subfloor    = new AtomicReference<String>(null);
	public final AtomicReference<String> block_subceiling  = new AtomicReference<String>(null);
	public final AtomicReference<String> block_carpet      = new AtomicReference<String>(null);
	public final AtomicReference<String> block_ceiling     = new AtomicReference<String>(null);



	public Gen_000(final BackroomsLevel backlevel, final int seed, final BackroomsGen gen_below) {
		super(backlevel, gen_below, seed);
		final int level_number = this.getLevelNumber();
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(level_number);
		final ConfigurationSection cfgBlocks = this.plugin.getConfigLevelBlocks(level_number);
		// params
		this.enable_gen    = cfgParams.getBoolean("Enable-Gen"   );
		this.enable_top    = cfgParams.getBoolean("Enable-Top"   );
		this.level_y       = cfgParams.getInt(    "Level-Y"      );
		this.level_h       = cfgParams.getInt(    "Level-Height" );
		this.subfloor      = cfgParams.getInt(    "SubFloor"     );
		this.subceiling    = cfgParams.getInt(    "SubCeiling"   );
		this.thresh_wall_L = cfgParams.getDouble( "Thresh-Wall-L");
		this.thresh_wall_H = cfgParams.getDouble( "Thresh-Wall-H");
		this.thresh_loot   = cfgParams.getDouble( "Thresh-Loot"  );
		// noise
		this.noiseLobbyWalls = this.register(new FastNoiseLiteD());
		this.noiseLoot       = this.register(new FastNoiseLiteD());
		if (this.subceiling < 1) throw new RuntimeException("Invalid SubCeiling value for level 0, must be >=1");
	}



	@Override
	public int getLevelNumber() {
		return 0;
	}

	@Override
	public int getNextY() {
		return this.bedrock_barrier + this.level_y + this.subfloor + this.level_h + this.subceiling + 2;
	}



	public class LobbyData implements PreGenData {

		public final double valueWall;
		public final boolean isWall;
		public boolean wall_n = false;
		public boolean wall_s = false;
		public boolean wall_e = false;
		public boolean wall_w = false;
		public int wall_dist = WALL_SEARCH_DIST;
		public int boxed = 0;
		public BlockFace box_dir = null;

		public LobbyData(final double valueWall) {
			this.valueWall = valueWall;
			this.isWall = (
				valueWall > Gen_000.this.thresh_wall_L &&
				valueWall < Gen_000.this.thresh_wall_H
			);
		}

	}



	public void pregenerate(Map<Iab, LobbyData> data,
			final int chunkX, final int chunkZ) {
		LobbyData dao;
		int xx, zz;
		double valueWall;
		for (int iz=-1; iz<17; iz++) {
			zz = (chunkZ * 16) + iz;
			for (int ix=-1; ix<17; ix++) {
				xx = (chunkX * 16) + ix;
				valueWall = this.noiseLobbyWalls.getNoiseRot(xx, zz, 0.25);
				dao = new LobbyData(valueWall);
				data.put(new Iab(ix, iz), dao);
			}
		}
		// find wall distance
		LobbyData dao_near;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
				dao = data.get(new Iab(ix, iz));
				if (dao.isWall) {
					dao.wall_dist = 0;
					continue;
				}
				for (int i=1; i<WALL_SEARCH_DIST; i++) {
					dao.boxed = 0;
					// north
					dao_near = data.get(new Iab(ix, iz-i));
					if (dao_near != null && dao_near.isWall) {
						if (dao.wall_dist > i)
							dao.wall_dist = i;
						dao.wall_n = true;
						dao.boxed++;
					}
					// south
					dao_near = data.get(new Iab(ix, iz+i));
					if (dao_near != null && dao_near.isWall) {
						if (dao.wall_dist > i)
							dao.wall_dist = i;
						dao.wall_s = true;
						dao.boxed++;
					}
					// east
					dao_near = data.get(new Iab(ix+i, iz));
					if (dao_near != null && dao_near.isWall) {
						if (dao.wall_dist > i)
							dao.wall_dist = i;
						dao.wall_e = true;
						dao.boxed++;
					}
					// west
					dao_near = data.get(new Iab(ix-i, iz));
					if (dao_near != null && dao_near.isWall) {
						if (dao.wall_dist > i)
							dao.wall_dist = i;
						dao.wall_w = true;
						dao.boxed++;
					}
					// boxed walls
					if (dao.boxed > 2) {
						// north-east
						if (dao.wall_n || dao.wall_e) {
							dao_near = data.get(new Iab(ix+i, iz-i));
							if (dao_near != null && dao_near.isWall)
								dao.boxed++;
						}
						// north-west
						if (dao.wall_n || dao.wall_w) {
							dao_near = data.get(new Iab(ix-i, iz-i));
							if (dao_near != null && dao_near.isWall)
								dao.boxed++;
						}
						// south-east
						if (dao.wall_s || dao.wall_e) {
							dao_near = data.get(new Iab(ix+i, iz+i));
							if (dao_near != null && dao_near.isWall)
								dao.boxed++;
						}
						// south-west
						if (dao.wall_s || dao.wall_w) {
							dao_near = data.get(new Iab(ix-i, iz+i));
							if (dao_near != null && dao_near.isWall)
								dao.boxed++;
						}
						// boxed direction
						if (dao.boxed > 2) {
							if (!dao.wall_n) { dao.box_dir = BlockFace.NORTH; break; }
							if (!dao.wall_s) { dao.box_dir = BlockFace.SOUTH; break; }
							if (!dao.wall_e) { dao.box_dir = BlockFace.EAST;  break; }
							if (!dao.wall_w) { dao.box_dir = BlockFace.WEST;  break; }
							break;
						}
					}
				} // end distance loop
			} // end ix
		} // end iz
	}

	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!this.enable_gen) return;
		final BlockData block_wall            = StringToBlockData(this.block_wall,       DEFAULT_BLOCK_WALL      );
		final BlockData block_wall_base       = StringToBlockData(this.block_wall_base,  DEFAULT_BLOCK_WALL_BASE );
		final BlockData block_subfloor        = StringToBlockData(this.block_subfloor,   DEFAULT_BLOCK_SUBFLOOR  );
		final BlockData block_subceiling      = StringToBlockData(this.block_subceiling, DEFAULT_BLOCK_SUBCEILING);
		final BlockData block_carpet          = StringToBlockData(this.block_carpet,     DEFAULT_BLOCK_CARPET    );
		final BlockData block_ceiling         = StringToBlockData(this.block_ceiling,    DEFAULT_BLOCK_CEILING   );
		final BlockData block_overgrowth_wall = StringToBlockData(((Level_000)this.backlevel).gen_023.block_wall, Gen_023.DEFAULT_BLOCK_WALL);
		if (block_wall            == null) throw new RuntimeException("Invalid block type for level 0 Wall"      );
		if (block_wall_base       == null) throw new RuntimeException("Invalid block type for level 0 Wall-Base" );
		if (block_subfloor        == null) throw new RuntimeException("Invalid block type for level 0 SubFloor"  );
		if (block_subceiling      == null) throw new RuntimeException("Invalid block type for level 0 SubCeiling");
		if (block_carpet          == null) throw new RuntimeException("Invalid block type for level 0 Carpet"    );
		if (block_ceiling         == null) throw new RuntimeException("Invalid block type for level 0 Ceiling"   );
		if (block_overgrowth_wall == null) throw new RuntimeException("Invalid block type for level 23 Wall"     );
		final BlockData lamp = Bukkit.createBlockData("minecraft:redstone_lamp[lit=true]");
		final HashMap<Iab, LobbyData>    lobbyData    = ((PregenLevel0)pregen).lobby;
		final HashMap<Iab, BasementData> basementData = ((PregenLevel0)pregen).basement;
		final LinkedList<Iabc> chests = new LinkedList<Iabc>();
		final int h_walls = this.level_h + 3;
		final int y_base  = this.level_y + this.bedrock_barrier;
		final int y_floor = y_base + this.subfloor;
		final int y_ceil  = (y_floor + h_walls) - 2;
//		int outlets = 0;
//		int outlet_rnd;
		for (int iz=0; iz<16; iz++) {
			final int zz = (chunkZ * 16) + iz;
			final int mod_z = (zz < 0 ? 0-zz : zz) % 7;
			for (int ix=0; ix<16; ix++) {
				final int xx = (chunkX * 16) + ix;
				final int mod_x = (xx < 0 ? 1-xx : xx) % 7;
				// barrier
				for (int iy=0; iy<this.bedrock_barrier; iy++)
					chunk.setBlock(ix, this.level_y+iy, iz, Material.BEDROCK);
				// subfloor
				for (int iy=0; iy<this.subfloor; iy++)
					chunk.setBlock(ix, y_base+iy, iz, block_subfloor);
				final LobbyData dao_lobby = data_lobby.get(new Iab(ix, iz));
				if (dao_lobby == null) continue;
				// wall
				if (dao_lobby.isWall) {
					// lobby walls
					chunk.setBlock(ix, y_floor,   iz, block_subfloor );
					chunk.setBlock(ix, y_floor+1, iz, block_wall_base);
					for (int iy=0; iy<h_walls; iy++)
						chunk.setBlock(ix, y_floor+iy, iz, block_wall);
//TODO
//					// outlet
//					outlet_rnd = xRand.Get(0, 100+(int)Math.pow(10.0, outlets)).nextInt();
//					if (outlet_rnd == 11) {
//					}
				// room
				} else {
					// floor
					chunk.setBlock(ix, y_floor, iz, block_carpet);
					// ceiling
					if (this.enable_top) {
						if (mod_z == 0 && mod_x < 2
						&& dao_lobby.wall_dist > 2) {
							// ceiling lights
							chunk.setBlock(ix, y_ceil,   iz, lamp                   );
							chunk.setBlock(ix, y_ceil+1, iz, Material.REDSTONE_BLOCK);
						} else {
							// ceiling
							chunk.setBlock(ix, y_ceil,   iz, block_ceiling   );
							chunk.setBlock(ix, y_ceil+1, iz, block_subceiling);
						}
					}
				}
				// subceiling
				if (this.enable_top) {
					for (int iy=1; iy<this.subceiling; iy++)
						chunk.setBlock(ix, y_ceil+iy+1, iz, block_subceiling);
				}
				// special
				if (!dao_lobby.isWall) {
					if (dao_lobby.boxed > 4) {
						// loot
						if (dao_lobby.wall_dist == 1) {
//TODO
							level_000.loot_chests_0.add(xx, zz);
							final BlockData barrel = Bukkit.createBlockData("minecraft:barrel[facing=up]");
							chunk.setBlock(ix, y_floor+1, iz, barrel);
							chests.add(new Iabc(xx, y_floor+1, zz));
						} else
						// portal to basement
						if (dao_lobby.boxed     == 7
						&&  dao_lobby.wall_dist == 2
						&&  dao_lobby.box_dir != null) {
							boolean found_basement_wall = false;
							for (int izb=-2; izb<3; izb++) {
								for (int ixb=-2; ixb<3; ixb++) {
									final BasementData dao_basement = data_basement.get(new Iab(ixb+ix, izb+iz));
									if (dao_basement != null
									&&  dao_basement.isWall) {
										found_basement_wall = true;
										break;
									}
								}
							}
							if (!found_basement_wall) {
								level_000.portal_0_to_1.add(xx, zz);
								final int y_exit = gen_001.getNextY() - 2;
								final int h_exit = (y_ceil - gen_001.getNextY()) + 3;
								final BlockPlotter plot =
									(new BlockPlotter())
									.axis("use")
									.rotate(dao_lobby.box_dir)
									.y(y_exit)
									.whd(5, h_exit, 6);
								switch (dao_lobby.box_dir) {
								case NORTH: plot.xz(ix-3, iz-4); break;
								case SOUTH: plot.xz(ix,   iz  ); break;
								case EAST:  plot.xz(ix-2, iz-2); break;
								case WEST:  plot.xz(ix-4, iz-3); break;
								default: throw new RuntimeException("Unknown boxed walls direction: "+dao_lobby.box_dir.toString());
								}
								plot.type('.', Material.AIR         );
								plot.type('=', block_wall           );
								plot.type('x', Material.BEDROCK     );
								plot.type('g', Material.GLOWSTONE   );
								plot.type('m', block_overgrowth_wall);
								final StringBuilder[][] matrix = plot.getMatrix3D();
								// bottom
								matrix[0][0].append("xxxxx");
								matrix[0][1].append("xxxxx");
								matrix[0][2].append("xxxxx");
								matrix[0][3].append("xxgxx");
								matrix[0][4].append("xg.gx");
								matrix[0][5].append("xxgxx");
								int iy = 0;
								// basement subceiling
								final int h_exit_subceil = this.bedrock_barrier + 1;
								for (int i=1; i<h_exit_subceil; i++) {
									iy++;
									matrix[iy][1].append(" xxx");
									matrix[iy][2].append(" x.x");
									matrix[iy][3].append(" x.x");
									matrix[iy][4].append(" x.x");
									matrix[iy][5].append(" xxx");
								}
								// lower ceiling
								iy++;
								matrix[iy][1].append(" xxx");
								matrix[iy][2].append(" x.x");
								matrix[iy][3].append(" xxx");
								matrix[iy][4].append(" xxx");
								matrix[iy][5].append(" xxx");
								// overgrowth subfloor
								for (int i=0; i<gen_023.subfloor; i++) {
									iy++;
									matrix[iy][1].append(" xxx");
									matrix[iy][2].append(" x.x");
									matrix[iy][3].append(" xxx");
								}
								// shaft through overgrowth
								final int level_023_h = gen_023.level_h + 2;
								for (int i=0; i<level_023_h; i++) {
									iy++;
									matrix[iy][0].append("mmmmm");
									matrix[iy][1].append("mxxxm");
									matrix[iy][2].append("mx.xm");
									matrix[iy][3].append("mxxxm");
									matrix[iy][4].append("mmmmm");
								}
								// overgrowth subceiling
								final int h_exit_closed = gen_023.subceiling + this.bedrock_barrier + this.subfloor + 1;
								for (int i=0; i<h_exit_closed; i++) {
									iy++;
									matrix[iy][1].append(" xxx");
									matrix[iy][2].append(" x.x");
									matrix[iy][3].append(" xxx");
								}
								// opening in lobby
								for (int i=0; i<this.level_h; i++) {
									iy++;
									matrix[iy][0].append("=====");
									matrix[iy][1].append("=xxx=");
									matrix[iy][2].append("=x.x=");
									matrix[iy][3].append("=x.x=");
									matrix[iy][4].append("==.==");
								}
								// top
								iy++;
								matrix[iy][0].append("=====");
								matrix[iy][1].append("=xxx=");
								matrix[iy][2].append("=xxx=");
								matrix[iy][3].append("=xxx=");
								matrix[iy][4].append("== ==");
								plots.add(new Tuple<BlockPlotter, StringBuilder[][]>(plot, matrix));
							} // end no basement walls
						} // end portal to basement
					} // end special
				} // end wall/room
			} // end ix
		} // end iz
		if (!chests.isEmpty()) {
			for (final Iabc loc : chests) {
				(new ChestFiller_000(this.plugin, "level_000", loc.a, loc.b, loc.c))
					.start();
			}
		}
	}



	public class ChestFiller_000 extends DelayedChestFiller {

		public ChestFiller_000(final JavaPlugin plugin,
				final String worldName, final int x, final int y, final int z) {
			super(plugin, worldName, x, y, z);
		}

		@Override
		public void fill(final Inventory chest) {
//TODO
			final ItemStack item = new ItemStack(Material.BREAD);
			final Location loc = chest.getLocation();
			final int xx = loc.getBlockX();
			final int zz = loc.getBlockZ();
			int x, y;
			double value;
			for (int i=0; i<27; i++) {
				x = xx + (i % 9);
				y = zz + Math.floorDiv(i, 9);
				value = Gen_000.this.noiseLoot.getNoise(x, y);
				if (value > Gen_000.this.thresh_loot)
					chest.setItem(i, item);
			}
		}

	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void initNoise(final ConfigurationSection cfgParams) {
		super.initNoise(cfgParams);
		// lobby walls
		this.noiseLobbyWalls.setFrequency(                cfgParams.getDouble("Noise-Wall-Freq"    ) );
		this.noiseLobbyWalls.setFractalOctaves(           cfgParams.getInt(   "Noise-Wall-Octave"  ) );
		this.noiseLobbyWalls.setFractalGain(              cfgParams.getDouble("Noise-Wall-Gain"    ) );
		this.noiseLobbyWalls.setFractalLacunarity(        cfgParams.getDouble("Noise-Wall-Lacun"   ) );
		this.noiseLobbyWalls.setFractalPingPongStrength(  cfgParams.getDouble("Noise-Wall-Strength") );
		this.noiseLobbyWalls.setNoiseType(                NoiseType.Cellular                         );
		this.noiseLobbyWalls.setFractalType(              FractalType.PingPong                       );
		this.noiseLobbyWalls.setCellularDistanceFunction( CellularDistanceFunction.Manhattan         );
		this.noiseLobbyWalls.setCellularReturnType(       CellularReturnType.Distance                );
		// chest loot
		this.noiseLoot.setFrequency( cfgParams.getDouble("Noise-Loot-Freq") );
	}



	@Override
	protected void loadConfig(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// block types
		this.block_wall      .set(cfgBlocks.getString("Wall"      ));
		this.block_wall_base .set(cfgBlocks.getString("Wall-Base" ));
		this.block_subfloor  .set(cfgBlocks.getString("SubFloor"  ));
		this.block_subceiling.set(cfgBlocks.getString("SubCeiling"));
		this.block_carpet    .set(cfgBlocks.getString("Carpet"    ));
		this.block_ceiling   .set(cfgBlocks.getString("Ceiling"   ));
	}
	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		cfgParams.addDefault("Enable-Gen",          Boolean.TRUE                                );
		cfgParams.addDefault("Enable-Top",          Boolean.TRUE                                );
		cfgParams.addDefault("Level-Y",             Integer.valueOf(this.getDefaultY()         ));
		cfgParams.addDefault("Level-Height",        Integer.valueOf(DEFAULT_LEVEL_H            ));
		cfgParams.addDefault("SubFloor",            Integer.valueOf(DEFAULT_SUBFLOOR           ));
		cfgParams.addDefault("SubCeiling",          Integer.valueOf(DEFAULT_SUBCEILING         ));
		cfgParams.addDefault("Noise-Wall-Freq",     DEFAULT_NOISE_WALL_FREQ    );
		cfgParams.addDefault("Noise-Wall-Octave",   DEFAULT_NOISE_WALL_OCTAVE  );
		cfgParams.addDefault("Noise-Wall-Gain",     DEFAULT_NOISE_WALL_GAIN    );
		cfgParams.addDefault("Noise-Wall-Lacun",    DEFAULT_NOISE_WALL_LACUN   );
		cfgParams.addDefault("Noise-Wall-Strength", DEFAULT_NOISE_WALL_STRENGTH);
		cfgParams.addDefault("Noise-Loot-Freq",     DEFAULT_NOISE_LOOT_FREQ    );
		cfgParams.addDefault("Thresh-Wall-L",       DEFAULT_THRESH_WALL_L      );
		cfgParams.addDefault("Thresh-Wall-H",       DEFAULT_THRESH_WALL_H      );
		cfgParams.addDefault("Thresh-Loot",         DEFAULT_THRESH_LOOT        );
		// block types
		cfgBlocks.addDefault("Wall",       DEFAULT_BLOCK_WALL      );
		cfgBlocks.addDefault("Wall-Base",  DEFAULT_BLOCK_WALL_BASE );
		cfgBlocks.addDefault("SubFloor",   DEFAULT_BLOCK_SUBFLOOR  );
		cfgBlocks.addDefault("SubCeiling", DEFAULT_BLOCK_SUBCEILING);
		cfgBlocks.addDefault("Carpet",     DEFAULT_BLOCK_CARPET    );
		cfgBlocks.addDefault("Ceiling",    DEFAULT_BLOCK_CEILING   );
	}



}
