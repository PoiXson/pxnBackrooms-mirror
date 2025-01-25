package com.poixson.backrooms.commands;

import static com.poixson.backrooms.BackroomsPlugin.CHAT_PREFIX;

import org.bukkit.command.CommandSender;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.tasks.TaskReconvergence;
import com.poixson.tools.commands.pxnCommandRoot;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;


// reconvergence
public class Command_Reconvergence extends pxnCommandRoot {

	protected final BackroomsPlugin plugin;



	public Command_Reconvergence(final BackroomsPlugin plugin) {
		super(
			plugin,
			"backrooms", // namespace
			"Trigger a reconvergence event.", // desc
			null, // usage
			"backrooms.reconvergence", // perm
			// labels
			"reconvergence"
		);
		this.plugin = plugin;
	}



	@Override
	public boolean onCommand(final CommandSender sender, final String[] args) {
		if (!sender.hasPermission("backrooms.reconvergence"))
			return false;
		final TaskReconvergence task = this.plugin.getReconvergenceTask();
		task.update();
		sender.sendMessage(CHAT_PREFIX.append(Component.text(
			"Triggered a reconvergence event").color(NamedTextColor.GOLD)));
		return true;
	}



}
