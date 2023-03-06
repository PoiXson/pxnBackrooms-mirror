package com.poixson.backrooms.levels;

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

	public static final boolean ENABLE_GENERATE = true;



	public Gen_006(final LevelBackrooms backlevel,
			final int level_y, final int level_h) {
		super(backlevel, level_y, level_h);
	}



	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
		if (!ENABLE_GENERATE) return;
		final HashMap<Iab, LobbyData> lobbyData = ((PregenLevel0)pregen).lobby;
		LobbyData dao;
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
				// floor
				chunk.setBlock(x, this.level_y, z, Material.BEDROCK);
				dao = lobbyData.get(new Iab(x, z));
				if (dao == null) continue;
				// wall
				if (dao.isWall) {
					// lobby walls
					for (int yy=0; yy<this.level_h; yy++) {
						chunk.setBlock(x, this.level_y+yy+1, z, Material.GLOWSTONE);
					}
				} // end wall/room
			} // end x
		} // end z
	}



}
