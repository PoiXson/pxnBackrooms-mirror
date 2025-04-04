package com.poixson.backrooms.gens;

import static com.poixson.backrooms.gens.Gen_309.DEBUG_GLASS_GRID;
import static com.poixson.backrooms.gens.Gen_309.DEFAULT_BLOCK_FERN_SHORT;
import static com.poixson.backrooms.gens.Gen_309.DEFAULT_BLOCK_FERN_TALL_LOWER;
import static com.poixson.backrooms.gens.Gen_309.DEFAULT_BLOCK_FERN_TALL_UPPER;
import static com.poixson.backrooms.gens.Gen_309.DEFAULT_BLOCK_GRASS_SHORT;
import static com.poixson.backrooms.gens.Gen_309.DEFAULT_BLOCK_GRASS_TALL_LOWER;
import static com.poixson.backrooms.gens.Gen_309.DEFAULT_BLOCK_GRASS_TALL_UPPER;
import static com.poixson.backrooms.gens.Gen_309.DEFAULT_BLOCK_MUSHROOM;
import static com.poixson.backrooms.gens.Gen_309.DEFAULT_BLOCK_TREE_BRANCH;
import static com.poixson.backrooms.gens.Gen_309.DEFAULT_BLOCK_TREE_LEAVES;
import static com.poixson.backrooms.gens.Gen_309.DEFAULT_BLOCK_TREE_TRUNK;
import static com.poixson.utils.BlockUtils.StringToBlockDataDef;
import static com.poixson.utils.MathUtils.DistanceRadial;
import static com.poixson.utils.MathUtils.Remap;
import static com.poixson.utils.Utils.IsEmpty;

import java.util.LinkedList;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.LimitedRegion;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.BackroomsPop;
import com.poixson.backrooms.worlds.BackWorld_000;
import com.poixson.tools.xRand;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.dao.Iabc;
import com.poixson.tools.noise.FastNoiseLiteD;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.tools.plotter.generation.TreeBuilder;
import com.poixson.tools.plotter.placer.BlockPlacer;
import com.poixson.tools.sequences.InnerToOuterSquareXYZ;


// 309 | Radio Station
public class Pop_309 implements BackroomsPop {

	protected final BackroomsPlugin plugin;
	protected final BackWorld_000 world_000;
	protected final Gen_309 gen_309;

	protected final TreeBuilder builder_trees;

	protected final FastNoiseLiteD noiseTreePlacement;

	protected final xRand rnd_berm  = (new xRand()).seed_time();
	protected final xRand rnd_grass = (new xRand()).seed_time().weight(0.75);



	public Pop_309(final BackWorld_000 world_000) {
		this.plugin    = world_000.getPlugin();
		this.world_000 = world_000;
		this.gen_309   = world_000.gen_309;
		this.noiseTreePlacement = this.gen_309.noiseTreePlacement;
		// tree builder
		final int open_y = this.gen_309.getOpenY();
		this.builder_trees =
			(new TreeBuilder(
				this.gen_309.tree_style,
				open_y - 1,
				open_y + this.gen_309.ground_thickness_max + 2
			));
		this.rnd_berm.weight(this.gen_309.path_berm_weight);
		if (this.gen_309.tree_height_min               != Double.MIN_VALUE) this.builder_trees.setHeightMin(            this.gen_309.tree_height_min              );
		if (this.gen_309.tree_height_max               != Double.MIN_VALUE) this.builder_trees.setHeightMax(            this.gen_309.tree_height_max              );
		if (this.gen_309.tree_height_weight            != Double.MIN_VALUE) this.builder_trees.setHeightWeight(         this.gen_309.tree_height_weight           );
		if (this.gen_309.tree_trunk_size_min           != Double.MIN_VALUE) this.builder_trees.setTrunkSizeMin(         this.gen_309.tree_trunk_size_min          );
		if (this.gen_309.tree_trunk_size_max           != Double.MIN_VALUE) this.builder_trees.setTrunkSizeMax(         this.gen_309.tree_trunk_size_max          );
		if (this.gen_309.tree_trunk_size_factor        != Double.MIN_VALUE) this.builder_trees.setTrunkSizeFactor(      this.gen_309.tree_trunk_size_factor       );
		if (this.gen_309.tree_trunk_size_modify_min    != Double.MIN_VALUE) this.builder_trees.setTrunkSizeModifyMin(   this.gen_309.tree_trunk_size_modify_min   );
		if (this.gen_309.tree_trunk_size_modify_max    != Double.MIN_VALUE) this.builder_trees.setTrunkSizeModifyMax(   this.gen_309.tree_trunk_size_modify_max   );
		if (this.gen_309.tree_trunk_size_modify_weight != Double.MIN_VALUE) this.builder_trees.setTrunkSizeModifyWeight(this.gen_309.tree_trunk_size_modify_weight);
		if (this.gen_309.tree_branches_from_top        != Double.MIN_VALUE) this.builder_trees.setBranchesFromTop(      this.gen_309.tree_branches_from_top       );
		if (this.gen_309.tree_branch_zone_percent      != Double.MIN_VALUE) this.builder_trees.setBranchZonePercent(    this.gen_309.tree_branch_zone_percent     );
		if (this.gen_309.tree_branch_length_min        != Double.MIN_VALUE) this.builder_trees.setBranchLengthMin(      this.gen_309.tree_branch_length_min       );
		if (this.gen_309.tree_branch_length_max        != Double.MIN_VALUE) this.builder_trees.setBranchLengthMax(      this.gen_309.tree_branch_length_max       );
		if (this.gen_309.tree_branch_length_weight     != Double.MIN_VALUE) this.builder_trees.setBranchLengthWeight(   this.gen_309.tree_branch_length_weight    );
		if (this.gen_309.tree_branch_attenuation_min   != Double.MIN_VALUE) this.builder_trees.setBranchAttenuationMin( this.gen_309.tree_branch_attenuation_min  );
		if (this.gen_309.tree_branch_attenuation_max   != Double.MIN_VALUE) this.builder_trees.setBranchAttenuationMax( this.gen_309.tree_branch_attenuation_max  );
		if (this.gen_309.tree_branch_tier_len_add_min  != Double.MIN_VALUE) this.builder_trees.setBranchTierLenAddMin(  this.gen_309.tree_branch_tier_len_add_min );
		if (this.gen_309.tree_branch_tier_len_add_max  != Double.MIN_VALUE) this.builder_trees.setBranchTierLenAddMax(  this.gen_309.tree_branch_tier_len_add_max );
		if (this.gen_309.tree_branch_tier_space_min    != Double.MIN_VALUE) this.builder_trees.setBranchTierSpaceMin(   this.gen_309.tree_branch_tier_space_min   );
		if (this.gen_309.tree_branch_tier_space_max    != Double.MIN_VALUE) this.builder_trees.setBranchTierSpaceMax(   this.gen_309.tree_branch_tier_space_max   );
		if (this.gen_309.tree_branch_yaw_add_min       != Double.MIN_VALUE) this.builder_trees.setBranchYawAddMin(      this.gen_309.tree_branch_yaw_add_min      );
		if (this.gen_309.tree_branch_yaw_add_max       != Double.MIN_VALUE) this.builder_trees.setBranchYawAddMax(      this.gen_309.tree_branch_yaw_add_max      );
		if (this.gen_309.tree_branch_pitch_min         != Double.MIN_VALUE) this.builder_trees.setBranchPitchMin(       this.gen_309.tree_branch_pitch_min        );
		if (this.gen_309.tree_branch_pitch_max         != Double.MIN_VALUE) this.builder_trees.setBranchPitchMax(       this.gen_309.tree_branch_pitch_max        );
		if (this.gen_309.tree_branch_pitch_modify_min  != Double.MIN_VALUE) this.builder_trees.setBranchPitchModifyMin( this.gen_309.tree_branch_pitch_modify_min );
		if (this.gen_309.tree_branch_pitch_modify_max  != Double.MIN_VALUE) this.builder_trees.setBranchPitchModifyMax( this.gen_309.tree_branch_pitch_modify_max );
		if (this.gen_309.tree_branch_split_min_length  != Double.MIN_VALUE) this.builder_trees.setBranchSplitMinLength( this.gen_309.tree_branch_split_min_length );
		if (this.gen_309.tree_branch_num_splits_min    != Double.MIN_VALUE) this.builder_trees.setBranchNumSplitsMin(   this.gen_309.tree_branch_num_splits_min   );
		if (this.gen_309.tree_branch_num_splits_max    != Double.MIN_VALUE) this.builder_trees.setBranchNumSplitsMax(   this.gen_309.tree_branch_num_splits_max   );
		if (this.gen_309.tree_leaves_thickness         != Double.MIN_VALUE) this.builder_trees.setLeavesThickness(      this.gen_309.tree_leaves_thickness        );
	}



	@Override
	public void populate(final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final LimitedRegion region, final int chunkX, final int chunkZ) {
		if (!this.gen_309.enable_gen) return;
		final BlockData block_tree_trunk       = StringToBlockDataDef(this.gen_309.block_tree_trunk,       DEFAULT_BLOCK_TREE_TRUNK      );
		final BlockData block_tree_branch      = StringToBlockDataDef(this.gen_309.block_tree_branch,      DEFAULT_BLOCK_TREE_BRANCH     );
		final BlockData block_tree_leaves      = StringToBlockDataDef(this.gen_309.block_tree_leaves,      DEFAULT_BLOCK_TREE_LEAVES     );
		final BlockData block_grass_short      = StringToBlockDataDef(this.gen_309.block_grass_short,      DEFAULT_BLOCK_GRASS_SHORT     );
		final BlockData block_grass_tall_upper = StringToBlockDataDef(this.gen_309.block_grass_tall_upper, DEFAULT_BLOCK_GRASS_TALL_UPPER);
		final BlockData block_grass_tall_lower = StringToBlockDataDef(this.gen_309.block_grass_tall_lower, DEFAULT_BLOCK_GRASS_TALL_LOWER);
		final BlockData block_fern_short       = StringToBlockDataDef(this.gen_309.block_fern_short,       DEFAULT_BLOCK_FERN_SHORT      );
		final BlockData block_fern_tall_upper  = StringToBlockDataDef(this.gen_309.block_fern_tall_upper,  DEFAULT_BLOCK_FERN_TALL_UPPER );
		final BlockData block_fern_tall_lower  = StringToBlockDataDef(this.gen_309.block_fern_tall_lower,  DEFAULT_BLOCK_FERN_TALL_LOWER );
		final BlockData block_mushroom         = StringToBlockDataDef(this.gen_309.block_mushroom,         DEFAULT_BLOCK_MUSHROOM        );
		if (block_tree_trunk       == null) throw new RuntimeException("Invalid block type for level 309 Tree-Trunk"       );
		if (block_tree_branch      == null) throw new RuntimeException("Invalid block type for level 309 Tree-Branch"      );
		if (block_tree_leaves      == null) throw new RuntimeException("Invalid block type for level 309 Tree-Leaves"      );
		if (block_grass_short      == null) throw new RuntimeException("Invalid block type for level 309 Grass-Short"      );
		if (block_grass_tall_upper == null) throw new RuntimeException("Invalid block type for level 309 Grass-Tall-Upper" );
		if (block_grass_tall_lower == null) throw new RuntimeException("Invalid block type for level 309 Grass-Tall-Lower" );
		if (block_fern_short       == null) throw new RuntimeException("Invalid block type for level 309 Fern-Short"       );
		if (block_fern_tall_upper  == null) throw new RuntimeException("Invalid block type for level 309 Fern-Tall-Upper"  );
		if (block_fern_tall_lower  == null) throw new RuntimeException("Invalid block type for level 309 Fern-Tall-Lower"  );
		if (block_mushroom         == null) throw new RuntimeException("Invalid block type for level 309 Mushroom"         );
		final double thresh_grass        = this.gen_309.thresh_grass;
		final double thresh_mushroom     = this.gen_309.thresh_mushroom;
		final double grass_weight_factor = this.gen_309.grass_weight_factor;
		final double grass_berm_percent  = this.gen_309.grass_berm_percent;
		final BlockPlotter plot =
			(new BlockPlotter())
			.whd(1, 1, 1);
		plot.type('|', block_tree_trunk );
		plot.type('-', block_tree_branch);
		plot.type('#', block_tree_leaves);
		final BlockPlacer placer = new BlockPlacer(region);
		int count_trees = 0;
		boolean has_path = false;
		for (int iz=0; iz<16; iz++) {
			final int zz = (chunkZ * 16) + iz;
			for (int ix=0; ix<16; ix++) {
				final int xx = (chunkX * 16) + ix;
				plot.xyz(xx, this.builder_trees.y_min, zz);
				// search area
				final int search_xz = this.gen_309.path_berm_max;
				final int search_y  = this.builder_trees.y_max - this.builder_trees.y_min;
				final double berm_size = this.rnd_berm.nextDouble(this.gen_309.path_berm_min, this.gen_309.path_berm_max);
				double path_dist = ((double)search_xz) * 2.0;
				final InnerToOuterSquareXYZ it = new InnerToOuterSquareXYZ(search_xz, search_y);
				while (it.hasNext()) {
					final Iabc loc = it.next();
					if (plot.isType(placer, loc.a, loc.b, loc.c, Material.DIRT_PATH)) {
						final double dist = DistanceRadial(0, 0, loc.a, loc.c);
						if (path_dist > dist) {
							path_dist = dist;
							has_path = true;
						}
						it.nextXZ();
					}
				}
				// place tree
				if (path_dist > berm_size) {
					if (this.isTree(xx, zz)) {
						if (this.builder_trees.run(plot, region)) {
							count_trees++;
							if (DEBUG_GLASS_GRID) {
								plot.y(200);
								plot.setBlock(placer, 0, 0, 0, Material.OBSIDIAN);
							}
						}
					}
				}
				// grass
				if (path_dist > berm_size * grass_berm_percent) {
					final double value_grass = this.gen_309.noiseGrass.getNoise(xx, zz);
					LOOP_SURFACE:
					for (int iy=search_y; iy>=0; iy--) {
						if (plot.isType(placer, 0, iy, 0, Material.GRASS_BLOCK)) {
							if (plot.isType(placer, 0, iy+1, 0, Material.AIR)) {
								// grass
								if (value_grass > thresh_grass) {
									final int num_types = 4;
									// remap weight from noise value
									this.rnd_grass.weight(
										Remap(
											this.gen_309.thresh_grass, 1.0,
											this.gen_309.grass_weight_factor*2.0, 0.0,
											value_grass
										)
									);
									final int grass_type = this.rnd_grass.nextInt(0, this.gen_309.grass_short_bias+3);
									SWITCH_GRASS:
									switch (grass_type) {
									// tall grass
									case 0:
										if (plot.isType(placer, 0, iy+2, 0, Material.AIR)) {
											plot.setBlock(placer, 0, iy+2, 0, block_grass_tall_upper);
											plot.setBlock(placer, 0, iy+1, 0, block_grass_tall_lower);
											break SWITCH_GRASS;
										}
									// tall fern
									case 1:
										if (plot.isType(placer, 0, iy+2, 0, Material.AIR)) {
											plot.setBlock(placer, 0, iy+2, 0, block_fern_tall_upper);
											plot.setBlock(placer, 0, iy+1, 0, block_fern_tall_lower);
											break SWITCH_GRASS;
										}
									// short fern
									case 2:
										plot.setBlock(placer, 0, iy+1, 0, block_fern_short);
										break SWITCH_GRASS;
									default: {
										final double value_mushroom = this.gen_309.noiseMushroom.getNoise(xx, zz);
										// brown mushroom
										if (value_mushroom > this.gen_309.thresh_mushrooms) {
											final int mod_mush = ((int)Math.floor(value_mushroom * 1000.0)) % 42;
											if (mod_mush == 0)
												plot.setBlock(placer, 0, iy+1, 0, block_mushroom);
										// short grass
										} else {
											plot.setBlock(placer, 0, iy+1, 0, block_grass_short);
										}
										break SWITCH_GRASS;
									}
									} // end SWITCH_GRASS
								} // end grass thresh
							} // end found air above grass
							break LOOP_SURFACE;
						} // end grass block
					} // end search y loop
				} // end grass
			} // end ix
		} // end iz
		// special structures
		if (count_trees == 0
		&& !has_path) {
			final int maze_x = Math.floorDiv(chunkX*16, this.gen_309.cell_size);
			final int maze_z = Math.floorDiv(chunkZ*16, this.gen_309.cell_size);
			final Map<String, Object> keyval = this.world_000.radio_stations.getKeyValMap(maze_x, maze_z, false, true);
			if (keyval != null
			&& IsEmpty((String)keyval.get("structure"))) {
//TODO: add stairs, doors, and hatches
			}
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
