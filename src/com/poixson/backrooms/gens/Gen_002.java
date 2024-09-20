package com.poixson.backrooms.gens;

import static com.poixson.utils.BlockUtils.StringToBlockDataDef;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.Material;
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
	public static final int    DEFAULT_LEVEL_H                    = 3;
	public static final int    DEFAULT_SUBFLOOR                   = 3;
	public static final int    DEFAULT_NOISE_TUNNEL_COUNT         = 4;
	public static final double DEFAULT_NOISE_TUNNEL_FREQ          = 0.02;
	public static final double DEFAULT_NOISE_TUNNEL_FREQ_ADJUST   = 0.015;
	public static final double DEFAULT_NOISE_TUNNEL_JITTER        = 0.8;
	public static final double DEFAULT_NOISE_TUNNEL_JITTER_ADJUST = 2.0;
	public static final double DEFAULT_THRESH_TUNNEL              = 0.87;

	// default blocks
	public static final String DEFAULT_BLOCK_SUBFLOOR = "minecraft:cobblestone";
	public static final String DEFAULT_BLOCK_CEILING  = "minecraft:bricks";
	public static final String DEFAULT_BLOCK_FLOOR    = "minecraft:bricks";
	public static final String DEFAULT_BLOCK_WALLS    = "minecraft:bricks";
	public static final String DEFAULT_BLOCK_FILL     = "minecraft:cobblestone";
	public static final String DEFAULT_BLOCK_PIPE_END = "minecraft:copper_block";
	public static final String DEFAULT_BLOCK_PIPE_NS  = "minecraft:lightning_rod[facing=north]";
	public static final String DEFAULT_BLOCK_PIPE_EW  = "minecraft:lightning_rod[facing=east]";
	public static final String DEFAULT_BLOCK_LIGHT    = "minecraft:light[level=6]";

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
	public final String block_pipe_end;
	public final String block_pipe_ns;
	public final String block_pipe_ew;
	public final String block_light;

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
		this.block_pipe_end = cfgBlocks.getString("Pipe-End");
		this.block_pipe_ns  = cfgBlocks.getString("Pipe-NS" );
		this.block_pipe_ew  = cfgBlocks.getString("Pipe-EW" );
		this.block_light    = cfgBlocks.getString("Light"   );
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
	public int getLevelY() {
		return this.level_y;
	}
	@Override
	public int getOpenY() {
		return this.getMinY() + this.subfloor + 1;
	}

	@Override
	public int getMinY() {
		return this.getLevelY() + this.bedrock_barrier;
	}
	@Override
	public int getMaxY() {
		return this.getMinY() + this.subfloor + this.level_h + 1;
	}



	public class PipeData implements PreGenData {

		public final double value_tunnel;
		public final double values_tunnel[];
		public final boolean isTunnel;
		public       boolean isWall = false;
		public final int    modPipe;
		public       char   isPipeHigh = ' ';
		public       char   isPipeLow  = ' ';

		public PipeData(final int x, final int z) {
			final int noiseTunnelCount = Gen_002.this.noiseTunnelCount;
			this.values_tunnel = new double[noiseTunnelCount];
			double highest = -1.0;
			for (int i=0; i<noiseTunnelCount; i++) {
				final double value = Gen_002.this.noiseTunnels[i].getNoise(x, z);
				this.values_tunnel[i] = value;
				if (highest < value)
					highest = value;
			}
			this.value_tunnel = highest;
			this.isTunnel = (this.value_tunnel > Gen_002.this.thresh_tunnel);
			if (this.isTunnel) this.modPipe = ((int)(Math.floor(this.value_tunnel * 100.0) % 10.0)) % 7;
			else               this.modPipe = -1;
		}

	}



	public void pregenerate(Map<Iab, PipeData> data,
			final int chunkX, final int chunkZ) {
		// tunnel noise
		for (int iz=-1; iz<17; iz++) {
			final int zz = (chunkZ * 16) + iz;
			for (int ix=-1; ix<17; ix++) {
				final int xx = (chunkX * 16) + ix;
				data.put(
					new Iab(ix, iz),
					new PipeData(xx, zz)
				);
			}
		}
		// find walls/pipes
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
				final PipeData dao    = data.get(new Iab(ix,   iz  ));
				final PipeData dao_n  = data.get(new Iab(ix,   iz-1));
				final PipeData dao_s  = data.get(new Iab(ix,   iz+1));
				final PipeData dao_e  = data.get(new Iab(ix+1, iz  ));
				final PipeData dao_w  = data.get(new Iab(ix-1, iz  ));
				final PipeData dao_ne = data.get(new Iab(ix+1, iz-1));
				final PipeData dao_nw = data.get(new Iab(ix-1, iz-1));
				final PipeData dao_se = data.get(new Iab(ix+1, iz+1));
				final PipeData dao_sw = data.get(new Iab(ix-1, iz+1));
				// pipes
				if (dao.isTunnel) {
					// upper pipes
					if (dao.modPipe == 1) {
						if (dao_n.modPipe == 1 && dao_s.modPipe == 1 && dao_e.modPipe != 1 && dao_w.modPipe != 1) dao.isPipeHigh = 'n'; else
						if (dao_e.modPipe == 1 && dao_w.modPipe == 1 && dao_n.modPipe != 1 && dao_s.modPipe != 1) dao.isPipeHigh = 'e'; else
						if (dao_n.modPipe == 1 || dao_s.modPipe == 1 || dao_e.modPipe == 1 || dao_w.modPipe == 1) dao.isPipeHigh = 'x';
					}
					// lower pipes
					if (dao.modPipe == 3) {
						if (dao_n.modPipe == 3 && dao_s.modPipe == 3 && dao_e.modPipe != 3 && dao_w.modPipe != 3) dao.isPipeLow = 'n'; else
						if (dao_e.modPipe == 3 && dao_w.modPipe == 3 && dao_n.modPipe != 3 && dao_s.modPipe != 3) dao.isPipeLow = 'e'; else
						if (dao_n.modPipe == 3 || dao_s.modPipe == 3 || dao_e.modPipe == 3 || dao_w.modPipe == 3) dao.isPipeLow = 'x';
					}
				// tunnel walls
				} else {
					if (dao_n .isTunnel) dao.isWall = true; else
					if (dao_s .isTunnel) dao.isWall = true; else
					if (dao_e .isTunnel) dao.isWall = true; else
					if (dao_w .isTunnel) dao.isWall = true; else
					if (dao_ne.isTunnel) dao.isWall = true; else
					if (dao_nw.isTunnel) dao.isWall = true; else
					if (dao_se.isTunnel) dao.isWall = true; else
					if (dao_sw.isTunnel) dao.isWall = true;
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
		final BlockData block_pipe_end = StringToBlockDataDef(this.block_pipe_end, DEFAULT_BLOCK_PIPE_END);
		final BlockData block_pipe_ns  = StringToBlockDataDef(this.block_pipe_ns,  DEFAULT_BLOCK_PIPE_NS );
		final BlockData block_pipe_ew  = StringToBlockDataDef(this.block_pipe_ew,  DEFAULT_BLOCK_PIPE_EW );
		final BlockData block_light    = StringToBlockDataDef(this.block_light,    DEFAULT_BLOCK_LIGHT   );
		if (block_subfloor == null) throw new RuntimeException("Invalid block type for level 23 SubFloor");
		if (block_ceiling  == null) throw new RuntimeException("Invalid block type for level 23 Ceiling" );
		if (block_floor    == null) throw new RuntimeException("Invalid block type for level 23 Floor"   );
		if (block_walls    == null) throw new RuntimeException("Invalid block type for level 23 Walls"   );
		if (block_fill     == null) throw new RuntimeException("Invalid block type for level 23 Fill"    );
		if (block_pipe_end == null) throw new RuntimeException("Invalid block type for level 23 Pipe-End");
		if (block_pipe_ns  == null) throw new RuntimeException("Invalid block type for level 23 Pipe-NS" );
		if (block_pipe_ew  == null) throw new RuntimeException("Invalid block type for level 23 Pipe-EW" );
		if (block_light    == null) throw new RuntimeException("Invalid block type for level 23 Light"   );
		final Pregen_Level_000 pregen_000 = (Pregen_Level_000) pregen;
		final HashMap<Iab, PipeData> data_pipes = pregen_000.pipes;
		final int y_base  = this.level_y + this.bedrock_barrier;
		final int h_walls = this.level_h + (this.enable_top? 1 : 0 ) + 1;
		final int y_floor = y_base + this.subfloor;
		final int y_ceil  = y_floor + this.level_h + 1;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
				final PipeData dao_tunnels = data_pipes.get(new Iab(ix, iz));
				// barrier
				for (int iy=0; iy<this.bedrock_barrier; iy++)
					chunk.setBlock(ix, this.level_y+iy, iz, Material.BEDROCK);
				// subfloor
				if (this.enable_top) {
					if (this.subfloor > 0) {
						for (int iy=0; iy<this.subfloor; iy++)
							chunk.setBlock(ix, y_base+iy, iz, block_subfloor);
					}
				}
				// tunnel wall
				if (dao_tunnels.isWall) {
					for (int iy=0; iy<h_walls; iy++)
						chunk.setBlock(ix, y_floor+iy, iz, block_walls);
				} else
				// inside tunnel
				if (dao_tunnels.isTunnel) {
					chunk.setBlock(ix, y_floor, iz, block_floor);
					if (this.enable_top)
						chunk.setBlock(ix, y_ceil, iz, block_ceiling);
					// pipes
					SWITCH_PIPE_HIGH:
					switch (dao_tunnels.isPipeHigh) {
					case 'x': chunk.setBlock(ix, y_ceil-1, iz, block_pipe_end); chunk.setBlock(ix, y_floor+1, iz, block_light); break SWITCH_PIPE_HIGH;
					case 'n': chunk.setBlock(ix, y_ceil-1, iz, block_pipe_ns );                                                 break SWITCH_PIPE_HIGH;
					case 'e': chunk.setBlock(ix, y_ceil-1, iz, block_pipe_ew );                                                 break SWITCH_PIPE_HIGH;
					default: break SWITCH_PIPE_HIGH;
					}
					SWITCH_PIPE_LOW:
					switch (dao_tunnels.isPipeLow) {
					case 'x': chunk.setBlock(ix, y_floor+1, iz, block_pipe_end); chunk.setBlock(ix, y_ceil-1, iz, block_light); break SWITCH_PIPE_LOW;
					case 'n': chunk.setBlock(ix, y_floor+1, iz, block_pipe_ns );                                                break SWITCH_PIPE_LOW;
					case 'e': chunk.setBlock(ix, y_floor+1, iz, block_pipe_ew );                                                break SWITCH_PIPE_LOW;
					default: break SWITCH_PIPE_LOW;
					}
				// fill
				} else {
					if (this.enable_top) {
						for (int iy=0; iy<h_walls; iy++)
							chunk.setBlock(ix, y_floor+iy, iz, block_fill);
					}
				}
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
			this.noiseTunnels[i].setAngle(0.25);
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
		cfgBlocks.addDefault("Pipe-End", DEFAULT_BLOCK_PIPE_END);
		cfgBlocks.addDefault("Pipe-NS",  DEFAULT_BLOCK_PIPE_NS );
		cfgBlocks.addDefault("Pipe-EW",  DEFAULT_BLOCK_PIPE_EW );
		cfgBlocks.addDefault("Light",    DEFAULT_BLOCK_LIGHT   );
	}



}
