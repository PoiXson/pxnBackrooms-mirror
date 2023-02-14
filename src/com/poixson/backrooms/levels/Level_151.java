package com.poixson.backrooms.levels;

import org.bukkit.Location;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.dynmap.GeneratorTemplate;


// 151 | Dollhouse
public class Level_151 extends LevelBackrooms {

	public static final int SUBFLOOR   = 3;
	public static final int SUBCEILING = 3;

	public static final int LEVEL_Y = 100;
	public static final int LEVEL_H = 100;

	// generators
	public final Gen_151 gen;



	public Level_151(final BackroomsPlugin plugin) {
		super(plugin, 151);
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 0);
			gen_tpl.add(151, "dollhouse", "Dollhouse", LEVEL_Y+LEVEL_H+SUBFLOOR+1);
		}
		// generators
		this.gen = this.register(new Gen_151(plugin, LEVEL_Y, LEVEL_H, SUBFLOOR, SUBCEILING));
	}



//TODO
	@Override
	public Location getSpawn(final int level) {
		return this.getSpawn(level, 0, 0);
	}
	@Override
	public Location getSpawn(final int level, final int x, final int z) {
		return this.getSpawn(level, 255, x, LEVEL_Y+SUBFLOOR, z);
	}

	@Override
	public int getY(final int level) {
		return LEVEL_Y;
	}
	@Override
	public int getMaxY(final int level) {
		return 319;
	}



	@Override
	protected void generate(final ChunkData chunk, final int chunkX, final int chunkZ) {
		this.gen.generate(null, chunk, chunkX, chunkZ);
	}



}
