package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_000.SUBFLOOR;
import static com.poixson.backrooms.worlds.Level_011.ENABLE_GEN_011;
import static com.poixson.utils.BlockUtils.StringToBlockData;

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
import com.poixson.tools.plotter.BlockPlotter;


// 11 | Concrete Jungle
public class Gen_011 extends BackroomsGen {

	// default blocks
	public static final String DEFAULT_BLOCK_SUBFLOOR = "minecraft:stone";

	// blocks
	public final AtomicReference<String> block_subfloor = new AtomicReference<String>(null);



	public Gen_011(final BackroomsLevel backlevel, final int seed,
			final int level_y, final int level_h) {
		super(backlevel, seed, level_y, level_h);
	}



	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
		if (!ENABLE_GEN_011) return;
		final BlockData block_subfloor = StringToBlockData(this.block_subfloor, DEFAULT_BLOCK_SUBFLOOR);
		if (block_subfloor == null) throw new RuntimeException("Invalid block type for level 11 SubFloor");
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
				// subfloor
				chunk.setBlock(ix, this.level_y, iz, Material.BEDROCK);
				for (int iy=0; iy<SUBFLOOR; iy++)
					chunk.setBlock(ix, this.level_y+iy+1, iz, block_subfloor);
			} // end ix
		} // end iz
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void loadConfig() {
		// block types
		{
			final ConfigurationSection cfg = this.plugin.getLevelBlocks(11);
			this.block_subfloor.set(cfg.getString("SubFloor"));
		}
	}
	public static void ConfigDefaults(final FileConfiguration cfg) {
		// block types
		cfg.addDefault("Level11.Blocks.SubFloor", DEFAULT_BLOCK_SUBFLOOR);
	}



}
