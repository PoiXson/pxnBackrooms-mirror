package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_000.ENABLE_GEN_309;
import static com.poixson.backrooms.worlds.Level_000.ENABLE_TOP_309;
import static com.poixson.backrooms.worlds.Level_000.SUBFLOOR;

import org.bukkit.Material;

import com.poixson.commonmc.tools.TreePopulator;


public class Pop_309_Trees extends TreePopulator {

	public static final int PATH_WIDTH    = Gen_309.PATH_WIDTH;
	public static final int PATH_CLEARING = Gen_309.PATH_CLEARING;

	protected final Gen_309 gen;



	public Pop_309_Trees(final Gen_309 gen) {
		super(
			gen.getTreeNoise(),
			gen.level_y + SUBFLOOR + 1,
			Material.matchMaterial(gen.block_tree_trunk.get()),
			Material.matchMaterial(gen.block_tree_leaves.get())
		);
		this.gen = gen;
	}



	@Override
	public boolean isTree(final int x, final int z) {
		if (!ENABLE_GEN_309)     return false;
		if (!ENABLE_TOP_309)     return false;
		if (!super.isTree(x, z)) return false;
		if (this.gen.getCenterClearingDistance(x, z, 8.0) < 80.0) return false;
		if (this.gen.pathTrace.isPath(x, z, PATH_CLEARING))       return false;
		return true;
	}



}
