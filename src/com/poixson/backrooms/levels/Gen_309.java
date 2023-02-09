package com.poixson.backrooms.levels;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.commonmc.tools.PathTracer;
import com.poixson.commonmc.tools.TreePopulator;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.FractalType;


// 309 | Radio Station
public class Gen_309 extends GenBackrooms {

	public static final boolean ENABLE_GENERATE = true;
	public static final boolean ENABLE_ROOF     = true;

	public static final int PATH_WIDTH    = 3;
	public static final int PATH_CLEARING = 10;

	public static final Material PATH_TREE_TRUNK  = Material.BIRCH_LOG;
	public static final Material PATH_TREE_LEAVES = Material.BIRCH_LEAVES;

	public final int subfloor;

	// noise
	protected final FastNoiseLiteD noisePath;
	protected final FastNoiseLiteD noisePathGround;
	protected final FastNoiseLiteD noiseTrees;

	// populators
	protected final TreePopulator treePop;
	protected final PathTracer pathTrace;
	protected final AtomicReference<ConcurrentHashMap<Integer, Double>> pathCache =
			new AtomicReference<ConcurrentHashMap<Integer, Double>>(null);
	public final Pop_309 popRadio;



	public Gen_309(final BackroomsPlugin plugin,
			final int level_y, final int level_h,
			final int subfloor) {
		super(plugin, level_y, level_h);
		this.subfloor   = subfloor;
		// path
		this.noisePath = this.register(new FastNoiseLiteD());
		this.noisePath.setFrequency(0.01f);
		// path ground
		this.noisePathGround = this.register(new FastNoiseLiteD());
		this.noisePathGround.setFrequency(0.002f);
		this.noisePathGround.setFractalType(FractalType.Ridged);
		this.noisePathGround.setFractalOctaves(3);
		this.noisePathGround.setFractalGain(0.5f);
		this.noisePathGround.setFractalLacunarity(2.0f);
		// tree noise
		this.noiseTrees = this.register(new FastNoiseLiteD());
		this.noiseTrees.setFrequency(0.2f);
		// populators
		this.treePop = new Pop_309_Trees(this);
		this.pathTrace = new PathTracer(this.noisePath, this.getPathCacheMap());
		this.popRadio = new Pop_309(this);
	}
	@Override
	public void unload() {
		super.unload();
		this.pathCache.set(null);
	}



	public FastNoiseLiteD getTreeNoise() {
		return this.noiseTrees;
	}



	@Override
	public void generate(final PreGenData pregen,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!ENABLE_GENERATE) return;
		final int y = this.level_y + this.subfloor + 1;
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
				final int xx = (chunkX * 16) + x;
				final int zz = (chunkZ * 16) + z;
				chunk.setBlock(x, this.level_y, z, Material.BEDROCK);
				for (int i=0; i<this.subfloor; i++) {
					chunk.setBlock(x, this.level_y+i+1, z, Material.STONE);
				}
				final double ground;
				{
					final double g = this.noisePathGround.getNoise(xx, zz);
					ground = 1.0f + (g < 0.0f ? g * 0.6f : g);
				}
				// dirt
				final int elevation = (int) (ground * 2.5f); // 0 to 5
				for (int i=0; i<elevation; i++) {
					if (i >= elevation-1) {
						if (this.pathTrace.isPath(xx, zz, PATH_WIDTH)) {
							chunk.setBlock(x, y+i, z, Material.DIRT_PATH);
						} else {
							chunk.setBlock(x, y+i, z, Material.GRASS_BLOCK);
						}
					} else {
						chunk.setBlock(x, y+i, z, Material.DIRT);
					}
				}
			} // end x
		} // end z
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



	public boolean isCenterClearing(final int x, final int z) {
		if (Math.abs(x) > 100 || Math.abs(z) > 100)
			return false;
		final double distance = Math.sqrt( Math.pow((double)x, 2.0) + Math.pow((double)z, 2.0) )
			+ (this.noisePath.getNoise(x*5, z*5) * 8.0);
		return (distance < 80.0);
	}



}
