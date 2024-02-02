package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_000.ENABLE_GEN_023;
import static com.poixson.backrooms.worlds.Level_000.ENABLE_TOP_023;
import static com.poixson.backrooms.worlds.Level_000.SUBCEILING;
import static com.poixson.backrooms.worlds.Level_000.SUBFLOOR;
import static com.poixson.utils.BlockUtils.StringToBlockData;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.gens.Gen_000.LobbyData;
import com.poixson.backrooms.gens.Gen_001.BasementData;
import com.poixson.backrooms.worlds.Level_000.PregenLevel0;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.dao.Iab;
import com.poixson.tools.plotter.BlockPlotter;


// 23 | Overgrowth
public class Gen_023 extends BackroomsGen {

	// default params
	public static final int DEFAULT_GRASS_MODULUS = 25;

	// default blocks
	public static final String DEFAULT_BLOCK_WALL                 = "minecraft:moss_block";
	public static final String DEFAULT_BLOCK_WALL_GRASSY          = "minecraft:sculk_shrieker";
	public static final String DEFAULT_BLOCK_FLOOR                = "minecraft:moss_block";
	public static final String DEFAULT_BLOCK_CEILING              = "minecraft:moss_block";
	public static final String DEFAULT_BLOCK_CEILING_GRASS        = "minecraft:hanging_roots";
	public static final String DEFAULT_BLOCK_CEILING_FLOWER       = "minecraft:spore_blossom";
	public static final String DEFAULT_BLOCK_SUBFLOOR             = "minecraft:dirt";
	public static final String DEFAULT_BLOCK_SUBCEILING           = "minecraft:stone";
	public static final String DEFAULT_BLOCK_GRASS_WET_SHORT      = "minecraft:dead_bush";
	public static final String DEFAULT_BLOCK_GRASS_DRY_SHORT      = "minecraft:short_grass";
	public static final String DEFAULT_BLOCK_GRASS_WET_TALL_LOWER = "minecraft:small_dripleaf[half=lower]";
	public static final String DEFAULT_BLOCK_GRASS_WET_TALL_UPPER = "minecraft:small_dripleaf[half=upper]";
	public static final String DEFAULT_BLOCK_GRASS_DRY_TALL_LOWER = "minecraft:tall_grass[half=lower]";
	public static final String DEFAULT_BLOCK_GRASS_DRY_TALL_UPPER = "minecraft:tall_grass[half=upper]";

	// params
	public final AtomicInteger grass_modulus = new AtomicInteger(DEFAULT_GRASS_MODULUS);

	// blocks
	public final AtomicReference<String> block_wall                 = new AtomicReference<String>(null);
	public final AtomicReference<String> block_wall_grassy          = new AtomicReference<String>(null);
	public final AtomicReference<String> block_floor                = new AtomicReference<String>(null);
	public final AtomicReference<String> block_ceiling              = new AtomicReference<String>(null);
	public final AtomicReference<String> block_ceiling_grass        = new AtomicReference<String>(null);
	public final AtomicReference<String> block_ceiling_flower       = new AtomicReference<String>(null);
	public final AtomicReference<String> block_subfloor             = new AtomicReference<String>(null);
	public final AtomicReference<String> block_subceiling           = new AtomicReference<String>(null);
	public final AtomicReference<String> block_grass_wet_short      = new AtomicReference<String>(null);
	public final AtomicReference<String> block_grass_dry_short      = new AtomicReference<String>(null);
	public final AtomicReference<String> block_grass_wet_tall_lower = new AtomicReference<String>(null);
	public final AtomicReference<String> block_grass_wet_tall_upper = new AtomicReference<String>(null);
	public final AtomicReference<String> block_grass_dry_tall_lower = new AtomicReference<String>(null);
	public final AtomicReference<String> block_grass_dry_tall_upper = new AtomicReference<String>(null);

	protected final AtomicInteger rnd_grass = new AtomicInteger(0);



	public Gen_023(final BackroomsLevel backlevel, final int seed,
			final int level_y, final int level_h) {
		super(backlevel, seed, level_y, level_h);
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!ENABLE_GEN_023) return;
		final int grass_modulus = this.grass_modulus.get();
		final BlockData block_wall                 = StringToBlockData(this.block_wall,                 DEFAULT_BLOCK_WALL                );
		final BlockData block_wall_grassy          = StringToBlockData(this.block_wall_grassy,          DEFAULT_BLOCK_WALL_GRASSY         );
		final BlockData block_floor                = StringToBlockData(this.block_floor,                DEFAULT_BLOCK_FLOOR               );
		final BlockData block_ceiling              = StringToBlockData(this.block_ceiling,              DEFAULT_BLOCK_CEILING             );
		final BlockData block_ceiling_grass        = StringToBlockData(this.block_ceiling_grass,        DEFAULT_BLOCK_CEILING_GRASS       );
		final BlockData block_ceiling_flower       = StringToBlockData(this.block_ceiling_flower,       DEFAULT_BLOCK_CEILING_FLOWER      );
		final BlockData block_subfloor             = StringToBlockData(this.block_subfloor,             DEFAULT_BLOCK_SUBFLOOR            );
		final BlockData block_subceiling           = StringToBlockData(this.block_subceiling,           DEFAULT_BLOCK_SUBCEILING          );
		final BlockData block_grass_wet_short      = StringToBlockData(this.block_grass_wet_short,      DEFAULT_BLOCK_GRASS_WET_SHORT     );
		final BlockData block_grass_dry_short      = StringToBlockData(this.block_grass_dry_short,      DEFAULT_BLOCK_GRASS_DRY_SHORT     );
		final BlockData block_grass_wet_tall_lower = StringToBlockData(this.block_grass_wet_tall_lower, DEFAULT_BLOCK_GRASS_WET_TALL_LOWER);
		final BlockData block_grass_wet_tall_upper = StringToBlockData(this.block_grass_wet_tall_upper, DEFAULT_BLOCK_GRASS_WET_TALL_UPPER);
		final BlockData block_grass_dry_tall_lower = StringToBlockData(this.block_grass_dry_tall_lower, DEFAULT_BLOCK_GRASS_DRY_TALL_LOWER);
		final BlockData block_grass_dry_tall_upper = StringToBlockData(this.block_grass_dry_tall_upper, DEFAULT_BLOCK_GRASS_DRY_TALL_UPPER);
		if (grass_modulus       <= 0   ) throw new RuntimeException("Invalid param for level 23 Grass-Modulus"     );
		if (block_wall          == null) throw new RuntimeException("Invalid block type for level 23 Wall"         );
		if (block_wall_grassy   == null) throw new RuntimeException("Invalid block type for level 23 Wall-Grassy"  );
		if (block_floor         == null) throw new RuntimeException("Invalid block type for level 23 Floor"        );
		if (block_ceiling       == null) throw new RuntimeException("Invalid block type for level 23 Ceiling"      );
		if (block_ceiling_grass == null) throw new RuntimeException("Invalid block type for level 23 Ceiling-Grass");
		if (block_subfloor      == null) throw new RuntimeException("Invalid block type for level 23 SubFloor"     );
		if (block_subceiling    == null) throw new RuntimeException("Invalid block type for level 23 SubCeiling"   );
		final PregenLevel0 pregen0 = (PregenLevel0) pregen;
		final HashMap<Iab, LobbyData>    lobbyData    = pregen0.lobby;
		final HashMap<Iab, BasementData> basementData = pregen0.basement;
		LobbyData    dao_lobby;
		BasementData dao_basement;
		final int y  = this.level_y + SUBFLOOR + 1;
		final int cy = this.level_y + SUBFLOOR + this.level_h + 2;
		int xx, zz;
		int modX, modZ;
		for (int iz=0; iz<16; iz++) {
			zz = (chunkZ * 16) + iz;
			modZ = (zz < 0 ? 1-zz : zz) % 7;
			for (int ix=0; ix<16; ix++) {
				xx = (chunkX * 16) + ix;
				modX = (xx < 0 ? 1-xx : xx) % 7;
				dao_lobby = lobbyData.get(new Iab(ix, iz));
				dao_basement = basementData.get(new Iab(ix, iz));
				// floor
				chunk.setBlock(ix, this.level_y, iz, Material.BEDROCK);
				if (dao_basement.isWet) chunk.setBlock(ix, this.level_y+1, iz, Material.WATER);
				else                    chunk.setBlock(ix, this.level_y+1, iz, block_subfloor);
				for (int iy=1; iy<SUBFLOOR; iy++)
					chunk.setBlock(ix, this.level_y+iy+1, iz, block_subfloor);
				// wall
				if (dao_lobby.isWall) {
					final BlockData blk_wall = (dao_basement.isWet ? block_wall_grassy : block_wall);
					final int h = this.level_h + 2;
					for (int yi=0; yi<h; yi++)
						chunk.setBlock(ix, y+yi, iz, blk_wall);
				// room
				} else {
					chunk.setBlock(ix, this.level_y+SUBFLOOR+1, iz, block_floor);
					// grass
					final int rnd = this.rnd_grass.getAndIncrement();
					final int mod_grass = rnd % grass_modulus;
					// tall grass
					if (mod_grass == 0 || mod_grass == 5) {
						final BlockData blk_lower = (dao_basement.isWet ? block_grass_wet_tall_lower : block_grass_dry_tall_lower);
						final BlockData blk_upper = (dao_basement.isWet ? block_grass_wet_tall_upper : block_grass_dry_tall_upper);
						if (blk_lower != null) chunk.setBlock(ix, this.level_y+SUBFLOOR+2, iz, blk_lower);
						if (blk_upper != null) chunk.setBlock(ix, this.level_y+SUBFLOOR+3, iz, blk_upper);
					// short grass
					} else {
						final BlockData blk_grass = (dao_basement.isWet ? block_grass_wet_short : block_grass_dry_short);
						if (blk_grass != null) chunk.setBlock(ix, this.level_y+SUBFLOOR+2, iz, blk_grass);
					}
					// ceiling
					if (ENABLE_TOP_023) {
						if (modZ == 0 && modX < 2
						&& dao_lobby.wall_dist > 1) {
							// ceiling lights
							chunk.setBlock(ix, cy,   iz, Material.VERDANT_FROGLIGHT);
							chunk.setBlock(ix, cy-1, iz, Material.WEEPING_VINES);
						} else {
							// ceiling
							chunk.setBlock(ix, cy, iz, block_ceiling );
							if (mod_grass == 7 && block_ceiling_flower != null) chunk.setBlock(ix, cy-1, iz, block_ceiling_flower);
							else                                                chunk.setBlock(ix, cy-1, iz, block_ceiling_grass );
						}
					}
				}
				// subceiling
				if (ENABLE_TOP_023) {
					for (int iy=0; iy<SUBCEILING; iy++)
						chunk.setBlock(ix, cy+iy+1, iz, block_subceiling);
				}
			} // end ix
		} // end iz
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void loadConfig() {
		// params
		{
			final ConfigurationSection cfg = this.plugin.getLevelParams(23);
			this.grass_modulus.set(cfg.getInt("Grass-Modulus"));
		}
		// block types
		{
			final ConfigurationSection cfg = this.plugin.getLevelBlocks(23);
			this.block_wall                .set(cfg.getString("Wall"                ));
			this.block_wall_grassy         .set(cfg.getString("Wall-Grassy"         ));
			this.block_floor               .set(cfg.getString("Floor"               ));
			this.block_ceiling             .set(cfg.getString("Ceiling"             ));
			this.block_ceiling_grass       .set(cfg.getString("Ceiling-Grass"       ));
			this.block_ceiling_flower      .set(cfg.getString("Ceiling-Flower"      ));
			this.block_subfloor            .set(cfg.getString("SubFloor"            ));
			this.block_subceiling          .set(cfg.getString("SubCeiling"          ));
			this.block_grass_wet_short     .set(cfg.getString("Grass-Wet-Short"     ));
			this.block_grass_dry_short     .set(cfg.getString("Grass-Dry-Short"     ));
			this.block_grass_wet_tall_lower.set(cfg.getString("Grass-Wet-Tall-Lower"));
			this.block_grass_wet_tall_upper.set(cfg.getString("Grass-Wet-Tall-Upper"));
			this.block_grass_dry_tall_lower.set(cfg.getString("Grass-Dry-Tall-Lower"));
			this.block_grass_dry_tall_upper.set(cfg.getString("Grass-Dry-Tall-Upper"));
		}
	}
	public static void ConfigDefaults(final FileConfiguration cfg) {
		// params
		cfg.addDefault("Level23.Params.Grass-Modulus", DEFAULT_GRASS_MODULUS);
		// block types
		cfg.addDefault("Level23.Blocks.Wall",                 DEFAULT_BLOCK_WALL                );
		cfg.addDefault("Level23.Blocks.Wall-Grassy",          DEFAULT_BLOCK_WALL_GRASSY         );
		cfg.addDefault("Level23.Blocks.Floor",                DEFAULT_BLOCK_FLOOR               );
		cfg.addDefault("Level23.Blocks.Ceiling",              DEFAULT_BLOCK_CEILING             );
		cfg.addDefault("Level23.Blocks.Ceiling-Grass",        DEFAULT_BLOCK_CEILING_GRASS       );
		cfg.addDefault("Level23.Blocks.Ceiling-Flower",       DEFAULT_BLOCK_CEILING_FLOWER      );
		cfg.addDefault("Level23.Blocks.SubFloor",             DEFAULT_BLOCK_SUBFLOOR            );
		cfg.addDefault("Level23.Blocks.SubCeiling",           DEFAULT_BLOCK_SUBCEILING          );
		cfg.addDefault("Level23.Blocks.Grass-Wet-Short",      DEFAULT_BLOCK_GRASS_WET_SHORT     );
		cfg.addDefault("Level23.Blocks.Grass-Dry-Short",      DEFAULT_BLOCK_GRASS_DRY_SHORT     );
		cfg.addDefault("Level23.Blocks.Grass-Wet-Tall-Lower", DEFAULT_BLOCK_GRASS_WET_TALL_LOWER);
		cfg.addDefault("Level23.Blocks.Grass-Wet-Tall-Upper", DEFAULT_BLOCK_GRASS_WET_TALL_UPPER);
		cfg.addDefault("Level23.Blocks.Grass-Dry-Tall-Lower", DEFAULT_BLOCK_GRASS_DRY_TALL_LOWER);
		cfg.addDefault("Level23.Blocks.Grass-Dry-Tall-Upper", DEFAULT_BLOCK_GRASS_DRY_TALL_UPPER);
	}



}
