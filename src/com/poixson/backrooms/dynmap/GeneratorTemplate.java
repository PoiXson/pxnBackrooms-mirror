package com.poixson.backrooms.dynmap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.utils.Utils;


public class GeneratorTemplate {
	public static final Logger log = BackroomsPlugin.log;
	public static final String LOG_PREFIX = BackroomsPlugin.LOG_PREFIX;

	protected final BackroomsPlugin plugin;
	protected final GeneratorPerspective gen_persp;

	public final int level;
	public final String worldName;

	public final StringBuilder out = new StringBuilder();
	protected final AtomicBoolean committed = new AtomicBoolean(false);



	public GeneratorTemplate(final BackroomsPlugin plugin, final int level) {
		this.plugin = plugin;
		this.level = level;
		this.worldName = "backrooms"+Integer.toString(level);
		this.gen_persp = plugin.getDynmapPerspective();
		this.out
			.append("version: 0.20\n"           )
			.append("templates:\n"              )
			.append("  ").append(this.worldName).append(":\n")
			.append("    enabled: true\n"       )
			.append("    bigworld: true\n"      )
			.append("    extrazoomout: 3\n"     )
			.append("    showborder: true\n"    )
			.append("    sendposition: true\n"  )
			.append("    sendhealth: true\n"    )
			.append("    protected: false\n"    )
			.append("    fullrenderlocations:\n")
			.append("      - x: 0\n"            )
			.append("        z: 0\n"            )
			.append("    maps:\n"               );
	}



	public void add(final String name, final String title) {
		this.add(320, name, title);
	}
	public void add(final int y, final String name, final String title) {
		this.gen_persp.add(y, name);
		this.out
			.append("      - class: org.dynmap.hdmap.HDMap\n")
			.append("        name: ").append(name).append('\n')
			.append("        title: \"").append(title).append("\"\n")
			.append("        prefix: ").append(name).append('\n')
			.append("        perspective: iso_S_90_lowres_").append(name).append('\n')
			.append("        bigworld: true\n"    )
			.append("        shader: stdtexture\n")
			.append("        lighting: shadows\n" )
			.append("        mapzoomin: 2\n"      )
			.append("        center:\n"           )
			.append("          x: 0\n"            )
			.append("          y: ").append(y<320 ? y : 0).append('\n')
			.append("          z: 0\n"            );
	}



	@Override
	public String toString() {
		return this.out.toString();
	}



	public void commit() {
		if (!this.committed.compareAndSet(false, true)) return;
		log.info(LOG_PREFIX + "Creating dynmap template: backrooms_" + this.worldName);
		final File path = new File(this.plugin.getDataFolder(), "../dynmap/templates/");
		if (!path.isDirectory()) {
			log.warning(LOG_PREFIX + "Path not found: plugins/dynmap/templates/");
			return;
		}
		final File file = new File(path, "backrooms_"+this.worldName);
		try {
			FileWriter writer = new FileWriter(file);
			writer.write(this.toString());
			Utils.SafeClose(writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



}
