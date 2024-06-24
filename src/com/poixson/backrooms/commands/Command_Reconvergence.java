package com.poixson.backrooms.commands;

import static com.poixson.utils.BukkitUtils.FormatColors;

import org.bukkit.command.CommandSender;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.tasks.TaskReconvergence;
import com.poixson.tools.commands.pxnCommandRoot;


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
			new String[] { // labels
				"reconvergence"
			}
		);
		this.plugin = plugin;
	}



	@Override
	public boolean onCommand(final CommandSender sender, final String[] args) {
		if (!sender.hasPermission("backrooms.reconvergence"))
			return false;
		final TaskReconvergence task = this.plugin.getReconvergenceTask();
		task.update();
		sender.sendMessage(FormatColors("<GOLD>Triggered a reconvergence event"));
		return true;
	}



}
