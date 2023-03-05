package com.poixson.backrooms.levels;

import static com.poixson.backrooms.levels.Level_000.SUBCEILING;
import static com.poixson.backrooms.levels.Level_000.SUBFLOOR;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.block.data.type.Slab;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.levels.Gen_000.LobbyData;
import com.poixson.backrooms.levels.Level_000.PregenLevel0;
import com.poixson.tools.dao.Iab;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.NoiseType;


// 23 | Overgrowth
public class Gen_023 extends GenBackrooms {

	public static final boolean ENABLE_GENERATE = true;
	public static final boolean ENABLE_ROOF     = true;

	public static final double THRESH_CARPET = 0.4;

	public static final Material LOBBY_SUBFLOOR = Material.DIRT;

	protected final FastNoiseLiteD noiseCarpet;



	public Gen_023(final BackroomsPlugin plugin,
			final int level_y, final int level_h) {
		super(plugin, level_y, level_h);
		// moss carpets
		this.noiseCarpet = this.register(new FastNoiseLiteD());
		this.noiseCarpet.setFrequency(0.01);
		this.noiseCarpet.setNoiseType(NoiseType.OpenSimplex2);
		this.noiseCarpet.setFractalOctaves(1);
	}



	@Override
	public void generate(final PreGenData pregen,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!ENABLE_GENERATE) return;
		final HashMap<Iab, LobbyData> lobbyData = ((PregenLevel0)pregen).lobby;
		LobbyData dao;
		final int y  = this.level_y + SUBFLOOR + 1;
		final int cy = this.level_y + SUBFLOOR + this.level_h + 2;
		int xx, zz;
		double valueCarpet;
		for (int z=0; z<16; z++) {
			zz = (chunkZ * 16) + z;
			for (int x=0; x<16; x++) {
				xx = (chunkX * 16) + x;
				dao = lobbyData.get(new Iab(x, z));
				// floor
				chunk.setBlock(x, this.level_y, z, Material.BEDROCK);
				for (int iy=0; iy<SUBFLOOR; iy++)
					chunk.setBlock(x, this.level_y+iy+1, z, LOBBY_SUBFLOOR);
				chunk.setBlock(x, this.level_y+SUBFLOOR+1, z, Material.MOSSY_COBBLESTONE);
				// wall
				if (dao.isWall) {
					final int h = this.level_h + 1;
					for (int yi=0; yi<h; yi++)
						chunk.setBlock(x, y+yi+1, z, Material.MOSS_BLOCK);
				// room
				} else {
					// moss carpet
					valueCarpet = this.noiseCarpet.getNoise(xx, zz);
					if (valueCarpet > THRESH_CARPET)
						chunk.setBlock(x, this.level_y+SUBFLOOR+2, z, Material.MOSS_CARPET);
					// ceiling
					if (ENABLE_ROOF) {
						final int modX6 = Math.abs(xx) % 7;
						final int modZ6 = Math.abs(zz) % 7;
						if (modZ6 == 0 && modX6 < 2
						&& dao.wall_dist > 1) {
							// ceiling lights
							chunk.setBlock(x, cy,   z, Material.VERDANT_FROGLIGHT);
							chunk.setBlock(x, cy-1, z, Material.WEEPING_VINES);
						} else {
							// ceiling
							chunk.setBlock(x, cy, z, Material.SMOOTH_STONE_SLAB);
							final Slab slab = (Slab) chunk.getBlockData(x, cy, z);
							slab.setType(Slab.Type.TOP);
							chunk.setBlock(x, cy, z, slab);
						}
					}
				}
				// subceiling
				if (ENABLE_ROOF) {
					for (int iy=0; iy<SUBCEILING; iy++)
						chunk.setBlock(x, cy+iy+1, z, Material.STONE);
				}
			} // end x
		} // end z
	}



}
