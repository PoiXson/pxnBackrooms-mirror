package com.poixson.backrooms.gens;

import static com.poixson.utils.BlockUtils.StringToBlockDataDef;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsWorld;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.gens.Gen_000.LobbyData;
import com.poixson.backrooms.gens.Gen_001.BasementData;
import com.poixson.backrooms.worlds.BackWorld_000.Pregen_Level_000;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.dao.Iab;
import com.poixson.tools.plotter.BlockPlotter;


// 23 | Overgrowth
public class Gen_023 extends BackroomsGen {

	// default params
	public static final int DEFAULT_LEVEL_H       = 9;
	public static final int DEFAULT_SUBFLOOR      = 3;
	public static final int DEFAULT_SUBCEILING    = 3;
	public static final int DEFAULT_GRASS_MODULUS = 25;

	// default blocks
	public static final String DEFAULT_BLOCK_SUBFLOOR_WET         = "minecraft:mud";
	public static final String DEFAULT_BLOCK_SUBFLOOR_DRY         = "minecraft:dirt";
	public static final String DEFAULT_BLOCK_FLOOR_WET            = "minecraft:moss_block";
	public static final String DEFAULT_BLOCK_FLOOR_DRY            = "minecraft:moss_block";
	public static final String DEFAULT_BLOCK_SUBCEILING           = "minecraft:stone";
	public static final String DEFAULT_BLOCK_CEILING              = "minecraft:moss_block";
	public static final String DEFAULT_BLOCK_CEILING_GRASS        = "minecraft:hanging_roots";
	public static final String DEFAULT_BLOCK_CEILING_FLOWER       = "minecraft:spore_blossom";
	public static final String DEFAULT_BLOCK_WALL                 = "minecraft:moss_block";
	public static final String DEFAULT_BLOCK_WALL_GRASSY          = "minecraft:sculk_shrieker";
	public static final String DEFAULT_BLOCK_GRASS_WET_SHORT      = "minecraft:dead_bush";
	public static final String DEFAULT_BLOCK_GRASS_DRY_SHORT      = "minecraft:short_grass";
	public static final String DEFAULT_BLOCK_GRASS_WET_TALL_LOWER = "minecraft:small_dripleaf[half=lower]";
	public static final String DEFAULT_BLOCK_GRASS_WET_TALL_UPPER = "minecraft:small_dripleaf[half=upper]";
	public static final String DEFAULT_BLOCK_GRASS_DRY_TALL_LOWER = "minecraft:tall_grass[half=lower]";
	public static final String DEFAULT_BLOCK_GRASS_DRY_TALL_UPPER = "minecraft:tall_grass[half=upper]";

	// params
	public final boolean enable_gen;
	public final boolean enable_top;
	public final int     level_y;
	public final int     level_h;
	public final int     subfloor;
	public final int     subceiling;
	public final int     grass_modulus;

	// blocks
	public final String block_subfloor_wet;
	public final String block_subfloor_dry;
	public final String block_floor_wet;
	public final String block_floor_dry;
	public final String block_subceiling;
	public final String block_ceiling;
	public final String block_ceiling_grass;
	public final String block_ceiling_flower;
	public final String block_wall;
	public final String block_wall_grassy;
	public final String block_grass_wet_short;
	public final String block_grass_dry_short;
	public final String block_grass_wet_tall_lower;
	public final String block_grass_wet_tall_upper;
	public final String block_grass_dry_tall_lower;
	public final String block_grass_dry_tall_upper;

	protected final AtomicInteger inc_grass = new AtomicInteger(0);



	public Gen_023(final BackroomsWorld backworld, final int seed, final BackroomsGen gen_below) {
		super(backworld, gen_below, seed);
		final int level_number = this.getLevelNumber();
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(level_number);
		final ConfigurationSection cfgBlocks = this.plugin.getConfigLevelBlocks(level_number);
		// params
		this.enable_gen    = cfgParams.getBoolean("Enable-Gen"   );
		this.enable_top    = cfgParams.getBoolean("Enable-Top"   );
		this.level_y       = cfgParams.getInt(    "Level-Y"      );
		this.level_h       = cfgParams.getInt(    "Level-Height" );
		this.subfloor      = cfgParams.getInt(    "SubFloor"     );
		this.subceiling    = cfgParams.getInt(    "SubCeiling"   );
		this.grass_modulus = cfgParams.getInt(    "Grass-Modulus");
		// block types
		this.block_subfloor_wet         = cfgBlocks.getString("SubFloor-Wet"        );
		this.block_subfloor_dry         = cfgBlocks.getString("SubFloor-Dry"        );
		this.block_floor_wet            = cfgBlocks.getString("Floor-Wet"           );
		this.block_floor_dry            = cfgBlocks.getString("Floor-Dry"           );
		this.block_subceiling           = cfgBlocks.getString("SubCeiling"          );
		this.block_ceiling              = cfgBlocks.getString("Ceiling"             );
		this.block_ceiling_grass        = cfgBlocks.getString("Ceiling-Grass"       );
		this.block_ceiling_flower       = cfgBlocks.getString("Ceiling-Flower"      );
		this.block_wall                 = cfgBlocks.getString("Wall"                );
		this.block_wall_grassy          = cfgBlocks.getString("Wall-Grassy"         );
		this.block_grass_wet_short      = cfgBlocks.getString("Grass-Wet-Short"     );
		this.block_grass_dry_short      = cfgBlocks.getString("Grass-Dry-Short"     );
		this.block_grass_wet_tall_lower = cfgBlocks.getString("Grass-Wet-Tall-Lower");
		this.block_grass_wet_tall_upper = cfgBlocks.getString("Grass-Wet-Tall-Upper");
		this.block_grass_dry_tall_lower = cfgBlocks.getString("Grass-Dry-Tall-Lower");
		this.block_grass_dry_tall_upper = cfgBlocks.getString("Grass-Dry-Tall-Upper");
	}



	@Override
	public int getLevelNumber() {
		return 23;
	}



	@Override
	public int getLevelY() {
		return this.level_y;
	}
	@Override
	public int getOpenY() {
		return this.getMinY() + this.subfloor + 1;
	}

	@Override
	public int getMinY() {
		return this.getLevelY() + this.bedrock_barrier;
	}
	@Override
	public int getMaxY() {
		return this.getMinY() + this.subfloor + this.level_h + this.subceiling + 1;
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!this.enable_gen) return;
		final BlockData block_subfloor_wet         = StringToBlockDataDef(this.block_subfloor_wet,         DEFAULT_BLOCK_SUBFLOOR_WET        );
		final BlockData block_subfloor_dry         = StringToBlockDataDef(this.block_subfloor_dry,         DEFAULT_BLOCK_SUBFLOOR_DRY        );
		final BlockData block_floor_wet            = StringToBlockDataDef(this.block_floor_wet,            DEFAULT_BLOCK_FLOOR_WET           );
		final BlockData block_floor_dry            = StringToBlockDataDef(this.block_floor_dry,            DEFAULT_BLOCK_FLOOR_DRY           );
		final BlockData block_subceiling           = StringToBlockDataDef(this.block_subceiling,           DEFAULT_BLOCK_SUBCEILING          );
		final BlockData block_ceiling              = StringToBlockDataDef(this.block_ceiling,              DEFAULT_BLOCK_CEILING             );
		final BlockData block_ceiling_grass        = StringToBlockDataDef(this.block_ceiling_grass,        DEFAULT_BLOCK_CEILING_GRASS       );
		final BlockData block_ceiling_flower       = StringToBlockDataDef(this.block_ceiling_flower,       DEFAULT_BLOCK_CEILING_FLOWER      );
		final BlockData block_wall                 = StringToBlockDataDef(this.block_wall,                 DEFAULT_BLOCK_WALL                );
		final BlockData block_wall_grassy          = StringToBlockDataDef(this.block_wall_grassy,          DEFAULT_BLOCK_WALL_GRASSY         );
		final BlockData block_grass_wet_short      = StringToBlockDataDef(this.block_grass_wet_short,      DEFAULT_BLOCK_GRASS_WET_SHORT     );
		final BlockData block_grass_dry_short      = StringToBlockDataDef(this.block_grass_dry_short,      DEFAULT_BLOCK_GRASS_DRY_SHORT     );
		final BlockData block_grass_wet_tall_lower = StringToBlockDataDef(this.block_grass_wet_tall_lower, DEFAULT_BLOCK_GRASS_WET_TALL_LOWER);
		final BlockData block_grass_wet_tall_upper = StringToBlockDataDef(this.block_grass_wet_tall_upper, DEFAULT_BLOCK_GRASS_WET_TALL_UPPER);
		final BlockData block_grass_dry_tall_lower = StringToBlockDataDef(this.block_grass_dry_tall_lower, DEFAULT_BLOCK_GRASS_DRY_TALL_LOWER);
		final BlockData block_grass_dry_tall_upper = StringToBlockDataDef(this.block_grass_dry_tall_upper, DEFAULT_BLOCK_GRASS_DRY_TALL_UPPER);
		if (block_subfloor_wet         == null) throw new RuntimeException("Invalid block type for level 23 SubFloor"            );
		if (block_subfloor_dry         == null) throw new RuntimeException("Invalid block type for level 23 SubFloor"            );
		if (block_floor_wet            == null) throw new RuntimeException("Invalid block type for level 23 Floor-Wet"           );
		if (block_floor_dry            == null) throw new RuntimeException("Invalid block type for level 23 Floor-Dry"           );
		if (block_subceiling           == null) throw new RuntimeException("Invalid block type for level 23 SubCeiling"          );
		if (block_ceiling              == null) throw new RuntimeException("Invalid block type for level 23 Ceiling"             );
		if (block_ceiling_grass        == null) throw new RuntimeException("Invalid block type for level 23 Ceiling-Grass"       );
		if (block_ceiling_flower       == null) throw new RuntimeException("Invalid block type for level 23 Ceiling-Flower"      );
		if (block_wall                 == null) throw new RuntimeException("Invalid block type for level 23 Wall"                );
		if (block_wall_grassy          == null) throw new RuntimeException("Invalid block type for level 23 Wall-Grassy"         );
		if (block_grass_wet_short      == null) throw new RuntimeException("Invalid block type for level 23 Grass-Wet-Short"     );
		if (block_grass_dry_short      == null) throw new RuntimeException("Invalid block type for level 23 Grass-Dry-Short"     );
		if (block_grass_wet_tall_lower == null) throw new RuntimeException("Invalid block type for level 23 Grass-Wet-Tall-Lower");
		if (block_grass_wet_tall_upper == null) throw new RuntimeException("Invalid block type for level 23 Grass-Wet-Tall-Upper");
		if (block_grass_dry_tall_lower == null) throw new RuntimeException("Invalid block type for level 23 Grass-Dry-Tall-Lower");
		if (block_grass_dry_tall_upper == null) throw new RuntimeException("Invalid block type for level 23 Grass-Dry-Tall-Upper");
		final Pregen_Level_000 pregen_000 = (Pregen_Level_000) pregen;
		final HashMap<Iab, LobbyData>    data_lobby    = pregen_000.lobby;
		final HashMap<Iab, BasementData> data_basement = pregen_000.basement;
		final int h_walls = this.level_h + 2;
		final int y_base  = this.level_y + this.bedrock_barrier;
		final int y_floor = y_base + this.subfloor;
		final int y_ceil  = y_floor + this.level_h + 1;
		for (int iz=0; iz<16; iz++) {
			final int zz = (chunkZ * 16) + iz;
			final int modZ = (zz < 0 ? 1-zz : zz) % 7;
			for (int ix=0; ix<16; ix++) {
				final int xx = (chunkX * 16) + ix;
				final int mod_x = (xx < 0 ? 1-xx : xx) % 7;
				final LobbyData    dao_lobby    = data_lobby   .get(new Iab(ix, iz));
				final BasementData dao_basement = data_basement.get(new Iab(ix, iz));
				// barrier
				for (int iy=0; iy<this.bedrock_barrier; iy++)
					chunk.setBlock(ix, this.level_y+iy, iz, Material.BEDROCK);
				// subfloor
				if (this.subfloor > 0) {
					if (dao_basement.isWet) {
						chunk.setBlock(ix, y_base, iz, Material.WATER);
						for (int iy=1; iy<this.subfloor; iy++)
							chunk.setBlock(ix, y_base+iy, iz, block_subfloor_wet);
					} else {
						for (int iy=0; iy<this.subfloor; iy++)
							chunk.setBlock(ix, y_base+iy, iz, block_subfloor_dry);
					}
				}
				// wall
				if (dao_lobby.isWall) {
					final BlockData blk_wall = (dao_basement.isWet ? block_wall_grassy : block_wall);
					for (int yi=0; yi<h_walls; yi++)
						chunk.setBlock(ix, y_floor+yi, iz, blk_wall);
				// room
				} else {
					// floor
					if (dao_basement.isWet) chunk.setBlock(ix, y_floor, iz, block_floor_wet);
					else                    chunk.setBlock(ix, y_floor, iz, block_floor_dry);
					// grass
					final int inc = this.inc_grass.getAndIncrement();
					final int mod_grass = inc % this.grass_modulus;
					// tall grass
					if (mod_grass == 0 || mod_grass == 5) {
						final BlockData blk_lower = (dao_basement.isWet ? block_grass_wet_tall_lower : block_grass_dry_tall_lower);
						final BlockData blk_upper = (dao_basement.isWet ? block_grass_wet_tall_upper : block_grass_dry_tall_upper);
						if (blk_lower != null) chunk.setBlock(ix, y_floor+1, iz, blk_lower);
						if (blk_upper != null) chunk.setBlock(ix, y_floor+2, iz, blk_upper);
					// short grass
					} else {
						final BlockData blk_grass = (dao_basement.isWet ? block_grass_wet_short : block_grass_dry_short);
						if (blk_grass != null) chunk.setBlock(ix, y_floor+1, iz, blk_grass);
					}
					// ceiling
					if (this.enable_top) {
						if (modZ == 0 && mod_x < 2
						&& dao_lobby.wall_dist > 1) {
							// ceiling lights
							chunk.setBlock(ix, y_ceil,   iz, Material.VERDANT_FROGLIGHT);
							chunk.setBlock(ix, y_ceil-1, iz, Material.WEEPING_VINES    );
						} else {
							// ceiling
							chunk.setBlock(ix, y_ceil, iz, block_ceiling );
							if (mod_grass == 7 && block_ceiling_flower != null) chunk.setBlock(ix, y_ceil-1, iz, block_ceiling_flower);
							else                                                chunk.setBlock(ix, y_ceil-1, iz, block_ceiling_grass );
						}
					}
				}
				// subceiling
				if (this.enable_top) {
					for (int iy=0; iy<this.subceiling; iy++)
						chunk.setBlock(ix, y_ceil+iy+1, iz, block_subceiling);
				}
			} // end ix
		} // end iz
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		cfgParams.addDefault("Enable-Gen",    Boolean.TRUE                          );
		cfgParams.addDefault("Enable-Top",    Boolean.TRUE                          );
		cfgParams.addDefault("Level-Y",       Integer.valueOf(this.getDefaultY()   ));
		cfgParams.addDefault("Level-Height",  Integer.valueOf(DEFAULT_LEVEL_H      ));
		cfgParams.addDefault("SubFloor",      Integer.valueOf(DEFAULT_SUBFLOOR     ));
		cfgParams.addDefault("SubCeiling",    Integer.valueOf(DEFAULT_SUBCEILING   ));
		cfgParams.addDefault("Grass-Modulus", Integer.valueOf(DEFAULT_GRASS_MODULUS));
		// block types
		cfgBlocks.addDefault("SubFloor-Wet",         DEFAULT_BLOCK_SUBFLOOR_WET        );
		cfgBlocks.addDefault("SubFloor-Dry",         DEFAULT_BLOCK_SUBFLOOR_DRY        );
		cfgBlocks.addDefault("Floor-Wet",            DEFAULT_BLOCK_FLOOR_WET           );
		cfgBlocks.addDefault("Floor-Dry",            DEFAULT_BLOCK_FLOOR_DRY           );
		cfgBlocks.addDefault("SubCeiling",           DEFAULT_BLOCK_SUBCEILING          );
		cfgBlocks.addDefault("Ceiling",              DEFAULT_BLOCK_CEILING             );
		cfgBlocks.addDefault("Ceiling-Grass",        DEFAULT_BLOCK_CEILING_GRASS       );
		cfgBlocks.addDefault("Ceiling-Flower",       DEFAULT_BLOCK_CEILING_FLOWER      );
		cfgBlocks.addDefault("Wall",                 DEFAULT_BLOCK_WALL                );
		cfgBlocks.addDefault("Wall-Grassy",          DEFAULT_BLOCK_WALL_GRASSY         );
		cfgBlocks.addDefault("Grass-Wet-Short",      DEFAULT_BLOCK_GRASS_WET_SHORT     );
		cfgBlocks.addDefault("Grass-Dry-Short",      DEFAULT_BLOCK_GRASS_DRY_SHORT     );
		cfgBlocks.addDefault("Grass-Wet-Tall-Lower", DEFAULT_BLOCK_GRASS_WET_TALL_LOWER);
		cfgBlocks.addDefault("Grass-Wet-Tall-Upper", DEFAULT_BLOCK_GRASS_WET_TALL_UPPER);
		cfgBlocks.addDefault("Grass-Dry-Tall-Lower", DEFAULT_BLOCK_GRASS_DRY_TALL_LOWER);
		cfgBlocks.addDefault("Grass-Dry-Tall-Upper", DEFAULT_BLOCK_GRASS_DRY_TALL_UPPER);
	}



}
