package com.poixson.backrooms.levels;

import org.bukkit.Material;

import com.poixson.commonmc.tools.TreePopulator;


public class Pop_309_Trees extends TreePopulator {

	public static final boolean ENABLED = Gen_309.ENABLED;

	public static final int SUBFLOOR      = Gen_309.SUBFLOOR;
	public static final int PATH_Y        = Gen_309.PATH_Y;
	public static final int PATH_WIDTH    = Gen_309.PATH_WIDTH;
	public static final int PATH_CLEARING = Gen_309.PATH_CLEARING;

	public static final Material PATH_TREE_TRUNK  = Gen_309.PATH_TREE_TRUNK;
	public static final Material PATH_TREE_LEAVES = Gen_309.PATH_TREE_LEAVES;

	protected final Gen_309 gen_309;



	public Pop_309_Trees(final Gen_309 gen_309) {
		super(
			gen_309.getTreeNoise(),
			PATH_Y,
			PATH_TREE_TRUNK,
			PATH_TREE_LEAVES
		);
		this.gen_309 = gen_309;
	}



	@Override
	public boolean isTree(final int x, final int z) {
		if (!ENABLED)                                           return false;
		if (!super.isTree(x, z))                                return false;
		if (this.gen_309.isCenterClearing(x, z))                return false;
		if (this.gen_309.pathTrace.isPath(x, z, PATH_CLEARING)) return false;
		return true;
	}



}
