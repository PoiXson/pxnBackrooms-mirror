package com.poixson.backrooms.levels;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.type.Slab;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.tools.dao.Dxy;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;


// 5 | Hotel
public class Gen_005 extends BackroomsGenerator {

	public static final boolean BUILD_ROOF = Level_000.BUILD_ROOF;
	public static final int     SUBFLOOR   = Level_000.SUBFLOOR;
	public static final int     SUBCEILING = Level_000.SUBCEILING;

	public static final int HOTEL_Y = Level_000.Y_005;
	public static final int HOTEL_H = Level_000.H_005;

	public static final Material HOTEL_FLOOR = Material.BLACK_GLAZED_TERRACOTTA;
	public static final Material HOTEL_WALL  = Material.STRIPPED_SPRUCE_WOOD;

	// noise
	protected final FastNoiseLiteD noiseHotelWalls;
	protected final FastNoiseLiteD noiseHotelRooms;

	// populators
	public final Pop_005 roomPop;



	public Gen_005() {
		super();
		// hotel walls
		this.noiseHotelWalls = new FastNoiseLiteD();
		this.noiseHotelWalls.setFrequency(0.02);
		this.noiseHotelWalls.setFractalOctaves(1);
		this.noiseHotelWalls.setCellularJitter(0.3);
		this.noiseHotelWalls.setNoiseType(NoiseType.Cellular);
		this.noiseHotelWalls.setFractalType(FractalType.PingPong);
		this.noiseHotelWalls.setCellularDistanceFunction(CellularDistanceFunction.Manhattan);
		// hotel rooms
		this.noiseHotelRooms = new FastNoiseLiteD();
		this.noiseHotelRooms.setFrequency(0.008);
		this.noiseHotelRooms.setFractalOctaves(1);
		// populators
		this.roomPop = new Pop_005(this.noiseHotelRooms);
	}
	@Override
	public void unload() {
	}



	@Override
	public void setSeed(final int seed) {
		this.noiseHotelWalls.setSeed(seed);
	}



	public enum NodeType {
		HALL,
		ROOM,
		WALL
	};

	public class HotelDAO {
//TODO: add wall_1_away
		public final double value;
		public NodeType type;
		public HotelDAO(final double value) {
			this.value = value;
			if (value > 0.65) {
				this.type = NodeType.HALL;
			} else {
				this.type = NodeType.ROOM;
			}
		}
	}



	public HashMap<Dxy, HotelDAO> pregenerateHotel(final int chunkX, final int chunkZ) {
		final HashMap<Dxy, HotelDAO> prehotel = new HashMap<Dxy, HotelDAO>();
		int xx, zz;
		double value;
		HotelDAO dao;
		for (int z=-8; z<24; z++) {
			zz = (chunkZ * 16) + z;
			for (int x=-8; x<24; x++) {
				xx = (chunkX * 16) + x;
				value = this.noiseHotelWalls.getNoiseRot(xx, zz, 0.25);
				dao = new HotelDAO(value);
				prehotel.put(new Dxy(x, z), dao);
			}
		}
		HotelDAO daoN, daoS, daoE, daoW;
		HotelDAO daoNE, daoNW, daoSE, daoSW;
		// find walls
		for (int z=-8; z<24; z++) {
			for (int x=-8; x<24; x++) {
				dao   = prehotel.get(new Dxy(x,   z  ));
				daoN  = prehotel.get(new Dxy(x,   z-1));
				daoS  = prehotel.get(new Dxy(x,   z+1));
				daoE  = prehotel.get(new Dxy(x+1, z  ));
				daoW  = prehotel.get(new Dxy(x-1, z  ));
				daoNE = prehotel.get(new Dxy(x+1, z-1));
				daoNW = prehotel.get(new Dxy(x-1, z-1));
				daoSE = prehotel.get(new Dxy(x+1, z+1));
				daoSW = prehotel.get(new Dxy(x-1, z+1));
				if (NodeType.ROOM.equals(dao.type)) {
					if ((daoN  != null && NodeType.HALL.equals(daoN.type))
					||  (daoS  != null && NodeType.HALL.equals(daoS.type))
					||  (daoE  != null && NodeType.HALL.equals(daoE.type))
					||  (daoW  != null && NodeType.HALL.equals(daoW.type))
					||  (daoNE != null && NodeType.HALL.equals(daoNE.type))
					||  (daoNW != null && NodeType.HALL.equals(daoNW.type))
					||  (daoSE != null && NodeType.HALL.equals(daoSE.type))
					||  (daoSW != null && NodeType.HALL.equals(daoSW.type)) )
						dao.type = NodeType.WALL;
				}
			}
		}
		return prehotel;
	}
	public void generateHotel(final HashMap<Dxy, HotelDAO> prehotel,
			final int chunkX, final int chunkZ, final ChunkData chunk,
			final int x, final int z, final int xx, final int zz) {
		int y  = HOTEL_Y;
		int cy = HOTEL_Y + SUBFLOOR + HOTEL_H;
		// hotel floor
		chunk.setBlock(x, y, z, Material.BEDROCK);
		y++;
		for (int yy=0; yy<SUBFLOOR; yy++) {
			chunk.setBlock(x, y+yy, z, Material.SPRUCE_PLANKS);
		}
		y += SUBFLOOR;
		final HotelDAO dao = prehotel.get(new Dxy(x, z));
		if (dao == null)
			throw new RuntimeException("pre-generated data for hotel not found for x:"+
				Integer.toString(x)+" z:"+Integer.toString(z));
		Slab slab;
		switch (dao.type) {
		case WALL:
			for (int iy=0; iy<HOTEL_H; iy++) {
				chunk.setBlock(x, y+iy, z, HOTEL_WALL);
			}
			break;
		case HALL: {
			chunk.setBlock(x, y, z, HOTEL_FLOOR);
			final Directional tile = (Directional) chunk.getBlockData(x, y, z);
			if (z % 2 == 0) {
				if (x % 2 == 0) tile.setFacing(BlockFace.NORTH);
				else            tile.setFacing(BlockFace.WEST);
			} else {
				if (x % 2 == 0) tile.setFacing(BlockFace.EAST);
				else            tile.setFacing(BlockFace.SOUTH);
			}
			chunk.setBlock(x, y, z, tile);
			if (BUILD_ROOF) {
				// ceiling light
				if (xx % 5 == 0 && zz % 5 == 0) {
					chunk.setBlock(x, cy, z, Material.REDSTONE_LAMP);
					final Lightable lamp = (Lightable) chunk.getBlockData(x, cy, z);
					lamp.setLit(true);
					chunk.setBlock(x, cy, z, lamp);
					chunk.setBlock(x, cy+1, z, Material.REDSTONE_BLOCK);
				// ceiling
				} else {
					final HotelDAO daoN = prehotel.get(new Dxy(x, z-1));
					final HotelDAO daoS = prehotel.get(new Dxy(x, z+1));
					final HotelDAO daoE = prehotel.get(new Dxy(x+1, z));
					final HotelDAO daoW = prehotel.get(new Dxy(x-1, z));
					if (NodeType.WALL.equals(daoN.type)
					||  NodeType.WALL.equals(daoS.type)
					||  NodeType.WALL.equals(daoE.type)
					||  NodeType.WALL.equals(daoW.type) ) {
						chunk.setBlock(x, cy, z, Material.SMOOTH_STONE);
					} else {
						chunk.setBlock(x, cy, z, Material.SMOOTH_STONE_SLAB);
							slab = (Slab) chunk.getBlockData(x, cy, z);
							slab.setType(Slab.Type.TOP);
							chunk.setBlock(x, cy, z, slab);
					}
				}
			}
			break;
		}
		default: break;
		}
		if (BUILD_ROOF) {
			cy++;
			for (int i=0; i<SUBCEILING; i++) {
				chunk.setBlock(x, cy+i, z, Material.STONE);
			}
		}
	}



}
