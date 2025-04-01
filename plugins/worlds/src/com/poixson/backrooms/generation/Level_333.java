package com.poixson.backrooms.gens;

import static com.poixson.utils.BlockUtils.StringToBlockDataDef;
import static com.poixson.utils.MathUtils.IsMinMax;
import static com.poixson.utils.Utils.IsEmpty;

import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsWorld;
import com.poixson.backrooms.PreGenData;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.plotter.BlockPlotter;


// 333 | Cubes
public class Gen_333 extends BackroomsGen {

	// default params
	public static final int DEFAULT_LEVEL_Y = -64;
	public static final int DEFAULT_LEVEL_H = 384;

	// default blocks
	public static final String DEFAULT_BLOCK_CUBE_NORMAL = "minecraft:polished_diorite";
	public static final String DEFAULT_BLOCK_FILL        = "minecraft:bedrock";

	// params
	public final boolean enable_gen;
	public final boolean enable_top;
	public final int     level_y;
	public final int     level_h;

	// blocks
	public final String block_cube_normal;
	public final String block_fill;



	public Gen_333(final BackroomsWorld backworld, final int seed) {
		super(backworld, null, seed);
		final int level_number = this.getLevelNumber();
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(level_number);
		final ConfigurationSection cfgBlocks = this.plugin.getConfigLevelBlocks(level_number);
		// params
		this.enable_gen = cfgParams.getBoolean("Enable-Gen"  );
		this.enable_top = cfgParams.getBoolean("Enable-Top"  );
		this.level_y    = cfgParams.getInt(    "Level-Y"     );
		this.level_h    = cfgParams.getInt(    "Level-Height");
		// block types
		this.block_cube_normal = cfgBlocks.getString("Cube-Normal");
		this.block_fill        = cfgBlocks.getString("Fill"       );
	}



	@Override
	public int getLevelNumber() {
		return 333;
	}



	@Override
	public int getLevelY() {
		return this.level_y;
	}
	@Override
	public int getOpenY() {
		return 306;
	}



	// -------------------------------------------------------------------------------
	// generate



//TODO: biome structures
	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!this.enable_gen) return;
		final int area_size = 22 * 16;
		int mod_chunk_x = (chunkX % area_size) + (chunkX<0 ? area_size : 0);
		int mod_chunk_z = (chunkZ % area_size) + (chunkZ<0 ? area_size : 0);
		if (mod_chunk_x == 0 || mod_chunk_x == area_size-1
		||  mod_chunk_z == 0 || mod_chunk_z == area_size-1)
			return;
		final BlockData block_cube_normal = StringToBlockDataDef(this.block_cube_normal, DEFAULT_BLOCK_CUBE_NORMAL);
		final BlockData block_fill        = StringToBlockDataDef(this.block_fill,        DEFAULT_BLOCK_FILL       );
		if (block_cube_normal == null) throw new RuntimeException("Invalid block type for level 333 Cube-Normal");
		if (block_fill        == null) throw new RuntimeException("Invalid block type for level 333 Fill");
		final BlockPlotter plot_normal =
			(new BlockPlotter())
			.axis("use")
			.xyz( 0,  0,  0)
			.whd(16, 16, 16);
		plot_normal.type('X', this.enable_top ? block_fill : Bukkit.createBlockData(Material.AIR));
		plot_normal.type('#', block_cube_normal);
		plot_normal.type('H', "minecraft:ladder[facing=north]");
		final int cubes_h = Math.floorDiv(this.level_h, 16);
		for (int cube_y=0; cube_y<cubes_h; cube_y++) {
			final int yy = (cube_y * 16) + this.level_y;
//TODO: biomes
final BlockPlotter plot = plot_normal;
			plot.y(yy);
			final StringBuilder[][] matrix = plot.getMatrix3D();
			// fill
			for (int iy=0; iy<16; iy++) {
				for (int iz=0; iz<16; iz++)
					matrix[iy][iz].append("X".repeat(16));
			}
			// cube
			for (int iz=2; iz<13; iz++) {
				if (this.enable_top)
				ReplaceInString(matrix[13][iz], "#".repeat(13), 1); // ceiling
				ReplaceInString(matrix[ 1][iz], "#".repeat(13), 1); // floor
				for (int iy=2; iy<13; iy++)
					ReplaceInString(matrix[iy][iz], "#"+" ".repeat(11)+"#", 1); // east/west walls
			}
			for (int iy=1; iy<14; iy++) {
				ReplaceInString(matrix[iy][ 1], "#".repeat(13), 1); // north wall
				ReplaceInString(matrix[iy][13], "#".repeat(13), 1); // south wall
			}
			// top portal
			ReplaceInString(matrix[15][6], "###", 6); ReplaceInString(matrix[14][6], "###", 6);
			ReplaceInString(matrix[15][7], "#H#", 6); ReplaceInString(matrix[14][7], "#H#", 6);
			ReplaceInString(matrix[15][8], "###", 6); ReplaceInString(matrix[14][8], "###", 6);
			if (this.enable_top)
				ReplaceInString(matrix[13][7], "H", 7);
			// bottom portal
			ReplaceInString(matrix[0][6], "###", 6);
			ReplaceInString(matrix[0][7], "#H#", 6); ReplaceInString(matrix[1][7],  "H",  7);
			ReplaceInString(matrix[0][8], "###", 6);
			// north portal
			ReplaceInString(matrix[9][0], "###", 6); ReplaceInString(matrix[9][1], "###", 6);
			ReplaceInString(matrix[8][0], "# #", 6); ReplaceInString(matrix[8][1], "# #", 6); ReplaceInString(matrix[8][2], " ", 7);
			ReplaceInString(matrix[7][0], "# #", 6); ReplaceInString(matrix[7][1], "# #", 6); ReplaceInString(matrix[7][2], " ", 7);
			ReplaceInString(matrix[6][0], "###", 6); ReplaceInString(matrix[6][1], "###", 6);
			// south portal
			ReplaceInString(matrix[9][15], "###", 6); ReplaceInString(matrix[9][14], "###", 6);
			ReplaceInString(matrix[8][15], "# #", 6); ReplaceInString(matrix[8][14], "# #", 6); ReplaceInString(matrix[8][13], " ", 7);
			ReplaceInString(matrix[7][15], "# #", 6); ReplaceInString(matrix[7][14], "# #", 6); ReplaceInString(matrix[7][13], " ", 7);
			ReplaceInString(matrix[6][15], "###", 6); ReplaceInString(matrix[6][14], "###", 6);
			// east portal
			ReplaceInString(matrix[9][6], "##", 14); ReplaceInString(matrix[9][7], "##",  14); ReplaceInString(matrix[9][8], "##", 14);
			ReplaceInString(matrix[8][6], "##", 14); ReplaceInString(matrix[8][7], "   ", 13); ReplaceInString(matrix[8][8], "##", 14);
			ReplaceInString(matrix[7][6], "##", 14); ReplaceInString(matrix[7][7], "   ", 13); ReplaceInString(matrix[7][8], "##", 14);
			ReplaceInString(matrix[6][6], "##", 14); ReplaceInString(matrix[6][7], "##",  14); ReplaceInString(matrix[6][8], "##", 14);
			// west portal
			ReplaceInString(matrix[9][6], "#", 0); ReplaceInString(matrix[9][7], "#",  0); ReplaceInString(matrix[9][8], "#", 0);
			ReplaceInString(matrix[8][6], "#", 0); ReplaceInString(matrix[8][7], "  ", 0); ReplaceInString(matrix[8][8], "#", 0);
			ReplaceInString(matrix[7][6], "#", 0); ReplaceInString(matrix[7][7], "  ", 0); ReplaceInString(matrix[7][8], "#", 0);
			ReplaceInString(matrix[6][6], "#", 0); ReplaceInString(matrix[6][7], "#",  0); ReplaceInString(matrix[6][8], "#", 0);
			plot_normal.run(chunk, matrix);
		}
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void initNoise() {
//TODO
	}



	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		cfgParams.addDefault("Enable-Gen",   Boolean.TRUE                    );
		cfgParams.addDefault("Enable-Top",   Boolean.TRUE                    );
		cfgParams.addDefault("Level-Y",      Integer.valueOf(DEFAULT_LEVEL_Y));
		cfgParams.addDefault("Level-Height", Integer.valueOf(DEFAULT_LEVEL_H));
		// block types
		cfgBlocks.addDefault("Cube-Normal", DEFAULT_BLOCK_CUBE_NORMAL);
		cfgBlocks.addDefault("Fill",        DEFAULT_BLOCK_FILL       );
	}



}
