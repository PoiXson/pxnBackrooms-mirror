package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_000.ENABLE_GEN_023;
import static com.poixson.backrooms.worlds.Level_000.ENABLE_TOP_023;
import static com.poixson.backrooms.worlds.Level_000.SUBCEILING;
import static com.poixson.backrooms.worlds.Level_000.SUBFLOOR;
import static com.poixson.utils.BlockUtils.StringToBlockData;

import java.util.HashMap;
import java.util.LinkedList;
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
import com.poixson.tools.dao.Iab;
import com.poixson.tools.plotter.BlockPlotter;


// 23 | Overgrowth
public class Gen_023 extends BackroomsGen {

	// default blocks
	public static final String DEFAULT_BLOCK_WALL       = "minecraft:moss_block";
	public static final String DEFAULT_BLOCK_FLOOR      = "minecraft:mossy_cobblestone";
	public static final String DEFAULT_BLOCK_CEILING    = "minecraft:smooth_stone_slab[type=top]";
	public static final String DEFAULT_BLOCK_SUBFLOOR   = "minecraft:dirt";
	public static final String DEFAULT_BLOCK_SUBCEILING = "minecraft:stone";
	public static final String DEFAULT_BLOCK_CARPET     = "minecraft:moss_carpet";

	// blocks
	public final AtomicReference<String> block_wall       = new AtomicReference<String>(null);
	public final AtomicReference<String> block_floor      = new AtomicReference<String>(null);
	public final AtomicReference<String> block_ceiling    = new AtomicReference<String>(null);
	public final AtomicReference<String> block_subfloor   = new AtomicReference<String>(null);
	public final AtomicReference<String> block_subceiling = new AtomicReference<String>(null);
	public final AtomicReference<String> block_carpet     = new AtomicReference<String>(null);



	public Gen_023(final BackroomsLevel backlevel,
			final int level_y, final int level_h) {
		super(backlevel, level_y, level_h);
	}



	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
		if (!ENABLE_GEN_023) return;
		final BlockData block_wall       = StringToBlockData(this.block_wall,       DEFAULT_BLOCK_WALL      );
		final BlockData block_floor      = StringToBlockData(this.block_floor,      DEFAULT_BLOCK_FLOOR     );
		final BlockData block_ceiling    = StringToBlockData(this.block_ceiling,    DEFAULT_BLOCK_CEILING   );
		final BlockData block_subfloor   = StringToBlockData(this.block_subfloor,   DEFAULT_BLOCK_SUBFLOOR  );
		final BlockData block_subceiling = StringToBlockData(this.block_subceiling, DEFAULT_BLOCK_SUBCEILING);
		final BlockData block_carpet     = StringToBlockData(this.block_carpet,     DEFAULT_BLOCK_CARPET    );
		if (block_wall       == null) throw new RuntimeException("Invalid block type for level 23 Wall"      );
		if (block_floor      == null) throw new RuntimeException("Invalid block type for level 23 Floor"     );
		if (block_ceiling    == null) throw new RuntimeException("Invalid block type for level 23 Ceiling"   );
		if (block_subfloor   == null) throw new RuntimeException("Invalid block type for level 23 SubFloor"  );
		if (block_subceiling == null) throw new RuntimeException("Invalid block type for level 23 SubCeiling");
		if (block_carpet     == null) throw new RuntimeException("Invalid block type for level 23 Carpet"    );
		final PregenLevel0 pregen0 = (PregenLevel0) pregen;
		final HashMap<Iab, LobbyData>    lobbyData    = pregen0.lobby;
		final HashMap<Iab, BasementData> basementData = pregen0.basement;
		LobbyData    dao_lobby;
		BasementData dao_basement;
		final int y  = this.level_y + SUBFLOOR + 1;
		final int cy = this.level_y + SUBFLOOR + this.level_h + 2;
		int xx, zz;
		for (int iz=0; iz<16; iz++) {
			zz = (chunkZ * 16) + iz;
			for (int ix=0; ix<16; ix++) {
				xx = (chunkX * 16) + ix;
				dao_lobby = lobbyData.get(new Iab(ix, iz));
				dao_basement = basementData.get(new Iab(ix, iz));
				// floor
				chunk.setBlock(ix, this.level_y, iz, Material.BEDROCK);
				if (dao_basement.isWet) chunk.setBlock(ix, this.level_y+1, iz, Material.WATER);
				else                    chunk.setBlock(ix, this.level_y+1, iz, block_subfloor);
				for (int iy=1; iy<SUBFLOOR; iy++)
					chunk.setBlock(ix, this.level_y+iy+1, iz, block_subfloor);
				chunk.setBlock(ix, this.level_y+SUBFLOOR+1, iz, block_floor);
				// wall
				if (dao_lobby.isWall) {
					final int h = this.level_h + 1;
					for (int yi=0; yi<h; yi++)
						chunk.setBlock(ix, y+yi+1, iz, block_wall);
				// room
				} else {
					// moss carpet
					if (dao_basement.isWet)
						chunk.setBlock(ix, this.level_y+SUBFLOOR+2, iz, block_carpet);
					// ceiling
					if (ENABLE_TOP_023) {
						final int modX6 = Math.abs(xx) % 7;
						final int modZ6 = Math.abs(zz) % 7;
						if (modZ6 == 0 && modX6 < 2
						&& dao_lobby.wall_dist > 1) {
							// ceiling lights
							chunk.setBlock(ix, cy,   iz, Material.VERDANT_FROGLIGHT);
							chunk.setBlock(ix, cy-1, iz, Material.WEEPING_VINES);
						} else {
							// ceiling
							chunk.setBlock(ix, cy, iz, block_ceiling);
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
		// block types
		final ConfigurationSection cfg = this.plugin.getLevelBlocks(23);
		this.block_wall      .set(cfg.getString("Wall"      ));
		this.block_floor     .set(cfg.getString("Floor"     ));
		this.block_ceiling   .set(cfg.getString("Ceiling"   ));
		this.block_subfloor  .set(cfg.getString("SubFloor"  ));
		this.block_subceiling.set(cfg.getString("SubCeiling"));
		this.block_carpet    .set(cfg.getString("Carpet"    ));
	}
	public static void ConfigDefaults(final FileConfiguration cfg) {
		// block types
		cfg.addDefault("Level23.Blocks.Wall",       DEFAULT_BLOCK_WALL      );
		cfg.addDefault("Level23.Blocks.Floor",      DEFAULT_BLOCK_FLOOR     );
		cfg.addDefault("Level23.Blocks.Ceiling",    DEFAULT_BLOCK_CEILING   );
		cfg.addDefault("Level23.Blocks.SubFloor",   DEFAULT_BLOCK_SUBFLOOR  );
		cfg.addDefault("Level23.Blocks.SubCeiling", DEFAULT_BLOCK_SUBCEILING);
		cfg.addDefault("Level23.Blocks.Carpet",     DEFAULT_BLOCK_CARPET    );
	}



}
