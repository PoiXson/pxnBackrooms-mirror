/*
package com.poixson.backrooms.worlds;

import java.util.LinkedList;

import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.gens.Gen_007;
import com.poixson.commonmc.tools.plotter.BlockPlotter;


// 7 | Thalassophobia
public class Level_007 extends BackroomsLevel {

	public static final boolean ENABLE_GEN_007 = true;

	public static final int LEVEL_Y = -61;
	public static final int LEVEL_H = 360;

	// generators
	public final Gen_007 gen;



	public Level_007(final BackroomsPlugin plugin) {
		super(plugin, 007);
		this.gen = this.register(new Gen_007(this, LEVEL_Y, LEVEL_H));
	}



	@Override
	public int getY(final int level) {
		return LEVEL_Y + LEVEL_H + 1;
	}
	@Override
	public int getMaxY(final int level) {
		return 320;
	}
	@Override
	public boolean containsLevel(final int level) {
		return (level == 7);
	}



	@Override
	protected void generate(final int chunkX, final int chunkZ,
			final ChunkData chunk, final LinkedList<BlockPlotter> plots) {
		this.gen.generate(null, chunk, plots, chunkX, chunkZ);
	}



}
*/
