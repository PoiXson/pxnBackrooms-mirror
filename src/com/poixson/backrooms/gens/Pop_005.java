package com.poixson.backrooms.gens;

import static com.poixson.backrooms.gens.Gen_005.DEFAULT_BLOCK_SUBWALL;
import static com.poixson.backrooms.worlds.Level_000.SUBFLOOR;
import static com.poixson.utils.BlockUtils.StringToMaterial;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.LimitedRegion;

import com.poixson.backrooms.BackroomsPop;
import com.poixson.backrooms.gens.hotel.HotelRoomGuest;
import com.poixson.backrooms.gens.hotel.HotelRoomPool;
import com.poixson.backrooms.gens.hotel.HotelRoomStairs;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.dao.Iabcd;
import com.poixson.tools.plotter.BlockPlotter;


// 5 | Hotel
public class Pop_005 implements BackroomsPop {

	protected final Level_000 level0;
	protected final Gen_005   gen;



	public Pop_005(final Level_000 level0) {
		this.level0 = level0;
		this.gen    = level0.gen_005;
	}



	@Override
	public void populate(final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final LimitedRegion region, final int chunkX, final int chunkZ) {
		if (!this.gen_005.enable_gen) return;
		final int x = (chunkX * 16) + 7;
		final int z = (chunkZ * 16) + 7;
		final int y = this.gen.level_y + SUBFLOOR + 1;
		// returns x z w d
		final Iabcd area = this.findRoomWalls(region, x, y, z);
		if (area == null) return;
//TODO: alternate rooms: front desk, theater
		this.buildHotelRooms(plots, region, area, y);
	}



	// returns x z w d
	public Iabcd findRoomWalls(final LimitedRegion region,
			final int x, final int y, final int z) {
		final Material block_subwall = StringToMaterial(this.gen.block_subwall, DEFAULT_BLOCK_SUBWALL);
		if (block_subwall == null) throw new RuntimeException("Invalid block type for level 5 Hall-SubWall");
		// is room area, not hall or wall
		if (!block_subwall.equals(region.getType(x, y, z))) return null;
		int foundN = Integer.MIN_VALUE;
		int foundS = Integer.MIN_VALUE;
		int foundE = Integer.MIN_VALUE;
		int foundW = Integer.MIN_VALUE;
		//DISTANCE_LOOP:
		for (int i=2; i<34; i++) {
			// north
			if (foundN == Integer.MIN_VALUE
			&& region.isInRegion(x, y, z-i)) {
				if (!block_subwall.equals(region.getType(x, y, z-i)))
					foundN = (z - i) + 1;
			}
			// south
			if (foundS == Integer.MIN_VALUE
			&& region.isInRegion(x, y, z+i)) {
				if (!block_subwall.equals(region.getType(x, y, z+i)))
					foundS = (z + i) - 1;
			}
			// east
			if (foundE == Integer.MIN_VALUE
			&& region.isInRegion(x+i, y, z)) {
				if (!block_subwall.equals(region.getType(x+i, y, z)))
					foundE = (x + i) - 1;
			}
			// west
			if (foundW == Integer.MIN_VALUE
			&& region.isInRegion(x-i, y, z)) {
				if (!block_subwall.equals(region.getType(x-i, y, z)))
					foundW = (x - i) + 1;
			}
			if (foundN != Integer.MIN_VALUE
			&&  foundS != Integer.MIN_VALUE
			&&  foundE != Integer.MIN_VALUE
			&&  foundW != Integer.MIN_VALUE )
				//               x,      z,      w,             d
				return new Iabcd(foundW, foundN, foundE-foundW, foundS-foundN);
		} // end DISTANCE_LOOP
		return null;
	}



	// group of rooms
	public void buildHotelRooms(final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final LimitedRegion region, final Iabcd area, final int y) {
		final int room_size = this.gen.nominal_room_size.get();
		if (room_size < 4 || room_size > 20) throw new RuntimeException("Invalid nominal room size: "+Integer.toString(room_size));
		final Material door_guest = Material.matchMaterial(this.gen.door_guest.get());
		if (door_guest == null) throw new RuntimeException("Invalid block type for level 5 Door-Guest");
		// room builders
		final HotelRoomGuest  room_guest  = new HotelRoomGuest( this.level0, this.gen.noiseHotelRooms, door_guest);
		final HotelRoomPool   room_pool   = new HotelRoomPool(  this.level0);
		final HotelRoomStairs room_stairs = new HotelRoomStairs(this.level0);
		// area = x z w d
		final int num_rooms_wide = Math.floorDiv(area.c, room_size);
		final int num_rooms_deep = Math.floorDiv(area.d, room_size);
		if (num_rooms_deep < 1 || num_rooms_wide < 1) return;
		final int room_width = Math.floorDiv(area.c, num_rooms_wide);
		final int room_depth = Math.floorDiv(area.d, num_rooms_deep);
		final int rooms_mid_w = Math.floorDiv(num_rooms_wide, 2);
		final int rooms_mid_d = Math.floorDiv(num_rooms_deep, 2);
		final int extra_x = (area.c - (num_rooms_wide * room_width)) + 1;
		final int extra_z = (area.d - (num_rooms_deep * room_depth)) + 1;
		int x, z, w, d;
		BlockFace direction;
		for (int room_z=0; room_z<num_rooms_deep; room_z++) {
			d = room_depth;
			z = area.b + (room_z * d);
			if (room_z == rooms_mid_d) d += extra_z; else
			if (room_z >  rooms_mid_d) z += extra_z;
			LOOP_ROOM_X:
			for (int room_x=0; room_x<num_rooms_wide; room_x++) {
				// center of rooms
				if (room_x != 0 && room_x != num_rooms_wide-1
				&&  room_z != 0 && room_z != num_rooms_deep-1)
					continue LOOP_ROOM_X;
				w = room_width;
				x = area.a + (room_x * w);
				if (room_x == rooms_mid_w) w += extra_x; else
				if (room_x >  rooms_mid_w) x += extra_x;
				// find room direction
				// north
				if (room_z == 0) {
					// north-west
					if (room_x == 0) {
						if (0.0 < this.gen.noiseHotelRooms.getNoise(x, z)) direction = BlockFace.NORTH;
						else                                               direction = BlockFace.WEST;
					} else
					// north-east
					if (room_x == num_rooms_wide-1) {
						if (0.0 < this.gen.noiseHotelRooms.getNoise(x, z)) direction = BlockFace.NORTH;
						else                                               direction = BlockFace.EAST;
					} else {
						direction = BlockFace.NORTH;
					}
				} else
				// south
				if (room_z == num_rooms_deep-1) {
					// south-west
					if (room_x == 0) {
						if (0.0 < this.gen.noiseHotelRooms.getNoise(x, z)) direction = BlockFace.SOUTH;
						else                                               direction = BlockFace.WEST;
					} else
					// south-east
					if (room_x == num_rooms_wide-1) {
						if (0.0 < this.gen.noiseHotelRooms.getNoise(x, z)) direction = BlockFace.SOUTH;
						else                                               direction = BlockFace.EAST;
					} else {
						direction = BlockFace.SOUTH;
					}
				// east/west
				} else {
					if (room_x == 0) direction = BlockFace.WEST;
					else             direction = BlockFace.EAST;
				}
				// x, z, w, d
				final Iabcd room_area;
				// rotate the room
				switch (direction) {
				case NORTH: room_area = new Iabcd(x,   z-1, w, d+1); break;
				case SOUTH: room_area = new Iabcd(x-1, z-1, w, d+1); break;
				case EAST:  room_area = new Iabcd(x-1, z-1, d, w+1); break;
				case WEST:  room_area = new Iabcd(x-1, z,   d, w+1); break;
				default: throw new RuntimeException("Unknown room direction: "+direction.toString());
				}
				// attic stairs
				if (room_area.d == 9) {
					if (room_stairs.checkAtticWall(region, room_area, direction)) {
						this.level0.portal_5_to_19.add(room_area.a, room_area.b);
						room_stairs.build(plots, region, room_area, y, direction);
						continue LOOP_ROOM_X;
					}
				}
				// pool room
				final BlockFace pool_direction = room_pool.canBuildHere(room_area, region);
				if (pool_direction != null) {
					this.level0.portal_5_to_37.add(room_area.a, room_area.b);
					room_pool.build(plots, region, room_area, y, direction);
					continue LOOP_ROOM_X;
				}
				// hotel guest room
				room_guest.build(plots, region, room_area, y, direction);
			} // end ix
		} // end iz
	}



}
