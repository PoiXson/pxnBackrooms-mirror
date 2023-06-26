package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_094.ENABLE_GEN_094;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.worlds.Level_094.PregenLevel94;
import com.poixson.commonmc.tools.plotter.BlockPlotter;
import com.poixson.tools.abstractions.AtomicDouble;
import com.poixson.tools.dao.Iab;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;
import com.poixson.utils.RandomUtils;


// 94 | Motion
public class Gen_094 extends BackroomsGen {

	// default params
	public static final double DEFAULT_VALLEY_DEPTH =  0.33;
	public static final double DEFAULT_VALLEY_GAIN  =  0.3;
	public static final double DEFAULT_HILLS_GAIN   = 12.0;
	public static final int    DEFAULT_WATER_DEPTH  =  3;
	public static final double DEFAULT_ROSE_CHANCE  =  0.01;

	// default blocks
	public static final String DEFAULT_BLOCK_DIRT        = "minecraft:dirt";
	public static final String DEFAULT_BLOCK_GRASS_BLOCK = "minecraft:moss_block";
	public static final String DEFAULT_BLOCK_GRASS_SLAB  = "minecraft:mud_brick_slab";
	public static final String DEFAULT_BLOCK_GRASS       = "minecraft:grass";
	public static final String DEFAULT_BLOCK_FERN        = "minecraft:fern";
	public static final String DEFAULT_BLOCK_ROSE        = "minecraft:wither_rose";

	// noise
	public final FastNoiseLiteD noiseHills;

	// params
	public final AtomicDouble valley_depth = new AtomicDouble(DEFAULT_VALLEY_DEPTH);
	public final AtomicDouble valley_gain  = new AtomicDouble(DEFAULT_VALLEY_GAIN );
	public final AtomicDouble hills_gain   = new AtomicDouble(DEFAULT_HILLS_GAIN  );
	public final AtomicInteger water_depth = new AtomicInteger(DEFAULT_WATER_DEPTH);
	public final AtomicDouble rose_chance  = new AtomicDouble(DEFAULT_ROSE_CHANCE );

	// blocks
	public final AtomicReference<String> block_dirt        = new AtomicReference<String>(null);
	public final AtomicReference<String> block_grass_block = new AtomicReference<String>(null);
	public final AtomicReference<String> block_grass_slab  = new AtomicReference<String>(null);
	public final AtomicReference<String> block_grass       = new AtomicReference<String>(null);
	public final AtomicReference<String> block_fern        = new AtomicReference<String>(null);
	public final AtomicReference<String> block_rose        = new AtomicReference<String>(null);



	public Gen_094(final BackroomsLevel backlevel,
			final int level_y, final int level_h) {
		super(backlevel, level_y, level_h);
		// hills noise
		this.noiseHills = this.register(new FastNoiseLiteD());
		this.noiseHills.setFrequency(0.015);
		this.noiseHills.setFractalOctaves(2);
		this.noiseHills.setNoiseType(NoiseType.Cellular);
		this.noiseHills.setFractalType(FractalType.PingPong);
		this.noiseHills.setFractalPingPongStrength(1.8);
		this.noiseHills.setFractalLacunarity(0.8);
	}



	public class HillsData implements PreGenData {

		public final double valueHill;
		public final double depth;

		public HillsData(final double valueHill, final double valley_depth,
				final double valley_gain, final double hills_gain) {
			this.valueHill = valueHill;
			double depth = 1.0 - valueHill;
			if (depth < valley_depth)
				depth *= valley_gain;
			this.depth = depth * hills_gain;
		}

	}



	public void pregenerate(final Map<Iab, HillsData> data,
			final int chunkX, final int chunkZ) {
		final double valley_depth = this.valley_depth.get();
		final double valley_gain  = this.valley_gain.get();
		final double hills_gain   = this.hills_gain.get();
		HillsData dao;
		int xx, zz;
		double valueHill;
		for (int iz=-1; iz<17; iz++) {
			zz = (chunkZ * 16) + iz;
			for (int ix=-1; ix<17; ix++) {
				xx = (chunkX * 16) + ix;
				valueHill = this.noiseHills.getNoise(xx, zz);
				dao = new HillsData(valueHill, valley_depth, valley_gain, hills_gain);
				data.put(new Iab(ix, iz), dao);
			}
		}
	}



	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
		if (!ENABLE_GEN_094) return;
		final BlockData block_dirt        = StringToBlockData(this.block_dirt,        DEFAULT_BLOCK_DIRT       );
		final BlockData block_grass_block = StringToBlockData(this.block_grass_block, DEFAULT_BLOCK_GRASS_BLOCK);
		final BlockData block_grass_slab  = StringToBlockData(this.block_grass_slab,  DEFAULT_BLOCK_GRASS_SLAB );
		final BlockData block_grass       = StringToBlockData(this.block_grass,       DEFAULT_BLOCK_GRASS      );
		final BlockData block_fern        = StringToBlockData(this.block_fern,        DEFAULT_BLOCK_FERN       );
		final BlockData block_rose        = StringToBlockData(this.block_rose,        DEFAULT_BLOCK_ROSE       );
		if (block_dirt        == null) throw new RuntimeException("Invalid block type for level 94 Dirt"      );
		if (block_grass_block == null) throw new RuntimeException("Invalid block type for level 94 GrassBlock");
		if (block_grass_slab  == null) throw new RuntimeException("Invalid block type for level 94 GrassSlab" );
		if (block_grass       == null) throw new RuntimeException("Invalid block type for level 94 Grass"     );
		if (block_fern        == null) throw new RuntimeException("Invalid block type for level 94 Fern"      );
		final int depth_water = this.water_depth.get();;
		final double wither_rose_chance = this.rose_chance.get();
		final HashMap<Iab, HillsData> hillsData = ((PregenLevel94)pregen).hills;
		HillsData dao;
		final int y = this.level_y + 1;
		int depth_dirt;
		int mod_grass;
		int rnd, rose_chance;
		int last_rnd = 0;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
				chunk.setBlock(ix, this.level_y, iz, Material.BEDROCK);
				dao = hillsData.get(new Iab(ix, iz));
				// fill dirt
				depth_dirt = (int) Math.floor(dao.depth - 1.7);
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
					rose_chance = (int) Math.round(1.0 / wither_rose_chance);
					rnd = RandomUtils.GetNewRandom(0, rose_chance, last_rnd);
					last_rnd += rnd;
					if (rnd == 1)            chunk.setBlock(ix, y+depth_dirt+1, iz, block_rose );
					else if (mod_grass == 0) chunk.setBlock(ix, y+depth_dirt+1, iz, block_grass);
					else if (mod_grass == 1) chunk.setBlock(ix, y+depth_dirt+1, iz, block_fern );
				}
			}
		}
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void loadConfig() {
		// noise params
		{
			final ConfigurationSection cfg = this.plugin.getLevelParams(94);
			this.valley_depth.set(cfg.getDouble("ValleyDepth"));
			this.valley_gain .set(cfg.getDouble("ValleyGain" ));
			this.hills_gain  .set(cfg.getDouble("HillsGain"  ));
			this.water_depth .set(cfg.getInt(   "WaterDepth" ));
			this.rose_chance .set(cfg.getDouble("RoseChance" ));
		}
		// block types
		{
			final ConfigurationSection cfg = this.plugin.getLevelBlocks(94);
			this.block_dirt       .set(cfg.getString("Dirt"      ));
			this.block_grass_block.set(cfg.getString("GrassBlock"));
			this.block_grass_slab .set(cfg.getString("GrassSlab" ));
			this.block_grass      .set(cfg.getString("Grass"     ));
			this.block_fern       .set(cfg.getString("Fern"      ));
			this.block_rose       .set(cfg.getString("Rose"      ));
		}
	}
	public static void ConfigDefaults(final FileConfiguration cfg) {
		cfg.addDefault("Level94.Params.ValleyDepth", DEFAULT_VALLEY_DEPTH);
		cfg.addDefault("Level94.Params.ValleyGain",  DEFAULT_VALLEY_GAIN );
		cfg.addDefault("Level94.Params.HillsGain",   DEFAULT_HILLS_GAIN  );
		cfg.addDefault("Level94.Params.WaterDepth",  DEFAULT_WATER_DEPTH );
		cfg.addDefault("Level94.Params.RoseChance",  DEFAULT_ROSE_CHANCE );
		cfg.addDefault("Level94.Blocks.Dirt",       DEFAULT_BLOCK_DIRT       );
		cfg.addDefault("Level94.Blocks.GrassBlock", DEFAULT_BLOCK_GRASS_BLOCK);
		cfg.addDefault("Level94.Blocks.GrassSlab",  DEFAULT_BLOCK_GRASS_SLAB );
		cfg.addDefault("Level94.Blocks.Grass",      DEFAULT_BLOCK_GRASS      );
		cfg.addDefault("Level94.Blocks.Fern",       DEFAULT_BLOCK_FERN       );
		cfg.addDefault("Level94.Blocks.Rose",       DEFAULT_BLOCK_ROSE       );
	}



}
