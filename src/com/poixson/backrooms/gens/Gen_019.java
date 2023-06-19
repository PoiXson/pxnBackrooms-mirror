package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_000.ENABLE_GEN_019;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.gens.Gen_000.LobbyData;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.backrooms.worlds.Level_000.PregenLevel0;
import com.poixson.commonmc.tools.plotter.BlockPlotter;
import com.poixson.tools.dao.Iab;
import com.poixson.utils.FastNoiseLiteD;


// 19 | Attic
public class Gen_019 extends BackroomsGen {

	// noise
	public final FastNoiseLiteD noiseLamps;

	// blocks
	public final AtomicReference<String> block_wall  = new AtomicReference<String>(null);
	public final AtomicReference<String> block_floor = new AtomicReference<String>(null);
	public final AtomicReference<String> block_beam  = new AtomicReference<String>(null);



	public Gen_019(final BackroomsLevel backlevel,
			final int level_y, final int level_h) {
		super(backlevel, level_y, level_h);
		// lanterns
		this.noiseLamps = this.register(new FastNoiseLiteD());
		this.noiseLamps.setFrequency(0.045);
		this.noiseLamps.setFractalOctaves(1);
	}



	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
		if (!ENABLE_GEN_019) return;
		final Material block_wall  = Material.matchMaterial(this.block_wall .get());
		final Material block_floor = Material.matchMaterial(this.block_floor.get());
		final Material block_beam  = Material.matchMaterial(this.block_beam .get());
		if (block_wall  == null) throw new RuntimeException("Invalid block type for level 19 Wall" );
		if (block_floor == null) throw new RuntimeException("Invalid block type for level 19 Floor");
		if (block_beam  == null) throw new RuntimeException("Invalid block type for level 19 Beam" );
		final HashMap<Iab, LobbyData> lobbyData = ((PregenLevel0)pregen).lobby;
		LobbyData dao;
		double valueLamp;
		final int y  = this.level_y + Level_000.SUBFLOOR + 1;
		int xx, zz;
		int modX7, modZ7;
		for (int iz=0; iz<16; iz++) {
			zz = (chunkZ * 16) + iz;
			for (int ix=0; ix<16; ix++) {
				xx = (chunkX * 16) + ix;
				dao = lobbyData.get(new Iab(ix, iz));
				if (dao == null) continue;
				modX7 = (xx < 0 ? 1-xx : xx) % 7;
				modZ7 = (zz < 0 ? 1-zz : zz) % 7;
				// beam
				if (modX7 == 0 || modZ7 == 0)
					chunk.setBlock(ix, this.level_y+dao.wall_dist+3, iz, block_beam);
				// lantern
				if (modX7 == 0 && modZ7 == 0 && dao.wall_dist == 6) {
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
					for (int iy=0; iy<this.level_h+1; iy++)
						chunk.setBlock(ix, y+iy, iz, (iy>6 ? Material.BEDROCK : block_wall));
				}
			} // end ix
		} // end iz
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void loadConfig() {
		final ConfigurationSection cfg = this.plugin.getLevelBlocks(19);
		this.block_wall .set(cfg.getString("Wall" ));
		this.block_floor.set(cfg.getString("Floor"));
		this.block_beam .set(cfg.getString("Beam" ));
	}
	public static void ConfigDefaults(final FileConfiguration cfg) {
		cfg.addDefault("Level19.Blocks.Wall",  "minecraft:spruce_planks");
		cfg.addDefault("Level19.Blocks.Floor", "minecraft:spruce_planks");
		cfg.addDefault("Level19.Blocks.Beam",  "minecraft:spruce_wood"  );
	}



}
