package com.poixson.backrooms.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.commonmc.events.OutsideOfWorldEvent;
import com.poixson.commonmc.tools.plugin.xListener;


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



	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onOutsideOfWorld(final OutsideOfWorldEvent event) {
		final Player player = event.getPlayer();
		switch (event.getOutsideWhere()) {
		case SKY:  this.plugin.noclip(player,  78); break; // to space
//TODO: teleport relative to basement exit
		case VOID: this.plugin.noclip(player, 771); break; // to crossroads
		default: throw new RuntimeException("Unknown OutsideOfWorld event type");
		}
	}



}
