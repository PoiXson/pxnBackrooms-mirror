package com.poixson.backrooms.worlds;

import java.util.LinkedList;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.BackroomsWorld;
import com.poixson.backrooms.dynmap.GeneratorTemplate;
import com.poixson.backrooms.gens.Gen_333;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.plotter.BlockPlotter;


// 333 | Cubes
public class BackWorld_333 extends BackroomsWorld {

	// generators
	public final Gen_333 gen_333;



	public BackWorld_333(final BackroomsPlugin plugin) {
		super(plugin);
		// generators
		this.gen_333 = this.register(new Gen_333(this, this.seed));
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 333);
			gen_tpl.add(333, "cubes", "Cubes");
			gen_tpl.commit();
		}
	}



	// -------------------------------------------------------------------------------
	// locations



	@Override
	public int getMainLevel() {
		return 333; // cubes
	}
	@Override
	public boolean containsLevel(final int level) {
		return (level == 333);
	}
	@Override
	public int[] getLevels() {
		return new int[] {
			333, // cubes
		};
	}



	@Override
	public int getOpenY(final int level) {
		return this.gen_333.getOpenY();
	}

	@Override
	public int getMinY(final int level) {
		return this.gen_333.getMinY();
	}
	@Override
	public int getMaxY(final int level) {
		return this.gen_333.getMaxY();
	}



	// -------------------------------------------------------------------------------
	// spawn



//TODO: prevent spawning outside of a cube



	// -------------------------------------------------------------------------------
	// generate



	@Override
	protected void generate(final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		// generate
		this.gen_333.generate(null, plots, chunk, chunkX, chunkZ);
	}



}
