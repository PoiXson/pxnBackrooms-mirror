package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_000.ENABLE_GEN_005;
import static com.poixson.backrooms.worlds.Level_000.ENABLE_TOP_005;
import static com.poixson.backrooms.worlds.Level_000.SUBCEILING;
import static com.poixson.backrooms.worlds.Level_000.SUBFLOOR;
import static com.poixson.commonmc.utils.BlockUtils.StringToBlockData;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Lightable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.worlds.Level_000.PregenLevel0;
import com.poixson.commonmc.tools.plotter.BlockPlotter;
import com.poixson.tools.abstractions.AtomicDouble;
import com.poixson.tools.dao.Iab;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;


// 5 | Hotel
public class Gen_005 extends BackroomsGen {

	// default params
	public static final double DEFAULT_NOISE_WALL_FREQ   = 0.02;
	public static final double DEFAULT_NOISE_WALL_JITTER = 0.3;
	public static final double DEFAULT_NOISE_ROOM_FREQ   = 0.01;
	public static final int    DEFAULT_NOISE_ROOM_OCTAVE = 2;
	public static final double DEFAULT_NOISE_ROOM_GAIN   = 0.6;
	public static final double DEFAULT_THRESH_ROOM_HALL  = 0.65;

	// default blocks
	public static final String DEFAULT_BLOCK_SUBFLOOR     = "minecraft:oak_planks";
	public static final String DEFAULT_BLOCK_SUBCEILING   = "minecraft:smooth_stone";
	public static final String DEFAULT_BLOCK_HALL_WALL    = "minecraft:stripped_spruce_wood";
	public static final String DEFAULT_BLOCK_HALL_CARPET  = "minecraft:black_glazed_terracotta";
	public static final String DEFAULT_BLOCK_HALL_CEILING = "minecraft:smooth_stone_slab[type=top]";

	// noise
	public final FastNoiseLiteD noiseHotelWalls;
	public final FastNoiseLiteD noiseHotelRooms;
	public final FastNoiseLiteD noiseHotelStairs;

	// params
	public final AtomicDouble  noise_wall_freq   = new AtomicDouble( DEFAULT_NOISE_WALL_FREQ  );
	public final AtomicDouble  noise_wall_jitter = new AtomicDouble( DEFAULT_NOISE_WALL_JITTER);
	public final AtomicDouble  noise_room_freq   = new AtomicDouble( DEFAULT_NOISE_ROOM_FREQ  );
	public final AtomicInteger noise_room_octave = new AtomicInteger(DEFAULT_NOISE_ROOM_OCTAVE);
	public final AtomicDouble  noise_room_gain   = new AtomicDouble( DEFAULT_NOISE_ROOM_GAIN  );
	public final AtomicDouble  thresh_room_hall  = new AtomicDouble( DEFAULT_THRESH_ROOM_HALL );

	// blocks
	public final AtomicReference<String> block_subfloor     = new AtomicReference<String>(null);
	public final AtomicReference<String> block_subceiling   = new AtomicReference<String>(null);
	public final AtomicReference<String> block_hall_wall    = new AtomicReference<String>(null);
	public final AtomicReference<String> block_hall_carpet  = new AtomicReference<String>(null);
	public final AtomicReference<String> block_hall_ceiling = new AtomicReference<String>(null);



	public Gen_005(final BackroomsLevel backlevel,
			final int level_y, final int level_h) {
		super(backlevel, level_y, level_h);
		// noise
		this.noiseHotelWalls = this.register(new FastNoiseLiteD());
		this.noiseHotelRooms = this.register(new FastNoiseLiteD());
		this.noiseHotelStairs = this.register(new FastNoiseLiteD());
	}



	@Override
	public void initNoise() {
		// hotel walls
		this.noiseHotelWalls.setFrequency(     this.noise_wall_freq  .get());
		this.noiseHotelWalls.setCellularJitter(this.noise_wall_jitter.get());
		this.noiseHotelWalls.setNoiseType(NoiseType.Cellular);
		this.noiseHotelWalls.setFractalType(FractalType.PingPong);
		this.noiseHotelWalls.setCellularDistanceFunction(CellularDistanceFunction.Manhattan);
		// hotel rooms
		this.noiseHotelRooms.setFrequency(     this.noise_room_freq  .get());
		this.noiseHotelRooms.setFractalOctaves(this.noise_room_octave.get());
		this.noiseHotelRooms.setFractalGain(   this.noise_room_gain  .get());
		this.noiseHotelRooms.setFractalType(FractalType.FBm);
		// hotel stairs to attic
		this.noiseHotelStairs.setFrequency(0.03);
	}



	public enum NodeType {
		HALL,
		ROOM,
		WALL,
	};

	public class HotelData implements PreGenData {

		public final double value;
		public NodeType type;
		public boolean hall_center = false;

		public HotelData(final double value) {
			this.value = value;
			final double thresh_room_hall = Gen_005.this.thresh_room_hall.get();
			this.type = (value>thresh_room_hall ? NodeType.HALL : NodeType.ROOM);
		}

	}



	public void pregenerate(Map<Iab, HotelData> data,
			final int chunkX, final int chunkZ) {
		HotelData dao;
		int xx, zz;
		double value;
		for (int iz=-8; iz<24; iz++) {
			zz = (chunkZ * 16) + iz;
			for (int ix=-8; ix<24; ix++) {
				xx = (chunkX * 16) + ix;
				value = this.noiseHotelWalls.getNoiseRot(xx, zz, 0.25);
				dao = new HotelData(value);
				data.put(new Iab(ix, iz), dao);
			}
		}
		HotelData daoN, daoS, daoE, daoW;
		HotelData daoNE, daoNW, daoSE, daoSW;
		// find walls
		for (int iz=-8; iz<24; iz++) {
			for (int ix=-8; ix<24; ix++) {
				dao = data.get(new Iab(ix, iz));
				if (NodeType.ROOM.equals(dao.type)) {
					daoN  = data.get(new Iab(ix,   iz-1));
					daoS  = data.get(new Iab(ix,   iz+1));
					daoE  = data.get(new Iab(ix+1, iz  ));
					daoW  = data.get(new Iab(ix-1, iz  ));
					daoNE = data.get(new Iab(ix+1, iz-1));
					daoNW = data.get(new Iab(ix-1, iz-1));
					daoSE = data.get(new Iab(ix+1, iz+1));
					daoSW = data.get(new Iab(ix-1, iz+1));
					if (daoN != null && NodeType.HALL.equals(daoN.type)) {
						dao.type = NodeType.WALL;
						LOOP_I:
						for (int i=3; i<9; i++) {
							daoN = data.get(new Iab(ix, iz-i));
							if (daoN == null) break LOOP_I;
							if (!NodeType.HALL.equals(daoN.type)) {
								daoN = data.get(new Iab(ix, iz-Math.floorDiv(i, 2)));
								daoN.hall_center = true;
								break LOOP_I;
							}
						}
					}
					if (daoS != null && NodeType.HALL.equals(daoS.type)) {
						dao.type = NodeType.WALL;
						LOOP_I:
						for (int i=3; i<9; i++) {
							daoS = data.get(new Iab(ix, iz+i));
							if (daoS == null) break LOOP_I;
							if (!NodeType.HALL.equals(daoS.type)) {
								daoS = data.get(new Iab(ix, iz+Math.floorDiv(i, 2)));
								daoS.hall_center = true;
								break LOOP_I;
							}
						}
					}
					if (daoE != null && NodeType.HALL.equals(daoE.type)) {
						dao.type = NodeType.WALL;
						LOOP_I:
						for (int i=3; i<9; i++) {
							daoE = data.get(new Iab(ix+i, iz));
							if (daoE == null) break LOOP_I;
							if (!NodeType.HALL.equals(daoE.type)) {
								daoE = data.get(new Iab(ix+Math.floorDiv(i, 2), iz));
								daoE.hall_center = true;
								break LOOP_I;
							}
						}
					}
					if (daoW != null && NodeType.HALL.equals(daoW.type)) {
						dao.type = NodeType.WALL;
						LOOP_I:
						for (int i=3; i<9; i++) {
							daoW = data.get(new Iab(ix-i, iz));
							if (daoW == null) break LOOP_I;
							if (!NodeType.HALL.equals(daoW.type)) {
								daoW = data.get(new Iab(ix-Math.floorDiv(i, 2), iz));
								daoW.hall_center = true;
								break LOOP_I;
							}
						}
					}
					if ((daoNE != null && NodeType.HALL.equals(daoNE.type))
					||  (daoNW != null && NodeType.HALL.equals(daoNW.type))
					||  (daoSE != null && NodeType.HALL.equals(daoSE.type))
					||  (daoSW != null && NodeType.HALL.equals(daoSW.type)) )
						dao.type = NodeType.WALL;
				}
			} // end for x
		} // end for z
	}



	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
		if (!ENABLE_GEN_005) return;
		final BlockData block_subfloor     = StringToBlockData(this.block_subfloor,     DEFAULT_BLOCK_SUBFLOOR    );
		final BlockData block_subceiling   = StringToBlockData(this.block_subceiling,   DEFAULT_BLOCK_SUBCEILING  );
		final BlockData block_hall_wall    = StringToBlockData(this.block_hall_wall,    DEFAULT_BLOCK_HALL_WALL   );
		final BlockData block_hall_carpet  = StringToBlockData(this.block_hall_carpet,  DEFAULT_BLOCK_HALL_CARPET );
		final BlockData block_hall_ceiling = StringToBlockData(this.block_hall_ceiling, DEFAULT_BLOCK_HALL_CEILING);
		if (block_subfloor     == null) throw new RuntimeException("Invalid block type for level 5 SubFloor"    );
		if (block_subceiling   == null) throw new RuntimeException("Invalid block type for level 5 SubCeiling"  );
		if (block_hall_wall    == null) throw new RuntimeException("Invalid block type for level 5 Hall-Wall"   );
		if (block_hall_carpet  == null) throw new RuntimeException("Invalid block type for level 5 Hall-Carpet" );
		if (block_hall_ceiling == null) throw new RuntimeException("Invalid block type for level 5 Hall-Ceiling");
		final HashMap<Iab, HotelData> hotelData = ((PregenLevel0)pregen).hotel;
		final int y  = this.level_y + SUBFLOOR + 1;
		final int cy = this.level_y + SUBFLOOR + this.level_h + 2;
		final int h  = this.level_h + 2;
		int xx, zz;
		int mod_x, mod_z;
		HotelData dao;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
				xx = (chunkX * 16) + ix;
				zz = (chunkZ * 16) + iz;
				// hotel floor
				chunk.setBlock(ix, this.level_y, iz, Material.BEDROCK);
				for (int yy=0; yy<SUBFLOOR; yy++)
					chunk.setBlock(ix, this.level_y+yy+1, iz,block_subfloor);
				dao = hotelData.get(new Iab(ix, iz));
				if (dao == null) continue;
				switch (dao.type) {
				case WALL:
					for (int iy=0; iy<h; iy++)
						chunk.setBlock(ix, y+iy, iz, block_hall_wall);
					break;
				case HALL: {
					chunk.setBlock(ix, y, iz, block_hall_carpet);
					final Directional tile = (Directional) chunk.getBlockData(ix, y, iz);
					if (iz % 2 == 0) {
						if (ix % 2 == 0) tile.setFacing(BlockFace.NORTH);
						else             tile.setFacing(BlockFace.WEST );
					} else {
						if (ix % 2 == 0) tile.setFacing(BlockFace.EAST );
						else             tile.setFacing(BlockFace.SOUTH);
					}
					chunk.setBlock(ix, y, iz, tile);
					if (ENABLE_TOP_005) {
						// ceiling light
						mod_x = xx % 5;
						mod_z = zz % 5;
						if (dao.hall_center && (
						(mod_x >= 0 && mod_x < 2) ||
						(mod_z >= 1 && mod_z < 4) )) {
							chunk.setBlock(ix, cy, iz, Material.REDSTONE_LAMP);
							final Lightable lamp = (Lightable) chunk.getBlockData(ix, cy, iz);
							lamp.setLit(true);
							chunk.setBlock(ix, cy,   iz, lamp);
							chunk.setBlock(ix, cy+1, iz, Material.REDSTONE_BLOCK);
						// ceiling
						} else {
							if (NodeType.WALL.equals(hotelData.get(new Iab(ix, iz-1)).type) // north
							||  NodeType.WALL.equals(hotelData.get(new Iab(ix, iz+1)).type) // south
							||  NodeType.WALL.equals(hotelData.get(new Iab(ix+1, iz)).type) // east
							||  NodeType.WALL.equals(hotelData.get(new Iab(ix-1, iz)).type))// west
								chunk.setBlock(ix, cy, iz, block_subceiling);
							else {
								chunk.setBlock(ix, cy, iz, block_hall_ceiling);
//								final Slab slab = (Slab) chunk.getBlockData(ix, cy, iz);
//								slab.setType(Slab.Type.TOP);
//								chunk.setBlock(ix, cy, iz, slab);
							}
						}
					}
					break;
				}
				case ROOM: break;
				default: throw new RuntimeException("Unknown hotel type: " + dao.type.toString());
				}
				if (ENABLE_TOP_005) {
					for (int i=0; i<SUBCEILING; i++)
						chunk.setBlock(ix, cy+i+1, iz, block_subceiling);
				}
			} // end ix
		} // end iz
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void loadConfig() {
		// params
		{
			final ConfigurationSection cfg = this.plugin.getLevelParams(5);
			this.noise_wall_freq  .set(cfg.getDouble("Noise-Wall-Freq"    ));
			this.noise_wall_jitter.set(cfg.getDouble("Noise-Wall-Jitter"  ));
			this.noise_room_freq  .set(cfg.getDouble("Noise-Room-Freq"    ));
			this.noise_room_octave.set(cfg.getInt(   "Noise-Room-Octave"  ));
			this.noise_room_gain  .set(cfg.getDouble("Noise-Room-Gain"    ));
			this.thresh_room_hall .set(cfg.getDouble("Thresh-Room-Or-Hall"));
		}
		// block types
		{
			final ConfigurationSection cfg = this.plugin.getLevelBlocks(5);
			this.block_subfloor    .set(cfg.getString("SubFloor"    ));
			this.block_subceiling  .set(cfg.getString("SubCeiling"  ));
			this.block_hall_wall   .set(cfg.getString("Hall-Wall"   ));
			this.block_hall_carpet .set(cfg.getString("Hall-Carpet" ));
			this.block_hall_ceiling.set(cfg.getString("Hall-Ceiling"));
		}
	}
	public static void ConfigDefaults(final FileConfiguration cfg) {
		// params
		cfg.addDefault("Level5.Params.Noise-Wall-Freq",     DEFAULT_NOISE_WALL_FREQ  );
		cfg.addDefault("Level5.Params.Noise-Wall-Jitter",   DEFAULT_NOISE_WALL_JITTER);
		cfg.addDefault("Level5.Params.Noise-Room-Freq",     DEFAULT_NOISE_ROOM_FREQ  );
		cfg.addDefault("Level5.Params.Noise-Room-Octave",   DEFAULT_NOISE_ROOM_OCTAVE);
		cfg.addDefault("Level5.Params.Noise-Room-Gain",     DEFAULT_NOISE_ROOM_GAIN  );
		cfg.addDefault("Level5.Params.Thresh-Room-Or-Hall", DEFAULT_THRESH_ROOM_HALL );
		// block types
		cfg.addDefault("Level5.Blocks.SubFloor",     DEFAULT_BLOCK_SUBFLOOR   );
		cfg.addDefault("Level5.Blocks.SubCeiling",   DEFAULT_BLOCK_SUBCEILING );
		cfg.addDefault("Level5.Blocks.Hall-Wall",    DEFAULT_BLOCK_HALL_WALL   );
		cfg.addDefault("Level5.Blocks.Hall-Carpet",  DEFAULT_BLOCK_HALL_CARPET );
		cfg.addDefault("Level5.Blocks.Hall-Ceiling", DEFAULT_BLOCK_HALL_CEILING);
	}



}
