package com.poixson.backrooms.gens.hotel;

import static com.poixson.backrooms.worlds.Level_000.SUBCEILING;
import static com.poixson.backrooms.worlds.Level_000.SUBFLOOR;
import static com.poixson.utils.LocationUtils.FaceToAxisString;
import static com.poixson.utils.LocationUtils.FaceToIxz;
import static com.poixson.utils.LocationUtils.FaceToPillarAxisString;
import static com.poixson.utils.LocationUtils.Rotate;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.LimitedRegion;

import com.poixson.backrooms.worlds.Level_000;
import com.poixson.tools.dao.Iab;
import com.poixson.tools.dao.Iabcd;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.tools.plotter.PlotterFactory;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.StringUtils;


public class HotelRoomStairs implements HotelRoom {

	protected static final double THRESH_ATTIC_STAIRS = 0.8;

	protected final Level_000 level0;

	protected final FastNoiseLiteD noiseHotelStairs;



	public HotelRoomStairs(final Level_000 level0) {
		this.level0 = level0;
		this.noiseHotelStairs = level0.gen_005.noiseHotelStairs;
	}



	@Override
	public void build(final Iabcd area, final int y, final BlockFace direction,
			final LimitedRegion region, final LinkedList<BlockPlotter> plots) {
		this.buildHotelRoomStairs(area, y, direction, region, plots);
		this.buildAtticStairs(    area, y, direction, region, plots);
	}



	protected void buildHotelRoomStairs(final Iabcd area, final int y, final BlockFace facing,
			final LimitedRegion region, final LinkedList<BlockPlotter> plots) {
		final Material block_hall_wall = Material.matchMaterial(this.level0.gen_005.block_hall_wall.get());
		if (block_hall_wall == null) throw new RuntimeException("Invalid block type for level 5 Hall-Wall");
		final int x = area.a;
		final int z = area.b;
		final int w = area.c;
		final int d = area.d;
		final int h = Level_000.H_005 + 2;
		final BlockPlotter plot =
			(new PlotterFactory())
			.placer(region)
			.axis("use")
			.rotate(facing.getOppositeFace())
			.xyz(x, y, z)
			.whd(w, h, d)
			.build();
		plot.type('.', Material.AIR);
		plot.type('#', Material.BEDROCK);
		plot.type('=', Material.DARK_OAK_PLANKS);
		plot.type('$', block_hall_wall, "[axis=y]");
		plot.type('&', block_hall_wall, "[axis="+FaceToPillarAxisString(Rotate(facing, 0.25))+"]");
		plot.type('d', Material.SPRUCE_DOOR, "[half=upper,hinge=right,facing="+FaceToAxisString(facing)+"]");
		plot.type('D', Material.SPRUCE_DOOR, "[half=lower,hinge=right,facing="+FaceToAxisString(facing)+"]");
		plot.type('_', Material.DARK_OAK_PRESSURE_PLATE);
		plot.type('L', Material.DARK_OAK_STAIRS, "[half=bottom,facing="+FaceToAxisString(facing.getOppositeFace())+"]");
		final StringBuilder[][] matrix = plot.getMatrix3D();
		final int door_x = Math.floorDiv(w, 2) - 2;
		char fill;
		for (int iy=0; iy<h; iy++) {
			for (int iz=2; iz<d-1; iz++) {
				if (iy == 0   ) fill = '='; else // floor
				if (iy == iz-2) fill = 'L'; else // stairs
				if (iy >  iz-2) fill = ' '; else // air above stairs
					fill = '#';                  // fill under stairs
				matrix[iy][iz]
					.append(StringUtils.Repeat(door_x+1,   '#'))
					.append(StringUtils.Repeat(3,         fill))
					.append(StringUtils.Repeat(w-door_x-4, '#'));
			}
			// floor under pressure plate
			if (iy == 0) matrix[iy][1].append('#').append(StringUtils.Repeat(w-2, '=')).append('#');
			// front wall
			else         matrix[iy][1].append(StringUtils.Repeat(w, '#'));
			// back wall
			if (iy < h-1) {
				matrix[iy][d-1].append(StringUtils.Repeat(w, '#'));
			// top steps at hotel level
			} else {
				matrix[iy][d-1]
					.append(StringUtils.Repeat(door_x+1,   '#'))
					.append(StringUtils.Repeat(3,          'L'))
					.append(StringUtils.Repeat(w-door_x-4, '#'));
			}
		}
		// door
		matrix[4][0].append(StringUtils.Repeat(door_x, ' ')).append("&&&&&");
		matrix[3][0].append(StringUtils.Repeat(door_x, ' ')).append("$...$");
		matrix[2][0].append(StringUtils.Repeat(door_x, ' ')).append("$.d.$");
		matrix[1][0].append(StringUtils.Repeat(door_x, ' ')).append("$.D.$");
		matrix[0][0].append(StringUtils.Repeat(door_x, ' ')).append("$&&&$");
		// front wall
		StringUtils.ReplaceInString(matrix[3][1], "&&&", door_x+1);
		StringUtils.ReplaceInString(matrix[2][1], "$.$", door_x+1);
		StringUtils.ReplaceInString(matrix[1][1], "$_$", door_x+1);
		plot.run();
	}



	protected void buildAtticStairs(final Iabcd area, final int y, final BlockFace facing,
			final LimitedRegion region, final LinkedList<BlockPlotter> plots) {
		final int offset = 9;
		final int x = area.a + (BlockFace.EAST.equals( facing) ? -5 : 0);
		final int z = area.b + (BlockFace.SOUTH.equals(facing) ? -5 : 0);
		final int w = area.c;
		final int d = (area.d + offset) - 4;
		final int yy = Level_000.Y_019 - 1;
		final int h = (SUBCEILING + SUBFLOOR) - 1;
		final BlockPlotter plot =
			(new PlotterFactory())
			.placer(region)
			.axis("use")
			.rotate(facing.getOppositeFace())
			.xyz(x, yy, z)
			.whd(w, h, d)
			.build();
		plot.type('.', Material.AIR);
		plot.type('#', Material.BEDROCK);
		plot.type('L', Material.DARK_OAK_STAIRS, "[half=bottom,facing="+FaceToAxisString(facing.getOppositeFace())+"]");
		final StringBuilder[][] matrix = plot.getMatrix3D();
		final int door_x = Math.floorDiv(w, 2) - 2;
		char fill;
		for (int iy=0; iy<h; iy++) {
			matrix[iy][1]
				.append(StringUtils.Repeat(door_x, ' '))
				.append(StringUtils.Repeat(5,      '#'));
			for (int iz=2; iz<d; iz++) {
				if (iy == iz-offset) fill = 'L'; else // stairs
				if (iy >  iz-offset) fill = '.'; else // air above stairs
					fill = '#';                       // fill under stairs
				matrix[iy][iz]
					.append(StringUtils.Repeat(door_x, ' ')).append('#')
					.append(StringUtils.Repeat(3,     fill)).append('#');
			}
		}
		plots.add(plot);
	}



	public boolean checkAtticWall(final LimitedRegion region,
			final Iabcd room_area, final BlockFace direction) {
		// make rare with noise
		{
			final double value = this.noiseHotelStairs.getNoise(room_area.a, room_area.b);
			if (value < THRESH_ATTIC_STAIRS
			||  value < this.noiseHotelStairs.getNoise(room_area.a, room_area.b-8)  // north
			||  value < this.noiseHotelStairs.getNoise(room_area.a, room_area.b+8)  // south
			||  value < this.noiseHotelStairs.getNoise(room_area.a+8, room_area.b)  // east
			||  value < this.noiseHotelStairs.getNoise(room_area.a-8, room_area.b)) // west
				return false;
		}
		// find attic walls
		{
			final Material block_attic_wall = Material.matchMaterial(HotelRoomStairs.this.level0.gen_019.block_wall.get());
			if (block_attic_wall == null) throw new RuntimeException("Invalid block type for level 19 Wall");
			final Iab ab = FaceToIxz(direction);
			final int xx = (room_area.a + room_area.c) - (8 * ab.a);
			final int zz = (room_area.b + room_area.d) - (8 * ab.b);
			final int yy = Level_000.Y_019 + SUBFLOOR + 1;
			if (region.isInRegion(xx,             0, zz            )
			&&  region.isInRegion(xx-room_area.c, 0, zz-room_area.d)) {
				Material type;
				// find attic wall
				for (int iz=0; iz<room_area.d; iz++) {
					for (int ix=0; ix<room_area.c; ix++) {
						type = region.getType(xx-ix, yy+1, zz-iz);
						if (block_attic_wall.equals(type)) return false;
					}
				}
			}
		}
		return true;
	}



}
