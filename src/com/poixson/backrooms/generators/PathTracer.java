package com.poixson.backrooms.generators;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.NumberUtils;


public class PathTracer {

	protected final FastNoiseLiteD noise;

	protected final ConcurrentHashMap<Integer, Double> cache;
	protected final ThreadLocal<SoftReference<HashMap<Integer, Double>>> cacheLocal =
			new ThreadLocal<SoftReference<HashMap<Integer, Double>>>();



	public PathTracer(final FastNoiseLiteD noise) {
		this(noise, new ConcurrentHashMap<Integer, Double>());
	}
	public PathTracer(final FastNoiseLiteD noise, final ConcurrentHashMap<Integer, Double> cache) {
		this.noise = noise;
		this.cache = cache;
	}



	public boolean isPath(final int x, final int z, final int width) {
		if (z < 0)
			return false;
		final int xx = this.getPathX(z);
		return (x >= xx-width && x <= xx+width);
	}



	// find x
	public int getPathX(final int z) {
		if (z < 0)
			return 0;
		Double value;
		// cached
		final HashMap<Integer, Double> local = this.getLocalCache();
		value = local.get(Integer.valueOf(z));
		if (value != null) return (int) Math.round(value.intValue());
		value = this.cache.get(Integer.valueOf(z));
		if (value != null) return (int) Math.round(value.intValue());
		// find last cached value
		int from = 0;
		double x = 0.0;
		for (int i=z-1; i>=0; i--) {
			value = local.get(Integer.valueOf(i));
			if (value != null) {
				x = value.doubleValue();
				from = i;
				break;
			}
			value = this.cache.get(Integer.valueOf(i));
			if (value != null) {
				x = value.doubleValue();
				from = i;
				break;
			}
		}
		int step;
		double valueE, valueW;
		for (int i=from+1; i<z; i++) {
			valueE = this.noise.getNoise(x+1.0, i);
			valueW = this.noise.getNoise(x-1.0, i);
			x += (valueW - valueE) * 5.0;
			step = NumberUtils.MinMax( (int)Math.floor(Math.pow(i, 0.5)), 3, 1000 );
			local.put(Integer.valueOf(i), Double.valueOf(x));
			if (i % step == 0)
				this.cache.put(Integer.valueOf(i), Double.valueOf(x));
		}
		return (int) Math.round(x);
	}



	public HashMap<Integer, Double> getLocalCache() {
		// existing
		{
			final SoftReference<HashMap<Integer, Double>> soft = this.cacheLocal.get();
			if (soft != null) {
				final HashMap<Integer, Double> map = soft.get();
				if (map != null) {
					if (map.size() < 1000)
						return map;
				}
			}
		}
		// new instance
		{
			final HashMap<Integer, Double> map = new HashMap<Integer, Double>();
			this.cacheLocal.set(new SoftReference<HashMap<Integer,Double>>(map));
			return map;
		}
	}



}
