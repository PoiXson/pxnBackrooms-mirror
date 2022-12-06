package com.poixson.backrooms.generators;

import java.util.Random;

import org.bukkit.generator.WorldInfo;

import com.poixson.backrooms.BackroomsPlugin;

import me.auburn.FastNoiseLiteF;


public class BackGen_000 extends BackroomsGenerator {

	public static final int BASE_Y = 0;

	protected final ThreadLocal<FastNoiseLiteF> noiseBasementFloor = new ThreadLocal<FastNoiseLiteF>();
	protected final ThreadLocal<FastNoiseLiteF> noiseTree          = new ThreadLocal<FastNoiseLiteF>();



	public BackGen_000(final BackroomsPlugin plugin) {
		super(plugin);
	}



	@Override
	public void generateSurface(final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk) {
	}
/*
		final FastNoiseLiteF noiseBasementFloor = this.getNoiseBasementFloor();
		final TreeBuilder treeBuilder = new TreeBuilder(chunk, chunkX, BASE_Y+4, chunkZ);
		// basement floor
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
				chunk.setBlock(x, BASE_Y,   z, Material.BEDROCK     );
				chunk.setBlock(x, BASE_Y+1, z, Material.STONE       );
				chunk.setBlock(x, BASE_Y+2, z, Material.STONE       );
				final int xx = (chunkX * 16) + x;
				final int zz = (chunkZ * 16) + z;
				final float A = Math.abs(noiseBasementFloor.getNoise(xx, 0,   zz));
				final float B = Math.abs(noiseBasementFloor.getNoise(xx, 100, zz));
				final float valueFloor = Math.max(A, B);
				final boolean isTree = this.treeBuilder.isTree(this.noiseTree, xx, zz);
				final boolean isTree = (Math.round(floorValue*10.0f) % 5 == 0);
				final boolean isMycelium = (floorValue > 0.35f);
				if (isMycelium) {
					chunk.setBlock(x, BASE_Y+3, z, Material.MYCELIUM);
				} else {
					if (!isTree) {
						chunk.setBlock(x, BASE_Y+3, z, Material.GRASS_BLOCK);
						final float grassValue = floorValue + (NumberUtils.GetRandom(0, 10) / 10.0f);
						if (grassValue <= 0.3f)
							chunk.setBlock(x, BASE_Y+4, z, Material.GRASS);
					}
				}
				// trees
				if (isTree) {
					this.treeBuilder.build(this.noiseTree, xx, zz, chunk, x, z);
					chunk.setBlock(x, BASE_Y+4, z, Material.BIRCH_LOG);
//					final BlockData block = chunk.getBlockData(x, BASE_Y+4, z);
//					block.setFacing(BlockFace.DOWN);
				}
			}
		}
	}
		final ChunkDAO chunk =
			new ChunkDAO_Generate(
				worldInfo,
				chunkX, chunkZ,
				chunkData
			);
//TODO: find a better way
//worldInfo.getSeed()
		this.craftscript.call(ScriptHookType.GEN.name, chunk);
*/



	// ----------------------------------------
	// noise



	protected FastNoiseLiteF getNoiseBasementFloor() {
		{
			final FastNoiseLiteF noise = this.noiseBasementFloor.get();
			if (noise != null)
				return noise;
		}
		{
			final FastNoiseLiteF noise = new FastNoiseLiteF();
			noise.setFrequency(0.07f);
			this.noiseBasementFloor.set(noise);
			return noise;
		}
	}



}
