package com.poixson.backrooms.levels;

import java.util.Random;

import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;


// 19 | Attic
public class Pop_019 implements PopBackrooms {

	protected final Gen_019 gen;



	public Pop_019(final Gen_019 gen) {
		super();
		this.gen = gen;
	}



	@Override
	public void populate(final WorldInfo world, final Random rnd,
	final int chunkX, final int chunkZ, final LimitedRegion region) {
		if (!Gen_019.ENABLE_GENERATE) return;
	}



}