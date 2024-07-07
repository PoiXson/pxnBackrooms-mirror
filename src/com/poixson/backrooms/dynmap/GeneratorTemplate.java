package com.poixson.backrooms.dynmap;

import static com.poixson.utils.Utils.SafeClose;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import com.poixson.backrooms.BackroomsPlugin;


public class GeneratorTemplate {

	protected final BackroomsPlugin plugin;
	protected final GeneratorPerspective gen_persp;

	public final int level;
	public final String world_name;

	public final StringBuilder out = new StringBuilder();
	protected final AtomicBoolean committed = new AtomicBoolean(false);



	public GeneratorTemplate(final BackroomsPlugin plugin, final int level) {
		this.plugin = plugin;
		this.level  = level;
		this.world_name = String.format("backrooms_%03d", Integer.valueOf(level));
		this.gen_persp = plugin.getDynmapPerspective();
		this.out
			.append("version: 0.20\n"           )
			.append("templates:\n"              )
			.append("  ").append(this.world_name).append(":\n")
			.append("    enabled: true\n"       )
			.append("    bigworld: true\n"      )
			.append("    extrazoomout: 3\n"     )
			.append("    showborder: true\n"    )
			.append("    sendposition: true\n"  )
			.append("    sendhealth: true\n"    )
			.append("    protected: false\n"    )
			.append("    fullrenderlocations:\n")
			.append("      - x: 0\n"            )
			.append("        y: 0\n"            )
			.append("        z: 0\n"            )
			.append("    maps:\n"               );
	}



	public void add(final int level, final String name, final String title) {
		this.add(level, name, title, 320);
	}
	public void add(final int level, final String name, final String title, final int y) {
		this.gen_persp.add(y, name);
		this.out
			.append("      - class: org.dynmap.hdmap.HDMap\n")
			.append("        name: ").append(name).append('\n')
			.append("        title: \"").append(title).append("\"\n")
			.append("        prefix: ").append(name).append('\n')
			.append("        perspective: iso_S_90_lowres_").append(name).append('\n')
			.append("        bigworld: true\n"    )
			.append("        shader: stdtexture\n")
			.append("        lighting: "          );
		switch (level) {
		case 0:
		case 1:
		case 5:
		case 6:
		case 37:
		case 19: this.out.append("default\n"); break;
		default: this.out.append("shadows\n"); break;
		}
		this.out
			.append("        mapzoomin: 2\n")
			.append("        center:\n"     )
			.append("          x: 0\n"      )
			.append("          y: ").append(y<320 ? y : 0).append('\n')
			.append("          z: 0\n"      );
	}



	@Override
	public String toString() {
		return this.out.toString();
	}



	public void commit() {
		if (!this.committed.compareAndSet(false, true)) return;
		this.log().info("Creating dynmap template: "+this.world_name);
		final File path = new File(this.plugin.getDataFolder(), "../dynmap/templates/");
		if (!path.isDirectory()) {
			this.log().warning("Path not found: plugins/dynmap/templates/");
			return;
		}
		final File file = new File(path, this.world_name+".txt");
		try {
			FileWriter writer = new FileWriter(file);
			writer.write(this.toString());
			SafeClose(writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	public Logger log() {
		return this.plugin.log();
	}



}
