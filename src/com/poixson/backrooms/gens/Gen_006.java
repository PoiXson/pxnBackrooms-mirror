package com.poixson.backrooms.gens;

import static com.poixson.utils.BlockUtils.StringToBlockDataDef;

import java.util.HashMap;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsWorld;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.gens.Gen_000.LobbyData;
import com.poixson.backrooms.worlds.BackWorld_000;
import com.poixson.backrooms.worlds.BackWorld_000.Pregen_Level_000;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.dao.Iab;
import com.poixson.tools.noise.FastNoiseLiteD;
import com.poixson.tools.noise.FastNoiseLiteD.FractalType;
import com.poixson.tools.plotter.BlockPlotter;


// 6 | Lights Out
public class Gen_006 extends BackroomsGen {

	// default params
	public static final int    DEFAULT_LEVEL_H                   = 6;
	public static final double DEFAULT_NOISE_BUTTONSWITCH_FREQ   = 0.004;
	public static final int    DEFAULT_NOISE_BUTTONSWITCH_OCTAVE = 2;
	public static final double DEFAULT_NOISE_BUTTONSWITCH_GAIN   = 20.0;
	public static final double DEFAULT_NOISE_BUTTONSWITCH_LACUN  = 20.0;
	public static final double DEFAULT_THRESH_BUTTON             = 0.9;
	public static final double DEFAULT_THRESH_SWITCH             = 0.95;
	public static final double DEFAULT_TP_RANGE                  = 8.0;

	// default blocks
	public static final String DEFAULT_BLOCK_FLOOR   = "";
	public static final String DEFAULT_BLOCK_CEILING = "";
	public static final String DEFAULT_BLOCK_WALL    = "minecraft:glowstone";

	// params
	public final boolean enable_gen;
	public final boolean enable_top;
	public final int     level_y;
	public final int     level_h;
	public final double  thresh_button;
	public final double  thresh_switch;
	public final double  tp_range;

	// blocks
	public final String block_floor;
	public final String block_ceiling;
	public final String block_wall;

	// noise
	public final FastNoiseLiteD noiseButtonSwitch;

	public final BackWorld_000 world_000;



	public Gen_006(final BackroomsWorld backworld, final int seed, final BackroomsGen gen_below) {
		super(backworld, gen_below, seed);
		final int level_number = this.getLevelNumber();
		this.world_000 = (BackWorld_000) backworld;
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(level_number);
		final ConfigurationSection cfgBlocks = this.plugin.getConfigLevelBlocks(level_number);
		// params
		this.enable_gen    = cfgParams.getBoolean("Enable-Gen"   );
		this.enable_top    = cfgParams.getBoolean("Enable-Top"   );
		this.level_y       = cfgParams.getInt(    "Level-Y"      );
		this.level_h       = cfgParams.getInt(    "Level-Height" );
		this.thresh_button = cfgParams.getDouble( "Thresh-Button");
		this.thresh_switch = cfgParams.getDouble( "Thresh-Switch");
		this.tp_range      = cfgParams.getDouble( "TP-Range"     );
		// block types
		this.block_floor   = cfgBlocks.getString("Floor"  );
		this.block_ceiling = cfgBlocks.getString("Ceiling");
		this.block_wall    = cfgBlocks.getString("Wall"   );
		// noise
		this.noiseButtonSwitch = this.register(new FastNoiseLiteD());
	}



	@Override
	public int getLevelNumber() {
		return 6;
	}



	@Override
	public int getLevelY() {
		return this.level_y;
	}
	@Override
	public int getOpenY() {
		final BlockData block_floor = StringToBlockDataDef(this.block_floor, DEFAULT_BLOCK_FLOOR);
		return this.getMinY() + (block_floor==null ? 0 : 1);
	}

	@Override
	public int getMinY() {
		return this.getLevelY() + this.bedrock_barrier;
	}
	@Override
	public int getMaxY() {
		final BlockData block_floor   = StringToBlockDataDef(this.block_floor,   DEFAULT_BLOCK_FLOOR  );
		final BlockData block_ceiling = StringToBlockDataDef(this.block_ceiling, DEFAULT_BLOCK_CEILING);
		return (this.getMinY() + this.level_h + (block_floor==null ? 0 : 1) + (block_ceiling==null ? 0 : 1)) - 1;
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!this.enable_gen) return;
		final BlockData block_floor   = StringToBlockDataDef(this.block_floor,   DEFAULT_BLOCK_FLOOR  );
		final BlockData block_ceiling = StringToBlockDataDef(this.block_ceiling, DEFAULT_BLOCK_CEILING);
		final BlockData block_wall    = StringToBlockDataDef(this.block_wall,    DEFAULT_BLOCK_WALL   );
		if (block_wall == null) throw new RuntimeException("Invalid block type for level 6 Wall");
		final HashMap<Iab, LobbyData> data_lobby = ((Pregen_Level_000)pregen).lobby;
		final int h_walls = this.level_h + (block_floor==null ? 0 : 1) + (block_ceiling==null ? 0 : 1);
		final int y_base  = this.level_y + this.bedrock_barrier;
		final int y_ceil  = (y_base + h_walls) - 1;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
				// barrier
				for (int iy=0; iy<this.bedrock_barrier; iy++)
					chunk.setBlock(ix, this.level_y+iy, iz, Material.BEDROCK);
				final LobbyData dao_lobby = data_lobby.get(new Iab(ix, iz));
				if (dao_lobby == null) continue;
				// wall
				if (dao_lobby.isWall) {
					// lobby walls
					for (int iy=0; iy<h_walls; iy++)
						chunk.setBlock(ix, y_base+iy, iz, block_wall);
					// light switch
					if (ix > 0 && ix < 15
					&&  iz > 0 && iz < 15) {
						final LobbyData daoN = data_lobby.get(new Iab(ix, iz-1));
						final LobbyData daoS = data_lobby.get(new Iab(ix, iz+1));
						final LobbyData daoE = data_lobby.get(new Iab(ix+1, iz));
						final LobbyData daoW = data_lobby.get(new Iab(ix-1, iz));
						if (!daoN.isWall) this.generateLightSwitch(BlockFace.NORTH, chunk, chunkX, chunkZ, ix, iz-1); else
						if (!daoS.isWall) this.generateLightSwitch(BlockFace.SOUTH, chunk, chunkX, chunkZ, ix, iz+1); else
						if (!daoE.isWall) this.generateLightSwitch(BlockFace.EAST,  chunk, chunkX, chunkZ, ix+1, iz); else
						if (!daoW.isWall) this.generateLightSwitch(BlockFace.WEST,  chunk, chunkX, chunkZ, ix-1, iz);
					}
				// floor/ceiling
				} else {
					if (this.enable_top)
					if (block_ceiling != null) chunk.setBlock(ix, y_ceil, iz, block_ceiling);
					if (block_floor   != null) chunk.setBlock(ix, y_base, iz, block_floor  );
				}
			} // end ix
		} // end iz
	}

	protected void generateLightSwitch(final BlockFace facing, final ChunkData chunk,
			final int chunkX, final int chunkZ, final int ix, final int iz) {
		final int xx = (chunkX * 16) + ix;
		final int zz = (chunkZ * 16) + iz;
		final double value = this.noiseButtonSwitch.getNoise(xx, zz);
		if (value > this.thresh_button) {
			if (value > this.noiseButtonSwitch.getNoise(xx, zz-1)
			&&  value > this.noiseButtonSwitch.getNoise(xx, zz+1)
			&&  value > this.noiseButtonSwitch.getNoise(xx+1, zz)
			&&  value > this.noiseButtonSwitch.getNoise(xx-1, zz) ) {
				final Gen_000 gen_000 = this.world_000.gen_000;
				// switch
				if (value > this.thresh_switch) {
					this.world_000.portal_000_to_006.addLocation(xx, zz);
					// level 0 light switch
					{
						final int y = gen_000.level_y + gen_000.bedrock_barrier + gen_000.subfloor + 2;
						chunk.setBlock(ix, y, iz, Bukkit.createBlockData(Material.LEVER,
								"[face=wall,facing="+facing.toString().toLowerCase()+",powered=false]"));
					}
					// level 6 light switch
					{
						final int y = this.level_y + this.bedrock_barrier + 1;
						chunk.setBlock(ix, y, iz, Bukkit.createBlockData(Material.LEVER,
								"[face=wall,facing="+facing.toString().toLowerCase()+",powered=true]"));
					}
				// button
				} else {
					this.world_000.portal_006_to_111.addLocation(xx, zz);
					// level 6 game button
					{
						final int y = this.level_y + this.bedrock_barrier + 1;
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
	protected void initNoise() {
		super.initNoise();
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(this.getLevelNumber());
		// light switch
		this.noiseButtonSwitch.setFrequency(        cfgParams.getDouble("Noise-ButtonSwitch-Freq"  ));
		this.noiseButtonSwitch.setFractalType(      FractalType.FBm                                 );
		this.noiseButtonSwitch.setFractalOctaves(   cfgParams.getInt(   "Noise-ButtonSwitch-Octave"));
		this.noiseButtonSwitch.setFractalGain(      cfgParams.getDouble("Noise-ButtonSwitch-Gain"  ));
		this.noiseButtonSwitch.setFractalLacunarity(cfgParams.getDouble("Noise-ButtonSwitch-Lacun" ));
	}



	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		cfgParams.addDefault("Enable-Gen",                Boolean.TRUE                                      );
		cfgParams.addDefault("Enable-Top",                Boolean.TRUE                                      );
		cfgParams.addDefault("Level-Y",                   Integer.valueOf(this.getDefaultY()               ));
		cfgParams.addDefault("Level-Height",              Integer.valueOf(DEFAULT_LEVEL_H                  ));
		// light switch
		cfgParams.addDefault("Noise-ButtonSwitch-Freq",   Double .valueOf(DEFAULT_NOISE_BUTTONSWITCH_FREQ  ));
		cfgParams.addDefault("Noise-ButtonSwitch-Octave", Integer.valueOf(DEFAULT_NOISE_BUTTONSWITCH_OCTAVE));
		cfgParams.addDefault("Noise-ButtonSwitch-Gain",   Double .valueOf(DEFAULT_NOISE_BUTTONSWITCH_GAIN  ));
		cfgParams.addDefault("Noise-ButtonSwitch-Lacun",  Double .valueOf(DEFAULT_NOISE_BUTTONSWITCH_LACUN ));
		cfgParams.addDefault("Thresh-Button",             Double .valueOf(DEFAULT_THRESH_BUTTON            ));
		cfgParams.addDefault("Thresh-Switch",             Double .valueOf(DEFAULT_THRESH_SWITCH            ));
		cfgParams.addDefault("TP-Range",                  Double .valueOf(DEFAULT_TP_RANGE                 ));
		// block types
		cfgBlocks.addDefault("Floor",   DEFAULT_BLOCK_FLOOR  );
		cfgBlocks.addDefault("Ceiling", DEFAULT_BLOCK_CEILING);
		cfgBlocks.addDefault("Wall",    DEFAULT_BLOCK_WALL   );
	}



}
