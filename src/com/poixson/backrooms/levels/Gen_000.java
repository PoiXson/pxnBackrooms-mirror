package com.poixson.backrooms.levels;

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

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.levels.Gen_001.BasementData;
import com.poixson.backrooms.levels.Level_000.PregenLevel0;
import com.poixson.commonmc.tools.BlockPlotter;
import com.poixson.commonmc.tools.DelayedBlockPlotter;
import com.poixson.commonmc.tools.DelayedChestFiller;
import com.poixson.tools.dao.Ixy;
import com.poixson.tools.dao.Ixyz;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.CellularReturnType;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;
import com.poixson.utils.FastNoiseLiteD.RotationType3D;


// 0 | Lobby
public class Gen_000 extends GenBackrooms {

	public static final double THRESH_WALL_L = 0.38;
	public static final double THRESH_WALL_H = 0.5;

	public static final Material LOBBY_WALL = Material.YELLOW_TERRACOTTA;
	public static final Material LOBBY_SUBFLOOR  = Material.OAK_PLANKS;

	public final boolean buildroof;
	public final int subfloor;
	public final int subceiling;

	// noise
	protected final FastNoiseLiteD noiseLobbyWalls;
	protected final FastNoiseLiteD noiseLoot;



	public Gen_000(final BackroomsPlugin plugin, final int level_y, final int level_h,
			final boolean buildroof, final int subfloor, final int subceiling) {
		super(plugin, level_y, level_h);
		this.buildroof  = buildroof;
		this.subfloor   = subfloor;
		this.subceiling = subceiling;
		// lobby walls
		this.noiseLobbyWalls = this.register(new FastNoiseLiteD());
		this.noiseLobbyWalls.setFrequency(0.023);
		this.noiseLobbyWalls.setFractalOctaves(2);
		this.noiseLobbyWalls.setFractalGain(0.05);
		this.noiseLobbyWalls.setNoiseType(NoiseType.Cellular);
		this.noiseLobbyWalls.setFractalType(FractalType.PingPong);
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
			this.isWall = (valueWall > THRESH_WALL_L && valueWall < THRESH_WALL_H);
		}
	}



	public void pregenerate(Map<Ixy, LobbyData> data,
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
				data.put(new Ixy(x, z), dao);
			}
		}
		// find wall distance
		LobbyData dao_near;
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
				dao = data.get(new Ixy(x, z));
				if (dao.isWall) {
					dao.wall_dist = 0;
					continue;
				}
				for (int i=1; i<3; i++) {
					dao.boxed = 0;
					// north
					dao_near = data.get(new Ixy(x, z-i));
					if (dao_near != null && dao_near.isWall) {
						if (dao.wall_dist > i)
							dao.wall_dist = i;
						dao.wall_n = true;
						dao.boxed++;
					}
					// south
					dao_near = data.get(new Ixy(x, z+i));
					if (dao_near != null && dao_near.isWall) {
						if (dao.wall_dist > i)
							dao.wall_dist = i;
						dao.wall_s = true;
						dao.boxed++;
					}
					// east
					dao_near = data.get(new Ixy(x+i, z));
					if (dao_near != null && dao_near.isWall) {
						if (dao.wall_dist > i)
							dao.wall_dist = i;
						dao.wall_e = true;
						dao.boxed++;
					}
					// west
					dao_near = data.get(new Ixy(x-i, z));
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
							dao_near = data.get(new Ixy(x+i, z-i));
							if (dao_near != null && dao_near.isWall)
								dao.boxed++;
						}
						// north-west
						if (dao.wall_n || dao.wall_w) {
							dao_near = data.get(new Ixy(x-i, z-i));
							if (dao_near != null && dao_near.isWall)
								dao.boxed++;
						}
						// south-east
						if (dao.wall_s || dao.wall_e) {
							dao_near = data.get(new Ixy(x+i, z+i));
							if (dao_near != null && dao_near.isWall)
								dao.boxed++;
						}
						// south-west
						if (dao.wall_s || dao.wall_w) {
							dao_near = data.get(new Ixy(x-i, z+i));
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
	public void generate(final PreGenData pregen,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		final LinkedList<DelayedBlockPlotter> delayed = new LinkedList<DelayedBlockPlotter>();
		final LinkedList<Ixyz> chests = new LinkedList<Ixyz>();
		LobbyData dao;
		int cy = this.level_y + this.level_h + this.subfloor;
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
				final int xx = (chunkX * 16) + x;
				final int zz = (chunkZ * 16) + z;
				int y  = this.level_y;
				// lobby floor
				chunk.setBlock(x, y, z, Material.BEDROCK);
				y++;
				for (int yy=0; yy<this.subfloor; yy++) {
					chunk.setBlock(x, y+yy, z, LOBBY_SUBFLOOR);
				}
				y += this.subfloor;
				dao = (LobbyData) ((PregenLevel0)pregen).lobby.get(new Ixy(x, z));
				if (dao == null) continue;
				// wall
				if (dao.isWall) {
					// lobby walls
					final int h = this.level_h + 1;
					for (int yy=0; yy<h; yy++) {
						chunk.setBlock(x, y+yy, z, LOBBY_WALL);
					}
				// room
				} else {
					chunk.setBlock(x, y, z, Material.LIGHT_GRAY_WOOL);
					if (this.buildroof) {
						final int modX6 = Math.abs(xx) % 7;
						final int modZ6 = Math.abs(zz) % 7;
						if (modZ6 == 0 && modX6 < 2
						&& dao.wall_dist > 1) {
							// ceiling lights
							chunk.setBlock(x, cy, z, Material.REDSTONE_LAMP);
							final BlockData block = chunk.getBlockData(x, cy, z);
							((Lightable)block).setLit(true);
							chunk.setBlock(x, cy, z, block);
							chunk.setBlock(x, cy+1, z, Material.REDSTONE_BLOCK);
						} else {
							// ceiling
							chunk.setBlock(x, cy, z, Material.SMOOTH_STONE_SLAB);
							final Slab slab = (Slab) chunk.getBlockData(x, cy, z);
							slab.setType(Slab.Type.TOP);
							chunk.setBlock(x, cy, z, slab);
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
							chests.add(new Ixyz(xx, y+1, zz));
						} else
						// portal to basement
						if (dao.boxed     == 7
						&&  dao.wall_dist == 2
						&&  dao.box_dir != null) {
							boolean found_wall = false;
							BasementData base;
							for (int iz=-2; iz<3; iz++) {
								for (int ix=-2; ix<3; ix++) {
									base = ((PregenLevel0)pregen).basement.get(new Ixy(ix, iz));
									if (base.isWall) {
										found_wall = true;
										break;
									}
								}
							}
							if (!found_wall) {
								final String axis;
								final int xxx, zzz;
								switch (dao.box_dir) {
								case NORTH: axis = "YzX"; xxx = x - 2; zzz = z + 2; break;
								case SOUTH: axis = "YZX"; xxx = x - 2; zzz = z - 2; break;
								case EAST:  axis = "YXZ"; xxx = x - 2; zzz = z - 2; break;
								case WEST:  axis = "YxZ"; xxx = x + 2; zzz = z - 2; break;
								default: throw new RuntimeException("Unknown boxed walls direction: " + dao.box_dir.toString());
								}
								final BlockPlotter plot = new BlockPlotter(chunk, xxx, y-6, zzz);
								plot.types.put(Character.valueOf('.'), Material.AIR);
								plot.types.put(Character.valueOf('='), Material.YELLOW_TERRACOTTA);
								plot.types.put(Character.valueOf('x'), Material.BEDROCK);
								final int h = this.level_h + this.subfloor + 3;
								final StringBuilder[][] matrix = plot.getEmptyMatrix3D(h, 5);
								for (int i=0; i<h; i++) {
									// bottom
									if (i == 0) {
										matrix[i][0].append("xxxxx");
										matrix[i][1].append("xxxxx");
										matrix[i][2].append("xxxxx");
										matrix[i][3].append("xx.xx");
										matrix[i][4].append("xxxxx");
									} else
									// subfloor
									if (i < 6) {
										matrix[i][0].append("xxxxx");
										matrix[i][1].append("xxxxx");
										matrix[i][2].append("xx.xx");
										matrix[i][3].append("xx.xx");
										matrix[i][4].append("xxxxx");
									} else
									// floor
									if (i == 6) {
										matrix[i][0].append("xxxxx");
										matrix[i][1].append("xxxxx");
										matrix[i][2].append("xx.xx");
										matrix[i][3].append("xxxxx");
										matrix[i][4].append("xxxxx");
									} else
									// top
									if (i > h-2) {
										matrix[i][0].append("=====");
										matrix[i][1].append("=xxx=");
										matrix[i][2].append("=xxx=");
										matrix[i][3].append("=xxx=");
										matrix[i][4].append("== ==");
									// walls
									} else {
										matrix[i][0].append("=====");
										matrix[i][1].append("=xxx=");
										matrix[i][2].append("=x.x=");
										matrix[i][3].append("=x.x=");
										matrix[i][4].append("==.==");
									}
								}
								delayed.add(new DelayedBlockPlotter(plot, axis, matrix));
							}
						}
					}
				} // end wall/room
				if (this.buildroof) {
					for (int i=1; i<this.subceiling; i++) {
						chunk.setBlock(x, cy+i+1, z, Material.STONE);
					}
				}
			} // end x
		} // end z
		// place delayed blocks
		if (!delayed.isEmpty()) {
			for (final DelayedBlockPlotter plot : delayed) {
				plot.run();
			}
		}
		if (!chests.isEmpty()) {
			for (final Ixyz loc : chests) {
				(new ChestFiller_000(this.plugin, "level0", loc.x, loc.y, loc.z))
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
				if (value > 0.7)
					chest.setItem(i, item);
			}
		}

	}



}
