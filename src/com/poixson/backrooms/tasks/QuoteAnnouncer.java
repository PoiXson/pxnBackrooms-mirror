package com.poixson.backrooms.tasks;

import static com.poixson.utils.Utils.IsEmpty;
import static com.poixson.utils.Utils.SafeClose;

import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.tools.xRand;
import com.poixson.utils.FileUtils;


public class QuoteAnnouncer {

	public static final ChatColor DEFAULT_QUOTE_COLOR = ChatColor.BLACK;

	protected final BackroomsPlugin plugin;

	protected final String[] quotes;

	protected int last_index = -1;



	public QuoteAnnouncer(final BackroomsPlugin plugin, final String[] quotes) {
		this.plugin = plugin;
		this.quotes = quotes;
	}

	public static QuoteAnnouncer Load(final BackroomsPlugin plugin) {
		final LinkedList<String> quotes = new LinkedList<String>();
		final InputStream input = plugin.getResource("quotes.txt");
		if (input == null) throw new RuntimeException("Failed to load chances.json");
		final String data = FileUtils.ReadInputStream(input);
		SafeClose(input);
		final String[] array = data.split("\n");
		for (final String line : array) {
			if (!IsEmpty(line))
				quotes.add(line.trim());
		}
		return new QuoteAnnouncer(plugin, quotes.toArray(new String[0]));
	}



	public void announce() {
		final Collection<? extends Player> players = Bukkit.getOnlinePlayers();
		if (players.size() > 0) {
			final String quote = DEFAULT_QUOTE_COLOR + this.getQuote() + ChatColor.RESET;
			for (final Player player : players) {
				final int level = this.plugin.getLevel(player);
				if (level >= 0)
					this.announce(quote, player);
			}
		}
	}
	public void announce(final Player player) {
		final String quote = DEFAULT_QUOTE_COLOR + this.getQuote() + ChatColor.RESET;
		this.announce(quote, player);
	}
	public void announce(final String quote, final Player player) {
		player.sendMessage(quote);
	}



	public String getQuote() {
		final int count = this.quotes.length;
		final xRand rnd = xRand.Get(0, count);
		final int index = rnd.nextInt(this.last_index);
		this.last_index = index;
		return this.quotes[index];
	}



}
