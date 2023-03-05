package com.poixson.backrooms.levels;

import static com.poixson.backrooms.levels.Level_000.SUBFLOOR;
import static com.poixson.commonmc.utils.LocationUtils.Rotate;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Bed;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.poixson.tools.dao.Iabcd;


// 5 | Hotel
public class Pop_005 extends BlockPopulator {

	public static final boolean ENABLE_GENERATE = true;
	public static final boolean ENABLE_ROOF     = true;

	public static final Material HOTEL_WALL = Gen_005.HOTEL_WALL;

	protected final Gen_005 gen;



	public Pop_005(final Gen_005 gen) {
		super();
		this.gen = gen;
	}



	@Override
	public void populate(final WorldInfo world, final Random rnd,
	final int chunkX, final int chunkZ, final LimitedRegion region) {
		if (!Gen_005.ENABLE_GENERATE) return;
		if (!ENABLE_GENERATE) return;
		final int x = chunkX * 16;
		final int z = chunkZ * 16;
		final int y = this.gen.level_y + SUBFLOOR + 1;
		Iabcd dao = this.findRoomWalls(x, y, z, region);
		if (dao == null) dao = this.findRoomWalls(x+10, y, z+10, region);
		if (dao == null) dao = this.findRoomWalls(x,    y, z+10, region);
		if (dao == null) dao = this.findRoomWalls(x+10, y, z,    region);
		if (dao == null) return;
//TODO: alternate rooms: front desk, theater
		this.buildHotelRooms(dao, y, region, 7.0);
	}



	public Iabcd findRoomWalls(final int x, final int y, final int z,
			final LimitedRegion region) {
		// is wall
		if (!Material.AIR.equals(region.getType(x, y, z)))
			return null;
		// is hall
		if (Material.BLACK_GLAZED_TERRACOTTA.equals(region.getType(x, y-1, z)))
			return null;
		int foundN = Integer.MIN_VALUE;
		int foundS = Integer.MIN_VALUE;
		int foundE = Integer.MIN_VALUE;
		int foundW = Integer.MIN_VALUE;
		Material type;
		for (int i=2; i<34; i++) {
			if (!region.isInRegion(x, y, z-i)
			||  !region.isInRegion(x, y, z+i)
			||  !region.isInRegion(x+i, y, z)
			||  !region.isInRegion(x-i, y, z) )
				break;
			if (foundN != Integer.MIN_VALUE
			&&  foundS != Integer.MIN_VALUE
			&&  foundE != Integer.MIN_VALUE
			&&  foundW != Integer.MIN_VALUE )
				break;
			// north
			if (foundN == Integer.MIN_VALUE) {
				type = region.getType(x, y, z-i);
				if (Material.BLACK_GLAZED_TERRACOTTA.equals(type)) return null;
				if (HOTEL_WALL.equals(type))
					foundN = (z - i) + 1;
				else
				if (!Material.AIR.equals(type)) return null;
			}
			// south
			if (foundS == Integer.MIN_VALUE) {
				type = region.getType(x, y, z+i);
				if (Material.BLACK_GLAZED_TERRACOTTA.equals(type)) return null;
				if (HOTEL_WALL.equals(type))
					foundS = (z + i) - 1;
				else
				if (!Material.AIR.equals(type)) return null;
			}
			// east
			if (foundE == Integer.MIN_VALUE) {
				type = region.getType(x+i, y, z);
				if (Material.BLACK_GLAZED_TERRACOTTA.equals(type)) return null;
				if (HOTEL_WALL.equals(type))
					foundE = (x + i) - 1;
				else
				if (!Material.AIR.equals(type)) return null;
			}
			// west
			if (foundW == Integer.MIN_VALUE) {
				type = region.getType(x-i, y, z);
				if (Material.BLACK_GLAZED_TERRACOTTA.equals(type)) return null;
				if (HOTEL_WALL.equals(type))
					foundW = (x - i) + 1;
				else
				if (!Material.AIR.equals(type)) return null;
			}
		}
		// full area is available
		if (foundN == Integer.MIN_VALUE
		||  foundS == Integer.MIN_VALUE
		||  foundE == Integer.MIN_VALUE
		||  foundW == Integer.MIN_VALUE )
			return null;
		return new Iabcd(foundN, foundS, foundE, foundW);
	}



	public void buildHotelRooms(final Iabcd area, final int y,
			final LimitedRegion region, double room_size) {
		final int total_ns = area.b - area.a;
		final int total_ew = area.c - area.d;
		final int rooms_deep = (int) Math.floor( ((double)total_ns) / room_size );
		final int rooms_wide = (int) Math.floor( ((double)total_ew) / room_size );
		if (rooms_deep < 1 || rooms_wide < 1) return;
		final int room_depth = Math.floorDiv(total_ns, rooms_deep);
		final int room_width = Math.floorDiv(total_ew, rooms_wide);
		final int rooms_deep_half = (int)Math.floor( ((double)rooms_deep) * 0.5 );
		final int rooms_wide_half = (int)Math.floor( ((double)rooms_wide) * 0.5 );
		int room_x, room_z;
		int w, d;
		BlockFace direction;
		final int extra_x = (total_ew - (rooms_wide * room_width)) + 1;
		final int extra_z = (total_ns - (rooms_deep * room_depth)) + 1;
		for (int iz=0; iz<rooms_deep; iz++) {
			d = room_depth;
			room_z = area.a + (iz * d);
			if (iz == rooms_deep_half) d      += extra_z;
			if (iz >  rooms_deep_half) room_z += extra_z;
			for (int ix=0; ix<rooms_wide; ix++) {
				if (ix != 0 && ix != rooms_wide-1
				&&  iz != 0 && iz != rooms_deep-1)
					continue;
				w = room_width;
				room_x = area.d + (ix * w);
				if (ix == rooms_wide_half) w      += extra_x;
				if (ix >  rooms_wide_half) room_x += extra_x;
				// north
				if (iz == 0) {
					// north-west
					if (ix == 0) {
						if (0.0 < this.gen.noiseHotelRooms.getNoise(room_x, room_z)) direction = BlockFace.NORTH;
						else                                                         direction = BlockFace.WEST;
					} else
					// north-east
					if (ix == rooms_wide-1) {
						if (0.0 < this.gen.noiseHotelRooms.getNoise(room_x, room_z)) direction = BlockFace.NORTH;
						else                                                         direction = BlockFace.EAST;
					} else {
						direction = BlockFace.NORTH;
					}
				} else
				// south
				if (iz == rooms_deep-1) {
					// south-west
					if (ix == 0) {
						if (0.0 < this.gen.noiseHotelRooms.getNoise(room_x, room_z)) direction = BlockFace.SOUTH;
						else                                                         direction = BlockFace.WEST;
					} else
					// south-east
					if (ix == rooms_wide-1) {
						if (0.0 < this.gen.noiseHotelRooms.getNoise(room_x, room_z)) direction = BlockFace.SOUTH;
						else                                                         direction = BlockFace.EAST;
					} else {
						direction = BlockFace.SOUTH;
					}
				// east/west
				} else {
					if (ix == 0) direction = BlockFace.WEST;
					else         direction = BlockFace.EAST;
				}
				this.buildHotelRoom(room_x, y, room_z, w, d, direction, region);
			}
		}
	}



	public enum HotelRoomType {
		EMPTY,
		WALL,
		DOOR,
		DOOR_INSET,
		BED,
		LAMP,
	}



	public class HotelRoomDAO {

		public final double value;
		public HotelRoomType type;
		public Material block_wall;
		public Material block_carpet;
		public Material block_bed;

		public HotelRoomDAO(final double value, final HotelRoomType type,
				final Material block_wall, final Material block_carpet, final Material block_bed) {
			this.type  = type;
			this.value = value;
			this.block_wall   = block_wall;
			this.block_carpet = block_carpet;
			this.block_bed    = block_bed;
		}

	}



	public void buildHotelRoom(final int x, final int y, final int z, final int w, final int d,
			final BlockFace direction, final LimitedRegion region) {
		final int wh = (int) Math.floor( (double)area.c * 0.5 );
		final int dh = (int) Math.floor( (double)area.d * 0.5 );
		double value = this.gen.noiseHotelRooms.getNoise(x, z);
		// wall
		final Material block_wall;
//TODO: use cellular noise
//TODO: mod 16 ?
		switch ( (int)Math.round(((1.0+value)*0.5) * 20.0) ) {
		case 0:  block_wall = Material.WHITE_TERRACOTTA;      break;
		case 1:  block_wall = Material.ORANGE_TERRACOTTA;     break;
		case 2:  block_wall = Material.MAGENTA_TERRACOTTA;    break;
		case 3:  block_wall = Material.LIGHT_BLUE_TERRACOTTA; break;
		case 4:  block_wall = Material.YELLOW_TERRACOTTA;     break;
		case 5:  block_wall = Material.LIME_TERRACOTTA;       break;
		case 6:  block_wall = Material.PINK_TERRACOTTA;       break;
		case 7:  block_wall = Material.GRAY_TERRACOTTA;       break;
		case 8:  block_wall = Material.LIGHT_GRAY_TERRACOTTA; break;
		case 9:  block_wall = Material.CYAN_TERRACOTTA;       break;
		case 10: block_wall = Material.PURPLE_TERRACOTTA;     break;
		case 11: block_wall = Material.BLUE_TERRACOTTA;       break;
		case 12: block_wall = Material.BROWN_TERRACOTTA;      break;
		case 13: block_wall = Material.GREEN_TERRACOTTA;      break;
		case 14: block_wall = Material.RED_TERRACOTTA;        break;
		case 15: block_wall = Material.BLACK_TERRACOTTA;      break;
		default: block_wall = Material.STRIPPED_BIRCH_LOG;    break;
		}
		// carpet
		final Material block_carpet;
		switch ( (int)Math.round(((1.0+value)*0.5) * 18.0) ) {
		case 0:  block_carpet = Material.WHITE_WOOL;      break;
		case 1:  block_carpet = Material.ORANGE_WOOL;     break;
		case 2:  block_carpet = Material.MAGENTA_WOOL;    break;
		case 3:  block_carpet = Material.LIGHT_BLUE_WOOL; break;
		case 4:  block_carpet = Material.YELLOW_WOOL;     break;
		case 5:  block_carpet = Material.LIME_WOOL;       break;
		case 6:  block_carpet = Material.PINK_WOOL;       break;
		case 7:  block_carpet = Material.GRAY_WOOL;       break;
		case 8:  block_carpet = Material.LIGHT_GRAY_WOOL; break;
		case 9:  block_carpet = Material.CYAN_WOOL;       break;
		case 10: block_carpet = Material.PURPLE_WOOL;     break;
		case 11: block_carpet = Material.BLUE_WOOL;       break;
		case 12: block_carpet = Material.BROWN_WOOL;      break;
		case 13: block_carpet = Material.GREEN_WOOL;      break;
		case 14: block_carpet = Material.RED_WOOL;        break;
		case 15: block_carpet = Material.BLACK_WOOL;      break;
		default: block_carpet = Material.SPRUCE_PLANKS;   break;
		}
		// bed
		final Material block_bed;
		switch ( (int)Math.round(((1.0+value)*0.5) * 16.0) ) {
		case 0:  block_bed = Material.WHITE_BED;      break;
		case 1:  block_bed = Material.ORANGE_BED;     break;
		case 2:  block_bed = Material.MAGENTA_BED;    break;
		case 3:  block_bed = Material.LIGHT_BLUE_BED; break;
		case 4:  block_bed = Material.YELLOW_BED;     break;
		case 5:  block_bed = Material.LIME_BED;       break;
		case 6:  block_bed = Material.PINK_BED;       break;
		case 7:  block_bed = Material.GRAY_BED;       break;
		case 8:  block_bed = Material.LIGHT_GRAY_BED; break;
		case 9:  block_bed = Material.CYAN_BED;       break;
		case 10: block_bed = Material.PURPLE_BED;     break;
		case 11: block_bed = Material.BLUE_BED;       break;
		case 12: block_bed = Material.BROWN_BED;      break;
		case 13: block_bed = Material.GREEN_BED;      break;
		case 14: block_bed = Material.RED_BED;        break;
		case 15: block_bed = Material.BLACK_BED;      break;
		default: block_bed = Material.RED_BED;        break;
		}
		// pregen room
		final HashMap<Iab, HotelRoomDAO> room = new HashMap<Iab, HotelRoomDAO>();
		HotelRoomType type;
		for (int iz=0; iz<area.d; iz++) {
			for (int ix=0; ix<area.c; ix++) {
				value = this.gen.noiseHotelRooms.getNoise(x+ix, z+iz);
				type = HotelRoomType.EMPTY;
				// wall
				if (ix == 0 || ix == area.c-1
				||  iz == 0 || iz == area.d-1 ) {
					type = HotelRoomType.WALL;
				}
				if (ix == wh && iz == dh) {
					type = HotelRoomType.LAMP;
				}
				// door
				if (iz == 0) {
					if (ix == wh) {
						type = HotelRoomType.DOOR;
					}
					if (ix == wh-1 || ix == wh+1) {
						type = HotelRoomType.DOOR_INSET;
					}
				}
				// bed
				if (ix == wh && iz == area.d-2) {
					type = HotelRoomType.BED;
				}
				room.put(
					new Iab(ix, iz),
					new HotelRoomDAO(value, type, block_wall, block_carpet, block_bed)
				);
			}
		}
		// build room
		final BlockPlotter plot = new BlockPlotter(region, );
		plot.axis();
		plot.location(x, y, z);
		plot.w = area.c;
		plot.d = area.d;
		HotelRoomDAO dao;
		for (int iz=0; iz<area.d; iz++) {
			for (int ix=0; ix<area.c; ix++) {
				dao = room.get(new Iab(ix, iz));
				// carpet
				if (!HotelRoomType.WALL.equals(dao.type))
					plot.setRotBlock(ix, 0, iz, direction, dao.block_carpet);
				switch (dao.type) {
				case LAMP:
					if (Gen_005.ENABLE_ROOF) {
						plot.setRotBlock(ix, 6, iz, direction, Material.REDSTONE_BLOCK);
						plot.setRotBlock(ix, 5, iz, direction, Material.REDSTONE_LAMP, "on");
					}
					break;
				case WALL:
					for (int iy=0; iy<7; iy++)
						plot.setRotBlock(ix, iy, iz, direction, dao.block_wall);
					break;
				case DOOR:
				case DOOR_INSET:
					plot.setRotBlock(ix, 0, iz-1, direction, Material.BLACK_GLAZED_TERRACOTTA);
					for (int iy=1; iy<5; iy++)
						plot.setRotBlock(ix, iy, iz-1, direction, Material.AIR);
					plot.setRotBlock(ix, 4, iz, direction, HOTEL_WALL,
						(BlockFace.NORTH.equals(direction) || BlockFace.SOUTH.equals(direction) ? "x" : "z"));
					for (int i=0; i<2; i++) {
						plot.setRotBlock(ix, i+5, iz, direction, dao.block_wall);
					}
					break;
				default:
					// ceiling
					if (ENABLE_ROOF && Gen_005.ENABLE_ROOF) {
						plot.setRotBlock(ix, 6, iz, direction, Material.SMOOTH_STONE);
						plot.setRotBlock(ix, 5, iz, direction, Material.SMOOTH_STONE_SLAB, "top");
					}
					break;
				}
				switch (dao.type) {
				case DOOR:
					plot.setRotBlock(ix, 1, iz-1, direction, Material.DARK_OAK_DOOR, "bottom,"+direction.toString().toLowerCase());
					plot.setRotBlock(ix, 2, iz-1, direction, Material.DARK_OAK_DOOR, "top,"   +direction.toString().toLowerCase());
					plot.setRotBlock(ix, 3, iz,   direction, HOTEL_WALL);
					break;
				case DOOR_INSET:
					for (int iy=1; iy<4; iy++)
						plot.setRotBlock(ix, iy, iz, direction, HOTEL_WALL);
					plot.setRotBlock(ix, 3, iz-1, direction, Material.REDSTONE_WALL_TORCH, direction.toString().toLowerCase());
					break;
				case BED:
					plot.setRotBlock(ix, 1, iz, direction, dao.block_bed);
						Bed bed = (Bed) plot.getRotBlockData(ix, 1, iz, direction);
						bed.setFacing(Rotate(direction, 0.5));
						bed.setPart(Bed.Part.HEAD);
						plot.setRotBlockData(ix, 1, iz, direction, bed);
					plot.setRotBlock(ix, 1, iz-1, direction, dao.block_bed);
						bed = (Bed) plot.getRotBlockData(ix, 1, iz-1, direction);
						bed.setFacing(Rotate(direction, 0.5));
						bed.setPart(Bed.Part.FOOT);
						plot.setRotBlockData(ix, 1, iz-1, direction, bed);
					plot.setRotBlock(ix-1, 1, iz, direction, Material.SCAFFOLDING);
					plot.setRotBlock(ix+1, 1, iz, direction, Material.SCAFFOLDING);
					break;
				default: break;
				}
			}
		}
	}



}
