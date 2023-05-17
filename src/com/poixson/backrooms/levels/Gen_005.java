package com.poixson.backrooms.levels;

import static com.poixson.backrooms.levels.Level_000.ENABLE_GEN_005;
import static com.poixson.backrooms.levels.Level_000.ENABLE_TOP_005;
import static com.poixson.backrooms.levels.Level_000.SUBCEILING;
import static com.poixson.backrooms.levels.Level_000.SUBFLOOR;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.type.Slab;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.levels.Level_000.PregenLevel0;
import com.poixson.commonmc.tools.plotter.BlockPlotter;
import com.poixson.tools.dao.Iab;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;


// 5 | Hotel
public class Gen_005 extends GenBackrooms {

	public static final double THRESH_ROOM_HALL = 0.65;

	public static final Material HOTEL_FLOOR = Material.BLACK_GLAZED_TERRACOTTA;
	public static final Material HOTEL_WALL  = Material.STRIPPED_SPRUCE_WOOD;

	// noise
	public final FastNoiseLiteD noiseHotelWalls;
	public final FastNoiseLiteD noiseHotelRooms;



	public Gen_005(final LevelBackrooms backlevel,
			final int level_y, final int level_h) {
		super(backlevel, level_y, level_h);
		// hotel walls
		this.noiseHotelWalls = this.register(new FastNoiseLiteD());
		this.noiseHotelWalls.setFrequency(0.02);
		this.noiseHotelWalls.setCellularJitter(0.3);
		this.noiseHotelWalls.setNoiseType(NoiseType.Cellular);
		this.noiseHotelWalls.setFractalType(FractalType.PingPong);
		this.noiseHotelWalls.setCellularDistanceFunction(CellularDistanceFunction.Manhattan);
		// hotel rooms
		this.noiseHotelRooms = this.register(new FastNoiseLiteD());
		this.noiseHotelRooms.setFrequency(0.01);
		this.noiseHotelRooms.setFractalOctaves(2);
		this.noiseHotelRooms.setFractalType(FractalType.FBm);
		this.noiseHotelRooms.setFractalGain(0.6);
	}



	public enum NodeType {
		HALL,
		ROOM,
		WALL,
	};

	public class HotelData implements PreGenData {

		public final double value;
		public NodeType type;
		public boolean hall_center = false;

		public HotelData(final double value) {
			this.value = value;
			this.type = (value>THRESH_ROOM_HALL ? NodeType.HALL : NodeType.ROOM);
		}

	}



	public void pregenerate(Map<Iab, HotelData> data,
			final int chunkX, final int chunkZ) {
		HotelData dao;
		int xx, zz;
		double value;
		for (int z=-8; z<24; z++) {
			zz = (chunkZ * 16) + z;
			for (int x=-8; x<24; x++) {
				xx = (chunkX * 16) + x;
				value = this.noiseHotelWalls.getNoiseRot(xx, zz, 0.25);
				dao = new HotelData(value);
				data.put(new Iab(x, z), dao);
			}
		}
		HotelData daoN, daoS, daoE, daoW;
		HotelData daoNE, daoNW, daoSE, daoSW;
		// find walls
		for (int z=-8; z<24; z++) {
			for (int x=-8; x<24; x++) {
				dao = data.get(new Iab(x, z));
				if (NodeType.ROOM.equals(dao.type)) {
					daoN  = data.get(new Iab(x,   z-1));
					daoS  = data.get(new Iab(x,   z+1));
					daoE  = data.get(new Iab(x+1, z  ));
					daoW  = data.get(new Iab(x-1, z  ));
					daoNE = data.get(new Iab(x+1, z-1));
					daoNW = data.get(new Iab(x-1, z-1));
					daoSE = data.get(new Iab(x+1, z+1));
					daoSW = data.get(new Iab(x-1, z+1));
					if (daoN != null && NodeType.HALL.equals(daoN.type)) {
						dao.type = NodeType.WALL;
						LOOP_I:
						for (int i=3; i<9; i++) {
							daoN = data.get(new Iab(x, z-i));
							if (daoN == null) break LOOP_I;
							if (!NodeType.HALL.equals(daoN.type)) {
								daoN = data.get(new Iab(x, z-Math.floorDiv(i, 2)));
								daoN.hall_center = true;
								break LOOP_I;
							}
						}
					}
					if (daoS != null && NodeType.HALL.equals(daoS.type)) {
						dao.type = NodeType.WALL;
						LOOP_I:
						for (int i=3; i<9; i++) {
							daoS = data.get(new Iab(x, z+i));
							if (daoS == null) break LOOP_I;
							if (!NodeType.HALL.equals(daoS.type)) {
								daoS = data.get(new Iab(x, z+Math.floorDiv(i, 2)));
								daoS.hall_center = true;
								break LOOP_I;
							}
						}
					}
					if (daoE != null && NodeType.HALL.equals(daoE.type)) {
						dao.type = NodeType.WALL;
						LOOP_I:
						for (int i=3; i<9; i++) {
							daoE = data.get(new Iab(x+i, z));
							if (daoE == null) break LOOP_I;
							if (!NodeType.HALL.equals(daoE.type)) {
								daoE = data.get(new Iab(x+Math.floorDiv(i, 2), z));
								daoE.hall_center = true;
								break LOOP_I;
							}
						}
					}
					if (daoW != null && NodeType.HALL.equals(daoW.type)) {
						dao.type = NodeType.WALL;
						LOOP_I:
						for (int i=3; i<9; i++) {
							daoW = data.get(new Iab(x-i, z));
							if (daoW == null) break LOOP_I;
							if (!NodeType.HALL.equals(daoW.type)) {
								daoW = data.get(new Iab(x-Math.floorDiv(i, 2), z));
								daoW.hall_center = true;
								break LOOP_I;
							}
						}
					}
					if ((daoNE != null && NodeType.HALL.equals(daoNE.type))
					||  (daoNW != null && NodeType.HALL.equals(daoNW.type))
					||  (daoSE != null && NodeType.HALL.equals(daoSE.type))
					||  (daoSW != null && NodeType.HALL.equals(daoSW.type)) )
						dao.type = NodeType.WALL;
				}
			} // end for x
		} // end for z
	}
	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
		if (!ENABLE_GEN_005) return;
		final HashMap<Iab, HotelData> hotelData = ((PregenLevel0)pregen).hotel;
		final int y  = this.level_y + SUBFLOOR + 1;
		final int cy = this.level_y + SUBFLOOR + this.level_h + 2;
		final int h  = this.level_h + 2;
		int xx, zz;
		int mod_x, mod_z;
		HotelData dao;
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
				xx = (chunkX * 16) + x;
				zz = (chunkZ * 16) + z;
				// hotel floor
				chunk.setBlock(x, this.level_y, z, Material.BEDROCK);
				for (int yy=0; yy<SUBFLOOR; yy++)
					chunk.setBlock(x, this.level_y+yy+1, z, Material.SPRUCE_PLANKS);
				dao = hotelData.get(new Iab(x, z));
				if (dao == null) continue;
				switch (dao.type) {
				case WALL:
					for (int iy=0; iy<h; iy++) {
						chunk.setBlock(x, y+iy, z, HOTEL_WALL);
					}
					break;
				case HALL: {
					chunk.setBlock(x, y, z, HOTEL_FLOOR);
					final Directional tile = (Directional) chunk.getBlockData(x, y, z);
					if (z % 2 == 0) {
						if (x % 2 == 0) tile.setFacing(BlockFace.NORTH);
						else            tile.setFacing(BlockFace.WEST );
					} else {
						if (x % 2 == 0) tile.setFacing(BlockFace.EAST );
						else            tile.setFacing(BlockFace.SOUTH);
					}
					chunk.setBlock(x, y, z, tile);
					if (ENABLE_TOP_005) {
						// ceiling light
						mod_x = xx % 5;
						mod_z = zz % 5;
						if (dao.hall_center && (
						(mod_x >= 0 && mod_x < 2) ||
						(mod_z >= 1 && mod_z < 4) )) {
							chunk.setBlock(x, cy, z, Material.REDSTONE_LAMP);
							final Lightable lamp = (Lightable) chunk.getBlockData(x, cy, z);
							lamp.setLit(true);
							chunk.setBlock(x, cy, z, lamp);
							chunk.setBlock(x, cy+1, z, Material.REDSTONE_BLOCK);
						// ceiling
						} else {
							if (NodeType.WALL.equals(hotelData.get(new Iab(x, z-1)).type) // north
							||  NodeType.WALL.equals(hotelData.get(new Iab(x, z+1)).type) // south
							||  NodeType.WALL.equals(hotelData.get(new Iab(x+1, z)).type) // east
							||  NodeType.WALL.equals(hotelData.get(new Iab(x-1, z)).type))// west
								chunk.setBlock(x, cy, z, Material.SMOOTH_STONE);
							else {
								chunk.setBlock(x, cy, z, Material.SMOOTH_STONE_SLAB);
								final Slab slab = (Slab) chunk.getBlockData(x, cy, z);
								slab.setType(Slab.Type.TOP);
								chunk.setBlock(x, cy, z, slab);
							}
						}
					}
					break;
				}
				case ROOM: break;
				default: throw new RuntimeException("Unknown hotel type: " + dao.type.toString());
				}
				if (ENABLE_TOP_005) {
					for (int i=0; i<SUBCEILING; i++) {
						chunk.setBlock(x, cy+i+1, z, Material.STONE);
					}
				}
			} // end x
		} // end z
	}



}
