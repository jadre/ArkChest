package co.arkfall.play.ArkChest;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ScanTask extends BukkitRunnable {
	private int x;
	private int y;
	private int z;
	private final int minX;
	private final int minY;
	private final int maxX;
	private final int maxY;
	private final int maxZ;
	private final World world;
	private final int blocksPerTick;
	private List<Location> chests;
	private final CommandSender sender;
	private final Plugin plugin;
	private long time;
	private final long totalTime;

	public ScanTask(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, World world, int blocksPerTick, List<Location> chests, CommandSender sender, Plugin plugin) {
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
		this.world = world;
		this.blocksPerTick = blocksPerTick;
		this.chests = chests;
		this.sender = sender;
		x = minX;
		y = minY;
		z = minZ;
		this.runTaskTimer(plugin, 0, 1);
		this.plugin = plugin;
		time = 0;
		totalTime = (maxX - minX) * (maxY - minY) * (maxZ - minZ) / blocksPerTick;
	}

	@Override
	public void run() {
		for (int i = 0; i < blocksPerTick; i++) {
			Location block = new Location(world, x, y, z);
			if (world.getBlockAt(block).getType().equals(Material.CHEST)) {
				chests.add(block);
			}
			if (x == maxX) {
				if (y == maxY) {
					if (z == maxZ) {
						sender.sendMessage(ChatColor.GREEN + "Scan complete.");
						this.cancel();
						((ArkChest) plugin).saveToFile();
						return;
					}
					z++;
					y = minY;
				}
				else {
					y++;
				}
				x = minX;
			}
			else x++;
		}
		if (time++ % 200 == 199) {
			sender.sendMessage("Scan " + time * 100 / totalTime + "% complete.");
		}
	}

}
