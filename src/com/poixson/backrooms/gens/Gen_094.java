package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_094.ENABLE_GEN_094;
import static com.poixson.utils.BlockUtils.StringToBlockData;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.worlds.Level_094.PregenLevel94;
import com.poixson.tools.abstractions.AtomicDouble;
import com.poixson.tools.dao.Iab;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.tools.plotter.PlotterFactory;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;
import com.poixson.utils.RandomUtils;
import com.poixson.utils.StringUtils;


// 94 | Motion
public class Gen_094 extends BackroomsGen {

	// default params
	public static final double DEFAULT_NOISE_HILLS_FREQ     = 0.015;
	public static final int    DEFAULT_NOISE_HILLS_OCTAVE   = 2;
	public static final double DEFAULT_NOISE_HILLS_STRENGTH = 1.8;
	public static final double DEFAULT_NOISE_HILLS_LACUN    = 0.8;
	public static final double DEFAULT_NOISE_HOUSE_FREQ     = 0.07;
	public static final double DEFAULT_VALLEY_DEPTH         = 0.33;
	public static final double DEFAULT_VALLEY_GAIN          = 0.3;
	public static final double DEFAULT_HILLS_GAIN           = 12.0;
	public static final double DEFAULT_ROSE_CHANCE          = 0.01;
	public static final int    DEFAULT_WATER_DEPTH          = 3;
	public static final int    DEFAULT_HOUSE_WIDTH          = 8;
	public static final int    DEFAULT_HOUSE_HEIGHT         = 5;

	// default blocks
	public static final String DEFAULT_BLOCK_DIRT              = "minecraft:dirt";
	public static final String DEFAULT_BLOCK_GRASS_BLOCK       = "minecraft:moss_block";
	public static final String DEFAULT_BLOCK_GRASS_SLAB        = "minecraft:mud_brick_slab";
	public static final String DEFAULT_BLOCK_GRASS             = "minecraft:grass";
	public static final String DEFAULT_BLOCK_FERN              = "minecraft:fern";
	public static final String DEFAULT_BLOCK_ROSE              = "minecraft:wither_rose";
	public static final String DEFAULT_BLOCK_HOUSE_WALL        = "minecraft:stripped_birch_wood";
	public static final String DEFAULT_BLOCK_HOUSE_ROOF_STAIRS = "minecraft:deepslate_tile_stairs";
	public static final String DEFAULT_BLOCK_HOUSE_ROOF_SOLID  = "minecraft:deepslate_tiles";
	public static final String DEFAULT_BLOCK_HOUSE_WINDOW      = "minecraft:black_stained_glass";

	// noise
	public final FastNoiseLiteD noiseHills;
	public final FastNoiseLiteD noiseHouse;

	// params
	public final AtomicDouble  noise_hills_freq     = new AtomicDouble( DEFAULT_NOISE_HILLS_FREQ    );
	public final AtomicInteger noise_hills_octave   = new AtomicInteger(DEFAULT_NOISE_HILLS_OCTAVE  );
	public final AtomicDouble  noise_hills_strength = new AtomicDouble( DEFAULT_NOISE_HILLS_STRENGTH);
	public final AtomicDouble  noise_hills_lacun    = new AtomicDouble( DEFAULT_NOISE_HILLS_LACUN   );
	public final AtomicDouble  noise_house_freq     = new AtomicDouble( DEFAULT_NOISE_HOUSE_FREQ    );
	public final AtomicDouble  valley_depth         = new AtomicDouble( DEFAULT_VALLEY_DEPTH        );
	public final AtomicDouble  valley_gain          = new AtomicDouble( DEFAULT_VALLEY_GAIN         );
	public final AtomicDouble  hills_gain           = new AtomicDouble( DEFAULT_HILLS_GAIN          );
	public final AtomicDouble  rose_chance          = new AtomicDouble( DEFAULT_ROSE_CHANCE         );
	public final AtomicInteger water_depth          = new AtomicInteger(DEFAULT_WATER_DEPTH         );
	public final AtomicInteger house_width          = new AtomicInteger(DEFAULT_HOUSE_WIDTH         );
	public final AtomicInteger house_height         = new AtomicInteger(DEFAULT_HOUSE_HEIGHT        );

	// blocks
	public final AtomicReference<String> block_dirt              = new AtomicReference<String>(null);
	public final AtomicReference<String> block_grass_block       = new AtomicReference<String>(null);
	public final AtomicReference<String> block_grass_slab        = new AtomicReference<String>(null);
	public final AtomicReference<String> block_grass             = new AtomicReference<String>(null);
	public final AtomicReference<String> block_fern              = new AtomicReference<String>(null);
	public final AtomicReference<String> block_rose              = new AtomicReference<String>(null);
	public final AtomicReference<String> block_house_wall        = new AtomicReference<String>(null);
	public final AtomicReference<String> block_house_roof_stairs = new AtomicReference<String>(null);
	public final AtomicReference<String> block_house_roof_solid  = new AtomicReference<String>(null);
	public final AtomicReference<String> block_house_window      = new AtomicReference<String>(null);



	public Gen_094(final BackroomsLevel backlevel, final int level_y, final int level_h) {
		super(backlevel, level_y, level_h);
		// noise
		this.noiseHills = this.register(new FastNoiseLiteD());
		this.noiseHouse = this.register(new FastNoiseLiteD());
	}



	@Override
	public void initNoise() {
		super.initNoise();
		// hills noise
		this.noiseHills.setFrequency(              this.noise_hills_freq    .get());
		this.noiseHills.setFractalOctaves(         this.noise_hills_octave  .get());
		this.noiseHills.setFractalPingPongStrength(this.noise_hills_strength.get());
		this.noiseHills.setFractalLacunarity(      this.noise_hills_lacun   .get());
		this.noiseHills.setNoiseType(NoiseType.Cellular);
		this.noiseHills.setFractalType(FractalType.PingPong);
		// house noise
		this.noiseHouse.setFrequency(this.noise_house_freq.get());
	}



	public class HillsData implements PreGenData {

		public final double valueHill;
		public final double depth;
		public final double valueHouse;
		public boolean isHouse = false;
		public int     house_y = 0;
		public boolean house_direction;

		public HillsData(final double valueHill, final double valueHouse,
				final double valley_depth, final double valley_gain, final double hills_gain) {
			this.valueHill  = valueHill;
			double depth = 1.0 - valueHill;
			if (depth < valley_depth)
				depth *= valley_gain;
			this.depth = depth * hills_gain;
			this.valueHouse = valueHouse;
			this.house_direction = ((int)Math.floor(valueHouse * 100000.0) % 2 == 1);
		}

	}



	public void pregenerate(final Map<Iab, HillsData> data,
			final int chunkX, final int chunkZ) {
		final double valley_depth = this.valley_depth.get();
		final double valley_gain  = this.valley_gain.get();
		final double hills_gain   = this.hills_gain.get();
		final int    house_width  = this.house_width.get();
		final int house_lowest_y = 7;
		HillsData dao;
		int xx, zz;
		double valueHill, valueHouse;
		for (int iz=-1; iz<17; iz++) {
			zz = (chunkZ * 16) + iz;
			for (int ix=-1; ix<17; ix++) {
				xx = (chunkX * 16) + ix;
				valueHill  = this.noiseHills.getNoise(xx, zz);
				valueHouse = this.noiseHouse.getNoise(xx, zz);
				dao = new HillsData(valueHill, valueHouse, valley_depth, valley_gain, hills_gain);
				data.put(new Iab(ix, iz), dao);
			}
		}
		// find house y
		final int search_width = 16 - house_width;
		LOOP_Z:
		for (int iz=0; iz<search_width; iz++) {
			for (int ix=0; ix<search_width; ix++) {
				dao = data.get(new Iab(ix, iz));
				valueHouse = dao.valueHouse;
				if (valueHouse > data.get(new Iab(ix, iz-1)).valueHouse
				&&  valueHouse > data.get(new Iab(ix, iz+1)).valueHouse
				&&  valueHouse > data.get(new Iab(ix+1, iz)).valueHouse
				&&  valueHouse > data.get(new Iab(ix-1, iz)).valueHouse) {
					int lowest = Integer.MAX_VALUE;
					int depth;
					for (int izz=0; izz<search_width; izz++) {
						for (int ixx=0; ixx<search_width; ixx++) {
							depth = (int) Math.floor(data.get(new Iab(ix+ixx, iz+izz)).depth);
							if (lowest > depth)
								lowest = depth;
						}
					}
					if (lowest != Integer.MAX_VALUE
					&&  lowest > house_lowest_y) {
						dao.house_y = lowest;
						dao.isHouse = true;
					}
					break LOOP_Z;
				}
			}
		}
	}



	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
		if (!ENABLE_GEN_094) return;
		final BlockData block_dirt             = StringToBlockData(this.block_dirt,              DEFAULT_BLOCK_DIRT             );
		final BlockData block_grass_block      = StringToBlockData(this.block_grass_block,       DEFAULT_BLOCK_GRASS_BLOCK      );
		final BlockData block_grass_slab       = StringToBlockData(this.block_grass_slab,        DEFAULT_BLOCK_GRASS_SLAB       );
		final BlockData block_grass            = StringToBlockData(this.block_grass,             DEFAULT_BLOCK_GRASS            );
		final BlockData block_fern             = StringToBlockData(this.block_fern,              DEFAULT_BLOCK_FERN             );
		final BlockData block_rose             = StringToBlockData(this.block_rose,              DEFAULT_BLOCK_ROSE             );
		final BlockData block_house_wall       = StringToBlockData(this.block_house_wall,        DEFAULT_BLOCK_HOUSE_WALL       );
		final BlockData block_house_roofA      = StringToBlockData(this.block_house_roof_stairs, DEFAULT_BLOCK_HOUSE_ROOF_STAIRS);
		final BlockData block_house_roofB      = StringToBlockData(this.block_house_roof_stairs, DEFAULT_BLOCK_HOUSE_ROOF_STAIRS);
		final BlockData block_house_roof_solid = StringToBlockData(this.block_house_roof_solid,  DEFAULT_BLOCK_HOUSE_ROOF_SOLID );
		final BlockData block_house_window = StringToBlockData(this.block_house_window, DEFAULT_BLOCK_HOUSE_WINDOW);
		if (block_dirt             == null) throw new RuntimeException("Invalid block type for level 94 Dirt"             );
		if (block_grass_block      == null) throw new RuntimeException("Invalid block type for level 94 Grass-Block"       );
		if (block_grass_slab       == null) throw new RuntimeException("Invalid block type for level 94 Grass-Slab"        );
		if (block_grass            == null) throw new RuntimeException("Invalid block type for level 94 Grass"            );
		if (block_fern             == null) throw new RuntimeException("Invalid block type for level 94 Fern"             );
		if (block_rose             == null) throw new RuntimeException("Invalid block type for level 94 Rose"             );
		if (block_house_wall       == null) throw new RuntimeException("Invalid block type for level 94 House-Wall"       );
		if (block_house_roofA      == null) throw new RuntimeException("Invalid block type for level 94 House-Roof-Stairs");
		if (block_house_roofB      == null) throw new RuntimeException("Invalid block type for level 94 House-Roof-Stairs");
		if (block_house_roof_solid == null) throw new RuntimeException("Invalid block type for level 94 House-Roof-Solid" );
		if (block_house_window     == null) throw new RuntimeException("Invalid block type for level 94 House-Window"     );
		final int    depth_water  = this.water_depth.get();;
		final double rose_chance  = this.rose_chance.get();
		final int    house_width  = this.house_width.get();
		final int    house_height = this.house_height.get();
		final HashMap<Iab, HillsData> hillsData = ((PregenLevel94)pregen).hills;
		HillsData dao;
		final int y = this.level_y + 1;
		int depth_dirt;
		int mod_grass;
		int rnd, chance;
		int last_rnd = 0;
		Iab house_loc = null;
		int house_y   = 0;
		boolean house_dir = false;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
				chunk.setBlock(ix, this.level_y, iz, Material.BEDROCK);
				dao = hillsData.get(new Iab(ix, iz));
				// fill dirt
				depth_dirt = (int) Math.floor(dao.depth - 0.7);
				if (depth_dirt < 0) depth_dirt = 0;
				for (int iy=0; iy<depth_dirt; iy++)
					chunk.setBlock(ix, y+iy, iz, block_dirt);
				// water
				if (depth_dirt < depth_water) {
					for (int iy=depth_dirt; iy<depth_water; iy++)
						chunk.setBlock(ix, y+iy, iz, Material.WATER);
				} else
				// surface slab
				if (dao.depth % 1.0 > 0.7) {
					chunk.setBlock(ix, y+depth_dirt, iz, block_grass_slab);
				// surface
				} else {
					chunk.setBlock(ix, y+depth_dirt, iz, block_grass_block);
					mod_grass = (int)Math.floor(dao.valueHill * 1000.0) % 3;
					chance = (int) Math.round(1.0 / rose_chance);
					rnd = RandomUtils.GetNewRandom(0, chance, last_rnd);
					last_rnd += rnd;
					if (rnd == 1)            chunk.setBlock(ix, y+depth_dirt+1, iz, block_rose );
					else if (mod_grass == 0) chunk.setBlock(ix, y+depth_dirt+1, iz, block_grass);
					else if (mod_grass == 1) chunk.setBlock(ix, y+depth_dirt+1, iz, block_fern );
				}
				// house
				if (dao.isHouse) {
					house_loc = new Iab(ix, iz);
					house_y   = dao.house_y;
					house_dir = dao.house_direction;
				}
			}
		}
		// build house
		if (house_loc != null) {
			final int house_half = Math.floorDiv(house_width, 2);
			if (house_dir) {
				((Stairs)block_house_roofA).setFacing(BlockFace.NORTH);
				((Stairs)block_house_roofB).setFacing(BlockFace.SOUTH);
			} else {
				((Stairs)block_house_roofA).setFacing(BlockFace.WEST);
				((Stairs)block_house_roofB).setFacing(BlockFace.EAST);
			}
			final BlockPlotter plot =
				(new PlotterFactory())
				.placer(chunk)
				.axis("use")
				.xyz(house_loc.a, y+house_y, house_loc.b)
				.whd(house_width, house_height+house_half+1, house_width)
				.build();
			plot.type('#', block_house_wall      );
			plot.type('<', block_house_roofA     );
			plot.type('>', block_house_roofB     );
			plot.type('X', block_house_roof_solid);
			plot.type('w', block_house_window    );
			plot.type('.', Material.AIR      );
			plot.rotation = (house_dir ? BlockFace.EAST : BlockFace.SOUTH);
			final StringBuilder[][] matrix = plot.getMatrix3D();
			// walls
			for (int iy=0; iy<house_height; iy++) {
				matrix[iy][0            ].append("#".repeat(house_width));
				matrix[iy][house_width-1].append("#".repeat(house_width));
				for (int iz=1; iz<house_width-1; iz++)
					matrix[iy][iz].append('#').append(".".repeat(house_width-2)).append('#');
			}
			// roof
			int yy, fill;
			for (int iy=0; iy<house_half; iy++) {
				for (int iz=0; iz<house_width; iz++) {
					yy = iy + house_height;
					fill = house_width - (iy*2) - 2;
					matrix[yy][iz].append(".".repeat(iy)).append('>');
					if (iz == 0 || iz == house_width-1) {
						if (iy < house_half-1) matrix[yy][iz].append('X');
						if (fill > 2)          matrix[yy][iz].append("#".repeat(fill-2));
						if (iy < house_half-1) matrix[yy][iz].append('X');
					} else {
						matrix[yy][iz].append(".".repeat(fill));
					}
					matrix[yy][iz].append('<').append(".".repeat(iy));
				}
			}
			// windows
			for (int i=2; i<4; i++) {
				StringUtils.ReplaceInString(matrix[house_height-i][0],             "w", house_half-2);
				StringUtils.ReplaceInString(matrix[house_height-i][0],             "w", house_half+1);
				StringUtils.ReplaceInString(matrix[house_height-i][house_width-1], "w", house_half-2);
				StringUtils.ReplaceInString(matrix[house_height-i][house_width-1], "w", house_half+1);
				StringUtils.ReplaceInString(matrix[house_height-i][house_half-2],  "w", 0            );
				StringUtils.ReplaceInString(matrix[house_height-i][house_half-2],  "w", house_width-1);
				StringUtils.ReplaceInString(matrix[house_height-i][house_half+1],  "w", 0            );
				StringUtils.ReplaceInString(matrix[house_height-i][house_half+1],  "w", house_width-1);
			}
			plot.run();
		}
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void loadConfig() {
		// params
		{
			final ConfigurationSection cfg = this.plugin.getLevelParams(94);
			this.noise_hills_freq    .set(cfg.getDouble("Noise-Hills-Freq"    ));
			this.noise_hills_octave  .set(cfg.getInt(   "Noise-Hills-Octave"  ));
			this.noise_hills_strength.set(cfg.getDouble("Noise-Hills-Strength"));
			this.noise_hills_lacun   .set(cfg.getDouble("Noise-Hills-Lacun"   ));
			this.noise_house_freq    .set(cfg.getDouble("Noise-House-Freq"    ));
			this.valley_depth        .set(cfg.getDouble("Valley-Depth"        ));
			this.valley_gain         .set(cfg.getDouble("Valley-Gain"         ));
			this.hills_gain          .set(cfg.getDouble("Hills-Gain"          ));
			this.rose_chance         .set(cfg.getDouble("Rose-Chance"         ));
			this.water_depth         .set(cfg.getInt(   "Water-Depth"         ));
			this.house_width         .set(cfg.getInt(   "House-Width"         ));
			this.house_height        .set(cfg.getInt(   "House-Height"        ));
		}
		// block types
		{
			final ConfigurationSection cfg = this.plugin.getLevelBlocks(94);
			this.block_dirt             .set(cfg.getString("Dirt"             ));
			this.block_grass_block      .set(cfg.getString("Grass-Block"      ));
			this.block_grass_slab       .set(cfg.getString("Grass-Slab"       ));
			this.block_grass            .set(cfg.getString("Grass"            ));
			this.block_fern             .set(cfg.getString("Fern"             ));
			this.block_rose             .set(cfg.getString("Rose"             ));
			this.block_house_wall       .set(cfg.getString("House-Wall"       ));
			this.block_house_roof_stairs.set(cfg.getString("House-Roof-Stairs"));
			this.block_house_roof_solid .set(cfg.getString("House-Roof-Solid" ));
			this.block_house_window     .set(cfg.getString("House-Window"     ));
		}
	}
	public static void ConfigDefaults(final FileConfiguration cfg) {
		// params
		cfg.addDefault("Level94.Params.Noise-Hills-Freq",     DEFAULT_NOISE_HILLS_FREQ    );
		cfg.addDefault("Level94.Params.Noise-Hills-Octave",   DEFAULT_NOISE_HILLS_OCTAVE  );
		cfg.addDefault("Level94.Params.Noise-Hills-Strength", DEFAULT_NOISE_HILLS_STRENGTH);
		cfg.addDefault("Level94.Params.Noise-Hills-Lacun",    DEFAULT_NOISE_HILLS_LACUN   );
		cfg.addDefault("Level94.Params.Noise-House-Freq",     DEFAULT_NOISE_HOUSE_FREQ    );
		cfg.addDefault("Level94.Params.Valley-Depth",         DEFAULT_VALLEY_DEPTH        );
		cfg.addDefault("Level94.Params.Valley-Gain",          DEFAULT_VALLEY_GAIN         );
		cfg.addDefault("Level94.Params.Hills-Gain",           DEFAULT_HILLS_GAIN          );
		cfg.addDefault("Level94.Params.Rose-Chance",          DEFAULT_ROSE_CHANCE         );
		cfg.addDefault("Level94.Params.Water-Depth",          DEFAULT_WATER_DEPTH         );
		cfg.addDefault("Level94.Params.House-Width",          DEFAULT_HOUSE_WIDTH         );
		cfg.addDefault("Level94.Params.House-Height",         DEFAULT_HOUSE_HEIGHT        );
		// block types
		cfg.addDefault("Level94.Blocks.Dirt",              DEFAULT_BLOCK_DIRT             );
		cfg.addDefault("Level94.Blocks.Grass-Block",       DEFAULT_BLOCK_GRASS_BLOCK      );
		cfg.addDefault("Level94.Blocks.Grass-Slab",        DEFAULT_BLOCK_GRASS_SLAB       );
		cfg.addDefault("Level94.Blocks.Grass",             DEFAULT_BLOCK_GRASS            );
		cfg.addDefault("Level94.Blocks.Fern",              DEFAULT_BLOCK_FERN             );
		cfg.addDefault("Level94.Blocks.Rose",              DEFAULT_BLOCK_ROSE             );
		cfg.addDefault("Level94.Blocks.House-Wall",        DEFAULT_BLOCK_HOUSE_WALL       );
		cfg.addDefault("Level94.Blocks.House-Roof-Stairs", DEFAULT_BLOCK_HOUSE_ROOF_STAIRS);
		cfg.addDefault("Level94.Blocks.House-Roof-Solid",  DEFAULT_BLOCK_HOUSE_ROOF_SOLID );
		cfg.addDefault("Level94.Blocks.House-Window",      DEFAULT_BLOCK_HOUSE_WINDOW     );
	}



}
