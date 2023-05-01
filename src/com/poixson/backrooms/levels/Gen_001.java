package com.poixson.backrooms.levels;

import static com.poixson.backrooms.levels.Level_000.SUBFLOOR;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.levels.Level_000.PregenLevel0;
import com.poixson.commonmc.tools.plotter.BlockPlotter;
import com.poixson.tools.dao.Iab;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.CellularReturnType;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;


// 1 | Basement
public class Gen_001 extends GenBackrooms {

	public static final boolean ENABLE_GENERATE = true;

	public static final int LAMP_Y = 6;

	public static final double THRESH_WALL  = 0.9;
	public static final double THRESH_MOIST = 0.4;

	public static final Material BASEMENT_WALL      = Material.MUD_BRICKS;
	public static final Material BASEMENT_SUBFLOOR  = Material.DIRT;
	public static final Material BASEMENT_FLOOR_DRY = Material.BROWN_CONCRETE_POWDER;
	public static final Material BASEMENT_FLOOR_WET = Material.BROWN_CONCRETE;

	// noise
	public final FastNoiseLiteD noiseBasementWalls;
	public final FastNoiseLiteD noiseMoist;



	public Gen_001(final LevelBackrooms backlevel,
			final int level_y, final int level_h) {
		super(backlevel, level_y, level_h);
		// basement wall noise
		this.noiseBasementWalls = this.register(new FastNoiseLiteD());
		this.noiseBasementWalls.setFrequency(0.033);
		this.noiseBasementWalls.setFractalOctaves(2);
		this.noiseBasementWalls.setFractalGain(0.03);
		this.noiseBasementWalls.setFractalPingPongStrength(1.2);
		this.noiseBasementWalls.setNoiseType(NoiseType.Cellular);
		this.noiseBasementWalls.setFractalType(FractalType.PingPong);
		this.noiseBasementWalls.setCellularDistanceFunction(CellularDistanceFunction.Manhattan);
		this.noiseBasementWalls.setCellularReturnType(CellularReturnType.Distance);
		// moist noise
		this.noiseMoist = this.register(new FastNoiseLiteD());
		this.noiseMoist.setFrequency(0.015);
		this.noiseMoist.setFractalOctaves(2);
		this.noiseMoist.setFractalGain(2.0);
	}



	public class BasementData implements PreGenData {

		public final double valueWall;
		public final double valueMoistA;
		public final double valueMoistB;
		public boolean isWall;
		public boolean isWet;

		public BasementData(final double valueWall, final double valueMoistA, final double valueMoistB) {
			this.valueWall   = valueWall;
			this.valueMoistA = valueMoistA;
			this.valueMoistB = valueMoistB;
			this.isWall = (valueWall > THRESH_WALL);
			this.isWet = (
				valueMoistA > THRESH_MOIST ||
				valueMoistB > THRESH_MOIST
			);
		}

	}



	public void pregenerate(Map<Iab, BasementData> data,
			final int chunkX, final int chunkZ) {
		BasementData dao;
		int xx, zz;
		double valueWall, valueMoistA, valueMoistB;
		for (int z=0; z<16; z++) {
			zz = (chunkZ * 16) + z;
			for (int x=0; x<16; x++) {
				xx = (chunkX * 16) + x;
				valueWall   = this.noiseBasementWalls.getNoiseRot(xx, zz, 0.25);
				valueMoistA = this.noiseMoist.getNoise(xx, zz);
				valueMoistB = this.noiseMoist.getNoise(zz, xx);
				dao = new BasementData(valueWall, valueMoistA, valueMoistB);
				data.put(new Iab(x, z), dao);
			}
		}
	}
	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
		if (!ENABLE_GENERATE) return;
		final HashMap<Iab, BasementData> basementData = ((PregenLevel0)pregen).basement;
		BasementData dao;
		final int y  = this.level_y + SUBFLOOR + 1;
		final int h  = this.level_h + 1;
		int xx, zz;
		for (int z=0; z<16; z++) {
			zz = (chunkZ * 16) + z;
			for (int x=0; x<16; x++) {
				xx = (chunkX * 16) + x;
				// basement floor
				chunk.setBlock(x, this.level_y, z, Material.BEDROCK);
				for (int yy=0; yy<SUBFLOOR; yy++)
					chunk.setBlock(x, this.level_y+yy+1, z, BASEMENT_SUBFLOOR);
				dao = basementData.get(new Iab(x, z));
				if (dao == null) continue;
				// wall
				if (dao.isWall) {
					for (int yy=0; yy<h; yy++) {
						if (yy > 6) chunk.setBlock(x, y+yy, z, Material.BEDROCK);
						else        chunk.setBlock(x, y+yy, z, BASEMENT_WALL);
					}
				// room
				} else {
					if (dao.isWet) chunk.setBlock(x, y, z, BASEMENT_FLOOR_WET);
					else           chunk.setBlock(x, y, z, BASEMENT_FLOOR_DRY);
					// basement lights
					final int modX10 = Math.abs(xx) % 10;
					final int modZ10 = Math.abs(zz) % 10;
					if (modZ10 == 0) {
						if (modX10 < 3 || modX10 > 7) {
							chunk.setBlock(x, y+LAMP_Y, z, Material.REDSTONE_LAMP);
							switch (modX10) {
							case 0: chunk.setBlock(x, y+LAMP_Y+1, z, Material.BEDROCK);       break;
							case 1:
							case 9: chunk.setBlock(x, y+LAMP_Y+1, z, Material.REDSTONE_WIRE); break;
							case 2:
							case 8:
								for (int iy=0; iy<5; iy++) {
									chunk.setBlock(x, y+iy+LAMP_Y+1, z, Material.CHAIN);
								}
								break;
							}
						}
					}
				} // end wall/room
			} // end x
		} // end z
	}



}
