package com.poixson.backrooms.generators;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.WorldInfo;

import com.poixson.backrooms.BackroomsPlugin;

import me.auburn.FastNoiseLiteD;
import me.auburn.FastNoiseLiteD.FractalType;


// forest
public class BackGen_309 extends BackroomsGenerator {

	public static final int BASE_Y = 0; // bedrock
	public static final int BASE_H = 3; // stone

	public static final int PATH_WIDTH    = 3;
	public static final int PATH_CLEARING = 10;

	public static final Material STONE_BLOCK = Material.STONE;
	public static final Material GRASS_BLOCK = Material.GRASS_BLOCK;
	public static final Material PATH_BLOCK  = Material.DIRT_PATH;

	protected final FastNoiseLiteD noiseGround;
	protected final FastNoiseLiteD noiseTrees;
	protected final FastNoiseLiteD noisePath;

	protected final TreePopulator treePop;
	protected final PathTracer pathTrace;
	protected final AtomicReference<ConcurrentHashMap<Integer, Double>> pathCache =
			new AtomicReference<ConcurrentHashMap<Integer, Double>>(null);



	public BackGen_309(final BackroomsPlugin plugin) {
		super(plugin);
		// ground noise
		this.noiseGround = new FastNoiseLiteD();
		this.noiseGround.setFrequency(0.002f);
		this.noiseGround.setFractalType(FractalType.Ridged);
		this.noiseGround.setFractalOctaves(3);
		this.noiseGround.setFractalGain(0.5f);
		this.noiseGround.setFractalLacunarity(2.0f);
		// tree noise
		this.noiseTrees = new FastNoiseLiteD();
		this.noiseTrees.setFrequency(0.2f);
		// path noise
		this.noisePath = new FastNoiseLiteD();
		this.noisePath.setFrequency(0.01f);
		// populators
		this.treePop = new TreePopulator309(this.noiseTrees, BASE_Y);
		this.pathTrace = new PathTracer(this.noisePath, this.getPathCacheMap());
	}



	@Override
	public void generateSurface(final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk) {
		final int base_y = BASE_Y+BASE_H;
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
				chunk.setBlock(x, BASE_Y-1, z, Material.BEDROCK);
				// stone
				for (int i=0; i<BASE_H; i++) {
					chunk.setBlock(x, BASE_Y+i, z, STONE_BLOCK);
				}
				final int xx = (chunkX * 16) + x;
				final int zz = (chunkZ * 16) + z;
				final double ground;
				{
					final double g = this.noiseGround.getNoise(xx, zz);
					ground = 1.0f + (g < 0.0f ? g * 0.6f : g);
				}
				final int elevation = (int) (ground * 2.5f); // 0 to 5
				// dirt
				for (int i=0; i<elevation; i++) {
					if (i >= elevation-1) {
						if (this.pathTrace.isPath(xx, zz, PATH_WIDTH)) {
							chunk.setBlock(x, base_y+i, z, PATH_BLOCK);
						} else {
							chunk.setBlock(x, base_y+i, z, GRASS_BLOCK);
						}
					} else {
						chunk.setBlock(x, base_y+i, z, Material.DIRT);
					}
				}
			}
		}
	}



	@Override
	public List<BlockPopulator> getDefaultPopulators(final World world) {
		return Arrays.asList(
			this.treePop
		);
	}



	public class TreePopulator309 extends TreePopulator {

		public TreePopulator309(final FastNoiseLiteD noise, final int chunkY) {
			super(noise, chunkY);
		}

		public boolean isTree(final int x, final int z) {
			if (!super.isTree(x, z))
				return false;
			if (BackGen_309.this.pathTrace.isPath(x, z, PATH_CLEARING))
				return false;
			return true;
		}

	}



	public ConcurrentHashMap<Integer, Double> getPathCacheMap() {
		// existing
		{
			final ConcurrentHashMap<Integer, Double> cache = this.pathCache.get();
			if (cache != null)
				return cache;
		}
		// new instance
		{
			final ConcurrentHashMap<Integer, Double> cache = new ConcurrentHashMap<Integer, Double>();
			if (this.pathCache.compareAndSet(null, cache))
				return cache;
		}
		return this.getPathCacheMap();
	}



}
