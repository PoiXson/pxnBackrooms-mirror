package com.poixson.backrooms.gens;

import static com.poixson.utils.BlockUtils.StringToBlockDataDef;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
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


// 5 | Hotel
public class Gen_005 extends BackroomsGen {

	// default params
	public static final int    DEFAULT_LEVEL_H             = 6;
	public static final int    DEFAULT_SUBFLOOR            = 3;
	public static final int    DEFAULT_SUBCEILING          = 3;
	public static final double DEFAULT_NOISE_WALL_FREQ     = 0.02;
	public static final double DEFAULT_NOISE_WALL_JITTER   = 0.3;
	public static final double DEFAULT_NOISE_ROOM_FREQ     = 0.01;
	public static final int    DEFAULT_NOISE_ROOM_OCTAVE   = 2;
	public static final double DEFAULT_NOISE_ROOM_GAIN     = 0.6;
	public static final double DEFAULT_THRESH_ROOM_HALL    = 0.65;
	public static final int    DEFAULT_NOMINAL_ROOM_SIZE   = 8;
	public static final double DEFAULT_NOISE_STAIRS_FREQ   = 0.5;

	// default blocks
	public static final String DEFAULT_BLOCK_SUBFLOOR           = "minecraft:oak_planks";
	public static final String DEFAULT_BLOCK_SUBCEILING         = "minecraft:stone";
	public static final String DEFAULT_BLOCK_SUBWALL            = "minecraft:oak_planks";
	public static final String DEFAULT_BLOCK_HALL_CEILING       = "minecraft:smooth_stone";
	public static final String DEFAULT_BLOCK_HALL_CEILING_SLAB  = "minecraft:smooth_stone_slab[type=top]";
	public static final String DEFAULT_BLOCK_HALL_WALL_TOP_X    = "minecraft:stripped_spruce_wood[axis=x]";
	public static final String DEFAULT_BLOCK_HALL_WALL_TOP_Z    = "minecraft:stripped_spruce_wood[axis=z]";
	public static final String DEFAULT_BLOCK_HALL_WALL_CENTER   = "minecraft:brown_terracotta";
	public static final String DEFAULT_BLOCK_HALL_WALL_BOTTOM_X = "minecraft:stripped_spruce_wood[axis=x]";
	public static final String DEFAULT_BLOCK_HALL_WALL_BOTTOM_Z = "minecraft:stripped_spruce_wood[axis=z]";
	public static final String DEFAULT_BLOCK_DOOR_BORDER_TOP_X  = "minecraft:stripped_spruce_wood[axis=x]";
	public static final String DEFAULT_BLOCK_DOOR_BORDER_TOP_Z  = "minecraft:stripped_spruce_wood[axis=z]";
	public static final String DEFAULT_BLOCK_DOOR_BORDER_SIDE   = "minecraft:stripped_spruce_wood[axis=y]";
	public static final String DEFAULT_BLOCK_HALL_FLOOR_EE      = "minecraft:black_glazed_terracotta[facing=north]";
	public static final String DEFAULT_BLOCK_HALL_FLOOR_EO      = "minecraft:black_glazed_terracotta[facing=east]";
	public static final String DEFAULT_BLOCK_HALL_FLOOR_OE      = "minecraft:black_glazed_terracotta[facing=west]";
	public static final String DEFAULT_BLOCK_HALL_FLOOR_OO      = "minecraft:black_glazed_terracotta[facing=south]";
	public static final String DEFAULT_DOOR_GUEST               = "minecraft:dark_oak_door";

	// params
	public final boolean enable_gen;
	public final boolean enable_top;
	public final int     level_y;
	public final int     level_h;
	public final int     subfloor;
	public final int     subceiling;
	public final double  thresh_room_hall;
	public final int     nominal_room_size;

	// blocks
	public final String block_subfloor;
	public final String block_subceiling;
	public final String block_subwall;
	public final String block_hall_ceiling;
	public final String block_hall_ceiling_slab;
	public final String block_hall_wall_top_x;
	public final String block_hall_wall_top_z;
	public final String block_hall_wall_center;
	public final String block_hall_wall_bottom_x;
	public final String block_hall_wall_bottom_z;
	public final String block_door_border_top_x;
	public final String block_door_border_top_z;
	public final String block_door_border_side;
	public final String block_hall_floor_ee;
	public final String block_hall_floor_eo;
	public final String block_hall_floor_oe;
	public final String block_hall_floor_oo;
	public final String door_guest;

	// noise
	public final FastNoiseLiteD noiseHotelWalls;
	public final FastNoiseLiteD noiseHotelRooms;



	public Gen_005(final BackroomsWorld backworld, final int seed, final BackroomsGen gen_below) {
		super(backworld, gen_below, seed);
		final int level_number = this.getLevelNumber();
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(level_number);
		final ConfigurationSection cfgBlocks = this.plugin.getConfigLevelBlocks(level_number);
		// params
		this.enable_gen        = cfgParams.getBoolean("Enable-Gen"         );
		this.enable_top        = cfgParams.getBoolean("Enable-Top"         );
		this.level_y           = cfgParams.getInt(    "Level-Y"            );
		this.level_h           = cfgParams.getInt(    "Level-Height"       );
		this.subfloor          = cfgParams.getInt(    "SubFloor"           );
		this.subceiling        = cfgParams.getInt(    "SubCeiling"         );
		this.thresh_room_hall  = cfgParams.getDouble( "Thresh-Room-Or-Hall");
		this.nominal_room_size = cfgParams.getInt(    "Nominal-Room-Size"  );
		// block types
		this.block_subfloor           = cfgBlocks.getString("SubFloor"          );
		this.block_subceiling         = cfgBlocks.getString("SubCeiling"        );
		this.block_subwall            = cfgBlocks.getString("SubWall"           );
		this.block_hall_ceiling       = cfgBlocks.getString("Hall-Ceiling"      );
		this.block_hall_ceiling_slab  = cfgBlocks.getString("Hall-Ceiling-Slab" );
		this.block_hall_wall_top_x    = cfgBlocks.getString("Hall-Wall-Top-X"   );
		this.block_hall_wall_top_z    = cfgBlocks.getString("Hall-Wall-Top-Z"   );
		this.block_hall_wall_center   = cfgBlocks.getString("Hall-Wall-Center"  );
		this.block_hall_wall_bottom_x = cfgBlocks.getString("Hall-Wall-Bottom-X");
		this.block_hall_wall_bottom_z = cfgBlocks.getString("Hall-Wall-Bottom-Z");
		this.block_door_border_top_x  = cfgBlocks.getString("Door-Border-Top-X" );
		this.block_door_border_top_z  = cfgBlocks.getString("Door-Border-Top-Z" );
		this.block_door_border_side   = cfgBlocks.getString("Door-Border-Side"  );
		this.block_hall_floor_ee      = cfgBlocks.getString("Hall-Floor-EE"     );
		this.block_hall_floor_eo      = cfgBlocks.getString("Hall-Floor-EO"     );
		this.block_hall_floor_oe      = cfgBlocks.getString("Hall-Floor-OE"     );
		this.block_hall_floor_oo      = cfgBlocks.getString("Hall-Floor-OO"     );
		this.door_guest               = cfgBlocks.getString("Door-Guest"        );
		// noise
		this.noiseHotelWalls  = this.register(new FastNoiseLiteD());
		this.noiseHotelRooms  = this.register(new FastNoiseLiteD());
	}



	@Override
	public int getLevelNumber() {
		return 5;
	}

	@Override
	public int getNextY() {
		return this.level_y + this.bedrock_barrier + this.subfloor + this.level_h + this.subceiling + 2;
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

		public HotelData(final int x, final int z) {
			this.value = Gen_005.this.noiseHotelWalls.getNoiseRot(x, z, 0.25);
			this.type = (this.value>Gen_005.this.thresh_room_hall ? NodeType.HALL : NodeType.ROOM);
		}

	}



	public void pregenerate(Map<Iab, HotelData> data,
			final int chunkX, final int chunkZ) {
		for (int iz=-8; iz<24; iz++) {
			final int zz = (chunkZ * 16) + iz;
			for (int ix=-8; ix<24; ix++) {
				final int xx = (chunkX * 16) + ix;
				final HotelData dao = new HotelData(xx, zz);
				data.put(new Iab(ix, iz), dao);
			}
		}
		// find walls
		for (int iz=-8; iz<24; iz++) {
			for (int ix=-8; ix<24; ix++) {
				final HotelData dao = data.get(new Iab(ix, iz));
				HotelData daoN,  daoS,  daoE,  daoW;
				HotelData daoNE, daoNW, daoSE, daoSW;
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
		if (!this.enable_gen) return;
		final BlockData block_subfloor           = StringToBlockDataDef(this.block_subfloor,           DEFAULT_BLOCK_SUBFLOOR          );
		final BlockData block_subceiling         = StringToBlockDataDef(this.block_subceiling,         DEFAULT_BLOCK_SUBCEILING        );
		final BlockData block_subwall            = StringToBlockDataDef(this.block_subwall,            DEFAULT_BLOCK_SUBWALL           );
		final BlockData block_hall_ceiling       = StringToBlockDataDef(this.block_hall_ceiling,       DEFAULT_BLOCK_HALL_CEILING      );
		final BlockData block_hall_ceiling_slab  = StringToBlockDataDef(this.block_hall_ceiling_slab,  DEFAULT_BLOCK_HALL_CEILING_SLAB );
		final BlockData block_hall_wall_top_x    = StringToBlockDataDef(this.block_hall_wall_top_x,    DEFAULT_BLOCK_HALL_WALL_TOP_X   );
		final BlockData block_hall_wall_top_z    = StringToBlockDataDef(this.block_hall_wall_top_z,    DEFAULT_BLOCK_HALL_WALL_TOP_Z   );
		final BlockData block_hall_wall_center   = StringToBlockDataDef(this.block_hall_wall_center  , DEFAULT_BLOCK_HALL_WALL_CENTER  );
		final BlockData block_hall_wall_bottom_x = StringToBlockDataDef(this.block_hall_wall_bottom_x, DEFAULT_BLOCK_HALL_WALL_BOTTOM_X);
		final BlockData block_hall_wall_bottom_z = StringToBlockDataDef(this.block_hall_wall_bottom_z, DEFAULT_BLOCK_HALL_WALL_BOTTOM_Z);
		final BlockData block_hall_floor_ee      = StringToBlockDataDef(this.block_hall_floor_ee,      DEFAULT_BLOCK_HALL_FLOOR_EE     );
		final BlockData block_hall_floor_eo      = StringToBlockDataDef(this.block_hall_floor_eo,      DEFAULT_BLOCK_HALL_FLOOR_EO     );
		final BlockData block_hall_floor_oe      = StringToBlockDataDef(this.block_hall_floor_oe,      DEFAULT_BLOCK_HALL_FLOOR_OE     );
		final BlockData block_hall_floor_oo      = StringToBlockDataDef(this.block_hall_floor_oo,      DEFAULT_BLOCK_HALL_FLOOR_OO     );
		if (block_subfloor           == null) throw new RuntimeException("Invalid block type for level 5 SubFloor"          );
		if (block_subceiling         == null) throw new RuntimeException("Invalid block type for level 5 SubCeiling"        );
		if (block_hall_ceiling       == null) throw new RuntimeException("Invalid block type for level 5 Hall-Ceiling"      );
		if (block_hall_ceiling_slab  == null) throw new RuntimeException("Invalid block type for level 5 Hall-Ceiling-Slab" );
		if (block_hall_wall_top_x    == null) throw new RuntimeException("Invalid block type for level 5 Hall-Wall-Top-X"   );
		if (block_hall_wall_top_z    == null) throw new RuntimeException("Invalid block type for level 5 Hall-Wall-Top-Z"   );
		if (block_hall_wall_center   == null) throw new RuntimeException("Invalid block type for level 5 Hall-Wall-Center"  );
		if (block_hall_wall_bottom_x == null) throw new RuntimeException("Invalid block type for level 5 Hall-Wall-Bottom-X");
		if (block_hall_wall_bottom_z == null) throw new RuntimeException("Invalid block type for level 5 Hall-Wall-Bottom-Z");
		if (block_hall_floor_ee      == null) throw new RuntimeException("Invalid block type for level 5 Hall-Floor-EE"     );
		if (block_hall_floor_eo      == null) throw new RuntimeException("Invalid block type for level 5 Hall-Floor-EO"     );
		if (block_hall_floor_oe      == null) throw new RuntimeException("Invalid block type for level 5 Hall-Floor-OE"     );
		if (block_hall_floor_oo      == null) throw new RuntimeException("Invalid block type for level 5 Hall-Floor-OO"     );
		final BlockData lamp = Bukkit.createBlockData("minecraft:redstone_lamp[lit=true]");
		final HashMap<Iab, HotelData> data_hotel = ((Pregen_Level_000)pregen).hotel;
		final int h_walls = this.level_h + 2;
		final int y_base  = this.level_y + this.bedrock_barrier;
		final int y_floor = y_base + this.subfloor;
		final int y_ceil  = (y_floor + h_walls) - 1;
		for (int iz=0; iz<16; iz++) {
			final int zz = (chunkZ * 16) + iz;
			for (int ix=0; ix<16; ix++) {
				final int xx = (chunkX * 16) + ix;
				// barrier
				for (int iy=0; iy<this.bedrock_barrier; iy++)
					chunk.setBlock(ix, this.level_y+iy, iz, Material.BEDROCK);
				// subfloor
				for (int iy=0; iy<this.subfloor; iy++)
					chunk.setBlock(ix, y_base+iy, iz, block_subfloor);
				final HotelData dao_hotel   = data_hotel.get(new Iab(ix, iz  ));
				final HotelData dao_hotel_n = data_hotel.get(new Iab(ix, iz-1));
				final HotelData dao_hotel_s = data_hotel.get(new Iab(ix, iz+1));
				final HotelData dao_hotel_e = data_hotel.get(new Iab(ix+1, iz));
				final HotelData dao_hotel_w = data_hotel.get(new Iab(ix-1, iz));
				if (dao_hotel == null) continue;
				if (this.enable_top) {
					for (int iy=0; iy<this.subceiling; iy++)
						chunk.setBlock(ix, y_ceil+iy+1, iz, block_subceiling);
				}
				switch (dao_hotel.type) {
				case WALL: {
					final boolean ns = (
						NodeType.HALL.equals(dao_hotel_e.type) ||
						NodeType.HALL.equals(dao_hotel_w.type)
					);
					for (int iy=2; iy<h_walls-2; iy++)
						chunk.setBlock(ix, y_floor+iy, iz, block_hall_wall_center);
					for (int iy=0; iy<2; iy++) {
						if (ns) {
							chunk.setBlock(ix,  y_floor         +iy,   iz, block_hall_wall_bottom_z);
							chunk.setBlock(ix, (y_floor+h_walls)-iy-1, iz, block_hall_wall_top_z   );
						} else {
							chunk.setBlock(ix,  y_floor         +iy,   iz, block_hall_wall_bottom_x);
							chunk.setBlock(ix, (y_floor+h_walls)-iy-1, iz, block_hall_wall_top_x   );
						}
					}
					break;
				}
				case HALL: {
					// hall floors
					if (iz % 2 == 0) {
						if (ix % 2 == 0) chunk.setBlock(ix, y_floor, iz, block_hall_floor_ee); // even x, even z
						else             chunk.setBlock(ix, y_floor, iz, block_hall_floor_oe); // odd x,  even z
					} else {
						if (ix % 2 == 0) chunk.setBlock(ix, y_floor, iz, block_hall_floor_eo); // even x, odd z
						else             chunk.setBlock(ix, y_floor, iz, block_hall_floor_oo); // odd x,  odd z
					}
					// hall ceiling
					if (this.enable_top) {
						// ceiling light
						if (dao_hotel.hall_center &&
						(Math.floorDiv(xx, 2) + Math.floorDiv(zz, 2)) % 3 == 0) {
							chunk.setBlock(ix, y_ceil+1, iz, Material.REDSTONE_BLOCK);
							chunk.setBlock(ix, y_ceil,   iz, lamp                   );
						// ceiling
						} else {
							final boolean isNearWall = (
								NodeType.WALL.equals(dao_hotel_n.type) || // north
								NodeType.WALL.equals(dao_hotel_s.type) || // south
								NodeType.WALL.equals(dao_hotel_e.type) || // east
								NodeType.WALL.equals(dao_hotel_w.type)    // west
							);
							if (isNearWall) chunk.setBlock(ix, y_ceil, iz, block_hall_ceiling     );
							else            chunk.setBlock(ix, y_ceil, iz, block_hall_ceiling_slab);
						}
					}
					break;
				}
				case ROOM: {
					for (int iy=0; iy<h_walls; iy++)
						chunk.setBlock(ix, y_floor+iy, iz, block_subwall);
					break;
				}
				default: throw new RuntimeException("Unknown hotel type: "+dao_hotel.type.toString());
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
		// hotel walls
		this.noiseHotelWalls.setFrequency(               cfgParams.getDouble("Noise-Wall-Freq"  ));
		this.noiseHotelWalls.setCellularJitter(          cfgParams.getDouble("Noise-Wall-Jitter"));
		this.noiseHotelWalls.setNoiseType(               NoiseType.Cellular                      );
		this.noiseHotelWalls.setFractalType(             FractalType.PingPong                    );
		this.noiseHotelWalls.setCellularDistanceFunction(CellularDistanceFunction.Manhattan      );
		// hotel rooms
		this.noiseHotelRooms.setFrequency(     cfgParams.getDouble("Noise-Room-Freq"  ));
		this.noiseHotelRooms.setFractalOctaves(cfgParams.getInt(   "Noise-Room-Octave"));
		this.noiseHotelRooms.setFractalGain(   cfgParams.getDouble("Noise-Room-Gain"  ));
		this.noiseHotelRooms.setFractalType(   FractalType.FBm                         );
	}



	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		cfgParams.addDefault("Enable-Gen",          Boolean.TRUE                              );
		cfgParams.addDefault("Enable-Top",          Boolean.TRUE                              );
		cfgParams.addDefault("Level-Y",             Integer.valueOf(this.getDefaultY()       ));
		cfgParams.addDefault("Level-Height",        Integer.valueOf(DEFAULT_LEVEL_H          ));
		cfgParams.addDefault("SubFloor",            Integer.valueOf(DEFAULT_SUBFLOOR         ));
		cfgParams.addDefault("SubCeiling",          Integer.valueOf(DEFAULT_SUBCEILING       ));
		cfgParams.addDefault("Noise-Wall-Freq",     Double .valueOf(DEFAULT_NOISE_WALL_FREQ  ));
		cfgParams.addDefault("Noise-Wall-Jitter",   Double .valueOf(DEFAULT_NOISE_WALL_JITTER));
		cfgParams.addDefault("Noise-Room-Freq",     Double .valueOf(DEFAULT_NOISE_ROOM_FREQ  ));
		cfgParams.addDefault("Noise-Room-Octave",   Integer.valueOf(DEFAULT_NOISE_ROOM_OCTAVE));
		cfgParams.addDefault("Noise-Room-Gain",     Double .valueOf(DEFAULT_NOISE_ROOM_GAIN  ));
		cfgParams.addDefault("Thresh-Room-Or-Hall", Double .valueOf(DEFAULT_THRESH_ROOM_HALL ));
		cfgParams.addDefault("Nominal-Room-Size",   Integer.valueOf(DEFAULT_NOMINAL_ROOM_SIZE));
		cfgParams.addDefault("Noise-Stairs-Freq",   Double .valueOf(DEFAULT_NOISE_STAIRS_FREQ));
		// block types
		cfgBlocks.addDefault("SubFloor",           DEFAULT_BLOCK_SUBFLOOR          );
		cfgBlocks.addDefault("SubCeiling",         DEFAULT_BLOCK_SUBCEILING        );
		cfgBlocks.addDefault("SubWall",            DEFAULT_BLOCK_SUBWALL           );
		cfgBlocks.addDefault("Hall-Ceiling",       DEFAULT_BLOCK_HALL_CEILING      );
		cfgBlocks.addDefault("Hall-Ceiling-Slab",  DEFAULT_BLOCK_HALL_CEILING_SLAB );
		cfgBlocks.addDefault("Hall-Wall-Top-X",    DEFAULT_BLOCK_HALL_WALL_TOP_X   );
		cfgBlocks.addDefault("Hall-Wall-Top-Z",    DEFAULT_BLOCK_HALL_WALL_TOP_Z   );
		cfgBlocks.addDefault("Hall-Wall-Center",   DEFAULT_BLOCK_HALL_WALL_CENTER  );
		cfgBlocks.addDefault("Hall-Wall-Bottom-X", DEFAULT_BLOCK_HALL_WALL_BOTTOM_X);
		cfgBlocks.addDefault("Hall-Wall-Bottom-Z", DEFAULT_BLOCK_HALL_WALL_BOTTOM_Z);
		cfgBlocks.addDefault("Door-Border-Top-X",  DEFAULT_BLOCK_DOOR_BORDER_TOP_X );
		cfgBlocks.addDefault("Door-Border-Top-Z",  DEFAULT_BLOCK_DOOR_BORDER_TOP_Z );
		cfgBlocks.addDefault("Door-Border-Side",   DEFAULT_BLOCK_DOOR_BORDER_SIDE  );
		cfgBlocks.addDefault("Hall-Floor-EE",      DEFAULT_BLOCK_HALL_FLOOR_EE     );
		cfgBlocks.addDefault("Hall-Floor-EO",      DEFAULT_BLOCK_HALL_FLOOR_EO     );
		cfgBlocks.addDefault("Hall-Floor-OE",      DEFAULT_BLOCK_HALL_FLOOR_OE     );
		cfgBlocks.addDefault("Hall-Floor-OO",      DEFAULT_BLOCK_HALL_FLOOR_OO     );
		cfgBlocks.addDefault("Door-Guest",         DEFAULT_DOOR_GUEST              );
	}



}
