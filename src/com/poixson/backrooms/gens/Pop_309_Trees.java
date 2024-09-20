package com.poixson.backrooms.gens;

import org.bukkit.Material;

import com.poixson.tools.FastNoiseLiteD;
import com.poixson.tools.TreePopulator;


// 309 | Radio Station
public class Pop_309_Trees extends TreePopulator {

	protected final Gen_309 gen_309;

	protected final int path_clearing;



	public Pop_309_Trees(final Gen_309 gen_309) {
		super(
			gen_309.getTreeNoise(),
			gen_309.level_y + gen_309.subfloor + 1,
			Material.matchMaterial(gen_309.block_tree_trunk ),
			Material.matchMaterial(gen_309.block_tree_leaves)
		);
		this.gen_309 = gen_309;
		this.path_clearing = gen_309.path_clearing;
	}



	@Override
	public boolean isTree(final int x, final int z) {
		if (!this.gen_309.enable_gen) return false;
		if (!this.gen_309.enable_top) return false;
		if (!super.isTree(x, z))  return false;
		if (this.gen_309.getCenterClearingDistance(x, z, 8.0) < 80.0) return false;
		if (this.gen_309.pathTrace.isPath(x, z, this.path_clearing))  return false;
		final FastNoiseLiteD noisePrairie = this.gen_309.getPrairieNoise();
		final double value = noisePrairie.getNoise(x, z);
		if (value > this.gen_309.thresh_prairie)
			return false;
		return true;
	}



}
