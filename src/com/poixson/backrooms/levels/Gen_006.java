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
	public static final boolean ENABLE_ROOF     = true;



	public Gen_006(final BackroomsPlugin plugin,
			final int level_y, final int level_h,
			final int subfloor, final int subceiling) {
		super(plugin, level_y, level_h);
	}



	@Override
	public void generate(final PreGenData pregen,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!ENABLE_GENERATE) return;
		LobbyData dao;
		final int y  = this.level_y;
		final int cy = this.level_y + this.level_h;
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
				if (ENABLE_ROOF) {
					// floor/ceiling
					chunk.setBlock(x, y, z, Material.BEDROCK);
					chunk.setBlock(x, cy, z, Material.BEDROCK);
				}
				dao = (LobbyData) ((PregenLevel0)pregen).lobby.get(new Ixy(x, z));
				if (dao == null) continue;
				// wall
				if (dao.isWall) {
					// lobby walls
					final int h = this.level_h - 1;
					for (int yy=0; yy<h; yy++) {
						chunk.setBlock(x, y+yy+1, z, Material.GLOWSTONE);
					}
				// room
				} else {
				} // end wall/room
			} // end x
		} // end z
	}



}
