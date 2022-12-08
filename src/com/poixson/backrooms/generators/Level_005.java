package com.poixson.backrooms.generators;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.generator.WorldInfo;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.tools.dao.Dxy;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;


public class Level_005 extends BackroomsGenerator {

	public static final int SUBFLOOR = BackGen_000.SUBFLOOR;

	public static final int HOTEL_Y      = 43;
	public static final int HOTEL_HEIGHT = 11;

	public static final Material HOTEL_FLOOR = Material.BLACK_GLAZED_TERRACOTTA;
	public static final Material HOTEL_WALL  = Material.STRIPPED_SPRUCE_LOG;

	protected final FastNoiseLiteD noiseHotelWalls;



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
	public class HotelDAO {
		public final double value;
		public HotelType type;
		public HotelDAO(final double value) {
			this.value = value;
			if (value > 0.65)
				this.type = HotelType.HALL;
			else
				this.type = HotelType.ROOM;
		}
	}



	protected HashMap<Dxy, HotelDAO> pregenerateHotel(
			final int chunkX, final int chunkZ) {
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
		// hall floor
		switch (dao.type) {
		case WALL:
			for (int yy=0; yy<3; yy++) {
				chunk.setBlock(x, y+yy, z, HOTEL_WALL);
			}
			break;
		case ROOM:
//			chunk.setBlock(x, y, z, Material.STONE);
			break;
		case HALL:
			chunk.setBlock(x, y, z, HOTEL_FLOOR);
			break;
		}
	}



}
