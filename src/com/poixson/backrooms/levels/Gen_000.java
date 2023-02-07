package com.poixson.backrooms.levels;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.type.Slab;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.levels.Gen_001.BasementData;
import com.poixson.backrooms.levels.Level_000.PregenLevel0;
import com.poixson.tools.dao.Ixy;
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
						if (modZ6 == 0 && modX6 < 2) {
//TODO: not near walls
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
				}
				if (this.buildroof) {
					for (int i=1; i<this.subceiling; i++) {
						chunk.setBlock(x, cy+i+1, z, Material.STONE);
					}
				}
			} // end x
		} // end z
	}



}
