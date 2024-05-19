package com.poixson.backrooms.gens;

import static com.poixson.utils.BlockUtils.StringToBlockDataDef;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsWorld;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.worlds.Level_000.Pregen_Level_000;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.dao.Iab;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;


// 2 | Pipe Dreams
public class Gen_002 extends BackroomsGen {

	// default params
	public static final int    DEFAULT_LEVEL_H                    = 2;
	public static final int    DEFAULT_SUBFLOOR                   = 3;
	public static final int    DEFAULT_NOISE_TUNNEL_COUNT         = 4;
	public static final double DEFAULT_NOISE_TUNNEL_FREQ          = 0.01;
	public static final double DEFAULT_NOISE_TUNNEL_FREQ_ADJUST   = 0.015;
	public static final double DEFAULT_NOISE_TUNNEL_JITTER        = 0.8;
	public static final double DEFAULT_NOISE_TUNNEL_JITTER_ADJUST = 2.0;
	public static final double DEFAULT_THRESH_TUNNEL              = 0.93;

	// default blocks
	public static final String DEFAULT_BLOCK_SUBFLOOR = "minecraft:cobblestone";
	public static final String DEFAULT_BLOCK_CEILING  = "minecraft:bricks";
	public static final String DEFAULT_BLOCK_FLOOR    = "minecraft:bricks";
	public static final String DEFAULT_BLOCK_WALLS    = "minecraft:bricks";
	public static final String DEFAULT_BLOCK_FILL     = "minecraft:cobblestone";

	// params
	public final boolean enable_gen;
	public final boolean enable_top;
	public final int     level_y;
	public final int     level_h;
	public final int     subfloor;
	public final int     noiseTunnelCount;
	public final double  thresh_tunnel;

	// blocks
	public final String block_subfloor;
	public final String block_ceiling;
	public final String block_floor;
	public final String block_walls;
	public final String block_fill;

	// noise
	public final FastNoiseLiteD noiseTunnels[];



	public Gen_002(final BackroomsWorld backworld, final int seed, final BackroomsGen gen_below)
			throws InvalidConfigurationException {
		super(backworld, gen_below, seed);
		final int level_number = this.getLevelNumber();
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(level_number);
		final ConfigurationSection cfgBlocks = this.plugin.getConfigLevelBlocks(level_number);
		// params
		this.enable_gen       = cfgParams.getBoolean("Enable-Gen"        );
		this.enable_top       = cfgParams.getBoolean("Enable-Top"        );
		this.level_y          = cfgParams.getInt(    "Level-Y"           );
		this.level_h          = cfgParams.getInt(    "Level-Height"      );
		this.subfloor         = cfgParams.getInt(    "SubFloor"          );
		this.noiseTunnelCount = cfgParams.getInt(    "Noise-Tunnel-Count");
		this.thresh_tunnel    = cfgParams.getDouble( "Thresh-Tunnel"     );
		// block types
		this.block_subfloor = cfgBlocks.getString("SubFloor");
		this.block_ceiling  = cfgBlocks.getString("Ceiling" );
		this.block_floor    = cfgBlocks.getString("Floor"   );
		this.block_walls    = cfgBlocks.getString("Walls"   );
		this.block_fill     = cfgBlocks.getString("Fill"    );
		// noise
		this.noiseTunnels = new FastNoiseLiteD[this.noiseTunnelCount];
		for (int i=0; i<this.noiseTunnelCount; i++)
			this.noiseTunnels[i] = this.register(new FastNoiseLiteD());
	}



	@Override
	public int getLevelNumber() {
		return 2;
	}

	@Override
	public int getNextY() {
		return this.level_y + this.level_h + this.subfloor + 2;
	}



	public class PipeData implements PreGenData {

		public final double value_tunnel;
		public final double values_tunnel[];
		public final boolean isTunnel;
		public       boolean isWall = false;

		public PipeData(final int x, final int z) {
			final int noiseTunnelCount = Gen_002.this.noiseTunnelCount;
			this.values_tunnel = new double[noiseTunnelCount];
			double highest = -1.0;
			for (int i=0; i<noiseTunnelCount; i++) {
				final double value = Gen_002.this.noiseTunnels[i].getNoiseRot(x, z, 0.25);
				this.values_tunnel[i] = value;
				if (highest < value)
					highest = value;
			}
			this.value_tunnel = highest;
			this.isTunnel = (this.value_tunnel > Gen_002.this.thresh_tunnel);
		}

	}



	public void pregenerate(Map<Iab, PipeData> data,
			final int chunkX, final int chunkZ) {
		// tunnel noise
		for (int iz=-1; iz<17; iz++) {
			final int zz = (chunkZ * 16) + iz;
			for (int ix=-1; ix<17; ix++) {
				final int xx = (chunkX * 16) + ix;
				final PipeData dao = new PipeData(xx, zz);
				data.put(new Iab(ix, iz), dao);
			}
		}
		// find walls
		for (int iz=0; iz<16; iz++) {
			X_LOOP:
			for (int ix=0; ix<16; ix++) {
				final PipeData dao = data.get(new Iab(ix, iz));
				if (!dao.isTunnel) {
					if (data.get(new Iab(ix,   iz-1)).isTunnel) { dao.isWall = true; continue X_LOOP; } // north
					if (data.get(new Iab(ix,   iz+1)).isTunnel) { dao.isWall = true; continue X_LOOP; } // south
					if (data.get(new Iab(ix+1, iz  )).isTunnel) { dao.isWall = true; continue X_LOOP; } // east
					if (data.get(new Iab(ix-1, iz  )).isTunnel) { dao.isWall = true; continue X_LOOP; } // west
					if (data.get(new Iab(ix+1, iz-1)).isTunnel) { dao.isWall = true; continue X_LOOP; } // north/east
					if (data.get(new Iab(ix-1, iz-1)).isTunnel) { dao.isWall = true; continue X_LOOP; } // north/west
					if (data.get(new Iab(ix+1, iz+1)).isTunnel) { dao.isWall = true; continue X_LOOP; } // south/east
					if (data.get(new Iab(ix-1, iz+1)).isTunnel) { dao.isWall = true; continue X_LOOP; } // south/west
				}
			}
		}
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!this.enable_gen) return;
		final BlockData block_subfloor = StringToBlockDataDef(this.block_subfloor, DEFAULT_BLOCK_SUBFLOOR);
		final BlockData block_ceiling  = StringToBlockDataDef(this.block_ceiling,  DEFAULT_BLOCK_CEILING );
		final BlockData block_floor    = StringToBlockDataDef(this.block_floor,    DEFAULT_BLOCK_FLOOR   );
		final BlockData block_walls    = StringToBlockDataDef(this.block_walls,    DEFAULT_BLOCK_WALLS   );
		final BlockData block_fill     = StringToBlockDataDef(this.block_fill,     DEFAULT_BLOCK_FILL    );
		if (block_subfloor == null) throw new RuntimeException("Invalid block type for level 23 SubFloor" );
		if (block_ceiling    == null) throw new RuntimeException("Invalid block type for level 23 Ceiling");
		if (block_floor      == null) throw new RuntimeException("Invalid block type for level 23 Floor"  );
		if (block_walls      == null) throw new RuntimeException("Invalid block type for level 23 Walls"  );
		if (block_fill       == null) throw new RuntimeException("Invalid block type for level 23 Fill"   );
		final Pregen_Level_000 pregen_000 = (Pregen_Level_000) pregen;
		final HashMap<Iab, PipeData> data_pipes = pregen_000.pipes;
		final int height  = this.level_h + (this.enable_top? 1 : 0 ) + 1;
		final int y_floor = this.level_y;
		final int y_ceil  = (y_floor + height) - 1;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
				final PipeData dao_tunnels = data_pipes.get(new Iab(ix, iz));
				// tunnel wall
				if (dao_tunnels.isWall) {
					for (int iy=0; iy<height; iy++)
						chunk.setBlock(ix, y_floor+iy, iz, block_walls);
				} else
				// inside tunnel
				if (dao_tunnels.isTunnel) {
					chunk.setBlock(ix, y_floor, iz, block_floor);
					if (this.enable_top)
						chunk.setBlock(ix, y_ceil, iz, block_ceiling);
				// fill
				} else {
					if (this.enable_top) {
						for (int iy=0; iy<height; iy++)
							chunk.setBlock(ix, y_floor+iy, iz, block_fill);
					}
				}
//				// subceiling
//				if (this.enable_top) {
//					for (int iy=0; iy<this.subceiling; iy++)
//						chunk.setBlock(ix, y_ceil+iy+1, iz, block_subceiling);
//				}
			} // end ix
		} // end iz
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void initNoise() {
		super.initNoise();
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(this.getLevelNumber());
		// tunnels
		final double noiseTunnelFreq         = cfgParams.getDouble("Noise-Tunnel-Freq"         );
		final double noiseTunnelJitter       = cfgParams.getDouble("Noise-Tunnel-Jitter"       );
		final double noiseTunnelFreqAdjust   = cfgParams.getDouble("Noise-Tunnel-Freq-Adjust"  );
		final double noiseTunnelJitterAdjust = cfgParams.getDouble("Noise-Tunnel-Jitter-Adjust");
		for (int i=0; i<this.noiseTunnelCount; i++) {
			final double ii = (double) i;
			final double freq_adjusted   = noiseTunnelFreq   * (1.0 - (ii *noiseTunnelFreqAdjust) );
			final double jitter_adjusted = noiseTunnelJitter * (  (1.0+ii)*noiseTunnelJitterAdjust);
			this.noiseTunnels[i].setFrequency(               freq_adjusted                     );
			this.noiseTunnels[i].setCellularJitter(          jitter_adjusted                   );
			this.noiseTunnels[i].setNoiseType(               NoiseType.Cellular                );
			this.noiseTunnels[i].setFractalType(             FractalType.PingPong              );
			this.noiseTunnels[i].setCellularDistanceFunction(CellularDistanceFunction.Manhattan);
		}
	}



	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		cfgParams.addDefault("Enable-Gen",                 Boolean.TRUE                                       );
		cfgParams.addDefault("Enable-Top",                 Boolean.TRUE                                       );
		cfgParams.addDefault("Level-Y",                    Integer.valueOf(this.getDefaultY()                ));
		cfgParams.addDefault("Level-Height",               Integer.valueOf(DEFAULT_LEVEL_H                   ));
		cfgParams.addDefault("SubFloor",                   Integer.valueOf(DEFAULT_SUBFLOOR                  ));
		cfgParams.addDefault("Noise-Tunnel-Count",         Integer.valueOf(DEFAULT_NOISE_TUNNEL_COUNT        ));
		cfgParams.addDefault("Noise-Tunnel-Freq",          Double .valueOf(DEFAULT_NOISE_TUNNEL_FREQ         ));
		cfgParams.addDefault("Noise-Tunnel-Jitter",        Double .valueOf(DEFAULT_NOISE_TUNNEL_JITTER       ));
		cfgParams.addDefault("Noise-Tunnel-Freq-Adjust",   Double .valueOf(DEFAULT_NOISE_TUNNEL_FREQ_ADJUST  ));
		cfgParams.addDefault("Noise-Tunnel-Jitter-Adjust", Double .valueOf(DEFAULT_NOISE_TUNNEL_JITTER_ADJUST));
		cfgParams.addDefault("Thresh-Tunnel",              Double .valueOf(DEFAULT_THRESH_TUNNEL             ));
		// block types
		cfgBlocks.addDefault("SubFloor", DEFAULT_BLOCK_SUBFLOOR);
		cfgBlocks.addDefault("Ceiling",  DEFAULT_BLOCK_CEILING );
		cfgBlocks.addDefault("Floor",    DEFAULT_BLOCK_FLOOR   );
		cfgBlocks.addDefault("Walls",    DEFAULT_BLOCK_WALLS   );
		cfgBlocks.addDefault("Fill",     DEFAULT_BLOCK_FILL    );
	}



}
