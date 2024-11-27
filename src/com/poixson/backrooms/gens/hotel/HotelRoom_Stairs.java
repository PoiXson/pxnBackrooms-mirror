package com.poixson.backrooms.gens.hotel;

import static com.poixson.backrooms.gens.Gen_005.DEFAULT_BLOCK_DOOR_BORDER_SIDE;
import static com.poixson.backrooms.gens.Gen_005.DEFAULT_BLOCK_DOOR_BORDER_TOP_X;
import static com.poixson.backrooms.gens.Gen_005.DEFAULT_BLOCK_DOOR_BORDER_TOP_Z;
import static com.poixson.backrooms.gens.Gen_019.DEFAULT_BLOCK_WALL;
import static com.poixson.utils.BlockUtils.StringToBlockDataDef;
import static com.poixson.utils.LocationUtils.FaceToAxisString;
import static com.poixson.utils.LocationUtils.FaceToIxz;
import static com.poixson.utils.LocationUtils.FaceToPillarAxChar;
import static com.poixson.utils.LocationUtils.Rotate;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.LimitedRegion;

import com.poixson.backrooms.gens.Gen_005;
import com.poixson.backrooms.gens.Gen_019;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.dao.Iab;
import com.poixson.tools.dao.Iabcd;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.utils.StringUtils;


public class HotelRoom_Stairs implements HotelRoom {

	protected final Gen_005 gen_005;
	protected final Gen_019 gen_019;



	public HotelRoom_Stairs(final Level_000 level_000) {
		this.gen_005 = level_000.gen_005;
		this.gen_019 = level_000.gen_019;
	}



	@Override
	public void build(final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final LimitedRegion region, final Iabcd area, final int y, final BlockFace direction) {
		this.buildHotelRoomStairs(plots, region, area, y, direction);
		this.buildAtticStairs(    plots, region, area, y, direction);
	}



	// hotel stairs
	protected void buildHotelRoomStairs(final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final LimitedRegion region, final Iabcd area, final int y_stairs, final BlockFace facing) {
		final boolean axis_x = (FaceToPillarAxChar(Rotate(facing, 0.25)) == 'x');
		final BlockData block_hotel_door_border_top = (axis_x
			? StringToBlockDataDef(this.gen_005.block_door_border_top_x, DEFAULT_BLOCK_DOOR_BORDER_TOP_X)
			: StringToBlockDataDef(this.gen_005.block_door_border_top_z, DEFAULT_BLOCK_DOOR_BORDER_TOP_Z));
		final BlockData block_hotel_door_border_side =
			StringToBlockDataDef(this.gen_005.block_door_border_side, DEFAULT_BLOCK_DOOR_BORDER_SIDE);
		final int x = area.a;
		final int z = area.b;
		final int w = area.c;
		final int d = area.d;
		final int h_stairs = this.gen_005.level_h + this.gen_005.subceiling + 1;
		final int door_x = Math.floorDiv(w, 2) - 2;
		final BlockPlotter plot =
			(new BlockPlotter())
			.axis("use")
			.rotate(facing.getOppositeFace())
			.xyz(x, y_stairs, z)
			.whd(w, h_stairs, d);
		plot.type('.', Material.AIR);
		plot.type('#', Material.BEDROCK);
		plot.type('=', Material.DARK_OAK_PLANKS);
		plot.type('&', block_hotel_door_border_top );
		plot.type('$', block_hotel_door_border_side);
		plot.type('d', Material.SPRUCE_DOOR, "[half=upper,hinge=right,facing="+FaceToAxisString(facing)+"]");
		plot.type('D', Material.SPRUCE_DOOR, "[half=lower,hinge=right,facing="+FaceToAxisString(facing)+"]");
		plot.type('_', Material.DARK_OAK_PRESSURE_PLATE);
		plot.type('L', Material.DARK_OAK_STAIRS, "[half=bottom,facing="+FaceToAxisString(facing.getOppositeFace())+"]");
		final StringBuilder[][] matrix = plot.getMatrix3D();
		char fill;
		for (int iy=0; iy<h_stairs; iy++) {
			for (int iz=2; iz<d-1; iz++) {
				if (iy == 0   ) fill = '='; else // floor
				if (iy == iz-2) fill = 'L'; else // stairs
				if (iy >  iz-2) fill = '.'; else // air above stairs
					fill = '#';                  // fill under stairs
				// tall side wall
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
			if (iy < h_stairs-1) {
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
		plot.run(region, matrix);
	}



	// attic stairs
	protected void buildAtticStairs(final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final LimitedRegion region, final Iabcd area, final int y_stairs, final BlockFace facing) {
		final int x = area.a + (BlockFace.EAST .equals(facing) ? -3 : 0);
		final int z = area.b + (BlockFace.SOUTH.equals(facing) ? -3 : 0);
		final int y_attic  = (this.gen_019.level_y - this.gen_019.bedrock_barrier) - 2;
		final int h_stairs = this.gen_005.subceiling + this.gen_019.bedrock_barrier + this.gen_019.subfloor + 2;
		final int w = area.c;
		final int d = (area.d + h_stairs) - 3;
		final int door_x = Math.floorDiv(w, 2) - 2;
		final int offset = (this.gen_005.subceiling + this.gen_019.bedrock_barrier) - h_stairs - 1;
		final BlockPlotter plot =
			(new BlockPlotter())
			.axis("use")
			.rotate(facing.getOppositeFace())
			.xyz(x, y_attic,  z)
			.whd(w, h_stairs, d+2);
		plot.type('.', Material.AIR    );
		plot.type('#', Material.BEDROCK);
		plot.type('L', Material.DARK_OAK_STAIRS, "[half=bottom,facing="+FaceToAxisString(facing.getOppositeFace())+"]");
		final StringBuilder[][] matrix = plot.getMatrix3D();
		for (int iy=0; iy<h_stairs; iy++) {
			// tall side wall
			matrix[iy][1]
				.append(StringUtils.Repeat(door_x, ' '))
				.append(StringUtils.Repeat(5,      '#'));
			for (int iz=0; iz<d; iz++) {
				final char fill;
				if      (iy == iz+offset) fill = 'L'; // stairs
				else if (iy >  iz+offset) fill = '.'; // air above stairs
				else                            fill = '#'; // fill under stairs
				matrix[iy][iz+2]
					.append(StringUtils.Repeat(door_x, ' ')).append('#')
					.append(StringUtils.Repeat(3,     fill)).append('#');
			}
		}
		plots.add(new Tuple<BlockPlotter, StringBuilder[][]>(plot, matrix));
	}



	public boolean checkAtticWall(final LimitedRegion region,
			final Iabcd room_area, final BlockFace direction) {
		// find attic walls
		{
			final BlockData block_attic_wall = StringToBlockDataDef(this.gen_019.block_wall, DEFAULT_BLOCK_WALL);
			if (block_attic_wall == null) throw new RuntimeException("Invalid block type for level 19 Wall");
			final Iab ab = FaceToIxz(direction);
			final int xx = (room_area.a + room_area.c) - (8 * ab.a);
			final int zz = (room_area.b + room_area.d) - (8 * ab.b);
			final int yy = this.gen_019.level_y + this.gen_019.subfloor + 1;
			if (region.isInRegion(xx,             0, zz            )
			&&  region.isInRegion(xx-room_area.c, 0, zz-room_area.d)) {
				// find attic wall
				for (int iz=0; iz<room_area.d; iz++) {
					for (int ix=0; ix<room_area.c; ix++) {
						final BlockData type = region.getBlockData(xx-ix, yy+1, zz-iz);
						if (block_attic_wall.equals(type)) return false;
					}
				}
			}
		}
		return true;
	}



}
