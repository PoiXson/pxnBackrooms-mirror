package com.poixson.backrooms.levels;

import java.util.LinkedList;

import org.bukkit.Material;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.dynmap.GeneratorTemplate;
import com.poixson.commonmc.tools.plotter.BlockPlotter;


// 866 | Dirtfield
public class Level_866 extends LevelBackrooms {

	public static final boolean ENABLE_GEN_866 = true;
	public static final boolean ENABLE_TOP_866 = true;

	public static final int LEVEL_Y = 0;
	public static final int SUBFLOOR = 3;

	public static final Material GROUND      = Material.RED_SANDSTONE;
	public static final Material GROUND_SLAB = Material.RED_SANDSTONE_SLAB;

	// generators
	public final Gen_866 gen;



	public Level_866(final BackroomsPlugin plugin) {
		super(plugin, 866);
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 0);
			gen_tpl.add(866, "dirtfield", "Dirtfield");
		}
		// generators
		this.gen = this.register(new Gen_866(this, LEVEL_Y, 0));
	}



	@Override
	public int getY(final int level) {
		return LEVEL_Y;
	}
	@Override
	public int getMaxY(final int level) {
		return LEVEL_Y + 20;
	}
	@Override
	public boolean containsLevel(final int level) {
		return (level == 866);
	}



	@Override
	protected void generate(final int chunkX, final int chunkZ,
			final ChunkData chunk, final LinkedList<BlockPlotter> plots) {
		this.gen.generate(null, chunk, plots, chunkX, chunkZ);
	}



}
