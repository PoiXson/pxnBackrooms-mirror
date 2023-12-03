package com.poixson.backrooms.gens.hotel;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.LimitedRegion;

import com.poixson.backrooms.gens.hotel.HotelRoomSpecs.RoomTheme;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.pluginlib.tools.plotter.BlockPlotter;
import com.poixson.pluginlib.tools.plotter.PlotterFactory;
import com.poixson.tools.dao.Iabcd;
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
	public void build(final Iabcd area, final int y, final BlockFace direction,
			final LimitedRegion region, final LinkedList<BlockPlotter> plots) {
		final Material block_hotel_wall = Material.matchMaterial(this.level0.gen_005.block_hall_wall.get());
		if (block_hotel_wall == null) throw new RuntimeException("Invalid block type for level 5 HallWall");
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
			.rotate(direction.getOppositeFace())
			.xyz(x, y, z)
			.whd(w, h, d)
			.build();
		plot.type('#', specs.walls);
		plot.type(',', specs.carpet);
		plot.type('.', Material.AIR);
//TODO
plot.type('$', block_hotel_wall);
plot.type('&', block_hotel_wall);
plot.type('d', specs.door);
plot.type('D', specs.door);
//		plot.type('$', block_hotel_wall, "up");
//		plot.type('&', block_hotel_wall, FaceToAxString(Rotate(direction, 0.25)));
//		plot.type('d', specs.door, "top",    "right", "closed", FaceToAxString(direction));
//		plot.type('D', specs.door, "bottom", "right", "closed", FaceToAxString(direction));
		plot.type('_', specs.door_plate);
		final StringBuilder[][] matrix = plot.getMatrix3D();
		for (int iy=0; iy<h; iy++) {
			for (int iz=2; iz<d-1; iz++) {
				matrix[iy][iz]
					.append('#')
					.append(StringUtils.Repeat(w-2, iy==0 ? ',' : ' '))
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
