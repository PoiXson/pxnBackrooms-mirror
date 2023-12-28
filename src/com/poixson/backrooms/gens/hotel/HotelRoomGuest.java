package com.poixson.backrooms.gens.hotel;

import static com.poixson.backrooms.gens.Gen_005.DEFAULT_BLOCK_DOOR_BORDER_SIDE;
import static com.poixson.backrooms.gens.Gen_005.DEFAULT_BLOCK_DOOR_BORDER_TOP_X;
import static com.poixson.backrooms.gens.Gen_005.DEFAULT_BLOCK_DOOR_BORDER_TOP_Z;
import static com.poixson.utils.BlockUtils.StringToBlockData;
import static com.poixson.utils.LocationUtils.FaceToAxisString;
import static com.poixson.utils.LocationUtils.FaceToPillarAxisString;
import static com.poixson.utils.LocationUtils.Rotate;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.LimitedRegion;

import com.poixson.backrooms.gens.hotel.HotelRoomSpecs.RoomTheme;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.tools.dao.Iabcd;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.tools.plotter.PlotterFactory;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.StringUtils;


public class HotelRoomGuest implements HotelRoom {

	protected final Level_000 level0;

	protected final FastNoiseLiteD noise;



	public HotelRoomGuest(final Level_000 level0, final FastNoiseLiteD noise) {
		this.level0 = level0;
		this.noise  = noise;
	}



	@Override
	public void build(final Iabcd area, final int y, final BlockFace facing,
			final LimitedRegion region, final LinkedList<BlockPlotter> plots) {
		final boolean axis_x = "x".equals(FaceToPillarAxisString(Rotate(facing, 0.25)));
		final BlockData block_hotel_door_border_top = (axis_x
			? StringToBlockData(this.level0.gen_005.block_door_border_top_x, DEFAULT_BLOCK_DOOR_BORDER_TOP_X)
			: StringToBlockData(this.level0.gen_005.block_door_border_top_z, DEFAULT_BLOCK_DOOR_BORDER_TOP_Z));
		final BlockData block_hotel_door_border_side =
			StringToBlockData(this.level0.gen_005.block_door_border_side, DEFAULT_BLOCK_DOOR_BORDER_SIDE);
		// room specs
		final HotelRoomSpecs specs =
			HotelRoomSpecs.SpecsFromValue(
				this.noise.getNoise(area.a, area.b)
			);
		if (RoomTheme.CHEESE.equals(specs.theme))
			this.level0.cheese_rooms.add(area.a, area.b);
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
		plot.type('#', specs.walls);
		plot.type(',', specs.carpet);
		plot.type('.', Material.AIR);
		plot.type('&', block_hotel_door_border_top );
		plot.type('$', block_hotel_door_border_side);
		plot.type('d', specs.door, "[half=upper,hinge=right,facing="+FaceToAxisString(facing)+"]");
		plot.type('D', specs.door, "[half=lower,hinge=right,facing="+FaceToAxisString(facing)+"]");
		plot.type('_', specs.door_plate);
		final StringBuilder[][] matrix = plot.getMatrix3D();
		for (int iy=0; iy<h; iy++) {
			for (int iz=2; iz<d-1; iz++) {
				matrix[iy][iz]
					.append('#')
					.append(StringUtils.Repeat(w-2, iy==0 ? ',' : '.'))
					.append('#');
			}
			// front wall
			matrix[iy][1].append(StringUtils.Repeat(w, '#'));
			// back wall
			matrix[iy][d-1].append(StringUtils.Repeat(w, '#'));
		}
		// door
		final int door_x = Math.floorDiv(w, 2) - 2;
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



}
