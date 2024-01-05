package com.poixson.backrooms.worlds;

import java.util.HashMap;
import java.util.LinkedList;

import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.dynmap.GeneratorTemplate;
import com.poixson.backrooms.gens.Gen_094;
import com.poixson.backrooms.gens.Gen_094.HillsData;
import com.poixson.tools.dao.Iab;
import com.poixson.tools.plotter.BlockPlotter;


// 94 | Motion
public class Level_094 extends BackroomsLevel {

	public static final boolean ENABLE_GEN_094 = true;

	public static final int LEVEL_Y = 0;

	// generators
	public final Gen_094 gen;



	public Level_094(final BackroomsPlugin plugin) {
		super(plugin);
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 94);
			gen_tpl.add(94, "motion", "Motion");
			gen_tpl.commit();
		}
		// generators
		this.gen = this.register(new Gen_094(this, 0, 0));
	}



	@Override
	public int getMainLevel() {
		return 94; // motion
	}



	// -------------------------------------------------------------------------------
	// locations



	@Override
	public int getY(final int level) {
		return 255;
	}
	@Override
	public int getMaxY(final int level) {
		return 319;
	}
	@Override
	public boolean containsLevel(final int level) {
		return (level == 94);
	}



	// -------------------------------------------------------------------------------
	// spawn



	// -------------------------------------------------------------------------------
	// generate



	public class PregenLevel94 implements PreGenData {
		public final HashMap<Iab, HillsData> hills = new HashMap<Iab, HillsData>();
		public PregenLevel94() {}
	}



	@Override
	protected void generate(final int chunkX, final int chunkZ,
			final ChunkData chunk, final LinkedList<BlockPlotter> plots) {
		// pre-generate
		final PregenLevel94 pregen = new PregenLevel94();
		this.gen.pregenerate(pregen.hills, chunkX, chunkZ);
		// generate
		this.gen.generate(pregen, chunk, plots, chunkX, chunkZ);
	}



}
