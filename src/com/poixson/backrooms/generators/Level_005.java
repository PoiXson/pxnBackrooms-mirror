package com.poixson.backrooms.generators;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.type.Slab;
import org.bukkit.generator.WorldInfo;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.tools.dao.Dxy;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;


// 5 | hotel
public class Level_005 extends BackroomsGenerator {

	public static final boolean BUILD_ROOF = BackGen_000.BUILD_ROOF;

	public static final int SUBFLOOR = BackGen_000.SUBFLOOR;

	public static final int HOTEL_Y      = 43;
	public static final int HOTEL_HEIGHT = 11;

	public static final Material HOTEL_FLOOR = Material.BLACK_GLAZED_TERRACOTTA;
	public static final Material HOTEL_WALL  = Material.STRIPPED_SPRUCE_WOOD;

	protected final FastNoiseLiteD noiseHotelWalls;
	protected final FastNoiseLiteD noiseHotelRooms;

	public final Level_005_Populator roomPop;



	public Level_005(final BackroomsPlugin plugin) {
		super(plugin);
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
		this.roomPop = new Level_005_Populator(this.noiseHotelRooms);
	}



	public void setSeed(final int seed) {
		this.noiseHotelWalls.setSeed(seed);
	}



	@Override
	public void generateSurface(
			final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk) {
	}



	public enum HotelType {
		HALL,
		ROOM,
		WALL
	};
//TODO: is this needed?
	public class HotelDAO {
//TODO: add wall_1_away
		public final double value;
		public HotelType type;
		public HotelDAO(final double value) {
			this.value = value;
			if (value > 0.65) {
				this.type = HotelType.HALL;
			} else {
				this.type = HotelType.ROOM;
			}
		}
	}



	protected HashMap<Dxy, HotelDAO> pregenerateHotel(final int chunkX, final int chunkZ) {
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
				if (HotelType.ROOM.equals(dao.type)) {
					if ((daoN  != null && HotelType.HALL.equals(daoN.type))
					||  (daoS  != null && HotelType.HALL.equals(daoS.type))
					||  (daoE  != null && HotelType.HALL.equals(daoE.type))
					||  (daoW  != null && HotelType.HALL.equals(daoW.type))
					||  (daoNE != null && HotelType.HALL.equals(daoNE.type))
					||  (daoNW != null && HotelType.HALL.equals(daoNW.type))
					||  (daoSE != null && HotelType.HALL.equals(daoSE.type))
					||  (daoSW != null && HotelType.HALL.equals(daoSW.type)) )
						dao.type = HotelType.WALL;
				}
			}
		}
		return prehotel;
	}
	protected void generateHotel(final HashMap<Dxy, HotelDAO> prehotel,
			final int chunkX, final int chunkZ, final ChunkData chunk,
			final int x, final int z, final int xx, final int zz) {
		int y = HOTEL_Y;
		if (BUILD_ROOF)
			chunk.setBlock(x, y+11, z, Material.STONE);
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
			for (int yy=0; yy<7; yy++) {
				chunk.setBlock(x, y+yy, z, HOTEL_WALL);
			}
			break;
		case HALL:
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
					chunk.setBlock(x, y+6, z, Material.REDSTONE_LAMP);
					final Lightable lamp = (Lightable) chunk.getBlockData(x, y+6, z);
					lamp.setLit(true);
					chunk.setBlock(x, y+6, z, lamp);
					chunk.setBlock(x, y+7, z, Material.REDSTONE_BLOCK);
				// ceiling
				} else {
					final HotelDAO daoN = prehotel.get(new Dxy(x, z-1));
					final HotelDAO daoS = prehotel.get(new Dxy(x, z+1));
					final HotelDAO daoE = prehotel.get(new Dxy(x+1, z));
					final HotelDAO daoW = prehotel.get(new Dxy(x-1, z));
					if (HotelType.WALL.equals(daoN.type)
					||  HotelType.WALL.equals(daoS.type)
					||  HotelType.WALL.equals(daoE.type)
					||  HotelType.WALL.equals(daoW.type) ) {
						chunk.setBlock(x, y+6, z, Material.SMOOTH_STONE);
					} else {
						chunk.setBlock(x, y+6, z, Material.SMOOTH_STONE_SLAB);
							slab = (Slab) chunk.getBlockData(x, y+6, z);
							slab.setType(Slab.Type.TOP);
							chunk.setBlock(x, y+6, z, slab);
					}
				}
			}
			break;
		default: break;
		}
	}



}
