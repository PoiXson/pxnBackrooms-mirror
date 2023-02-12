package com.poixson.backrooms.dynmap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.utils.Utils;


public class GeneratorPerspective {
	public static final Logger log = BackroomsPlugin.log;
	public static final String LOG_PREFIX = BackroomsPlugin.LOG_PREFIX;

	public final StringBuilder out = new StringBuilder();
	protected final AtomicBoolean committed = new AtomicBoolean(false);



	public GeneratorPerspective() {
		this.out.append("perspectives:\n");
	}



	public void add(final int y, final String name) {
		this.out
			.append("  - class: org.dynmap.hdmap.IsoHDPerspective\n")
			.append("    name: iso_S_90_lowres_").append(name).append('\n');
		if (y < 320)
			this.out.append("    maximumheight: ").append(y).append('\n');
		this.out
			.append("    azimuth: 180\n")
			.append("    inclination: 90\n")
			.append("    scale: 4\n");
	}



	@Override
	public String toString() {
		return this.out.toString();
	}



	public void commit(final File path) {
		if (!this.committed.compareAndSet(false, true)) return;
		log.info(LOG_PREFIX + "Creating dynmap config: custom-perspectives.txt");
		if (!path.isDirectory()) {
			log.warning(LOG_PREFIX + "Path not found: plugins/dynmap/");
			return;
		}
		final File file = new File(path, "custom-perspectives.txt");
		final File last = new File(path, "custom-perspectives-last.txt");
		if (last.isFile())
			last.delete();
		file.renameTo(last);
		try {
			FileWriter writer = new FileWriter(file);
			writer.write(this.toString());
			Utils.SafeClose(writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



}
