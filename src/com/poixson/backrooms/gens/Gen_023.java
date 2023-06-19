package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_000.ENABLE_GEN_023;
import static com.poixson.backrooms.worlds.Level_000.ENABLE_TOP_023;
import static com.poixson.backrooms.worlds.Level_000.SUBCEILING;
import static com.poixson.backrooms.worlds.Level_000.SUBFLOOR;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Material;
import org.bukkit.block.data.type.Slab;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.gens.Gen_000.LobbyData;
import com.poixson.backrooms.gens.Gen_001.BasementData;
import com.poixson.backrooms.worlds.Level_000.PregenLevel0;
import com.poixson.commonmc.tools.plotter.BlockPlotter;
import com.poixson.tools.dao.Iab;


// 23 | Overgrowth
public class Gen_023 extends BackroomsGen {

	public static final double THRESH_CARPET = 0.4;

	// blocks
	public final AtomicReference<String> block_wall     = new AtomicReference<String>(null);
	public final AtomicReference<String> block_subfloor = new AtomicReference<String>(null);
	public final AtomicReference<String> block_ceiling  = new AtomicReference<String>(null);



	public Gen_023(final BackroomsLevel backlevel,
			final int level_y, final int level_h) {
		super(backlevel, level_y, level_h);
	}



	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
		if (!ENABLE_GEN_023) return;
		final Material block_wall     = Material.matchMaterial(this.block_wall    .get());
		final Material block_subfloor = Material.matchMaterial(this.block_subfloor.get());
		final Material block_ceiling  = Material.matchMaterial(this.block_ceiling .get());
		if (block_wall     == null) throw new RuntimeException("Invalid block type for level 23 Wall"    );
		if (block_subfloor == null) throw new RuntimeException("Invalid block type for level 23 SubFloor");
		if (block_ceiling  == null) throw new RuntimeException("Invalid block type for level 23 Ceiling" );
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
				chunk.setBlock(ix, this.level_y+1, iz, dao_basement.isWet ? Material.WATER : block_subfloor);
				for (int iy=1; iy<SUBFLOOR; iy++)
					chunk.setBlock(ix, this.level_y+iy+1, iz, block_subfloor);
				chunk.setBlock(ix, this.level_y+SUBFLOOR+1, iz, Material.MOSSY_COBBLESTONE);
				// wall
				if (dao_lobby.isWall) {
					final int h = this.level_h + 1;
					for (int yi=0; yi<h; yi++)
						chunk.setBlock(ix, y+yi+1, iz, block_wall);
				// room
				} else {
					// moss carpet
					if (dao_basement.isWet)
						chunk.setBlock(ix, this.level_y+SUBFLOOR+2, iz, Material.MOSS_CARPET);
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
							final Slab slab = (Slab) chunk.getBlockData(ix, cy, iz);
							// random concurrent modification exception
							synchronized (slab) {
								slab.setType(Slab.Type.TOP);
								chunk.setBlock(ix, cy, iz, slab);
							}
						}
					}
				}
				// subceiling
				if (ENABLE_TOP_023) {
					for (int iy=0; iy<SUBCEILING; iy++)
						chunk.setBlock(ix, cy+iy+1, iz, Material.STONE);
				}
			} // end ix
		} // end iz
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void loadConfig() {
		final ConfigurationSection cfg = this.plugin.getLevelBlocks(23);
		this.block_wall    .set(cfg.getString("Wall"    ));
		this.block_subfloor.set(cfg.getString("SubFloor"));
		this.block_ceiling .set(cfg.getString("Ceiling" ));
	}
	public static void ConfigDefaults(final FileConfiguration cfg) {
		cfg.addDefault("Level23.Blocks.Wall",     "minecraft:moss_block"       );
		cfg.addDefault("Level23.Blocks.SubFloor", "minecraft:dirt"             );
		cfg.addDefault("Level23.Blocks.Ceiling",  "minecraft:smooth_stone_slab");
	}



}
