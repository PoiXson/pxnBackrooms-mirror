package com.poixson.backrooms.levels;

import org.bukkit.Material;

import com.poixson.commonmc.tools.TreePopulator;


public class Pop_309_Trees extends TreePopulator {

	public static final int PATH_WIDTH    = Gen_309.PATH_WIDTH;
	public static final int PATH_CLEARING = Gen_309.PATH_CLEARING;

	public static final Material PATH_TREE_TRUNK  = Gen_309.PATH_TREE_TRUNK;
	public static final Material PATH_TREE_LEAVES = Gen_309.PATH_TREE_LEAVES;

	protected final Gen_309 gen;



	public Pop_309_Trees(final Gen_309 gen) {
		super(
			gen.getTreeNoise(),
			gen.level_y + gen.subfloor + 1,
			PATH_TREE_TRUNK,
			PATH_TREE_LEAVES
		);
		this.gen = gen;
	}



	@Override
	public boolean isTree(final int x, final int z) {
		if (!super.isTree(x, z))                            return false;
		if (this.gen.isCenterClearing(x, z))                return false;
		if (this.gen.pathTrace.isPath(x, z, PATH_CLEARING)) return false;
		return true;
	}



}
