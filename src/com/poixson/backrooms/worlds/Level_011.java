package com.poixson.backrooms.worlds;

import java.util.LinkedList;

import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.dynmap.GeneratorTemplate;
import com.poixson.backrooms.gens.Gen_004;
import com.poixson.backrooms.gens.Gen_011;
import com.poixson.backrooms.gens.Gen_040;
import com.poixson.backrooms.gens.Gen_308;
import com.poixson.backrooms.gens.Pop_308;
import com.poixson.tools.plotter.BlockPlotter;


//  11 | Concrete Jungle
//  40 | Arcade
// 308 | Ikea
//   4 | Abandoned Office
public class Level_011 extends BackroomsLevel {

	public static final boolean ENABLE_GEN_011 = false;
	public static final boolean ENABLE_GEN_040 = false;
	public static final boolean ENABLE_GEN_308 = true;
	public static final boolean ENABLE_GEN_004 = false;

	public static final boolean ENABLE_TOP_011 = false;
	public static final boolean ENABLE_TOP_040 = false;
	public static final boolean ENABLE_TOP_308 = false;
	public static final boolean ENABLE_TOP_004 = false;

	// abandoned office
	public static final int Y_004 = 70;
	public static final int H_004 =  8;
	// ikea
	public static final int Y_308 = Y_004 + H_004 + 1;
	public static final int H_308 = 30;
	// concrete jungle
	public static final int Y_011 = Y_308 + H_308 + 1;
	// arcade
	public static final int Y_040 = Y_308;
	public static final int H_040 = H_308;

	// generators
	public final Gen_011 gen_011;
	public final Gen_040 gen_040;
	public final Gen_308 gen_308;
	public final Gen_004 gen_004;

	// populators
	public final Pop_308 pop_308;



	public Level_011(final BackroomsPlugin plugin) {
		super(plugin, 11);
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 0);
			gen_tpl.add(  4, "office", "Abandoned Office", Y_004 +1);
			gen_tpl.add(308, "ikea",   "Ikea",             Y_308 +1);
			gen_tpl.add( 40, "arcade", "Arcade",           Y_040 +1);
			gen_tpl.add( 11, "city",   "Concrete Jungle"           );
			gen_tpl.commit();
		}
		// generators
		this.gen_004 = this.register(new Gen_004(this, Y_004, H_004)); // abandoned office
		this.gen_308 = this.register(new Gen_308(this, Y_308, H_308)); // ikea
		this.gen_040 = this.register(new Gen_040(this, Y_040, H_040)); // arcade
		this.gen_011 = this.register(new Gen_011(this, Y_011,     0)); // concrete jungle
		// populators
		this.pop_308 = this.register(new Pop_308(this)); // ikea
	}



	@Override
	public int getLevelFromY(final int y) {
		if (y < Y_308) return   4; // abandoned office
		if (y < Y_011) return 308; // ikea
		return 11;                 // concrete jungle
	}
	@Override
	public int getY(final int level) {
		switch (level) {
		case   4: return Y_004; // abandoned office
		case 308: return Y_308; // ikea
		case  40: return Y_040; // arcade
		case  11: return Y_011; // concrete jungle
		default: throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		}
	}
	@Override
	public int getMaxY(final int level) {
		switch (level) {
		case   4: return Y_308 - 1; // abandoned office
		case 308: return Y_011 - 1; // ikea
		case  40: return Y_011 - 1; // arcade
		case  11: return 320;       // concrete jungle
		default: throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		}
	}
	@Override
	public boolean containsLevel(final int level) {
		switch (level) {
		case   4: // abandoned office
		case 308: // ikea
		case  40: // arcade
		case  11: // concrete jungle
			return true;
		default: return false;
		}
	}



	@Override
	protected void generate(final int chunkX, final int chunkZ,
			final ChunkData chunk, final LinkedList<BlockPlotter> plots) {
		// generate
		this.gen_004.generate(null, chunk, plots, chunkX, chunkZ); // abandoned office
		this.gen_308.generate(null, chunk, plots, chunkX, chunkZ); // ikea
		this.gen_040.generate(null, chunk, plots, chunkX, chunkZ); // arcade
		this.gen_011.generate(null, chunk, plots, chunkX, chunkZ); // concrete jungle
	}



}
