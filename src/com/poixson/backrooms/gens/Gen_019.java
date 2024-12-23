package com.poixson.backrooms.gens;

import static com.poixson.utils.BlockUtils.StringToBlockDataDef;

import java.util.HashMap;
import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsWorld;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.gens.Gen_000.LobbyData;
import com.poixson.backrooms.worlds.BackWorld_000.Pregen_Level_000;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.dao.Iab;
import com.poixson.tools.noise.FastNoiseLiteD;
import com.poixson.tools.plotter.BlockPlotter;


// 19 | Attic
public class Gen_019 extends BackroomsGen {

	// default params
	public static final int    DEFAULT_LEVEL_H          = 13;
	public static final int    DEFAULT_SUBFLOOR         = 3;
	public static final double DEFAULT_NOISE_LAMPS_FREQ = 0.045;

	// default blocks
	public static final String DEFAULT_BLOCK_WALL  = "minecraft:spruce_planks";
	public static final String DEFAULT_BLOCK_FLOOR = "minecraft:spruce_planks";
	public static final String DEFAULT_BLOCK_BEAM  = "minecraft:spruce_wood";
	public static final String DEFAULT_BLOCK_LAMP  = "minecraft:lantern[hanging=true]";

	// params
	public final boolean enable_gen;
	public final int     level_y;
	public final int     level_h;
	public final int     subfloor;

	// blocks
	public final String block_wall;
	public final String block_floor;
	public final String block_beam;
	public final String block_lamp;

	// noise
	public final FastNoiseLiteD noiseLamps;



	public Gen_019(final BackroomsWorld backworld, final int seed, final BackroomsGen gen_below) {
		super(backworld, gen_below, seed);
		final int level_number = this.getLevelNumber();
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(level_number);
		final ConfigurationSection cfgBlocks = this.plugin.getConfigLevelBlocks(level_number);
		// params
		this.enable_gen = cfgParams.getBoolean("Enable-Gen"  );
		this.level_y    = cfgParams.getInt(    "Level-Y"     );
		this.level_h    = cfgParams.getInt(    "Level-Height");
		this.subfloor   = cfgParams.getInt(    "SubFloor"    );
		// block types
		this.block_wall  = cfgBlocks.getString("Wall" );
		this.block_floor = cfgBlocks.getString("Floor");
		this.block_beam  = cfgBlocks.getString("Beam" );
		this.block_lamp  = cfgBlocks.getString("Lamp" );
		// noise
		this.noiseLamps = this.register(new FastNoiseLiteD());
	}



	@Override
	public int getLevelNumber() {
		return 19;
	}



	@Override
	public int getLevelY() {
		return this.level_y;
	}
	@Override
	public int getOpenY() {
		return this.getMinY() + this.subfloor;
	}

	@Override
	public int getMinY() {
		return this.getLevelY() + this.bedrock_barrier;
	}
	@Override
	public int getMaxY() {
		return (this.getMinY() + this.subfloor + this.level_h) - 1;
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!this.enable_gen) return;
		final BlockData block_wall  = StringToBlockDataDef(this.block_wall,  DEFAULT_BLOCK_WALL );
		final BlockData block_floor = StringToBlockDataDef(this.block_floor, DEFAULT_BLOCK_FLOOR);
		final BlockData block_beam  = StringToBlockDataDef(this.block_beam,  DEFAULT_BLOCK_BEAM );
		final BlockData block_lamp  = StringToBlockDataDef(this.block_lamp,  DEFAULT_BLOCK_LAMP );
		if (block_wall  == null) throw new RuntimeException("Invalid block type for level 19 Wall" );
		if (block_floor == null) throw new RuntimeException("Invalid block type for level 19 Floor");
		if (block_beam  == null) throw new RuntimeException("Invalid block type for level 19 Beam" );
		if (block_lamp  == null) throw new RuntimeException("Invalid block type for level 19 Lamp" );
		final HashMap<Iab, LobbyData> lobbyData = ((Pregen_Level_000)pregen).lobby;
		final int y_base  = this.level_y + this.bedrock_barrier;
		final int y_floor = (y_base + this.subfloor) - 1;
		for (int iz=0; iz<16; iz++) {
			final int zz = (chunkZ * 16) + iz;
			final int mod_z = (zz < 0 ? 1-zz : zz) % 7;
			for (int ix=0; ix<16; ix++) {
				final int xx = (chunkX * 16) + ix;
				final int mod_x = (xx < 0 ? 1-xx : xx) % 7;
				// barrier
				for (int iy=0; iy<this.bedrock_barrier; iy++)
					chunk.setBlock(ix, this.level_y+iy, iz, Material.BEDROCK);
				// attic floor
				for (int iy=0; iy<this.subfloor; iy++)
					chunk.setBlock(ix, y_base+iy, iz, block_floor);
				final LobbyData dao_lobby = lobbyData.get(new Iab(ix, iz));
				if (dao_lobby == null) continue;
				// beam
				if (mod_x == 0 || mod_z == 0)
					chunk.setBlock(ix, y_floor+dao_lobby.wall_dist, iz, block_beam);
				// lantern
				if (mod_x == 0 && mod_z == 0 && dao_lobby.wall_dist == 6) {
					final double value_lamp = this.noiseLamps.getNoise(xx, zz);
					if (value_lamp < 0.0)
						chunk.setBlock(ix, y_floor+dao_lobby.wall_dist-1, iz, block_lamp);
				}
				// wall
				if (dao_lobby.isWall) {
					for (int iy=0; iy<this.level_h+1; iy++) {
						if (iy > 6) chunk.setBlock(ix, y_floor+iy, iz, Material.BEDROCK);
						else        chunk.setBlock(ix, y_floor+iy, iz, block_wall      );
					}
				}
			} // end ix
		} // end iz
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void initNoise() {
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(this.getLevelNumber());
		// lanterns
		this.noiseLamps.setFrequency(cfgParams.getDouble("Noise-Lamps-Freq"));
	}



	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		cfgParams.addDefault("Enable-Gen",       Boolean.TRUE                             );
		cfgParams.addDefault("Level-Y",          Integer.valueOf(this.getDefaultY()      ));
		cfgParams.addDefault("Level-Height",     Integer.valueOf(DEFAULT_LEVEL_H         ));
		cfgParams.addDefault("SubFloor",         Integer.valueOf(DEFAULT_SUBFLOOR        ));
		cfgParams.addDefault("Noise-Lamps-Freq", Double .valueOf(DEFAULT_NOISE_LAMPS_FREQ));
		// block types
		cfgBlocks.addDefault("Wall",  DEFAULT_BLOCK_WALL );
		cfgBlocks.addDefault("Floor", DEFAULT_BLOCK_FLOOR);
		cfgBlocks.addDefault("Beam",  DEFAULT_BLOCK_BEAM );
		cfgBlocks.addDefault("Lamp",  DEFAULT_BLOCK_LAMP );
	}



}
