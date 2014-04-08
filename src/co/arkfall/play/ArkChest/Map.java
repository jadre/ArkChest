package co.arkfall.play.ArkChest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class Map {
	final private World world;
	private List<Location> chests;
	final private Location positiveCorner;
	final private Location negativeCorner;
	private int itemsPerChest;
	private ItemStack[] chestItems;
	private Random ran;
	final private String name;
	
	public Map(String name, Location negativeCorner, Location positiveCorner) {
		this (name, negativeCorner, positiveCorner, new ArrayList<Location> (), 5, new ItemStack[] {new ItemStack(Material.STONE), new ItemStack(Material.DIAMOND_SWORD)});
	}
	
	public Map(String name, Location negativeCorner, Location positiveCorner, List<Location> chests, int itemsPerChest, ItemStack[] chestItems) {
		this.negativeCorner = negativeCorner;
		this.positiveCorner = positiveCorner;
		this.chests = chests;
		this.name = name;
		this.itemsPerChest = itemsPerChest;
		this.chestItems = chestItems;
		
		world = negativeCorner.getWorld();
		
		ran = new Random();
	}
	
	public void addchest (Location chest) {
		chests.add(chest);
	}
	
	public String getName() {
		return name;
	}
	
	public Location getNegativeCorner() {
		return negativeCorner;
	}
	
	public Location getPositiveCorner() {
		return positiveCorner;
	}
	
	public void scan(Plugin plugin, final int blocksPerTick, final CommandSender sender) {
		chests = new ArrayList<Location> ();
		
		new ScanTask(negativeCorner.getBlockX(), negativeCorner.getBlockY(), negativeCorner.getBlockZ(), positiveCorner.getBlockX(), positiveCorner.getBlockY(), positiveCorner.getBlockZ(), world, blocksPerTick, chests, sender, plugin);
	}
	
	private ItemStack[] getRandomItems() {
		List<ItemStack> items = new ArrayList<ItemStack> ();
		for (int i = 0; i < itemsPerChest; i++) {
			items.add(getRandomItem());
		}
		return items.toArray(new ItemStack[0]);
	}
	
	private ItemStack getRandomItem() {
		if (chestItems.length == 0) {
			return null;
		}
		else {
			return chestItems[ran.nextInt(chestItems.length)];
		}
	}
	
	private ItemStack[] getArrangedItems() {
		ItemStack[] items = getRandomItems();
		ItemStack[] inventory = {null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};
		List<Integer> openSpots = new ArrayList<Integer> ();
		for (int i = 0; i < 27; i++) openSpots.add(i);
		int randSpot;
		for (int i = 0; i < items.length; i++) {
			int rand = ran.nextInt(openSpots.size());
			randSpot = openSpots.get(rand);
			inventory[randSpot] = items[i];
			openSpots.remove(rand);
		}
		return inventory;
	}
	
	private void refillChest(Location chest) {
		if (!(world.getBlockAt(chest) instanceof Chest)) {
			world.getBlockAt(chest).setType(Material.CHEST);
		}
		((Chest) chest.getBlock().getState()).getBlockInventory().setContents(getArrangedItems());
	}
	
	public void refill() {
		for (Location chest : chests) {
			refillChest(chest);
		}
	}
	
	public List<Location> getChests() {
		return chests;
	}
	
	public int getItemsPerChest() {
		return itemsPerChest;
	}
	
	public void setItemsPerChest(int items) {
		itemsPerChest = items;
	}
	
	public ItemStack[] getChestItems() {
		return chestItems;
	}
	
	public void setChestItems(ItemStack[] items) {
		chestItems = items;
	}
}
