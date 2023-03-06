package com.poixson.backrooms.levels;

import static com.poixson.utils.RandomUtils.Rnd10K;

import java.util.LinkedList;

import org.bukkit.Location;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.dynmap.GeneratorTemplate;
import com.poixson.commonmc.tools.plotter.BlockPlotter;


// 9 | Suburbs
public class Level_009 extends LevelBackrooms {

	public static final int LEVEL_Y = 0;

	// generators
	public final Gen_009 gen;



	public Level_009(final BackroomsPlugin plugin) {
		super(plugin, 9);
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 0);
			gen_tpl.add(9, "suburbs", "Suburbs");
		}
		// generators
		this.gen = this.register(new Gen_009(this, LEVEL_Y, 0));
	}



	@Override
	public Location getSpawn(final int level) {
		final int x = (Rnd10K() * 2) - 10000;
		final int z = (Rnd10K() * 2) - 10000;
		return this.getSpawn(level, x, z);
	}
	@Override
	public Location getSpawn(final int level, final int x, final int z) {
		return this.getSpawn(level, 10, x, LEVEL_Y, z);
	}

	@Override
	public int getY(final int level) {
		return LEVEL_Y;
	}
	@Override
	public int getMaxY(final int level) {
		return 255;
	}



	@Override
	protected void generate(final int chunkX, final int chunkZ,
			final ChunkData chunk, final LinkedList<BlockPlotter> plots) {
		this.gen.generate(null, chunk, plots, chunkX, chunkZ);
	}



}
