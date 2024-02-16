package com.poixson.backrooms.worlds;

import java.util.LinkedList;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;

import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.dynmap.GeneratorTemplate;
import com.poixson.backrooms.gens.DataHolder_City;
import com.poixson.backrooms.gens.Gen_004;
import com.poixson.backrooms.gens.Gen_011;
import com.poixson.backrooms.gens.Gen_039;
import com.poixson.backrooms.gens.Gen_040;
import com.poixson.backrooms.gens.Gen_122;
import com.poixson.backrooms.gens.Gen_264;
import com.poixson.backrooms.gens.Gen_308;
import com.poixson.backrooms.gens.Pop_308;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.plotter.BlockPlotter;


//  11 | City - Concrete Jungle
//  39 | Metro
//  40 | Arcade
// 308 | Ikea
// 122 | Mall
// 264 | Museum
//   4 | Abandoned Office
public class Level_011 extends BackroomsLevel {

	public static final boolean ENABLE_GEN_011 = true;
	public static final boolean ENABLE_GEN_039 = true;
	public static final boolean ENABLE_GEN_040 = true;
	public static final boolean ENABLE_GEN_308 = true;
	public static final boolean ENABLE_GEN_122 = true;
	public static final boolean ENABLE_GEN_264 = true;
	public static final boolean ENABLE_GEN_004 = true;

	public static final boolean ENABLE_TOP_011 = true;
	public static final boolean ENABLE_TOP_039 = true;
	public static final boolean ENABLE_TOP_040 = true;
	public static final boolean ENABLE_TOP_308 = true;
	public static final boolean ENABLE_TOP_122 = true;
	public static final boolean ENABLE_TOP_264 = true;
	public static final boolean ENABLE_TOP_004 = true;

	public static final int SUBFLOOR   = 3;
	public static final int SUBCEILING = 3;

	// office
	public static final int Y_004 = 70;
	public static final int H_004 =  8;
	// museum
	public static final int Y_264 = Y_004 + H_004 + SUBFLOOR + 3;
	public static final int H_264 = 8;
	// mall
	public static final int Y_122 = Y_264 + H_264 + SUBFLOOR + 3;
	public static final int H_122 = 8;
	// ikea
	public static final int Y_308 = Y_122 + H_122 + SUBFLOOR + 3;
	public static final int H_308 = 9;
	// metro
	public static final int Y_039 = Y_122 + H_122 + SUBFLOOR + 3;
	public static final int H_039 = 8;
	// city
	public static final int Y_011 = Y_039 + H_039 + SUBFLOOR + 3;
	// arcade
	public static final int Y_040 = Y_122;
	public static final int H_040 = H_122;

	// generators
	public final Gen_011 gen_011;
	public final Gen_039 gen_039;
	public final Gen_040 gen_040;
	public final Gen_308 gen_308;
	public final Gen_122 gen_122;
	public final Gen_264 gen_264;
	public final Gen_004 gen_004;

	// populators
	public final Pop_308 pop_308;



	public Level_011(final BackroomsPlugin plugin) {
		super(plugin);
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 0);
			gen_tpl.add(  4, "office", "Abandoned Office", Y_004+SUBFLOOR+1);
			gen_tpl.add(264, "museum", "Museum",           Y_264+SUBFLOOR+1);
			gen_tpl.add(122, "mall",   "Mall",             Y_122+SUBFLOOR+1);
			gen_tpl.add(308, "ikea",   "Ikea",             Y_308+SUBFLOOR+1);
			gen_tpl.add( 40, "arcade", "Arcade",           Y_040+SUBFLOOR+1);
			gen_tpl.add( 39, "metro",  "Metro",            Y_039+SUBFLOOR+1);
			gen_tpl.add( 11, "city",   "Concrete Jungle"                   );
			gen_tpl.commit();
		}
		// generators
		this.gen_004 = this.register(new Gen_004(this, this.seed, Y_004, H_004)); // office
		this.gen_264 = this.register(new Gen_264(this, this.seed, Y_264, H_264)); // museum
		this.gen_122 = this.register(new Gen_122(this, this.seed, Y_122, H_122)); // mall
		this.gen_308 = this.register(new Gen_308(this, this.seed, Y_308, H_308)); // ikea
		this.gen_040 = this.register(new Gen_040(this, this.seed, Y_040, H_040)); // arcade
		this.gen_039 = this.register(new Gen_039(this, this.seed, Y_039, H_039)); // metro
		this.gen_011 = this.register(new Gen_011(this, this.seed, Y_011,     0)); // city
		// populators
		this.pop_308 = this.register(new Pop_308(this)); // ikea
	}



	// -------------------------------------------------------------------------------
	// locations



	@Override
	public int getMainLevel() {
		return 11; // city
	}
	@Override
	public int getLevel(final Location loc) {
		final int y = loc.getBlockY();
		if (y < this.getMaxY(  4)) return   4; // office
		if (y < this.getMaxY(264)) return 264; // museum
		if (y < this.getMaxY(122)) return 122; // mall
		if (y < this.getMaxY(308)) return 308; // ikea
		if (y < this.getMaxY( 39)) return  39; // metro
		return 11;                             // city
	}
	@Override
	public boolean containsLevel(final int level) {
		switch (level) {
		case   4: // office
		case 264: // museum
		case 122: // mall
		case 308: // ikea
		case  40: // arcade
		case  39: // metro
		case  11: // city
			return true;
		default: return false;
		}
	}



	@Override
	public int getY(final int level) {
		switch (level) {
		case   4: return Y_004; // office
		case 264: return Y_264; // museum
		case 122: return Y_122; // mall
		case 308: return Y_308; // ikea
		case  40: return Y_040; // arcade
		case  39: return Y_039; // metro
		case  11: return Y_011; // city
		default: throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		}
	}
	@Override
	public int getMaxY(final int level) {
		switch (level) {
		case   4: return Y_264 - 1; // office
		case 264: return Y_122 - 1; // museum
		case 122: return Y_308 - 1; // mall
		case 308: return Y_011 - 1; // ikea
		case  40: return Y_011 - 1; // arcade
		case  39: return Y_011 - 1; // metro
		case  11: return 320;       // city
		default: throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		}
	}



	// -------------------------------------------------------------------------------
	// spawn



	@Override
	public Location getSpawnArea(final int level) {
		switch (level) {
		case 40: return super.getSpawnArea(40);
		default: return super.getSpawnArea(11);
		}
	}

	@Override
	public Location getFixedSpawnLocation(final World world, final Random random) {
		final int y = this.getY(11);
		return world.getBlockAt(100, y, 100).getLocation();
	}



	// -------------------------------------------------------------------------------
	// generate



	public class PregenLevel11 implements PreGenData {
		public final DataHolder_City city;

		public PregenLevel11(final int chunkX, final int chunkZ) {
			this.city = new DataHolder_City(Level_011.this.gen_011, chunkX, chunkZ);
			this.city.findEdges();
		}

	}



	@Override
	protected void generate(final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		// pre-generate
		final PregenLevel11 pregen = new PregenLevel11(chunkX, chunkZ);
		// generate
		this.gen_004.generate(pregen, plots, chunk, chunkX, chunkZ); // office
		this.gen_264.generate(pregen, plots, chunk, chunkX, chunkZ); // museum
		if (chunkX >-4 && chunkZ >-4
		&&  chunkX < 4 && chunkZ < 4) {
			// arcade
			this.gen_040.generate(null, null, chunk, chunkX, chunkZ);
		} else {
			// mall
			this.gen_122.generate(pregen, plots, chunk, chunkX, chunkZ); // mall
		}
		this.gen_039.generate(pregen, plots, chunk, chunkX, chunkZ); // metro
		this.gen_308.generate(pregen, plots, chunk, chunkX, chunkZ); // ikea
		this.gen_011.generate(pregen, plots, chunk, chunkX, chunkZ); // city
	}



}
