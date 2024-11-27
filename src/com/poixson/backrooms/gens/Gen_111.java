package com.poixson.backrooms.gens;

import static com.poixson.utils.BlockUtils.StringToBlockDataDef;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsWorld;
import com.poixson.backrooms.PreGenData;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.noise.FastNoiseLiteD;
import com.poixson.tools.plotter.BlockPlotter;


// 111 | Run For Your Life!
public class Gen_111 extends BackroomsGen {

	// default params
	public static final int    DEFAULT_LEVEL_Y            = 50;
	public static final int    DEFAULT_LEVEL_H            = 10;
	public static final double DEFAULT_NOISE_FLOOR_FREQ   = 0.1;
	public static final int    DEFAULT_NOISE_FLOOR_OCTAVE = 2;
	public static final double DEFAULT_NOISE_FLOOR_GAIN   = 2.0;
	public static final double DEFAULT_THRESH_FLOOR       =-0.2;
	public static final double DEFAULT_THRESH_HAZARD      = 0.7;
	public static final int    DEFAULT_DANGER_CHUNKS      = 3;

	// default blocks
	public static final String DEFAULT_BLOCK_WALL       = "minecraft:blackstone";
	public static final String DEFAULT_BLOCK_CEILING    = "minecraft:glowstone";
	public static final String DEFAULT_BLOCK_FLOOR      = "minecraft:polished_deepslate";
	public static final String DEFAULT_BLOCK_FLOOR_SAFE = "minecraft:cracked_deepslate_tiles";
	public static final String DEFAULT_BLOCK_SUBFLOOR   = "minecraft:dark_oak_slab[type=top]";
	public static final String DEFAULT_BLOCK_PLATE      = "minecraft:stone_pressure_plate";
	public static final String DEFAULT_BLOCK_HAZARD     = "minecraft:bricks";

	// params
	public final boolean enable_gen;
	public final boolean enable_top;
	public final int     level_y;
	public final int     level_h;
	public final double  thresh_floor;
	public final double  thresh_hazard;
	public final int     danger_chunks;

	// blocks
	public final String block_wall;
	public final String block_ceiling;
	public final String block_floor;
	public final String block_floor_safe;
	public final String block_subfloor;
	public final String block_plate;
	public final String block_hazard;

	// noise
	public final FastNoiseLiteD noiseFloor;



	public Gen_111(final BackroomsWorld backworld, final int seed) {
		super(backworld, null, seed);
		final int level_number = this.getLevelNumber();
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(level_number);
		final ConfigurationSection cfgBlocks = this.plugin.getConfigLevelBlocks(level_number);
		// params
		this.enable_gen    = cfgParams.getBoolean("Enable-Gen"   );
		this.enable_top    = cfgParams.getBoolean("Enable-Top"   );
		this.level_y       = cfgParams.getInt(    "Level-Y"      );
		this.level_h       = cfgParams.getInt(    "Level-Height" );
		this.thresh_floor  = cfgParams.getDouble( "Thresh-Floor" );
		this.thresh_hazard = cfgParams.getDouble( "Thresh-Hazard");
		this.danger_chunks = cfgParams.getInt(    "Danger-Chunks");
		// block types
		this.block_wall       = cfgBlocks.getString("Wall"      );
		this.block_ceiling    = cfgBlocks.getString("Ceiling"   );
		this.block_floor      = cfgBlocks.getString("Floor"     );
		this.block_floor_safe = cfgBlocks.getString("Floor-Safe");
		this.block_subfloor   = cfgBlocks.getString("SubFloor"  );
		this.block_plate      = cfgBlocks.getString("Plate"     );
		this.block_hazard     = cfgBlocks.getString("Hazard"    );
		// noise
		this.noiseFloor = this.register(new FastNoiseLiteD());
	}



	@Override
	public int getLevelNumber() {
		return 111;
	}



	@Override
	public int getLevelY() {
		return this.level_y;
	}
	@Override
	public int getOpenY() {
		return this.getLevelY() + 1;
	}

	@Override
	public int getMinY() {
		return this.getLevelY();
	}
	@Override
	public int getMaxY() {
		return (this.getOpenY() + this.level_h) - 3;
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!this.enable_gen) return;
		final BlockData block_wall       = StringToBlockDataDef(this.block_wall,       DEFAULT_BLOCK_WALL      );
		final BlockData block_ceiling    = StringToBlockDataDef(this.block_ceiling,    DEFAULT_BLOCK_CEILING   );
		final BlockData block_floor      = StringToBlockDataDef(this.block_floor,      DEFAULT_BLOCK_FLOOR     );
		final BlockData block_floor_safe = StringToBlockDataDef(this.block_floor_safe, DEFAULT_BLOCK_FLOOR_SAFE);
		final BlockData block_subfloor   = StringToBlockDataDef(this.block_subfloor,   DEFAULT_BLOCK_SUBFLOOR  );
		final BlockData block_plate      = StringToBlockDataDef(this.block_plate,      DEFAULT_BLOCK_PLATE     );
		final BlockData block_hazard     = StringToBlockDataDef(this.block_hazard,     DEFAULT_BLOCK_HAZARD    );
		if (block_wall       == null) throw new RuntimeException("Invalid block type for level 111 Wall"      );
		if (block_ceiling    == null) throw new RuntimeException("Invalid block type for level 111 Ceiling"   );
		if (block_floor      == null) throw new RuntimeException("Invalid block type for level 111 Floor"     );
		if (block_floor_safe == null) throw new RuntimeException("Invalid block type for level 111 Floor-Safe");
		if (block_subfloor   == null) throw new RuntimeException("Invalid block type for level 111 SubFloor"  );
		if (block_plate      == null) throw new RuntimeException("Invalid block type for level 111 Plate"     );
		if (block_hazard     == null) throw new RuntimeException("Invalid block type for level 111 Hazard"    );
		final int danger_chunks = this.danger_chunks + 1;
		for (int iz=0; iz<16; iz++) {
			final int zz = (chunkZ * 16) + iz;
			for (int ix=0; ix<16; ix++) {
				final int xx = (chunkX * 16) + ix;
				// fill bedrock sky
				if (this.enable_top) {
					for (int iy=this.level_y+this.level_h; iy<320; iy++)
						chunk.setBlock(ix, iy, iz, Material.BEDROCK);
				}
				switch (ix) {
				// bedrock wall
				case  0: case  1:
				case 14: case 15:
					for (int iy=(this.enable_top?-64:-2); iy<this.level_h; iy++)
						chunk.setBlock(ix, iy+this.level_y, iz, Material.BEDROCK);
					break;
				// inside wall
				case 2:
				case 13:
					for (int iy=-2; iy<this.level_h; iy++)
						chunk.setBlock(ix, iy+this.level_y, iz, block_wall);
					break;
				default: {
					final double value_floor = 0.0 - this.noiseFloor.getNoise(xx, zz*2);
					final boolean safe = (chunkZ % danger_chunks == 0);
					// ceiling
					if (this.enable_top)
						chunk.setBlock(ix, this.level_y+this.level_h-1, iz, block_ceiling);
					// floor
					if (safe) {
						if (chunkZ == 0
						||  value_floor > this.thresh_floor) {
							chunk.setBlock(ix, this.level_y,   iz, block_floor_safe);
							chunk.setBlock(ix, this.level_y-1, iz, block_floor_safe);
						}
					} else {
						if (value_floor > this.thresh_floor) {
							chunk.setBlock(ix, this.level_y+1, iz, block_plate );
							chunk.setBlock(ix, this.level_y,   iz, block_floor );
							final long mod_tnt = (ix+iz) % 5;
							if (mod_tnt == 0 || mod_tnt == 2) chunk.setBlock(ix, this.level_y-1, iz, Material.GLOWSTONE);
							else                              chunk.setBlock(ix, this.level_y-1, iz, Material.TNT);
							chunk.setBlock(ix, this.level_y-2, iz, block_subfloor);
							if (value_floor > this.thresh_hazard) {
								chunk.setBlock(ix, this.level_y+2, iz, block_hazard);
								chunk.setBlock(ix, this.level_y+1, iz, block_hazard);
							}
						}
					}
					break;
				}
				}
			} // end ix
		} // end iz
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void initNoise() {
		super.initNoise();
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(this.getLevelNumber());
		// pool rooms
		this.noiseFloor.setFrequency(     cfgParams.getDouble("Noise-Floor-Freq"  ));
		this.noiseFloor.setFractalOctaves(cfgParams.getInt(   "Noise-Floor-Octave"));
		this.noiseFloor.setFractalGain(   cfgParams.getDouble("Noise-Floor-Gain"  ));
		this.noiseFloor.setFractalType(   FastNoiseLiteD.FractalType.FBm           );
	}



	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		cfgParams.addDefault("Enable-Gen",         Boolean.TRUE                               );
		cfgParams.addDefault("Enable-Top",         Boolean.TRUE                               );
		cfgParams.addDefault("Level-Y",            Integer.valueOf(DEFAULT_LEVEL_Y           ));
		cfgParams.addDefault("Level-Height",       Integer.valueOf(DEFAULT_LEVEL_H           ));
		// floor noise
		cfgParams.addDefault("Noise-Floor-Freq",   Double .valueOf(DEFAULT_NOISE_FLOOR_FREQ  ));
		cfgParams.addDefault("Noise-Floor-Octave", Integer.valueOf(DEFAULT_NOISE_FLOOR_OCTAVE));
		cfgParams.addDefault("Noise-Floor-Gain",   Double .valueOf(DEFAULT_NOISE_FLOOR_GAIN  ));
		cfgParams.addDefault("Thresh-Floor",       Double .valueOf(DEFAULT_THRESH_FLOOR      ));
		cfgParams.addDefault("Thresh-Hazard",      Double .valueOf(DEFAULT_THRESH_HAZARD     ));
		cfgParams.addDefault("Danger-Chunks",      Integer.valueOf(DEFAULT_DANGER_CHUNKS     ));
		// block types
		cfgBlocks.addDefault("Wall",       DEFAULT_BLOCK_WALL      );
		cfgBlocks.addDefault("Ceiling",    DEFAULT_BLOCK_CEILING   );
		cfgBlocks.addDefault("Floor",      DEFAULT_BLOCK_FLOOR     );
		cfgBlocks.addDefault("Floor-Safe", DEFAULT_BLOCK_FLOOR_SAFE);
		cfgBlocks.addDefault("SubFloor",   DEFAULT_BLOCK_SUBFLOOR  );
		cfgBlocks.addDefault("Plate",      DEFAULT_BLOCK_PLATE     );
		cfgBlocks.addDefault("Hazard",     DEFAULT_BLOCK_HAZARD    );
	}



}
