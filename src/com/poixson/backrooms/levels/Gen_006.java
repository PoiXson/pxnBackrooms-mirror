package com.poixson.backrooms.levels;

import static com.poixson.backrooms.levels.Level_000.ENABLE_GEN_006;

import java.util.HashMap;
import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.levels.Gen_000.LobbyData;
import com.poixson.backrooms.levels.Level_000.PregenLevel0;
import com.poixson.commonmc.tools.plotter.BlockPlotter;
import com.poixson.tools.dao.Iab;


// 6 | Lights Out
public class Gen_006 extends GenBackrooms {



	public Gen_006(final LevelBackrooms backlevel,
			final int level_y, final int level_h) {
		super(backlevel, level_y, level_h);
	}



	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
		if (!ENABLE_GEN_006) return;
		final HashMap<Iab, LobbyData> lobbyData = ((PregenLevel0)pregen).lobby;
		LobbyData dao;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
				// floor
				chunk.setBlock(ix, this.level_y, iz, Material.BEDROCK);
				dao = lobbyData.get(new Iab(ix, iz));
				if (dao == null) continue;
				// wall
				if (dao.isWall) {
					// lobby walls
					for (int iy=0; iy<this.level_h; iy++)
						chunk.setBlock(ix, this.level_y+iy+1, iz, Material.GLOWSTONE);
				}
			} // end ix
		} // end iz
	}



}
