package com.poixson.backrooms.gens;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.generator.LimitedRegion;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.BackroomsPop;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.tools.xRand;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.noise.FastNoiseLiteD;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.tools.plotter.generation.TreeBuilder;
import com.poixson.tools.plotter.placer.BlockPlacer;


// 309 | Radio Station
public class Pop_309 implements BackroomsPop {

	protected final BackroomsPlugin plugin;
	protected final Level_000 level_000;
	protected final Gen_309 gen_309;

	protected final TreeBuilder builder_trees;

	protected final FastNoiseLiteD noiseTreePlacement;

	protected final xRand rnd_berm = (new xRand()).seed_time();



	public Pop_309(final Level_000 level_000) {
		this.plugin    = level_000.getPlugin();
		this.level_000 = level_000;
		this.gen_309   = level_000.gen_309;
		this.noiseTreePlacement = this.gen_309.noiseTreePlacement;
		// tree builder
		final int open_y = this.gen_309.getOpenY();
		this.builder_trees =
			(new TreeBuilder(
				this.gen_309.tree_style,
				open_y,
				open_y + this.gen_309.ground_thickness_max
			));
		this.rnd_berm.weight(this.gen_309.path_berm_weight);
		if (this.gen_309.tree_height_min              != Double.MIN_VALUE) this.builder_trees.setHeightMin(           this.gen_309.tree_height_min             );
		if (this.gen_309.tree_height_max              != Double.MIN_VALUE) this.builder_trees.setHeightMax(           this.gen_309.tree_height_max             );
		if (this.gen_309.tree_height_weight           != Double.MIN_VALUE) this.builder_trees.setHeightWeight(        this.gen_309.tree_height_weight          );
		if (this.gen_309.tree_trunk_size_min          != Double.MIN_VALUE) this.builder_trees.setTrunkSizeMin(        this.gen_309.tree_trunk_size_min         );
		if (this.gen_309.tree_trunk_size_max          != Double.MIN_VALUE) this.builder_trees.setTrunkSizeMax(        this.gen_309.tree_trunk_size_max         );
		if (this.gen_309.tree_trunk_size_factor       != Double.MIN_VALUE) this.builder_trees.setTrunkSizeFactor(     this.gen_309.tree_trunk_size_factor      );
		if (this.gen_309.tree_trunk_size_modify_min   != Double.MIN_VALUE) this.builder_trees.setTrunkSizeModifyMin(  this.gen_309.tree_trunk_size_modify_min  );
		if (this.gen_309.tree_trunk_size_modify_max   != Double.MIN_VALUE) this.builder_trees.setTrunkSizeModifyMax(  this.gen_309.tree_trunk_size_modify_max  );
		if (this.gen_309.tree_branches_from_top       != Double.MIN_VALUE) this.builder_trees.setBranchesFromTop(     this.gen_309.tree_branches_from_top      );
		if (this.gen_309.tree_branch_zone_percent     != Double.MIN_VALUE) this.builder_trees.setBranchZonePercent(   this.gen_309.tree_branch_zone_percent    );
		if (this.gen_309.tree_branch_length_min       != Double.MIN_VALUE) this.builder_trees.setBranchLengthMin(     this.gen_309.tree_branch_length_min      );
		if (this.gen_309.tree_branch_length_max       != Double.MIN_VALUE) this.builder_trees.setBranchLengthMax(     this.gen_309.tree_branch_length_max      );
		if (this.gen_309.tree_branch_length_weight    != Double.MIN_VALUE) this.builder_trees.setBranchLengthWeight(  this.gen_309.tree_branch_length_weight   );
		if (this.gen_309.tree_branch_attenuation_min  != Double.MIN_VALUE) this.builder_trees.setBranchAttenuationMin(this.gen_309.tree_branch_attenuation_min );
		if (this.gen_309.tree_branch_attenuation_max  != Double.MIN_VALUE) this.builder_trees.setBranchAttenuationMax(this.gen_309.tree_branch_attenuation_max );
		if (this.gen_309.tree_branch_tier_len_add_min != Double.MIN_VALUE) this.builder_trees.setBranchTierLenAddMin( this.gen_309.tree_branch_tier_len_add_min);
		if (this.gen_309.tree_branch_tier_len_add_max != Double.MIN_VALUE) this.builder_trees.setBranchTierLenAddMax( this.gen_309.tree_branch_tier_len_add_max);
		if (this.gen_309.tree_branch_tier_space_min   != Double.MIN_VALUE) this.builder_trees.setBranchTierSpaceMin(  this.gen_309.tree_branch_tier_space_min  );
		if (this.gen_309.tree_branch_tier_space_max   != Double.MIN_VALUE) this.builder_trees.setBranchTierSpaceMax(  this.gen_309.tree_branch_tier_space_max  );
		if (this.gen_309.tree_branch_yaw_add_min      != Double.MIN_VALUE) this.builder_trees.setBranchYawAddMin(     this.gen_309.tree_branch_yaw_add_min     );
		if (this.gen_309.tree_branch_yaw_add_max      != Double.MIN_VALUE) this.builder_trees.setBranchYawAddMax(     this.gen_309.tree_branch_yaw_add_max     );
		if (this.gen_309.tree_branch_pitch_min        != Double.MIN_VALUE) this.builder_trees.setBranchPitchMin(      this.gen_309.tree_branch_pitch_min       );
		if (this.gen_309.tree_branch_pitch_max        != Double.MIN_VALUE) this.builder_trees.setBranchPitchMax(      this.gen_309.tree_branch_pitch_max       );
		if (this.gen_309.tree_branch_pitch_modify_min != Double.MIN_VALUE) this.builder_trees.setBranchPitchModifyMin(this.gen_309.tree_branch_pitch_modify_min);
		if (this.gen_309.tree_branch_pitch_modify_max != Double.MIN_VALUE) this.builder_trees.setBranchPitchModifyMax(this.gen_309.tree_branch_pitch_modify_max);
		if (this.gen_309.tree_branch_split_min_length != Double.MIN_VALUE) this.builder_trees.setBranchSplitMinLength(this.gen_309.tree_branch_split_min_length);
		if (this.gen_309.tree_branch_num_splits_min   != Double.MIN_VALUE) this.builder_trees.setBranchNumSplitsMin(  this.gen_309.tree_branch_num_splits_min  );
		if (this.gen_309.tree_branch_num_splits_max   != Double.MIN_VALUE) this.builder_trees.setBranchNumSplitsMax(  this.gen_309.tree_branch_num_splits_max  );
		if (this.gen_309.tree_leaves_thickness        != Double.MIN_VALUE) this.builder_trees.setLeavesThickness(     this.gen_309.tree_leaves_thickness       );
	}



	@Override
	public void populate(final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final LimitedRegion region, final int chunkX, final int chunkZ) {
		if (!this.gen_309.enable_gen) return;
		final int y_min = this.builder_trees.y_min;
		final int y_max = this.builder_trees.y_max - 5;
		final BlockPlotter plot =
			(new BlockPlotter())
			.whd(1, 1, 1);
		plot.type('|', this.gen_309.block_tree_trunk );
		plot.type('-', this.gen_309.block_tree_branch);
		plot.type('#', this.gen_309.block_tree_leaves);
		final BlockPlacer placer = new BlockPlacer(region);
		int count_trees = 0;
		for (int iz=0; iz<16; iz++) {
			final int zz = (chunkZ * 16) + iz;
			LOOP_X:
			for (int ix=0; ix<16; ix++) {
				final int xx = (chunkX * 16) + ix;
				if (this.isTree(xx, zz)) {
					plot.xyz(xx, y_max, zz);
					// search area for path blocks
					final int berm_size = this.rnd_berm.nextInt(this.gen_309.path_berm_min, this.gen_309.path_berm_max);
					final int search_y  = (y_max - y_min) + 5;
					final int half = Math.ceilDiv(berm_size, 2);
					for (int layer=0; layer<=half; layer++) {
						final int max = half - layer;
						final int min = 0 - max;
						for (int iy=-1; iy<search_y; iy++) {
							// west to east
							for (int i=min; i<=max; i++) {
								if (plot.isType(placer, i, iy, min, Material.DIRT_PATH)) continue LOOP_X; // north
								if (plot.isType(placer, i, iy, max, Material.DIRT_PATH)) continue LOOP_X; // south
							}
							// north to south
							for (int i=min+1; i<max; i++) {
								if (plot.isType(placer, min, iy, i, Material.DIRT_PATH)) continue LOOP_X; // west
								if (plot.isType(placer, max, iy, i, Material.DIRT_PATH)) continue LOOP_X; // east
							}
						}
					}
					// place tree
					if (this.builder_trees.run(plot, region))
						count_trees++;
				}
			}
		}
		// special structures
		if (count_trees == 0) {
//TODO: add stairs, doors, and hatches
		}
	}



	protected boolean isTree(final int x, final int z) {
		final int search_noise = 1;
		final double value = this.noiseTreePlacement.getNoise(x, z);
		if (value < 0.8)
			return false;
		for (int iz=0-search_noise; iz<search_noise+1; iz++) {
			final int zz = z + iz;
			for (int ix=0-search_noise; ix<search_noise+1; ix++) {
				if (ix != 0 || iz != 0) {
					if (this.noiseTreePlacement.getNoise(x+ix, zz) >= value)
						return false;
				}
			}
		}
		return true;
	}



}
