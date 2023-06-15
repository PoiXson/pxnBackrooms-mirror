/*
package com.poixson.backrooms.worlds;

import java.util.LinkedList;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.dynmap.GeneratorTemplate;
import com.poixson.commonmc.tools.plotter.BlockPlotter;


//  11 | Concrete Jungle
//   4 | Abandoned Office
//  40 | Arcade
// 308 | Ikea
public class Level_011 extends BackroomsLevel {

	public static final boolean ENABLE_GEN_011 = true;
	public static final boolean ENABLE_GEN_004 = true;
	public static final boolean ENABLE_GEN_040 = true;
	public static final boolean ENABLE_GEN_308 = true;

	public static final boolean ENABLE_TOP_011 = true;
	public static final boolean ENABLE_TOP_004 = true;
	public static final boolean ENABLE_TOP_040 = true;
	public static final boolean ENABLE_TOP_308 = true;

	// ikea
	public static final int Y_308 = 100;
	public static final int H_308 = 30;
	// arcade
	public static final int Y_040 = ;
	public static final int H_040 = ;
	// abandoned office
	public static final int Y_004 = ;
	public static final int H_004 = ;
	// concrete jungle
	public static final int Y_011 = Y_308 + H_308 + SUBFLOOR + SUBCEILING + 1;

	// generators
	public final Gen_011 gen_011;
	public final Gen_004 gen_004;
	public final Gen_040 gen_040;
	public final Gen_308 gen_308;



	public Level_011(final BackroomsPlugin plugin) {
		super(plugin, 11);
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 0);
			gen_tpl.add(308, "ikea", "Ikea", Y_308+1);
			gen_tpl.add(11,  "city", "Concrete Jungle");
		}
		// generators
		this.gen_308 = this.register(new Gen_308(this, Y_308, H_308)); // ikea
		this.gen_040 = this.register(new Gen_011(this, Y_040, H_040)); // arcade
		this.gen_004 = this.register(new Gen_011(this, Y_004, H_004)); // abandoned office
		this.gen_011 = this.register(new Gen_011(this, Y_011, 0    )); // concrete jungle
	}



	@Override
	public int getLevelFromY(final int y) {
		if (y < Y_040) return 308; // ikea
		if (y < Y_004) return  40; // arcade
		if (y < Y_011) return   4; // abandoned office
		return 11;                 // concrete jungle
	}
	@Override
	public int getY(final int level) {
		switch (level) {
		case 11:  return Y_011; // concrete jungle
		case  4:  return Y_004; // abandoned office
		case 40:  return Y_040; // arcade
		case 308: return Y_308; // ikea
		default: throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		}
	}
	@Override
	public int getMaxY(final int level) {
		switch (level) {
		case 11:  return 320;       // concrete jungle
		case  4:  return Y_011 - 1; // abandoned office
		case 40:  return Y_004 - 1; // arcade
		case 308: return Y_040 - 1; // ikea
		default: throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		}
	}
	@Override
	public boolean containsLevel(final int level) {
		switch (level) {
		case 11:  // concrete jungle
		case  4:  // abandoned office
		case 40:  // arcade
		case 308: // ikea
			return true;
		default: return false;
		}
	}



	@Override
	protected void generate(final int chunkX, final int chunkZ,
			final ChunkData chunk, final LinkedList<BlockPlotter> plots) {
		this.gen_308.generate(null, chunk, plots, chunkX, chunkZ);
		this.gen_040.generate(null, chunk, plots, chunkX, chunkZ);
		this.gen_004.generate(null, chunk, plots, chunkX, chunkZ);
		this.gen_011.generate(null, chunk, plots, chunkX, chunkZ);
	}



}
*/
