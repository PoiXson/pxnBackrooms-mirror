package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_000.ENABLE_GEN_005;
import static com.poixson.backrooms.worlds.Level_000.ENABLE_TOP_005;
import static com.poixson.backrooms.worlds.Level_000.SUBCEILING;
import static com.poixson.backrooms.worlds.Level_000.SUBFLOOR;
import static com.poixson.utils.BlockUtils.StringToBlockData;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.worlds.Level_000.PregenLevel0;
import com.poixson.tools.abstractions.AtomicDouble;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.dao.Iab;
import com.poixson.tools.plotter.BlockPlotter;
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
	public static final int    DEFAULT_NOMINAL_ROOM_SIZE = 8;

	// default blocks
	public static final String DEFAULT_BLOCK_SUBFLOOR           = "minecraft:oak_planks";
	public static final String DEFAULT_BLOCK_SUBCEILING         = "minecraft:smooth_stone";
	public static final String DEFAULT_BLOCK_SUBWALL            = "minecraft:oak_planks";
	public static final String DEFAULT_BLOCK_HALL_WALL_TOP_X    = "minecraft:stripped_spruce_wood[axis=x]";
	public static final String DEFAULT_BLOCK_HALL_WALL_TOP_Z    = "minecraft:stripped_spruce_wood[axis=z]";
	public static final String DEFAULT_BLOCK_HALL_WALL_CENTER   = "minecraft:brown_terracotta";
	public static final String DEFAULT_BLOCK_HALL_WALL_BOTTOM_X = "minecraft:stripped_spruce_wood[axis=x]";
	public static final String DEFAULT_BLOCK_HALL_WALL_BOTTOM_Z = "minecraft:stripped_spruce_wood[axis=z]";
	public static final String DEFAULT_BLOCK_DOOR_BORDER_TOP_X  = "minecraft:stripped_spruce_wood[axis=x]";
	public static final String DEFAULT_BLOCK_DOOR_BORDER_TOP_Z  = "minecraft:stripped_spruce_wood[axis=z]";
	public static final String DEFAULT_BLOCK_DOOR_BORDER_SIDE   = "minecraft:stripped_spruce_wood[axis=y]";
	public static final String DEFAULT_BLOCK_HALL_CEILING       = "minecraft:smooth_stone_slab[type=top]";
	public static final String DEFAULT_BLOCK_HALL_FLOOR_EE      = "minecraft:black_glazed_terracotta[facing=north]";
	public static final String DEFAULT_BLOCK_HALL_FLOOR_EO      = "minecraft:black_glazed_terracotta[facing=east]";
	public static final String DEFAULT_BLOCK_HALL_FLOOR_OE      = "minecraft:black_glazed_terracotta[facing=west]";
	public static final String DEFAULT_BLOCK_HALL_FLOOR_OO      = "minecraft:black_glazed_terracotta[facing=south]";
	public static final String DEFAULT_DOOR_GUEST               = "minecraft:dark_oak_door";

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
	public final AtomicInteger nominal_room_size = new AtomicInteger(DEFAULT_NOMINAL_ROOM_SIZE);

	// blocks
	public final AtomicReference<String> block_subfloor           = new AtomicReference<String>(null);
	public final AtomicReference<String> block_subceiling         = new AtomicReference<String>(null);
	public final AtomicReference<String> block_subwall            = new AtomicReference<String>(null);
	public final AtomicReference<String> block_hall_wall_top_x    = new AtomicReference<String>(null);
	public final AtomicReference<String> block_hall_wall_top_z    = new AtomicReference<String>(null);
	public final AtomicReference<String> block_hall_wall_center   = new AtomicReference<String>(null);
	public final AtomicReference<String> block_hall_wall_bottom_x = new AtomicReference<String>(null);
	public final AtomicReference<String> block_hall_wall_bottom_z = new AtomicReference<String>(null);
	public final AtomicReference<String> block_door_border_top_x  = new AtomicReference<String>(null);
	public final AtomicReference<String> block_door_border_top_z  = new AtomicReference<String>(null);
	public final AtomicReference<String> block_door_border_side   = new AtomicReference<String>(null);
	public final AtomicReference<String> block_hall_ceiling       = new AtomicReference<String>(null);
	public final AtomicReference<String> block_hall_floor_ee      = new AtomicReference<String>(null);
	public final AtomicReference<String> block_hall_floor_eo      = new AtomicReference<String>(null);
	public final AtomicReference<String> block_hall_floor_oe      = new AtomicReference<String>(null);
	public final AtomicReference<String> block_hall_floor_oo      = new AtomicReference<String>(null);
	public final AtomicReference<String> door_guest               = new AtomicReference<String>(null);



	public Gen_005(final BackroomsLevel backlevel, final int seed,
			final int level_y, final int level_h) {
		super(backlevel, seed, level_y, level_h);
		// noise
		this.noiseHotelWalls = this.register(new FastNoiseLiteD());
		this.noiseHotelRooms = this.register(new FastNoiseLiteD());
		this.noiseHotelStairs = this.register(new FastNoiseLiteD());
	}



	@Override
	public void initNoise() {
		super.initNoise();
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
		HotelData daoN,  daoS,  daoE,  daoW;
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
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!ENABLE_GEN_005) return;
		final BlockData block_subfloor           = StringToBlockData(this.block_subfloor,           DEFAULT_BLOCK_SUBFLOOR          );
		final BlockData block_subceiling         = StringToBlockData(this.block_subceiling,         DEFAULT_BLOCK_SUBCEILING        );
		final BlockData block_subwall            = StringToBlockData(this.block_subwall,            DEFAULT_BLOCK_SUBWALL           );
		final BlockData block_hall_wall_top_x    = StringToBlockData(this.block_hall_wall_top_x,    DEFAULT_BLOCK_HALL_WALL_TOP_X   );
		final BlockData block_hall_wall_top_z    = StringToBlockData(this.block_hall_wall_top_z,    DEFAULT_BLOCK_HALL_WALL_TOP_Z   );
		final BlockData block_hall_wall_center   = StringToBlockData(this.block_hall_wall_center  , DEFAULT_BLOCK_HALL_WALL_CENTER  );
		final BlockData block_hall_wall_bottom_x = StringToBlockData(this.block_hall_wall_bottom_x, DEFAULT_BLOCK_HALL_WALL_BOTTOM_X);
		final BlockData block_hall_wall_bottom_z = StringToBlockData(this.block_hall_wall_bottom_z, DEFAULT_BLOCK_HALL_WALL_BOTTOM_Z);
		final BlockData block_hall_ceiling       = StringToBlockData(this.block_hall_ceiling,       DEFAULT_BLOCK_HALL_CEILING      );
		final BlockData block_hall_floor_ee      = StringToBlockData(this.block_hall_floor_ee,      DEFAULT_BLOCK_HALL_FLOOR_EE     );
		final BlockData block_hall_floor_eo      = StringToBlockData(this.block_hall_floor_eo,      DEFAULT_BLOCK_HALL_FLOOR_EO     );
		final BlockData block_hall_floor_oe      = StringToBlockData(this.block_hall_floor_oe,      DEFAULT_BLOCK_HALL_FLOOR_OE     );
		final BlockData block_hall_floor_oo      = StringToBlockData(this.block_hall_floor_oo,      DEFAULT_BLOCK_HALL_FLOOR_OO     );
		if (block_subfloor           == null) throw new RuntimeException("Invalid block type for level 5 SubFloor"          );
		if (block_subceiling         == null) throw new RuntimeException("Invalid block type for level 5 SubCeiling"        );
		if (block_hall_wall_top_x    == null) throw new RuntimeException("Invalid block type for level 5 Hall-Wall-Top-X"   );
		if (block_hall_wall_top_z    == null) throw new RuntimeException("Invalid block type for level 5 Hall-Wall-Top-Z"   );
		if (block_hall_wall_center   == null) throw new RuntimeException("Invalid block type for level 5 Hall-Wall-Center"  );
		if (block_hall_wall_bottom_x == null) throw new RuntimeException("Invalid block type for level 5 Hall-Wall-Bottom-X");
		if (block_hall_wall_bottom_z == null) throw new RuntimeException("Invalid block type for level 5 Hall-Wall-Bottom-Z");
		if (block_hall_ceiling       == null) throw new RuntimeException("Invalid block type for level 5 Hall-Ceiling"      );
		if (block_hall_floor_ee      == null) throw new RuntimeException("Invalid block type for level 5 Hall-Floor-EE"     );
		if (block_hall_floor_eo      == null) throw new RuntimeException("Invalid block type for level 5 Hall-Floor-EO"     );
		if (block_hall_floor_oe      == null) throw new RuntimeException("Invalid block type for level 5 Hall-Floor-OE"     );
		if (block_hall_floor_oo      == null) throw new RuntimeException("Invalid block type for level 5 Hall-Floor-OO"     );
		final BlockData lamp = Bukkit.createBlockData("minecraft:redstone_lamp[lit=true]");
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
				if (ENABLE_TOP_005) {
					for (int i=0; i<SUBCEILING; i++)
						chunk.setBlock(ix, cy+i+1, iz, block_subceiling);
				}
				switch (dao.type) {
				case WALL:
//TODO: use block_hall_wall_top_z and block_hall_wall_bottom_z
					chunk.setBlock(ix, y+6, iz, block_hall_wall_top_x);
					chunk.setBlock(ix, y+5, iz, block_hall_wall_top_x);
					for (int iy=2; iy<5; iy++)
						chunk.setBlock(ix, y+iy, iz, block_hall_wall_center);
					chunk.setBlock(ix, y+1, iz, block_hall_wall_bottom_x);
					chunk.setBlock(ix, y,   iz, block_hall_wall_bottom_x);
					break;
				case HALL: {
					if (iz % 2 == 0) {
						if (ix % 2 == 0) chunk.setBlock(ix, y, iz, block_hall_floor_ee); // even x, even z
						else             chunk.setBlock(ix, y, iz, block_hall_floor_oe); // odd x,  even z
					} else {
						if (ix % 2 == 0) chunk.setBlock(ix, y, iz, block_hall_floor_eo); // even x, odd z
						else             chunk.setBlock(ix, y, iz, block_hall_floor_oo); // odd x,  odd z
					}
					if (ENABLE_TOP_005) {
						// ceiling light
						mod_x = xx % 5;
						mod_z = zz % 5;
						if (dao.hall_center && (
						(mod_x >= 0 && mod_x < 2) ||
						(mod_z >= 1 && mod_z < 4) )) {
							chunk.setBlock(ix, cy+1, iz, Material.REDSTONE_BLOCK);
							chunk.setBlock(ix, cy,   iz, lamp);
						// ceiling
						} else {
							if (NodeType.WALL.equals(hotelData.get(new Iab(ix, iz-1)).type) // north
							||  NodeType.WALL.equals(hotelData.get(new Iab(ix, iz+1)).type) // south
							||  NodeType.WALL.equals(hotelData.get(new Iab(ix+1, iz)).type) // east
							||  NodeType.WALL.equals(hotelData.get(new Iab(ix-1, iz)).type))// west
								chunk.setBlock(ix, cy, iz, block_subceiling);
							else {
								chunk.setBlock(ix, cy, iz, block_hall_ceiling);
							}
						}
					}
					break;
				}
				case ROOM: {
					for (int iy=0; iy<h; iy++)
						chunk.setBlock(ix, y+iy, iz, block_subwall);
					break;
				}
				default: throw new RuntimeException("Unknown hotel type: "+dao.type.toString());
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
			final ConfigurationSection cfg = this.plugin.getConfigLevelParams(5);
			this.noise_wall_freq  .set(cfg.getDouble("Noise-Wall-Freq"    ));
			this.noise_wall_jitter.set(cfg.getDouble("Noise-Wall-Jitter"  ));
			this.noise_room_freq  .set(cfg.getDouble("Noise-Room-Freq"    ));
			this.noise_room_octave.set(cfg.getInt(   "Noise-Room-Octave"  ));
			this.noise_room_gain  .set(cfg.getDouble("Noise-Room-Gain"    ));
			this.thresh_room_hall .set(cfg.getDouble("Thresh-Room-Or-Hall"));
			this.nominal_room_size.set(cfg.getInt(   "Nominal-Room-Size"  ));
		}
		// block types
		{
			final ConfigurationSection cfg = this.plugin.getConfigLevelBlocks(5);
			this.block_subfloor          .set(cfg.getString("SubFloor"          ));
			this.block_subceiling        .set(cfg.getString("SubCeiling"        ));
			this.block_subwall           .set(cfg.getString("SubWall"           ));
			this.block_hall_wall_top_x   .set(cfg.getString("Hall-Wall-Top-X"   ));
			this.block_hall_wall_top_z   .set(cfg.getString("Hall-Wall-Top-Z"   ));
			this.block_hall_wall_center  .set(cfg.getString("Hall-Wall-Center"  ));
			this.block_hall_wall_bottom_x.set(cfg.getString("Hall-Wall-Bottom-X"));
			this.block_hall_wall_bottom_z.set(cfg.getString("Hall-Wall-Bottom-Z"));
			this.block_door_border_top_x .set(cfg.getString("Door-Border-Top-X" ));
			this.block_door_border_top_z .set(cfg.getString("Door-Border-Top-Z" ));
			this.block_door_border_side  .set(cfg.getString("Door-Border-Side"  ));
			this.block_hall_ceiling      .set(cfg.getString("Hall-Ceiling"      ));
			this.block_hall_floor_ee     .set(cfg.getString("Hall-Floor-EE"     ));
			this.block_hall_floor_eo     .set(cfg.getString("Hall-Floor-EO"     ));
			this.block_hall_floor_oe     .set(cfg.getString("Hall-Floor-OE"     ));
			this.block_hall_floor_oo     .set(cfg.getString("Hall-Floor-OO"     ));
			this.door_guest              .set(cfg.getString("Door-Guest"        ));
		}
	}
	@Override
	public void configDefaults() {
		// params
		{
			final ConfigurationSection cfg = this.plugin.getConfigLevelParams();
			cfg.addDefault("Level5.Noise-Wall-Freq",     DEFAULT_NOISE_WALL_FREQ  );
			cfg.addDefault("Level5.Noise-Wall-Jitter",   DEFAULT_NOISE_WALL_JITTER);
			cfg.addDefault("Level5.Noise-Room-Freq",     DEFAULT_NOISE_ROOM_FREQ  );
			cfg.addDefault("Level5.Noise-Room-Octave",   DEFAULT_NOISE_ROOM_OCTAVE);
			cfg.addDefault("Level5.Noise-Room-Gain",     DEFAULT_NOISE_ROOM_GAIN  );
			cfg.addDefault("Level5.Thresh-Room-Or-Hall", DEFAULT_THRESH_ROOM_HALL );
			cfg.addDefault("Level5.Nominal-Room-Size",   DEFAULT_NOMINAL_ROOM_SIZE);
		}
		// block types
		{
			final ConfigurationSection cfg = this.plugin.getConfigLevelBlocks();
			cfg.addDefault("Level5.SubFloor",           DEFAULT_BLOCK_SUBFLOOR          );
			cfg.addDefault("Level5.SubCeiling",         DEFAULT_BLOCK_SUBCEILING        );
			cfg.addDefault("Level5.SubWall",            DEFAULT_BLOCK_SUBWALL           );
			cfg.addDefault("Level5.Hall-Wall-Top-X",    DEFAULT_BLOCK_HALL_WALL_TOP_X   );
			cfg.addDefault("Level5.Hall-Wall-Top-Z",    DEFAULT_BLOCK_HALL_WALL_TOP_Z   );
			cfg.addDefault("Level5.Hall-Wall-Center",   DEFAULT_BLOCK_HALL_WALL_CENTER  );
			cfg.addDefault("Level5.Hall-Wall-Bottom-X", DEFAULT_BLOCK_HALL_WALL_BOTTOM_X);
			cfg.addDefault("Level5.Hall-Wall-Bottom-Z", DEFAULT_BLOCK_HALL_WALL_BOTTOM_Z);
			cfg.addDefault("Level5.Door-Border-Top-X",  DEFAULT_BLOCK_DOOR_BORDER_TOP_X );
			cfg.addDefault("Level5.Door-Border-Top-Z",  DEFAULT_BLOCK_DOOR_BORDER_TOP_Z );
			cfg.addDefault("Level5.Door-Border-Side",   DEFAULT_BLOCK_DOOR_BORDER_SIDE  );
			cfg.addDefault("Level5.Hall-Ceiling",       DEFAULT_BLOCK_HALL_CEILING      );
			cfg.addDefault("Level5.Hall-Floor-EE",      DEFAULT_BLOCK_HALL_FLOOR_EE     );
			cfg.addDefault("Level5.Hall-Floor-EO",      DEFAULT_BLOCK_HALL_FLOOR_EO     );
			cfg.addDefault("Level5.Hall-Floor-OE",      DEFAULT_BLOCK_HALL_FLOOR_OE     );
			cfg.addDefault("Level5.Hall-Floor-OO",      DEFAULT_BLOCK_HALL_FLOOR_OO     );
			cfg.addDefault("Level5.Door-Guest",         DEFAULT_DOOR_GUEST              );
		}
	}



}
