package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_094.ENABLE_GEN_094;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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


// 94 | Motion
public class Gen_094 extends BackroomsGen {

	// default params
	public static final double DEFAULT_VALLEY_DEPTH =  0.33;
	public static final double DEFAULT_VALLEY_GAIN  =  0.3;
	public static final double DEFAULT_HILLS_GAIN   = 12.0;

	// default blocks
	public static final String DEFAULT_BLOCK_DIRT        = "minecraft:dirt";
	public static final String DEFAULT_BLOCK_GRASS_BLOCK = "minecraft:moss_block";
	public static final String DEFAULT_BLOCK_GRASS_SLAB  = "minecraft:mud_brick_slab";
	public static final String DEFAULT_BLOCK_GRASS       = "minecraft:grass";

	// noise
	public final FastNoiseLiteD noiseHills;

	// params
	public final AtomicDouble valley_depth = new AtomicDouble(DEFAULT_VALLEY_DEPTH);
	public final AtomicDouble valley_gain  = new AtomicDouble(DEFAULT_VALLEY_GAIN );
	public final AtomicDouble hills_gain   = new AtomicDouble(DEFAULT_HILLS_GAIN  );

	// blocks
	public final AtomicReference<String> block_dirt        = new AtomicReference<String>(null);
	public final AtomicReference<String> block_grass_block = new AtomicReference<String>(null);
	public final AtomicReference<String> block_grass_slab  = new AtomicReference<String>(null);
	public final AtomicReference<String> block_grass       = new AtomicReference<String>(null);



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
		if (block_dirt        == null) throw new RuntimeException("Invalid block type for level 94 Dirt"      );
		if (block_grass_block == null) throw new RuntimeException("Invalid block type for level 94 GrassBlock");
		if (block_grass_slab  == null) throw new RuntimeException("Invalid block type for level 94 GrassSlab" );
		if (block_grass       == null) throw new RuntimeException("Invalid block type for level 94 Grass"     );
		final HashMap<Iab, HillsData> hillsData = ((PregenLevel94)pregen).hills;
		HillsData dao;
		final int y = this.level_y + 1;
		int depth_dirt;
		int mod_grass;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
				dao = hillsData.get(new Iab(ix, iz));
				// fill dirt
				depth_dirt = (int) Math.floor(dao.depth - 0.7);
				for (int iy=0; iy<depth_dirt; iy++)
					chunk.setBlock(ix, y+iy, iz, block_dirt);
				// surface
				if (dao.depth % 1.0 > 0.7) {
					chunk.setBlock(ix, y+depth_dirt, iz, block_grass_slab);
				} else {
					chunk.setBlock(ix, y+depth_dirt, iz, block_grass_block);
					mod_grass = (int)Math.floor(dao.valueHill * 1000.0) % 3;
					if (mod_grass == 0)
					chunk.setBlock(ix, y+depth_dirt+1, iz, block_grass);
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
		}
		// block types
		{
			final ConfigurationSection cfg = this.plugin.getLevelBlocks(94);
			this.block_dirt       .set(cfg.getString("Dirt"      ));
			this.block_grass_block.set(cfg.getString("GrassBlock"));
			this.block_grass_slab .set(cfg.getString("GrassSlab" ));
			this.block_grass      .set(cfg.getString("Grass"     ));
		}
	}
	public static void ConfigDefaults(final FileConfiguration cfg) {
		cfg.addDefault("Level94.Params.ValleyDepth", DEFAULT_VALLEY_DEPTH);
		cfg.addDefault("Level94.Params.ValleyGain",  DEFAULT_VALLEY_GAIN );
		cfg.addDefault("Level94.Params.HillsGain",   DEFAULT_HILLS_GAIN  );
		cfg.addDefault("Level94.Blocks.Dirt",       DEFAULT_BLOCK_DIRT       );
		cfg.addDefault("Level94.Blocks.GrassBlock", DEFAULT_BLOCK_GRASS_BLOCK);
		cfg.addDefault("Level94.Blocks.GrassSlab",  DEFAULT_BLOCK_GRASS_SLAB );
		cfg.addDefault("Level94.Blocks.Grass",      DEFAULT_BLOCK_GRASS      );
	}



}
