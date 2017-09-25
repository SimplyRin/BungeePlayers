package net.simplyrin.bungeeplayers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitScheduler;

public class BungeePlayers extends JavaPlugin implements Listener, PluginMessageListener {

	private static BungeePlayers plugin;
	private static HashMap<String, Integer> map = new HashMap<String, Integer>();
	private static Integer count = 0;

	public void onEnable() {
		plugin = this;

		plugin.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		plugin.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);


		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				BungeePlayers.sendMessage(Arrays.asList("GetServers"));
			}
		}, 0L, 20L);
	}

	public void onDisable() {
		plugin.getServer().getMessenger().unregisterIncomingPluginChannel(this);
		plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		if (!channel.equals("BungeeCord")) {
			return;
		}

		DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));

		try {
			String command = in.readUTF();

			if(command.equals("GetServers")) {
				String[] serverList = in.readUTF().split(", ");
				for(String list : serverList) {
					BungeePlayers.sendMessage(Arrays.asList("PlayerCount", list));
				}
			}

			if(command.equals("PlayerCount")) {
				String server = in.readUTF();
				int playerCount = in.readInt();

				map.put(server, Integer.valueOf(playerCount));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void sendMessage(List<String> list) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);

		try {
			for(String s : list) {
				out.writeUTF(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
		Bukkit.getServer().sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
	}

	public static HashMap<String, Integer> getServers() {
		return map;
	}

	public static Integer getNetworkPlayers() {
		count = 0;
		for(Integer s : map.values()) {
			count += s;
		}
		return count;
	}

}
