/*
package com.poixson.backrooms.worlds;

import java.util.LinkedList;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.dynmap.GeneratorTemplate;
import com.poixson.commonmc.tools.plotter.BlockPlotter;


//  11 | City
// 308 | Ikea
public class Level_011 extends BackroomsLevel {

	public static final boolean ENABLE_GEN_011 = true;
	public static final boolean ENABLE_TOP_011 = true;

	public static final int Y_011 = 0;
	public static final int Y_308 = 0;

	// generators
	public final Gen_011 gen_011;
	public final Gen_308 gen_308;



	public Level_011(final BackroomsPlugin plugin) {
		super(plugin, 11);
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 0);
			gen_tpl.add(11, "city", "City");
		}
		// generators
		this.gen_011 = this.register(new Gen_011(this, Y_011, 0));
		this.gen_308 = this.register(new Gen_308(this, Y_308, 0));
	}



	@Override
	public int getLevelFromY(final int y) {
		if (y < Y_011) return 308; // ikea
		return 11;                 // city
	}
	@Override
	public int getY(final int level) {
		switch (level) {
		case 308: return Y_308; // ikea
		case 11:  return Y_011; // city
		default: throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		}
	}
	@Override
	public int getMaxY(final int level) {
		switch (level) {
		case 308: return Y_011 - 1; // ikea
		case 11:  return 320;       // city
		default: throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		}
	}
	@Override
	public boolean containsLevel(final int level) {
		switch (level) {
		case 308: // ikea
		case 11:  // city
			return true;
		default: return false;
		}
	}



	@Override
	protected void generate(final int chunkX, final int chunkZ,
			final ChunkData chunk, final LinkedList<BlockPlotter> plots) {
		this.gen_308.generate(null, chunk, plots, chunkX, chunkZ);
		this.gen_011.generate(null, chunk, plots, chunkX, chunkZ);
	}



}
*/
