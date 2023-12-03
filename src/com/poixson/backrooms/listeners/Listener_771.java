package com.poixson.backrooms.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.pluginlib.events.OutsideOfWorldEvent;
import com.poixson.pluginlib.tools.plugin.xListener;


// 771 | Crossroads
public class Listener_771 extends xListener<BackroomsPlugin> {



	public Listener_771(final BackroomsPlugin plugin) {
		super(plugin);
	}



//TODO: chest refiller - cooldown



/*
final ItemStack itemB = new ItemStack(Material.SPLASH_POTION, 2);
final PotionMeta potion = (PotionMeta) itemB.getItemMeta();
potion.setBasePotionData(new PotionData(PotionType.SPEED, true, true));
itemB.setItemMeta(potion);
*/



	// -------------------------------------------------------------------------------
	// void/sky teleport



//TODO: teleport relative to exit
	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onOutsideOfWorld(final OutsideOfWorldEvent event) {
		final int level = this.plugin.getLevelFromWorld(event.getTo().getWorld());
		if (level == 771) {
			if (event.getOutsideDistance() > 20) {
				final Player player = event.getPlayer();
				switch (event.getOutsideWhere()) {
				case SKY:  this.plugin.noclip(player, 309); break; // to radio station
				case VOID: this.plugin.noclip(player,   1); break; // to basement
				default: throw new RuntimeException("Unknown OutsideOfWorld event type");
				}
			}
		}
	}



}
