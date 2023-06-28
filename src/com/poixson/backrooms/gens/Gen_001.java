package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_000.ENABLE_GEN_001;
import static com.poixson.backrooms.worlds.Level_000.SUBFLOOR;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.worlds.Level_000.PregenLevel0;
import com.poixson.commonmc.tools.plotter.BlockPlotter;
import com.poixson.tools.abstractions.AtomicDouble;
import com.poixson.tools.dao.Iab;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.CellularReturnType;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;


// 1 | Basement
public class Gen_001 extends BackroomsGen {

	// default params
	public static final double DEFAULT_THRESH_WALL  = 0.9;
	public static final double DEFAULT_THRESH_MOIST = 0.4;
	public static final double DEFAULT_THRESH_WELL  = 0.9;

	public static final int LAMP_Y = 6;

	// default blocks
	public static final String DEFAULT_BLOCK_WALL      = "minecraft:mud_bricks";
	public static final String DEFAULT_BLOCK_SUBFLOOR  = "minecraft:dirt";
	public static final String DEFAULT_BLOCK_FLOOR_DRY = "minecraft:brown_concrete_powder";
	public static final String DEFAULT_BLOCK_FLOOR_WET = "minecraft:brown_concrete";

	// noise
	public final FastNoiseLiteD noiseBasementWalls;
	public final FastNoiseLiteD noiseMoist;
	public final FastNoiseLiteD noiseWell;

	// params
	public final AtomicDouble thresh_wall  = new AtomicDouble(DEFAULT_THRESH_WALL );
	public final AtomicDouble thresh_moist = new AtomicDouble(DEFAULT_THRESH_MOIST);
	public final AtomicDouble thresh_well  = new AtomicDouble(DEFAULT_THRESH_WELL );

	// blocks
	public final AtomicReference<String> block_wall      = new AtomicReference<String>(null);
	public final AtomicReference<String> block_subfloor  = new AtomicReference<String>(null);
	public final AtomicReference<String> block_floor_dry = new AtomicReference<String>(null);
	public final AtomicReference<String> block_floor_wet = new AtomicReference<String>(null);



	public Gen_001(final BackroomsLevel backlevel,
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
		// well noise
		this.noiseWell = this.register(new FastNoiseLiteD());
		this.noiseWell.setFrequency(0.0028);
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
			this.isWall = (valueWall > Gen_001.this.thresh_wall.get());
			final double thresh_moist = Gen_001.this.thresh_moist.get();
			this.isWet = (
				valueMoistA > thresh_moist ||
				valueMoistB > thresh_moist
			);
		}

	}



	public void pregenerate(Map<Iab, BasementData> data,
			final int chunkX, final int chunkZ) {
		BasementData dao;
		int xx, zz;
		double valueWall, valueMoistA, valueMoistB;
		for (int iz=0; iz<16; iz++) {
			zz = (chunkZ * 16) + iz;
			for (int ix=0; ix<16; ix++) {
				xx = (chunkX * 16) + ix;
				valueWall   = this.noiseBasementWalls.getNoiseRot(xx, zz, 0.25);
				valueMoistA = this.noiseMoist.getNoise(xx, zz);
				valueMoistB = this.noiseMoist.getNoise(zz, xx);
				dao = new BasementData(valueWall, valueMoistA, valueMoistB);
				data.put(new Iab(ix, iz), dao);
			}
		}
	}



	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
		if (!ENABLE_GEN_001) return;
		final BlockData block_wall      = StringToBlockData(this.block_wall,      DEFAULT_BLOCK_WALL     );
		final BlockData block_subfloor  = StringToBlockData(this.block_subfloor,  DEFAULT_BLOCK_SUBFLOOR );
		final BlockData block_floor_dry = StringToBlockData(this.block_floor_dry, DEFAULT_BLOCK_FLOOR_DRY);
		final BlockData block_floor_wet = StringToBlockData(this.block_floor_wet, DEFAULT_BLOCK_FLOOR_WET);
		if (block_wall      == null) throw new RuntimeException("Invalid block type for level 1 Wall"     );
		if (block_subfloor  == null) throw new RuntimeException("Invalid block type for level 1 SubFloor" );
		if (block_floor_dry == null) throw new RuntimeException("Invalid block type for level 1 Floor-Dry");
		if (block_floor_wet == null) throw new RuntimeException("Invalid block type for level 1 Floor-Wet");
		final HashMap<Iab, BasementData> basementData = ((PregenLevel0)pregen).basement;
		BasementData dao;
		final int y = this.level_y + SUBFLOOR + 1;
		final int h = this.level_h + 1;
		int xx, zz;
		for (int iz=0; iz<16; iz++) {
			zz = (chunkZ * 16) + iz;
			for (int ix=0; ix<16; ix++) {
				xx = (chunkX * 16) + ix;
				// basement floor
				chunk.setBlock(ix, this.level_y, iz, Material.BEDROCK);
				for (int yy=0; yy<SUBFLOOR; yy++)
					chunk.setBlock(ix, this.level_y+yy+1, iz, block_subfloor);
				dao = basementData.get(new Iab(ix, iz));
				if (dao == null) continue;
				// wall
				if (dao.isWall) {
					for (int yy=0; yy<h; yy++) {
						if (yy > 6) chunk.setBlock(ix, y+yy, iz, Material.BEDROCK);
						else        chunk.setBlock(ix, y+yy, iz, block_wall);
					}
				// room
				} else {
					if (dao.isWet) chunk.setBlock(ix, y, iz, block_floor_wet);
					else           chunk.setBlock(ix, y, iz, block_floor_dry);
					// basement lights
					final int modX10 = Math.abs(xx) % 10;
					final int modZ10 = Math.abs(zz) % 10;
					if (modZ10 == 0) {
						if (modX10 < 3 || modX10 > 7) {
							chunk.setBlock(ix, y+LAMP_Y, iz, Material.REDSTONE_LAMP);
							switch (modX10) {
							case 0: chunk.setBlock(ix, y+LAMP_Y+1, iz, Material.BEDROCK);       break;
							case 1:
							case 9: chunk.setBlock(ix, y+LAMP_Y+1, iz, Material.REDSTONE_WIRE); break;
							case 2:
							case 8:
								for (int iy=0; iy<5; iy++)
									chunk.setBlock(ix, y+iy+LAMP_Y+1, iz, Material.CHAIN);
								break;
							}
						}
					}
				} // end wall/room
			} // end ix
		} // end iz
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void loadConfig() {
		// params
		{
			final ConfigurationSection cfg = this.plugin.getLevelParams(1);
			this.thresh_wall .set(cfg.getDouble("Thresh-Wall" ));
			this.thresh_moist.set(cfg.getDouble("Thresh-Moist"));
			this.thresh_well .set(cfg.getDouble("Thresh-Well" ));
		}
		// block types
		{
			final ConfigurationSection cfg = this.plugin.getLevelBlocks(1);
			this.block_wall     .set(cfg.getString("Wall"     ));
			this.block_subfloor .set(cfg.getString("SubFloor" ));
			this.block_floor_dry.set(cfg.getString("Floor-Dry"));
			this.block_floor_wet.set(cfg.getString("Floor-Wet"));
		}
	}
	public static void ConfigDefaults(final FileConfiguration cfg) {
		// params
		cfg.addDefault("Level1.Params.Thresh-Wall",  DEFAULT_THRESH_WALL );
		cfg.addDefault("Level1.Params.Thresh-Moist", DEFAULT_THRESH_MOIST);
		cfg.addDefault("Level1.Params.Thresh-Well",  DEFAULT_THRESH_WELL );
		// block types
		cfg.addDefault("Level1.Blocks.Wall",      DEFAULT_BLOCK_WALL     );
		cfg.addDefault("Level1.Blocks.SubFloor",  DEFAULT_BLOCK_SUBFLOOR );
		cfg.addDefault("Level1.Blocks.Floor-Dry", DEFAULT_BLOCK_FLOOR_DRY);
		cfg.addDefault("Level1.Blocks.Floor-Wet", DEFAULT_BLOCK_FLOOR_WET);
	}



}
