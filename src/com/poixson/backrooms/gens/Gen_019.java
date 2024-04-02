package com.poixson.backrooms.gens;

import static com.poixson.utils.BlockUtils.StringToBlockData;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.gens.Gen_000.LobbyData;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.backrooms.worlds.Level_000.PregenLevel0;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.dao.Iab;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.utils.FastNoiseLiteD;


// 19 | Attic
public class Gen_019 extends BackroomsGen {

	// default params
	public static final double DEFAULT_NOISE_LAMPS_FREQ = 0.045;

	// default blocks
	public static final String DEFAULT_BLOCK_WALL  = "minecraft:spruce_planks";
	public static final String DEFAULT_BLOCK_FLOOR = "minecraft:spruce_planks";
	public static final String DEFAULT_BLOCK_BEAM  = "minecraft:spruce_wood";

	// params
	public final boolean enable_gen;
	// noise
	public final FastNoiseLiteD noiseLamps;

	// blocks
	public final AtomicReference<String> block_wall  = new AtomicReference<String>(null);
	public final AtomicReference<String> block_floor = new AtomicReference<String>(null);
	public final AtomicReference<String> block_beam  = new AtomicReference<String>(null);



	public Gen_019(final BackroomsLevel backlevel, final int seed,
			final int level_y, final int level_h) {
		super(backlevel, seed, level_y, level_h);
		this.enable_gen = cfgParams.getBoolean("Enable-Gen"  );
		// noise
		this.noiseLamps = this.register(new FastNoiseLiteD());
	}



	@Override
	public int getLevelNumber() {
		return 19;
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!this.enable_gen) return;
		final BlockData block_wall  = StringToBlockData(this.block_wall,  DEFAULT_BLOCK_WALL );
		final BlockData block_floor = StringToBlockData(this.block_floor, DEFAULT_BLOCK_FLOOR);
		final BlockData block_beam  = StringToBlockData(this.block_beam,  DEFAULT_BLOCK_BEAM );
		if (block_wall  == null) throw new RuntimeException("Invalid block type for level 19 Wall" );
		if (block_floor == null) throw new RuntimeException("Invalid block type for level 19 Floor");
		if (block_beam  == null) throw new RuntimeException("Invalid block type for level 19 Beam" );
		final HashMap<Iab, LobbyData> lobbyData = ((PregenLevel0)pregen).lobby;
		LobbyData dao;
		double valueLamp;
		final int y  = this.level_y + Level_000.SUBFLOOR + 1;
		int xx, zz;
		int modX, modZ;
		for (int iz=0; iz<16; iz++) {
			zz = (chunkZ * 16) + iz;
			modZ = (zz < 0 ? 1-zz : zz) % 7;
			for (int ix=0; ix<16; ix++) {
				xx = (chunkX * 16) + ix;
				dao = lobbyData.get(new Iab(ix, iz));
				if (dao == null) continue;
				modX = (xx < 0 ? 1-xx : xx) % 7;
				// beam
				if (modX == 0 || modZ == 0)
					chunk.setBlock(ix, this.level_y+dao.wall_dist+3, iz, block_beam);
				// lantern
				if (modX == 0 && modZ == 0 && dao.wall_dist == 6) {
					valueLamp = this.noiseLamps.getNoise(xx, zz);
					if (valueLamp < 0.0)
						chunk.setBlock(ix, this.level_y+dao.wall_dist+2, iz, Material.LANTERN);
				}
				// attic floor
				chunk.setBlock(ix, this.level_y, iz, Material.BEDROCK);
				for (int iy=0; iy<Level_000.SUBFLOOR; iy++)
					chunk.setBlock(ix, this.level_y+iy+1, iz, block_floor);
				// wall
				if (dao.isWall) {
					for (int iy=0; iy<this.level_h+1; iy++) {
						if (iy > 6) chunk.setBlock(ix, y+iy, iz, Material.BEDROCK);
						else        chunk.setBlock(ix, y+iy, iz, block_wall      );
					}
				}
			} // end ix
		} // end iz
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void initNoise(final ConfigurationSection cfgParams) {
		super.initNoise(cfgParams);
		// lanterns
		this.noiseLamps.setFrequency( cfgParams.getDouble("Noise-Lamps-Freq") );
	}



	@Override
	protected void loadConfig(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// block types
		this.block_wall .set(cfgBlocks.getString("Wall" ));
		this.block_floor.set(cfgBlocks.getString("Floor"));
		this.block_beam .set(cfgBlocks.getString("Beam" ));
	}
	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		cfgParams.addDefault("Enable-Gen",       Boolean.TRUE                             );
		cfgParams.addDefault("Noise-Lamps-Freq", DEFAULT_NOISE_LAMPS_FREQ);
		// block types
		cfgBlocks.addDefault("Wall",  DEFAULT_BLOCK_WALL );
		cfgBlocks.addDefault("Floor", DEFAULT_BLOCK_FLOOR);
		cfgBlocks.addDefault("Beam",  DEFAULT_BLOCK_BEAM );
	}



}
