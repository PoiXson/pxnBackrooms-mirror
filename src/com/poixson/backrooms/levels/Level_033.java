package com.poixson.backrooms.levels;

import static com.poixson.utils.RandomUtils.Rnd10K;

import org.bukkit.Location;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.dynmap.GeneratorTemplate;


// 33 | Run For Your Life!
public class Level_033 extends LevelBackrooms {

	public static final int SUBFLOOR   = 3;
	public static final int SUBCEILING = 3;

	public static final int LEVEL_Y = 0;
	public static final int LEVEL_H = 8;

	// generators
	public final Gen_033 gen;



	public Level_033(final BackroomsPlugin plugin) {
		super(plugin, 33);
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 0);
			gen_tpl.add(33, "run", "Run For Your Life", LEVEL_Y+LEVEL_H+SUBFLOOR+1);
		}
		// generators
		this.gen = this.register(new Gen_033(plugin, LEVEL_Y, LEVEL_H, SUBFLOOR, SUBCEILING));
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
		return LEVEL_Y + LEVEL_H;
	}



	@Override
	protected void generate(final ChunkData chunk, final int chunkX, final int chunkZ) {
		this.gen.generate(null, chunk, chunkX, chunkZ);
	}



}
