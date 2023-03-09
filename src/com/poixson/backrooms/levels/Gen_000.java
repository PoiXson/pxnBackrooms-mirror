package com.poixson.backrooms.levels;

import static com.poixson.backrooms.levels.Level_000.SUBCEILING;
import static com.poixson.backrooms.levels.Level_000.SUBFLOOR;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.type.Barrel;
import org.bukkit.block.data.type.Slab;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.poixson.backrooms.levels.Gen_001.BasementData;
import com.poixson.backrooms.levels.Level_000.PregenLevel0;
import com.poixson.commonmc.tools.DelayedChestFiller;
import com.poixson.commonmc.tools.plotter.BlockPlotter;
import com.poixson.tools.dao.Iab;
import com.poixson.tools.dao.Iabc;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.CellularReturnType;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;
import com.poixson.utils.FastNoiseLiteD.RotationType3D;


// 0 | Lobby
public class Gen_000 extends GenBackrooms {

	public static final boolean ENABLE_GENERATE = true;
	public static final boolean ENABLE_ROOF     = true;

	public static final double THRESH_WALL_L = 0.38;
	public static final double THRESH_WALL_H = 0.5;
	public static final double THRESH_LOOT   = 0.65;

	public static final Material LOBBY_WALL     = Material.YELLOW_TERRACOTTA;
	public static final Material LOBBY_SUBFLOOR = Material.OAK_PLANKS;
	public static final Material LOBBY_CARPET   = Material.LIGHT_GRAY_WOOL;

	// noise
	protected final FastNoiseLiteD noiseLobbyWalls;
	protected final FastNoiseLiteD noiseLoot;



	public Gen_000(final LevelBackrooms backlevel,
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
		this.noiseLobbyWalls.setRotationType3D(RotationType3D.ImproveXYPlanes);
		// chest loot
		this.noiseLoot = this.register(new FastNoiseLiteD());
		this.noiseLoot.setFrequency(0.1);
		this.noiseLoot.setFractalOctaves(1);
		this.noiseLoot.setNoiseType(NoiseType.OpenSimplex2);
		this.noiseLoot.setRotationType3D(RotationType3D.ImproveXYPlanes);
	}



	public class LobbyData implements PreGenData {

		public final double valueWall;
		public final boolean isWall;
		public boolean wall_n = false;
		public boolean wall_s = false;
		public boolean wall_e = false;
		public boolean wall_w = false;
		public int wall_dist = 5;
		public int boxed = 0;
		public BlockFace box_dir = null;

		public LobbyData(final double valueWall) {
			this.valueWall = valueWall;
			this.isWall = (
				valueWall > THRESH_WALL_L &&
				valueWall < THRESH_WALL_H
			);
		}

	}



	public void pregenerate(Map<Iab, LobbyData> data,
			final int chunkX, final int chunkZ) {
		LobbyData dao;
		int xx, zz;
		double valueWall;
		for (int z=-1; z<17; z++) {
			zz = (chunkZ * 16) + z;
			for (int x=-1; x<17; x++) {
				xx = (chunkX * 16) + x;
				valueWall = this.noiseLobbyWalls.getNoiseRot(xx, zz, 0.25);
				dao = new LobbyData(valueWall);
				data.put(new Iab(x, z), dao);
			}
		}
		// find wall distance
		LobbyData dao_near;
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
				dao = data.get(new Iab(x, z));
				if (dao.isWall) {
					dao.wall_dist = 0;
					continue;
				}
				for (int i=1; i<3; i++) {
					dao.boxed = 0;
					// north
					dao_near = data.get(new Iab(x, z-i));
					if (dao_near != null && dao_near.isWall) {
						if (dao.wall_dist > i)
							dao.wall_dist = i;
						dao.wall_n = true;
						dao.boxed++;
					}
					// south
					dao_near = data.get(new Iab(x, z+i));
					if (dao_near != null && dao_near.isWall) {
						if (dao.wall_dist > i)
							dao.wall_dist = i;
						dao.wall_s = true;
						dao.boxed++;
					}
					// east
					dao_near = data.get(new Iab(x+i, z));
					if (dao_near != null && dao_near.isWall) {
						if (dao.wall_dist > i)
							dao.wall_dist = i;
						dao.wall_e = true;
						dao.boxed++;
					}
					// west
					dao_near = data.get(new Iab(x-i, z));
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
							dao_near = data.get(new Iab(x+i, z-i));
							if (dao_near != null && dao_near.isWall)
								dao.boxed++;
						}
						// north-west
						if (dao.wall_n || dao.wall_w) {
							dao_near = data.get(new Iab(x-i, z-i));
							if (dao_near != null && dao_near.isWall)
								dao.boxed++;
						}
						// south-east
						if (dao.wall_s || dao.wall_e) {
							dao_near = data.get(new Iab(x+i, z+i));
							if (dao_near != null && dao_near.isWall)
								dao.boxed++;
						}
						// south-west
						if (dao.wall_s || dao.wall_w) {
							dao_near = data.get(new Iab(x-i, z+i));
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
			} // end x
		} // end z
	}

	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
		if (!ENABLE_GENERATE) return;
		final HashMap<Iab, LobbyData>    lobbyData    = ((PregenLevel0)pregen).lobby;
		final HashMap<Iab, BasementData> basementData = ((PregenLevel0)pregen).basement;
		final LinkedList<Iabc> chests = new LinkedList<Iabc>();
		LobbyData dao;
		final int y  = this.level_y + SUBFLOOR + 1;
		final int cy = this.level_h + y + 1;
		int xx, zz;
		for (int z=0; z<16; z++) {
			zz = (chunkZ * 16) + z;
			for (int x=0; x<16; x++) {
				xx = (chunkX * 16) + x;
				// lobby floor
				chunk.setBlock(x, this.level_y, z, Material.BEDROCK);
				for (int iy=0; iy<SUBFLOOR; iy++)
					chunk.setBlock(x, this.level_y+iy+1, z, LOBBY_SUBFLOOR);
				dao = lobbyData.get(new Iab(x, z));
				if (dao == null) continue;
				// wall
				if (dao.isWall) {
					// lobby walls
					final int h = this.level_h + 3;
					for (int iy=0; iy<h; iy++)
						chunk.setBlock(x, y+iy, z, LOBBY_WALL);
				// room
				} else {
					// floor
					chunk.setBlock(x, y, z, LOBBY_CARPET);
					if (ENABLE_ROOF) {
						final int  modX7 = (xx < 0 ? 1-xx : xx) % 7;
						final int  modZ7 = (zz < 0 ? 0-zz : zz) % 7;
						if (modZ7 == 0 && modX7 < 2
						&& dao.wall_dist > 1) {
							// ceiling lights
							chunk.setBlock(x, cy, z, Material.REDSTONE_LAMP);
							final BlockData block = chunk.getBlockData(x, cy, z);
							((Lightable)block).setLit(true);
							chunk.setBlock(x, cy,   z, block);
							chunk.setBlock(x, cy+1, z, Material.REDSTONE_BLOCK);
						} else {
							// ceiling
							chunk.setBlock(x, cy, z, Material.SMOOTH_STONE_SLAB);
							final Slab slab = (Slab) chunk.getBlockData(x, cy, z);
							slab.setType(Slab.Type.TOP);
							chunk.setBlock(x, cy,   z, slab);
							chunk.setBlock(x, cy+1, z, Material.STONE);
						}
					}
					// special
					if (dao.boxed > 4) {
						// barrel
						if (dao.wall_dist == 1) {
							chunk.setBlock(x, y+1, z, Material.BARREL);
							final Barrel barrel = (Barrel) chunk.getBlockData(x, y+1, z);
							barrel.setFacing(BlockFace.UP);
							chunk.setBlock(x, y+1, z, barrel);
							chests.add(new Iabc(xx, y+1, zz));
						} else
						// portal to basement
						if (dao.boxed     == 7
						&&  dao.wall_dist == 2
						&&  dao.box_dir != null) {
							boolean found_basement_wall = false;
							BasementData base;
							for (int iz=-2; iz<3; iz++) {
								for (int ix=-2; ix<3; ix++) {
									base = basementData.get(new Iab(ix+x, iz+z));
									if (base != null
									&&  base.isWall) {
										found_basement_wall = true;
										break;
									}
								}
							}
							if (!found_basement_wall) {
								final int h = Level_000.H_023 + this.level_h + (SUBFLOOR*3) + (SUBCEILING*2);
								final BlockPlotter plot = new BlockPlotter(chunk, h, 6, 5);
								switch (dao.box_dir) {
								case NORTH: plot.axis("une").location(x-2, 0, z+2); break;
								case SOUTH: plot.axis("use").location(x-2, 0, z-2); break;
								case EAST:  plot.axis("ues").location(x-2, 0, z-2); break;
								case WEST:  plot.axis("uws").location(x+2, 0, z-2); break;
								default: throw new RuntimeException("Unknown boxed walls direction: " + dao.box_dir.toString());
								}
								plot.y((cy - h) + 1);
								plot.type('.', Material.AIR              );
								plot.type('=', Material.YELLOW_TERRACOTTA);
								plot.type('x', Material.BEDROCK          );
								plot.type('g', Material.GLOWSTONE        );
								plot.type('m', Material.MOSS_BLOCK       );
								final StringBuilder[][] matrix = plot.getMatrix3D();
								// bottom
								matrix[0][0].append("xxxxx");
								matrix[0][1].append("xxxxx");
								matrix[0][2].append("xxxxx");
								matrix[0][3].append("xxgxx");
								matrix[0][4].append("xg.gx");
								matrix[0][5].append("xxgxx");
								// lower area
								for (int iy=1; iy<5; iy++) {
									matrix[iy][1].append(" xxx ");
									matrix[iy][2].append(" x.x ");
									matrix[iy][3].append(" x.x ");
									matrix[iy][4].append(" x.x ");
									matrix[iy][5].append(" xxx ");
								}
								// lower ceiling
								matrix[5][1].append(" xxx ");
								matrix[5][2].append(" x.x ");
								matrix[5][3].append(" xxx ");
								matrix[5][4].append(" xxx ");
								matrix[5][5].append(" xxx ");
								// shaft
								final int hh = h - this.level_h - 1;
								for (int i=6; i<hh; i++) {
									if (i > 6 && i < Level_000.H_023+8) {
										matrix[i][0].append("mmmmm");
										matrix[i][1].append("mxxxm");
										matrix[i][2].append("mx.xm");
										matrix[i][3].append("mxxxm");
										matrix[i][4].append("mmmmm");
									} else {
										matrix[i][1].append(" xxx ");
										matrix[i][2].append(" x.x ");
										matrix[i][3].append(" xxx ");
									}
								}
								// opening
								for (int i=hh; i<h-1; i++) {
									matrix[i][0].append("=====");
									matrix[i][1].append("=xxx=");
									matrix[i][2].append("=x.x=");
									matrix[i][3].append("=x.x=");
									matrix[i][4].append("==.==");
								}
								// top
								matrix[h-1][0].append("=====");
								matrix[h-1][1].append("=xxx=");
								matrix[h-1][2].append("=xxx=");
								matrix[h-1][3].append("=xxx=");
								matrix[h-1][4].append("== ==");
								plots.add(plot);
							}
						} // end portal to basement
					} // end special
				} // end wall/room
				// subceiling
				if (ENABLE_ROOF) {
					for (int i=1; i<SUBCEILING; i++)
						chunk.setBlock(x, cy+i+1, z, Material.STONE);
				}
			} // end x
		} // end z
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
				if (value > THRESH_LOOT)
					chest.setItem(i, item);
			}
		}

	}



}
