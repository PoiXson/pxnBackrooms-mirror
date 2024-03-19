package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_000.ENABLE_GEN_006;
import static com.poixson.backrooms.worlds.Level_000.SUBFLOOR;
import static com.poixson.utils.BlockUtils.StringToBlockData;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.gens.Gen_000.LobbyData;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.backrooms.worlds.Level_000.PregenLevel0;
import com.poixson.tools.abstractions.AtomicDouble;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.dao.Iab;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.FractalType;


// 6 | Lights Out
public class Gen_006 extends BackroomsGen {

	// default params
	public static final double DEFAULT_NOISE_BUTTONSWITCH_FREQ   = 0.004;
	public static final int    DEFAULT_NOISE_BUTTONSWITCH_OCTAVE = 2;
	public static final double DEFAULT_NOISE_BUTTONSWITCH_GAIN   = 20.0;
	public static final double DEFAULT_NOISE_BUTTONSWITCH_LAC    = 20.0;
	public static final double DEFAULT_THRESH_BUTTON             = 0.9;
	public static final double DEFAULT_THRESH_SWITCH             = 0.95;
	public static final double DEFAULT_TP_RANGE                  = 8.0;

	// default blocks
	public static final String DEFAULT_BLOCK_WALL = "minecraft:glowstone";

	// noise
	public final FastNoiseLiteD noiseButtonSwitch;

	// params
	public final AtomicDouble thresh_button = new AtomicDouble( DEFAULT_THRESH_BUTTON);
	public final AtomicDouble thresh_switch = new AtomicDouble( DEFAULT_THRESH_SWITCH);
	public final AtomicDouble tp_range      = new AtomicDouble( DEFAULT_TP_RANGE     );

	// blocks
	public final AtomicReference<String> block_wall = new AtomicReference<String>(null);



	public Gen_006(final BackroomsLevel backlevel, final int seed,
			final int level_y, final int level_h) {
		super(backlevel, seed, level_y, level_h);
		// noise
		this.noiseButtonSwitch = this.register(new FastNoiseLiteD());
	}



	@Override
	public int getLevelNumber() {
		return 6;
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
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
		final double thresh_button = this.thresh_button.get();
		final double thresh_switch = this.thresh_switch.get();
		final int xx = (chunkX * 16) + ix;
		final int zz = (chunkZ * 16) + iz;
		final double value = this.noiseButtonSwitch.getNoise(xx, zz);
		if (value > thresh_button) {
			if (value > this.noiseButtonSwitch.getNoise(xx, zz-1)
			&&  value > this.noiseButtonSwitch.getNoise(xx, zz+1)
			&&  value > this.noiseButtonSwitch.getNoise(xx+1, zz)
			&&  value > this.noiseButtonSwitch.getNoise(xx-1, zz) ) {
				// switch
				if (value > thresh_switch) {
					((Level_000)this.backlevel).portal_0_to_6.add(xx, zz);
					// level 0 light switch
					{
						final int y = ((Level_000)this.backlevel).gen_000.level_y + SUBFLOOR + 3;
						chunk.setBlock(ix, y, iz, Bukkit.createBlockData(Material.LEVER,
								"[face=wall,facing="+facing.toString().toLowerCase()+",powered=false]"));
					}
					// level 6 light switch
					{
						final int y = this.level_y + 2;
						chunk.setBlock(ix, y, iz, Bukkit.createBlockData(Material.LEVER,
								"[face=wall,facing="+facing.toString().toLowerCase()+",powered=true]"));
					}
				// button
				} else {
					((Level_000)this.backlevel).portal_6_to_33.add(xx, zz);
					// level 6 game button
					{
						final int y = this.level_y + 2;
						chunk.setBlock(ix, y, iz, Bukkit.createBlockData(Material.DARK_OAK_BUTTON,
								"[face=wall,facing="+facing.toString().toLowerCase()+"]"));
					}
				}
			}
		}
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void initNoise(final ConfigurationSection cfgParams) {
		super.initNoise(cfgParams);
		// light switch
		this.noiseButtonSwitch.setFrequency(         cfgParams.getDouble("Noise-ButtonSwitch-Freq"  ) );
		this.noiseButtonSwitch.setFractalType(       FractalType.FBm                                  );
		this.noiseButtonSwitch.setFractalOctaves(    cfgParams.getInt(   "Noise-ButtonSwitch-Octave") );
		this.noiseButtonSwitch.setFractalGain(       cfgParams.getDouble("Noise-ButtonSwitch-Gain"  ) );
		this.noiseButtonSwitch.setFractalLacunarity( cfgParams.getDouble("Noise-ButtonSwitch-Lac"   ) );
	}



	@Override
	protected void loadConfig(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		this.thresh_button.set(cfgParams.getDouble("Thresh-Button"));
		this.thresh_switch.set(cfgParams.getDouble("Thresh-Switch"));
		this.tp_range     .set(cfgParams.getDouble("TP-Range"     ));
		// block types
		this.block_wall.set(cfgBlocks.getString("Wall"));
	}
	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		cfgParams.addDefault("Noise-ButtonSwitch-Freq",   DEFAULT_NOISE_BUTTONSWITCH_FREQ  );
		cfgParams.addDefault("Noise-ButtonSwitch-Octave", DEFAULT_NOISE_BUTTONSWITCH_OCTAVE);
		cfgParams.addDefault("Noise-ButtonSwitch-Gain",   DEFAULT_NOISE_BUTTONSWITCH_GAIN  );
		cfgParams.addDefault("Noise-ButtonSwitch-Lac",    DEFAULT_NOISE_BUTTONSWITCH_LAC   );
		cfgParams.addDefault("Thresh-Button",             DEFAULT_THRESH_BUTTON            );
		cfgParams.addDefault("Thresh-Switch",             DEFAULT_THRESH_SWITCH            );
		cfgParams.addDefault("TP-Range",                  DEFAULT_TP_RANGE                 );
		// block types
		cfgBlocks.addDefault("Wall", DEFAULT_BLOCK_WALL);
	}



}
