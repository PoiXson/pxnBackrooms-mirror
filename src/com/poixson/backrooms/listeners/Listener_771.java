package com.poixson.backrooms.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.poixson.backrooms.BackroomsPlugin;


public class Listener_771 implements Listener {

	protected final BackroomsPlugin plugin;



	public Listener_771(final BackroomsPlugin plugin) {
		this.plugin = plugin;
	}



	public void register() {
		Bukkit.getPluginManager()
			.registerEvents(this, this.plugin);
	}
	public void unregister() {
		HandlerList.unregisterAll(this);
	}







//TODO: chest refiller - cooldown



/*
final ItemStack itemB = new ItemStack(Material.SPLASH_POTION, 2);
final PotionMeta potion = (PotionMeta) itemB.getItemMeta();
potion.setBasePotionData(new PotionData(PotionType.SPEED, true, true));
itemB.setItemMeta(potion);
*/









}
