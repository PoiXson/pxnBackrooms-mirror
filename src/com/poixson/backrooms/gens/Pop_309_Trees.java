package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_000.SUBFLOOR;

import org.bukkit.Material;

import com.poixson.tools.TreePopulator;
import com.poixson.utils.FastNoiseLiteD;


// 309 | Radio Station
public class Pop_309_Trees extends TreePopulator {

	protected final Gen_309 gen;

	protected final int path_clearing;



	public Pop_309_Trees(final Gen_309 gen) {
		super(
			gen.getTreeNoise(),
			gen.level_y + SUBFLOOR + 1,
			Material.matchMaterial(gen.block_tree_trunk.get()),
			Material.matchMaterial(gen.block_tree_leaves.get())
		);
		this.gen = gen;
		this.path_clearing = gen.path_clearing.get();
	}



	@Override
	public boolean isTree(final int x, final int z) {
		if (!this.gen_309.enable_gen) return false;
		if (!this.gen_309.enable_top) return false;
		if (!super.isTree(x, z)) return false;
		if (this.gen.getCenterClearingDistance(x, z, 8.0) < 80.0) return false;
		if (this.gen.pathTrace.isPath(x, z, this.path_clearing))  return false;
		final double thresh_prairie = this.gen.thresh_prairie.get();
		final FastNoiseLiteD noisePrairie = this.gen.getPrairieNoise();
		final double value = noisePrairie.getNoise(x, z);
		if (value > thresh_prairie)
			return false;
		return true;
	}



}
