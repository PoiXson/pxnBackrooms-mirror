package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_000.ENABLE_GEN_006;
import static com.poixson.backrooms.worlds.Level_000.SUBFLOOR;
import static com.poixson.utils.BlockUtils.StringToBlockData;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.FaceAttachable.AttachedFace;
import org.bukkit.block.data.type.Switch;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.gens.Gen_000.LobbyData;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.backrooms.worlds.Level_000.PregenLevel0;
import com.poixson.tools.abstractions.AtomicDouble;
import com.poixson.tools.dao.Iab;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.utils.FastNoiseLiteD;


// 6 | Lights Out
public class Gen_006 extends BackroomsGen {

	// default params
	public static final double DEFAULT_NOISE_SWITCH_FREQ   = 0.008;
	public static final int    DEFAULT_NOISE_SWITCH_OCTAVE = 2;

	// default blocks
	public static final String DEFAULT_BLOCK_WALL = "minecraft:glowstone";

	// noise
	public final FastNoiseLiteD noiseLightSwitch;

	// params
	public final AtomicDouble  noise_switch_freq   = new AtomicDouble( DEFAULT_NOISE_SWITCH_FREQ  );
	public final AtomicInteger noise_switch_octave = new AtomicInteger(DEFAULT_NOISE_SWITCH_OCTAVE);

	// blocks
	public final AtomicReference<String> block_wall = new AtomicReference<String>(null);



	public Gen_006(final BackroomsLevel backlevel,
			final int level_y, final int level_h) {
		super(backlevel, level_y, level_h);
		// noise
		this.noiseLightSwitch = this.register(new FastNoiseLiteD());
	}



	@Override
	public void initNoise() {
		// light switch
		this.noiseLightSwitch.setFrequency(     this.noise_switch_freq  .get());
		this.noiseLightSwitch.setFractalOctaves(this.noise_switch_octave.get());
	}



	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
		if (!ENABLE_GEN_006) return;
		final BlockData block_wall = StringToBlockData(this.block_wall, DEFAULT_BLOCK_WALL);
		if (block_wall == null) throw new RuntimeException("Invalid block type for level 6 Wall");
		final HashMap<Iab, LobbyData> lobbyData = ((PregenLevel0)pregen).lobby;
		LobbyData dao, daoN, daoS, daoE, daoW;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
				// floor
				chunk.setBlock(ix, this.level_y, iz, Material.BEDROCK);
				dao = lobbyData.get(new Iab(ix, iz));
				if (dao == null) continue;
				// wall
				if (dao.isWall) {
					// lobby walls
					for (int iy=0; iy<this.level_h; iy++)
						chunk.setBlock(ix, this.level_y+iy+1, iz, block_wall);
					// light switch
					if (ix > 0 && ix < 15
					&&  iz > 0 && iz < 15) {
						daoN = lobbyData.get(new Iab(ix, iz-1));
						daoS = lobbyData.get(new Iab(ix, iz+1));
						daoE = lobbyData.get(new Iab(ix+1, iz));
						daoW = lobbyData.get(new Iab(ix-1, iz));
						if (!daoN.isWall) this.generateLightSwitch(BlockFace.NORTH, chunk, chunkX, chunkZ, ix, iz-1); else
						if (!daoS.isWall) this.generateLightSwitch(BlockFace.SOUTH, chunk, chunkX, chunkZ, ix, iz+1); else
						if (!daoE.isWall) this.generateLightSwitch(BlockFace.EAST,  chunk, chunkX, chunkZ, ix+1, iz); else
						if (!daoW.isWall) this.generateLightSwitch(BlockFace.WEST,  chunk, chunkX, chunkZ, ix-1, iz);
					}
				}
			} // end ix
		} // end iz
	}

	protected void generateLightSwitch(final BlockFace facing, final ChunkData chunk,
			final int chunkX, final int chunkZ, final int ix, final int iz) {
		final int xx = (chunkX * 16) + ix;
		final int zz = (chunkZ * 16) + iz;
		final double value  = this.noiseLightSwitch.getNoise(xx, zz  );
		if (value > this.noiseLightSwitch.getNoise(xx, zz-1)
		&&  value > this.noiseLightSwitch.getNoise(xx, zz+1)
		&&  value > this.noiseLightSwitch.getNoise(xx+1, zz)
		&&  value > this.noiseLightSwitch.getNoise(xx-1, zz) ) {
			((Level_000)this.backlevel).portal_0_to_6.add(xx, zz);
			// level 0 light switch
			{
				final int y = ((Level_000)this.backlevel).gen_000.level_y + SUBFLOOR + 3;
				chunk.setBlock(ix, y, iz, Material.LEVER);
				final Switch lever = (Switch) chunk.getBlockData(ix, y, iz);
				lever.setAttachedFace(AttachedFace.WALL);
				lever.setFacing(facing);
				lever.setPowered(false);
				chunk.setBlock(ix, y, iz, lever);
			}
			// level 6 light switch
			{
				final int y = this.level_y + 2;
				chunk.setBlock(ix, y, iz, Material.LEVER);
				final Switch lever = (Switch) chunk.getBlockData(ix, y, iz);
				lever.setAttachedFace(AttachedFace.WALL);
				lever.setFacing(facing);
				lever.setPowered(true);
				chunk.setBlock(ix, y, iz, lever);
			}
		}
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void loadConfig() {
		// params
		{
			final ConfigurationSection cfg = this.plugin.getLevelParams(6);
			this.noise_switch_freq  .set(cfg.getDouble("Noise-Switch-Freq"  ));
			this.noise_switch_octave.set(cfg.getInt(   "Noise-Switch-Octave"));
		}
		// block types
		{
			final ConfigurationSection cfg = this.plugin.getLevelBlocks(6);
			this.block_wall.set(cfg.getString("Wall"));
		}
	}
	public static void ConfigDefaults(final FileConfiguration cfg) {
		// params
		cfg.addDefault("Level6.Params.Noise-Switch-Freq",   DEFAULT_NOISE_SWITCH_FREQ  );
		cfg.addDefault("Level6.Params.Noise-Switch-Octave", DEFAULT_NOISE_SWITCH_OCTAVE);
		// block types
		cfg.addDefault("Level6.Blocks.Wall", DEFAULT_BLOCK_WALL);
	}



}
