package com.poixson.backrooms.gens;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.tools.abstractions.AtomicDouble;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularReturnType;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;


// 308 | Ikea
public class Gen_308 extends BackroomsGen {

	// default params
	public static final double DEFAULT_NOISE_WALL_FREQ = 0.05;

	// default blocks
	public static final String DEFAULT_BLOCK_WALL = "minecraft:yellow_terracotta";

	// noise
	public final FastNoiseLiteD noiseIkeaWalls;

	// params
	public final AtomicDouble noise_wall_freq = new AtomicDouble(DEFAULT_NOISE_WALL_FREQ);

	// blocks
	public final AtomicReference<String> block_wall = new AtomicReference<String>(null);



	public Gen_308(final BackroomsLevel backlevel, final int level_y, final int level_h) {
		super(backlevel, level_y, level_h);
		// noise
		this.noiseIkeaWalls = this.register(new FastNoiseLiteD());
	}



	@Override
	public void initNoise() {
		super.initNoise();
		// ikea walls
		this.noiseIkeaWalls.setFrequency(this.noise_wall_freq.get());
		this.noiseIkeaWalls.setNoiseType(NoiseType.Cellular);
		this.noiseIkeaWalls.setCellularReturnType(CellularReturnType.CellValue);
		this.noiseIkeaWalls.setCellularJitter(0.0);
		this.noiseIkeaWalls.setFractalType(FractalType.PingPong);
		this.noiseIkeaWalls.setFractalOctaves(2);
		this.noiseIkeaWalls.setFractalGain(1.0);
		this.noiseIkeaWalls.setFractalLacunarity(1.8);
	}



	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
		final double[][] values = new double[18][18];
		for (int iz=0; iz<18; iz++) {
			final int zz = (chunkZ * 16) + iz;
			for (int ix=0; ix<18; ix++) {
				final int xx = (chunkX * 16) + ix;
				values[iz][ix] = this.noiseIkeaWalls.getNoise(xx-1, zz-1);
			}
		}
		final int y = this.level_y;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
				final double value = ((values[iz+1][ix+1]+1.0) * 5.0) % 0.5;
//TODO
				final boolean isWall = (
					value != values[iz  ][ix+1] ||
					value != values[iz+2][ix+1] ||
					value != values[iz+1][ix+2] ||
					value != values[iz+1][ix  ]
				);
				if (isWall)
					chunk.setBlock(ix, y+(int)(value*10.0), iz, Material.GLOWSTONE);
			}
		}
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void loadConfig() {
	}
	public static void ConfigDefaults(final FileConfiguration cfg) {
	}



}
