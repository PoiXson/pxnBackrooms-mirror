/*
package com.poixson.backrooms.worlds;

import java.util.LinkedList;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.BackroomsWorld;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.dynmap.GeneratorTemplate;
import com.poixson.backrooms.gens.Gen_011;
import com.poixson.backrooms.gens.Gen_039;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.plotter.BlockPlotter;


//  11 | City - Concrete Jungle
//  39 | Metro
public class Level_011 extends BackroomsWorld {

	// generators
	public final Gen_039 gen_039;
	public final Gen_011 gen_011;

	// populators
//	public final Pop_011 pop_011;



	public Level_011(final BackroomsPlugin plugin)
			throws InvalidConfigurationException {
		super(plugin);
		// generators
		this.gen_039 = this.register(new Gen_039(this, this.seed              )); // metro
		this.gen_011 = this.register(new Gen_011(this, this.seed, this.gen_039)); // city
		// populators
//		this.pop_011 = this.register(new Pop_011(this)); // city
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 11);
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
		return this.getLevel(y);
	}
	@Override
	public int getLevel(final int y) {
		if (y < this.getMaxY(39)) return 39; // metro
		return 11;                           // city
	}

	@Override
	public boolean containsLevel(final int level) {
		switch (level) {
		case 11: // city
		case 39: // metro
			return true;
		default: return false;
		}
	}
	@Override
	public int[] getLevels() {
		return new int[] {
			11, // city
			39, // metro
		};
	}



	@Override
	public int getY(final int level) {
		switch (level) {
		case 39: return this.gen_039.level_y; // metro
		case 11: return this.gen_011.level_y; // city
		default: throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		}
	}
	@Override
	public int getMaxY(final int level) {
		switch (level) {
		case 39: return this.gen_039.getNextY(); // metro
		case 11: return 320;                     // city
		default: throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		}
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
		this.gen_039.generate(pregen, plots, chunk, chunkX, chunkZ); // metro
		this.gen_011.generate(pregen, plots, chunk, chunkX, chunkZ); // city
	}



}
*/
