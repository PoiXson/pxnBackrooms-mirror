package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_000.ENABLE_GEN_000;
import static com.poixson.backrooms.worlds.Level_000.ENABLE_TOP_000;
import static com.poixson.backrooms.worlds.Level_000.SUBCEILING;
import static com.poixson.backrooms.worlds.Level_000.SUBFLOOR;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.type.Barrel;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
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
import com.poixson.commonmc.tools.DelayedChestFiller;
import com.poixson.commonmc.tools.plotter.BlockPlotter;
import com.poixson.commonmc.tools.plotter.PlotterFactory;
import com.poixson.tools.abstractions.AtomicDouble;
import com.poixson.tools.dao.Iab;
import com.poixson.tools.dao.Iabc;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.CellularReturnType;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;


// 0 | Lobby
public class Gen_000 extends BackroomsGen {

	// default params
	public static final double DEFAULT_THRESH_WALL_L = 0.38;
	public static final double DEFAULT_THRESH_WALL_H = 0.5;
	public static final double DEFAULT_THRESH_LOOT   = 0.65;

	public static final int WALL_SEARCH_DIST = 6;

	// default blocks
	public static final String DEFAULT_BLOCK_WALL       = "minecraft:yellow_terracotta";
	public static final String DEFAULT_BLOCK_WALL_BASE  = "minecraft:orange_terracotta";
	public static final String DEFAULT_BLOCK_SUBFLOOR   = "minecraft:oak_planks";
	public static final String DEFAULT_BLOCK_SUBCEILING = "minecraft:oak_planks";
	public static final String DEFAULT_BLOCK_CARPET     = "minecraft:light_gray_wool";
	public static final String DEFAULT_BLOCK_CEILING    = "minecraft:smooth_stone_slab[type=top]";

	// noise
	public final FastNoiseLiteD noiseLobbyWalls;
	public final FastNoiseLiteD noiseLoot;

	// params
	public final AtomicDouble thresh_wall_L = new AtomicDouble(DEFAULT_THRESH_WALL_L);
	public final AtomicDouble thresh_wall_H = new AtomicDouble(DEFAULT_THRESH_WALL_H);
	public final AtomicDouble thresh_loot   = new AtomicDouble(DEFAULT_THRESH_LOOT  );

	// blocks
	public final AtomicReference<String> block_wall       = new AtomicReference<String>(null);
	public final AtomicReference<String> block_wall_base  = new AtomicReference<String>(null);
	public final AtomicReference<String> block_subfloor   = new AtomicReference<String>(null);
	public final AtomicReference<String> block_subceiling = new AtomicReference<String>(null);
	public final AtomicReference<String> block_carpet     = new AtomicReference<String>(null);
	public final AtomicReference<String> block_ceiling    = new AtomicReference<String>(null);



	public Gen_000(final BackroomsLevel backlevel,
			final int level_y, final int level_h) {
		super(backlevel, level_y, level_h);
		// lobby walls
		this.noiseLobbyWalls = this.register(new FastNoiseLiteD());
		this.noiseLobbyWalls.setFrequency(0.022);
		this.noiseLobbyWalls.setFractalOctaves(2);
		this.noiseLobbyWalls.setFractalGain(0.1);
		this.noiseLobbyWalls.setFractalLacunarity(0.4);
		this.noiseLobbyWalls.setNoiseType(NoiseType.Cellular);
		this.noiseLobbyWalls.setFractalType(FractalType.PingPong);
		this.noiseLobbyWalls.setFractalPingPongStrength(2.28);
		this.noiseLobbyWalls.setCellularDistanceFunction(CellularDistanceFunction.Manhattan);
		this.noiseLobbyWalls.setCellularReturnType(CellularReturnType.Distance);
		// chest loot
		this.noiseLoot = this.register(new FastNoiseLiteD());
		this.noiseLoot.setFrequency(0.1);
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
				valueWall > Gen_000.this.thresh_wall_L.get() &&
				valueWall < Gen_000.this.thresh_wall_H.get()
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
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
		if (!ENABLE_GEN_000) return;
		final BlockData block_wall       = StringToBlockData(this.block_wall,       DEFAULT_BLOCK_WALL      );
		final BlockData block_wall_base  = StringToBlockData(this.block_wall_base,  DEFAULT_BLOCK_WALL_BASE );
		final BlockData block_subfloor   = StringToBlockData(this.block_subfloor,   DEFAULT_BLOCK_SUBFLOOR  );
		final BlockData block_subceiling = StringToBlockData(this.block_subceiling, DEFAULT_BLOCK_SUBCEILING);
		final BlockData block_carpet     = StringToBlockData(this.block_carpet,     DEFAULT_BLOCK_CARPET    );
		final BlockData block_ceiling    = StringToBlockData(this.block_ceiling,    DEFAULT_BLOCK_CEILING   );
		final BlockData block_overgrowth_wall = StringToBlockData(((Level_000)this.backlevel).gen_023.block_wall, Gen_023.DEFAULT_BLOCK_WALL);
		if (block_wall       == null) throw new RuntimeException("Invalid block type for level 0 Wall"      );
		if (block_wall_base  == null) throw new RuntimeException("Invalid block type for level 0 WallBase"  );
		if (block_subfloor   == null) throw new RuntimeException("Invalid block type for level 0 SubFloor"  );
		if (block_subceiling == null) throw new RuntimeException("Invalid block type for level 0 SubCeiling");
		if (block_carpet     == null) throw new RuntimeException("Invalid block type for level 0 Carpet"    );
		if (block_ceiling    == null) throw new RuntimeException("Invalid block type for level 0 Ceiling"   );
		if (block_overgrowth_wall == null) throw new RuntimeException("Invalid block type for level 23 Wall");
		final HashMap<Iab, LobbyData>    lobbyData    = ((PregenLevel0)pregen).lobby;
		final HashMap<Iab, BasementData> basementData = ((PregenLevel0)pregen).basement;
		final LinkedList<Iabc> chests = new LinkedList<Iabc>();
		LobbyData dao;
		final int y  = this.level_y + SUBFLOOR + 1;
		final int cy = this.level_h + y + 1;
		int xx, zz;
		int modX7, modZ7;
		for (int iz=0; iz<16; iz++) {
			zz = (chunkZ * 16) + iz;
			for (int ix=0; ix<16; ix++) {
				xx = (chunkX * 16) + ix;
				// lobby floor
				chunk.setBlock(ix, this.level_y, iz, Material.BEDROCK);
				for (int iy=0; iy<SUBFLOOR; iy++)
					chunk.setBlock(ix, this.level_y+iy+1, iz, block_subfloor);
				dao = lobbyData.get(new Iab(ix, iz));
				if (dao == null) continue;
				// wall
				if (dao.isWall) {
					// lobby walls
					final int h = this.level_h + 3;
					chunk.setBlock(ix, y,   iz, block_subfloor );
					chunk.setBlock(ix, y+1, iz, block_wall_base);
					for (int iy=2; iy<h; iy++)
						chunk.setBlock(ix, y+iy, iz, block_wall);
				// room
				} else {
					// floor
					chunk.setBlock(ix, y, iz, block_carpet);
					if (ENABLE_TOP_000) {
						modX7 = (xx < 0 ? 1-xx : xx) % 7;
						modZ7 = (zz < 0 ? 0-zz : zz) % 7;
						if (modZ7 == 0 && modX7 < 2
						&& dao.wall_dist > 2) {
							// ceiling lights
							chunk.setBlock(ix, cy, iz, Material.REDSTONE_LAMP);
							final BlockData block = chunk.getBlockData(ix, cy, iz);
							((Lightable)block).setLit(true);
							chunk.setBlock(ix, cy,   iz, block);
							chunk.setBlock(ix, cy+1, iz, Material.REDSTONE_BLOCK);
						} else {
							// ceiling
							chunk.setBlock(ix, cy,   iz, block_ceiling   );
							chunk.setBlock(ix, cy+1, iz, block_subceiling);
						}
					}
					// special
					if (dao.boxed > 4) {
						// loot
						if (dao.wall_dist == 1) {
							((Level_000)this.backlevel).loot_chests_0.add(xx, zz);
							chunk.setBlock(ix, y+1, iz, Material.BARREL);
							final Barrel barrel = (Barrel) chunk.getBlockData(ix, y+1, iz);
							barrel.setFacing(BlockFace.UP);
							chunk.setBlock(ix, y+1, iz, barrel);
							chests.add(new Iabc(xx, y+1, zz));
						} else
						// portal to basement
						if (dao.boxed     == 7
						&&  dao.wall_dist == 2
						&&  dao.box_dir != null) {
							boolean found_basement_wall = false;
							BasementData base;
							for (int izb=-2; izb<3; izb++) {
								for (int ixb=-2; ixb<3; ixb++) {
									base = basementData.get(new Iab(ixb+ix, izb+iz));
									if (base != null
									&&  base.isWall) {
										found_basement_wall = true;
										break;
									}
								}
							}
							if (!found_basement_wall) {
								((Level_000)this.backlevel).portal_0_to_1.add(xx, zz);
								final int h = Level_000.H_023 + this.level_h + (SUBFLOOR*3) + (SUBCEILING*2);
								final PlotterFactory factory =
									(new PlotterFactory())
									.placer(chunk)
									.axis("use")
									.rotate(dao.box_dir)
									.y((cy - h) + 1)
									.whd(5, h, 6);
								switch (dao.box_dir) {
								case NORTH: factory.xz(ix-3, iz-4); break;
								case SOUTH: factory.xz(ix,   iz  ); break;
								case EAST:  factory.xz(ix-2, iz-2); break;
								case WEST:  factory.xz(ix-4, iz-3); break;
								default: throw new RuntimeException("Unknown boxed walls direction: " + dao.box_dir.toString());
								}
								final BlockPlotter plot = factory.build();
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
								// lower area
								for (int i=1; i<3; i++) {
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
								iy++;
								matrix[iy][1].append(" xxx");
								matrix[iy][2].append(" x.x");
								matrix[iy][3].append(" xxx");
								// shaft through overgrowth
								for (int i=-1; i<Level_000.H_023; i++) {
									iy++;
									matrix[iy][0].append("mmmmm");
									matrix[iy][1].append("mxxxm");
									matrix[iy][2].append("mx.xm");
									matrix[iy][3].append("mxxxm");
									matrix[iy][4].append("mmmmm");
								}
								// shaft between overgrowth and lobby
								final int hh = SUBCEILING + SUBFLOOR + 2;
								for (int i=0; i<hh; i++) {
									iy++;
									matrix[iy][1].append(" xxx");
									matrix[iy][2].append(" x.x");
									matrix[iy][3].append(" xxx");
								}
								// opening in lobby
								for (int i=0; i<Level_000.H_000; i++) {
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
								plots.add(plot);
							}
						} // end portal to basement
					} // end special
				} // end wall/room
				// subceiling
				if (ENABLE_TOP_000) {
					for (int i=1; i<SUBCEILING; i++)
						chunk.setBlock(ix, cy+i+1, iz, block_subceiling);
				}
			} // end ix
		} // end iz
		if (!chests.isEmpty()) {
			for (final Iabc loc : chests) {
				(new ChestFiller_000(this.plugin, "level0", loc.a, loc.b, loc.c))
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
				if (value > Gen_000.this.thresh_loot.get())
					chest.setItem(i, item);
			}
		}

	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void loadConfig() {
		// params
		{
			final ConfigurationSection cfg = this.plugin.getLevelParams(0);
			this.thresh_wall_L.set(cfg.getDouble("Thresh-Wall-L"));
			this.thresh_wall_H.set(cfg.getDouble("Thresh-Wall-H"));
			this.thresh_loot  .set(cfg.getDouble("Thresh-Loot"  ));
		}
		// block types
		{
			final ConfigurationSection cfg = this.plugin.getLevelBlocks(0);
			this.block_wall      .set(cfg.getString("Wall"      ));
			this.block_wall_base .set(cfg.getString("WallBase"  ));
			this.block_subfloor  .set(cfg.getString("SubFloor"  ));
			this.block_subceiling.set(cfg.getString("SubCeiling"));
			this.block_carpet    .set(cfg.getString("Carpet"    ));
			this.block_ceiling   .set(cfg.getString("Ceiling"   ));
		}
	}
	public static void ConfigDefaults(final FileConfiguration cfg) {
		// params
		cfg.addDefault("Level0.Params.Thresh-Wall-L", DEFAULT_THRESH_WALL_L);
		cfg.addDefault("Level0.Params.Thresh-Wall-H", DEFAULT_THRESH_WALL_H);
		cfg.addDefault("Level0.Params.Thresh-Loot",   DEFAULT_THRESH_LOOT  );
		// block types
		cfg.addDefault("Level0.Blocks.Wall",       DEFAULT_BLOCK_WALL      );
		cfg.addDefault("Level0.Blocks.WallBase",   DEFAULT_BLOCK_WALL_BASE );
		cfg.addDefault("Level0.Blocks.SubFloor",   DEFAULT_BLOCK_SUBFLOOR  );
		cfg.addDefault("Level0.Blocks.SubCeiling", DEFAULT_BLOCK_SUBCEILING);
		cfg.addDefault("Level0.Blocks.Carpet",     DEFAULT_BLOCK_CARPET    );
		cfg.addDefault("Level0.Blocks.Ceiling",    DEFAULT_BLOCK_CEILING   );
	}



}
