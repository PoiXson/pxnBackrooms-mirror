package com.poixson.backrooms.levels;

import java.util.HashMap;
import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.levels.Gen_000.LobbyData;
import com.poixson.backrooms.levels.Level_000.PregenLevel0;
import com.poixson.commonmc.tools.plotter.BlockPlotter;
import com.poixson.tools.dao.Iab;
import com.poixson.utils.FastNoiseLiteD;


// 19 | Attic
public class Gen_019 extends GenBackrooms {

	public static final boolean ENABLE_GENERATE = true;
	public static final boolean ENABLE_ROOF     = true;

	public static final Material ATTIC_FLOOR = Material.SPRUCE_PLANKS;
	public static final Material ATTIC_WALLS = Material.SPRUCE_PLANKS;

	// noise
	public final FastNoiseLiteD noiseLamps;



	public Gen_019(final LevelBackrooms backlevel,
			final int level_y, final int level_h) {
		super(backlevel, level_y, level_h);
		// lanterns
		this.noiseLamps = this.register(new FastNoiseLiteD());
		this.noiseLamps.setFrequency(0.045);
		this.noiseLamps.setFractalOctaves(1);
	}



	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
		if (!ENABLE_GENERATE) return;
		final HashMap<Iab, LobbyData> lobbyData = ((PregenLevel0)pregen).lobby;
		LobbyData dao;
		double valueLamp;
		final int y  = this.level_y + Level_000.SUBFLOOR + 1;
		int xx, zz;
		int modX7, modZ7;
		for (int iz=0; iz<16; iz++) {
			zz = (chunkZ * 16) + iz;
			for (int ix=0; ix<16; ix++) {
				xx = (chunkX * 16) + ix;
				dao = lobbyData.get(new Iab(ix, iz));
				if (dao == null) continue;
				modX7 = (xx < 0 ? 1-xx : xx) % 7;
				modZ7 = (zz < 0 ? 1-zz : zz) % 7;
				if (ENABLE_ROOF) {
					// beam
					if (modX7 == 0 || modZ7 == 0)
						chunk.setBlock(ix, this.level_y+dao.wall_dist+3, iz, Material.SPRUCE_WOOD);
					// lantern
					if (modX7 == 0 && modZ7 == 0 && dao.wall_dist == 6) {
						valueLamp = this.noiseLamps.getNoise(xx, zz);
						if (valueLamp < 0.0)
							chunk.setBlock(ix, this.level_y+dao.wall_dist+2, iz, Material.LANTERN);
					}
				}
				// attic floor
				chunk.setBlock(ix, this.level_y, iz, Material.BEDROCK);
				for (int iy=0; iy<Level_000.SUBFLOOR; iy++)
					chunk.setBlock(ix, this.level_y+iy+1, iz, ATTIC_FLOOR);
				// wall
				if (dao.isWall) {
					// lobby walls
					for (int iy=0; iy<this.level_h+1; iy++)
						chunk.setBlock(ix, y+iy, iz, (iy>6 ? Material.BEDROCK : ATTIC_WALLS));
				}
			} // end x
		} // end z
	}



}
