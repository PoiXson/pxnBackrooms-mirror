package com.poixson.backrooms.worlds;

import java.util.LinkedList;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.BackroomsWorld;
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
// 122 | Mall
// 308 | Ikea
// 264 | Museum
//   4 | Abandoned Office
//  40 | Arcade
public class Level_011 extends BackroomsWorld {

	// generators
	public final Gen_004 gen_004;
	public final Gen_264 gen_264;
	public final Gen_308 gen_308;
	public final Gen_122 gen_122;
	public final Gen_039 gen_039;
	public final Gen_011 gen_011;
	public final Gen_040 gen_040;

	// populators
	public final Pop_308 pop_308;



	public Level_011(final BackroomsPlugin plugin) {
		super(plugin);
		// generators
		this.gen_004 = this.register(new Gen_004(this, this.seed                      )); // office
		this.gen_264 = this.register(new Gen_264(this, this.seed, this.gen_004        )); // museum
		this.gen_308 = this.register(new Gen_308(this, this.seed, this.gen_264        )); // ikea
		this.gen_122 = this.register(new Gen_122(this, this.seed, this.gen_308        )); // mall
		this.gen_039 = this.register(new Gen_039(this, this.seed, this.gen_122        )); // metro
		this.gen_011 = this.register(new Gen_011(this, this.seed, this.gen_039        )); // city
		this.gen_040 = this.register(new Gen_040(this, this.seed, this.gen_122.level_y)); // arcade
		// populators
		this.pop_308 = this.register(new Pop_308(this)); // ikea
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 11);
			gen_tpl.add(  4, "office", "Office", this.gen_004.level_y+this.gen_004.bedrock_barrier+this.gen_004.subfloor+1);
			gen_tpl.add(264, "museum", "Museum", this.gen_264.level_y+this.gen_264.bedrock_barrier+this.gen_264.subfloor+1);
			gen_tpl.add(308, "ikea",   "Ikea",   this.gen_308.level_y+this.gen_308.bedrock_barrier+this.gen_308.subfloor+1);
			gen_tpl.add(122, "mall",   "Mall",   this.gen_122.level_y+this.gen_122.bedrock_barrier+this.gen_122.subfloor+1);
			gen_tpl.add( 39, "metro",  "Metro",  this.gen_039.level_y+this.gen_039.bedrock_barrier+this.gen_039.subfloor+1);
			gen_tpl.add( 11, "city",   "Concrete Jungle"                                                                  );
			gen_tpl.commit();
		}
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
		if (y < this.getMaxY(308)) return 308; // ikea
		if (y < this.getMaxY(122)) return 122; // mall
		if (y < this.getMaxY( 39)) return  39; // metro
		return 11;                             // city
	}

	@Override
	public boolean containsLevel(final int level) {
		switch (level) {
		case   4: // office
		case 264: // museum
		case 308: // ikea
		case 122: // mall
		case  39: // metro
		case  11: // city
		case  40: // arcade
			return true;
		default: return false;
		}
	}



	@Override
	public int getY(final int level) {
		switch (level) {
		case   4: return this.gen_004.level_y; // office
		case 264: return this.gen_264.level_y; // museum
		case 308: return this.gen_308.level_y; // ikea
		case 122: return this.gen_122.level_y; // mall
		case  39: return this.gen_039.level_y; // metro
		case  11: return this.gen_011.level_y; // city
		case  40: return this.gen_040.level_y; // arcade
		default: throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		}
	}
	@Override
	public int getMaxY(final int level) {
		switch (level) {
		case   4: return this.gen_004.getNextY(); // office
		case 264: return this.gen_264.getNextY(); // museum
		case 308: return this.gen_308.getNextY(); // ikea
		case 122:                                 // mall
		case  39: return this.gen_039.getNextY(); // metro
		case  11: return 320;                     // city
		case  40: return this.gen_040.getNextY(); // arcade
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
		this.gen_308.generate(pregen, plots, chunk, chunkX, chunkZ); // ikea
		if (chunkX >-4 && chunkZ >-4
		&&  chunkX < 4 && chunkZ < 4) {
			// arcade
			this.gen_040.generate(null, null, chunk, chunkX, chunkZ);
		} else {
			// mall
			this.gen_122.generate(pregen, plots, chunk, chunkX, chunkZ); // mall
		}
		this.gen_039.generate(pregen, plots, chunk, chunkX, chunkZ); // metro
		this.gen_011.generate(pregen, plots, chunk, chunkX, chunkZ); // city
	}



}
