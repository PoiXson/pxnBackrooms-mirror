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
import static com.poixson.utils.Utils.IsEmpty;

import java.util.LinkedList;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.LimitedRegion;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.BackroomsPop;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.tools.xRand;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.dao.Iabc;
import com.poixson.tools.noise.FastNoiseLiteD;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.tools.plotter.generation.TreeBuilder;
import com.poixson.tools.plotter.placer.BlockPlacer;
import com.poixson.tools.sequences.InnerToOuterSquareXYZ;
import com.poixson.utils.MathUtils;


// 309 | Radio Station
public class Pop_309 implements BackroomsPop {

	protected final BackroomsPlugin plugin;
	protected final Level_000 level_000;
	protected final Gen_309 gen_309;

	protected final TreeBuilder builder_trees;

	protected final FastNoiseLiteD noiseTreePlacement;

	protected final xRand rnd_berm = (new xRand()).seed_time();
	protected final xRand rnd_grass = (new xRand()).seed_time().weight(0.75);



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
		final BlockData block_grass_short      = StringToBlockDataDef(this.gen_309.block_grass_short,      DEFAULT_BLOCK_GRASS_SHORT     );
		final BlockData block_grass_tall_upper = StringToBlockDataDef(this.gen_309.block_grass_tall_upper, DEFAULT_BLOCK_GRASS_TALL_UPPER);
		final BlockData block_grass_tall_lower = StringToBlockDataDef(this.gen_309.block_grass_tall_lower, DEFAULT_BLOCK_GRASS_TALL_LOWER);
		final BlockData block_fern_short       = StringToBlockDataDef(this.gen_309.block_fern_short,       DEFAULT_BLOCK_FERN_SHORT      );
		final BlockData block_fern_tall_upper  = StringToBlockDataDef(this.gen_309.block_fern_tall_upper,  DEFAULT_BLOCK_FERN_TALL_UPPER );
		final BlockData block_fern_tall_lower  = StringToBlockDataDef(this.gen_309.block_fern_tall_lower,  DEFAULT_BLOCK_FERN_TALL_LOWER );
		final BlockData block_mushroom         = StringToBlockDataDef(this.gen_309.block_mushroom,         DEFAULT_BLOCK_MUSHROOM        );
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
										MathUtils.Remap(
											thresh_grass, 1.0,
											0.0-grass_weight_factor, grass_weight_factor,
											value_grass
										)
									);
									final int grass_type = this.rnd_grass.nextInt(0, num_types-1);
									SWITCH_GRASS:
									switch (grass_type) {
									// tall fern
									case 2:
										if (plot.isType(placer, 0, iy+2, 0, Material.AIR)) {
											plot.setBlock(placer, 0, iy+2, 0, block_fern_tall_upper);
											plot.setBlock(placer, 0, iy+1, 0, block_fern_tall_lower);
											break SWITCH_GRASS;
										}
									// short fern
									case 1:
										plot.setBlock(placer, 0, iy+1, 0, block_fern_short);
										break SWITCH_GRASS;
									// tall grass
									case 3:
										if (plot.isType(placer, 0, iy+2, 0, Material.AIR)) {
											plot.setBlock(placer, 0, iy+2, 0, block_grass_tall_upper);
											plot.setBlock(placer, 0, iy+1, 0, block_grass_tall_lower);
											break SWITCH_GRASS;
										}
									// short grass
									case 0:
									default:
										plot.setBlock(placer, 0, iy+1, 0, block_grass_short);
										break SWITCH_GRASS;
									} // end SWITCH_GRASS
								// not grass
								} else
								if (value_grass < thresh_mushroom) {
									plot.setBlock(placer, 0, iy+1, 0, 'm');
								}
							} // end found air above grass
							break LOOP_SURFACE;
						} // end grass block
					} // end search y loop
				} // end grass
			} // end ix
		} // end iz
			}
		}
		// special structures
		if (count_trees == 0) {
			final int maze_x = Math.floorDiv(chunkX*16, this.gen_309.cell_size);
			final int maze_z = Math.floorDiv(chunkZ*16, this.gen_309.cell_size);
			final Map<String, Object> keyval = this.level_000.radio_stations.getKeyValMap(maze_x, maze_z, false, true);
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
