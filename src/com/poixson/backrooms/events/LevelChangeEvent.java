package com.poixson.backrooms.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class LevelChangeEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	protected final Player player;
	protected final int level, level_previous;



	public LevelChangeEvent(final Player player, final int level, final int level_previous) {
		super();
		this.player         = player;
		this.level          = level;
		this.level_previous = level_previous;
	}



	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	public static HandlerList getHandlerList() {
		return handlers;
	}



	public Player getPlayer() {
		return this.player;
	}



	public int getLevel() {
		return this.level;
	}
	public int getLevelPrevious() {
		return this.level_previous;
	}



}
