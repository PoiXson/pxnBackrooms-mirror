package com.poixson.backrooms.levels;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.levels.Gen_000.LobbyData;
import com.poixson.backrooms.levels.Level_000.PregenLevel0;
import com.poixson.tools.dao.Ixy;


// 6 | Lights Out
public class Gen_006 extends GenBackrooms {

	public static final boolean ENABLE_GENERATE = true;



	public Gen_006(final BackroomsPlugin plugin,
			final int level_y, final int level_h) {
		super(plugin, level_y, level_h);
	}



	@Override
	public void generate(final PreGenData pregen,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!ENABLE_GENERATE) return;
		LobbyData dao;
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
				// floor
				chunk.setBlock(x, this.level_y, z, Material.BEDROCK);
				dao = ((PregenLevel0)pregen).lobby.get(new Ixy(x, z));
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
