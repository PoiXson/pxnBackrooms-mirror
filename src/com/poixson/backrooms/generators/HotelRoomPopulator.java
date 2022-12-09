package com.poixson.backrooms.generators;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Door;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.poixson.tools.dao.Insew;
import com.poixson.tools.dao.Ixy;


public class HotelRoomPopulator extends BlockPopulator {



	public HotelRoomPopulator() {
	}



	@Override
	public void populate(final WorldInfo world, final Random rnd,
	final int chunkX, final int chunkZ, final LimitedRegion region) {
		final int x = chunkX * 16;
		final int z = chunkZ * 16;
		final int y = Level_005.HOTEL_Y + Level_005.SUBFLOOR + 1;
		Insew dao = this.findRoomWalls(x, y, z, region);
		if (dao == null) dao = this.findRoomWalls(x+10, y, z+10, region);
		if (dao == null) dao = this.findRoomWalls(x,    y, z+10, region);
		if (dao == null) dao = this.findRoomWalls(x+10, y, z,    region);
		if (dao == null) return;
		this.buildHotelRooms(dao, y, region, 7.0);
	}



	public Insew findRoomWalls(final int x, final int y, final int z,
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
				if (Level_005.HOTEL_WALL.equals(type))
					foundN = (z - i) + 1;
				else
				if (!Material.AIR.equals(type)) return null;
			}
			// south
			if (foundS == Integer.MIN_VALUE) {
				type = region.getType(x, y, z+i);
				if (Material.BLACK_GLAZED_TERRACOTTA.equals(type)) return null;
				if (Level_005.HOTEL_WALL.equals(type))
					foundS = (z + i) - 1;
				else
				if (!Material.AIR.equals(type)) return null;
			}
			// east
			if (foundE == Integer.MIN_VALUE) {
				type = region.getType(x+i, y, z);
				if (Material.BLACK_GLAZED_TERRACOTTA.equals(type)) return null;
				if (Level_005.HOTEL_WALL.equals(type))
					foundE = (x + i) - 1;
				else
				if (!Material.AIR.equals(type)) return null;
			}
			// west
			if (foundW == Integer.MIN_VALUE) {
				type = region.getType(x-i, y, z);
				if (Material.BLACK_GLAZED_TERRACOTTA.equals(type)) return null;
				if (Level_005.HOTEL_WALL.equals(type))
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
		return new Insew(foundN, foundS, foundE, foundW);
	}



	public void buildHotelRooms(final Insew dao, final int y, final LimitedRegion region, double room_size) {
		final int total_ns = dao.s - dao.n;
		final int total_ew = dao.e - dao.w;
		final int rooms_deep = (int) Math.floor( ((double)total_ns) / room_size );
		final int rooms_wide = (int) Math.floor( ((double)total_ew) / room_size );
		final int room_depth = Math.floorDiv(total_ns, rooms_deep);
		final int room_width = Math.floorDiv(total_ew, rooms_wide);
		final int rooms_deep_half = (int)Math.floor( ((double)rooms_deep) * 0.5 );
		final int rooms_wide_half = (int)Math.floor( ((double)rooms_wide) * 0.5 );
		int room_x, room_z;
		int w, d;
		final int extra_x = (total_ew - (rooms_wide * room_width)) + 1;
		final int extra_z = (total_ns - (rooms_deep * room_depth)) + 1;
		for (int iz=0; iz<rooms_deep; iz++) {
			d = room_depth;
			room_z = dao.n + (iz * d);
			if (iz == rooms_deep_half) d      += extra_z;
			if (iz >  rooms_deep_half) room_z += extra_z;
			for (int ix=0; ix<rooms_wide; ix++) {
				if (ix != 0 && ix != rooms_wide-1
				&&  iz != 0 && iz != rooms_deep-1)
					continue;
				w = room_width;
				room_x = dao.w + (ix * w);
				if (ix == rooms_wide_half) w      += extra_x;
				if (ix >  rooms_wide_half) room_x += extra_x;
				this.buildHotelRoom(room_x, y, room_z, w, d, region);
			}
		}
	}

	public enum HotelRoomType {
		EMPTY,
		WALL,
		DOOR,
		DOOR_INSET,
	}

	public void buildHotelRoom(final int x, final int y, final int z, final int w, final int d, final LimitedRegion region) {
		final HashMap<Ixy, HotelRoomType> room = new HashMap<Ixy, HotelRoomType>();
		final int wh = (int) Math.floor( ((double)w) * 0.5 );
//		final int dh = (int) Math.floor( ((double)d) * 0.5 );
		int xx, zz;
		HotelRoomType type;
		// pregen room
		for (int iz=0; iz<d; iz++) {
			for (int ix=0; ix<w; ix++) {
				type = HotelRoomType.EMPTY;
				// wall
				if (ix == 0 || ix == w-1
				||  iz == 0 || iz == d-1 )
					type = HotelRoomType.WALL;
				// door
				if (iz == 0) {
					if (ix == wh)
						type = HotelRoomType.DOOR;
					else
					if (ix == wh-1
					||  ix == wh+1)
						type = HotelRoomType.DOOR_INSET;
				}
				room.put(new Ixy(ix, iz), type);
			}
		}
		// build room
		Door door;
		Orientable log;
		for (int iz=0; iz<d; iz++) {
			zz = iz + z;
			for (int ix=0; ix<w; ix++) {
				xx = ix + x;
				type = room.get(new Ixy(ix, iz));
				if (!HotelRoomType.WALL.equals(type))
					region.setType(xx, y, zz, Material.CYAN_WOOL);
				switch (type) {
				case WALL:
					for (int iy=0; iy<6; iy++) {
						region.setType(xx, y+iy, zz, Material.STRIPPED_BIRCH_LOG);
					}
					break;
				case DOOR:
				case DOOR_INSET:
					region.setType(xx, y, zz-1, Material.BLACK_GLAZED_TERRACOTTA);
					for (int iy=1; iy<5; iy++) {
						region.setType(xx, y+iy, zz-1, Material.AIR);
					}
					switch (type) {
					case DOOR:
						region.setType(xx, y+1, zz-1, Material.DARK_OAK_DOOR);
						region.setType(xx, y+2, zz-1, Material.DARK_OAK_DOOR);
							door = (Door) region.getBlockData(xx, y+1, zz-1);
							door.setHalf(Bisected.Half.BOTTOM);
							region.setBlockData(xx, y+1, zz-1, door);
							door = (Door) region.getBlockData(xx, y+2, zz-1);
							door.setHalf(Bisected.Half.TOP);
							region.setBlockData(xx, y+2, zz-1, door);
						region.setType(xx, y+3, zz, Material.STRIPPED_SPRUCE_WOOD);
						region.setType(xx, y+4, zz, Material.STRIPPED_SPRUCE_LOG);
							log = (Orientable) region.getBlockData(xx, y+4, zz);
							log.setAxis(Axis.X);
							region.setBlockData(xx, y+4, zz, log);
						region.setType(xx, y+5, zz, Material.STRIPPED_BIRCH_LOG);
						break;
					case DOOR_INSET:
						for (int iy=1; iy<4; iy++) {
							region.setType(xx, y+iy, zz, Material.STRIPPED_SPRUCE_LOG);
						}
						region.setType(xx, y+4, zz, Material.STRIPPED_SPRUCE_LOG);
							log = (Orientable) region.getBlockData(xx, y+4, zz);
							log.setAxis(Axis.X);
							region.setBlockData(xx, y+4, zz, log);
						region.setType(xx, y+5, zz, Material.STRIPPED_BIRCH_LOG);
						region.setType(xx, y+3, zz-1, Material.SOUL_WALL_TORCH);
							final Directional torch = (Directional) region.getBlockData(xx, y+3, zz-1);
							torch.setFacing(BlockFace.NORTH);
							region.setBlockData(xx, y+3, zz-1, torch);
						break;
					default: break;
					}
				default: break;
				}
			}
		}
	}



}
