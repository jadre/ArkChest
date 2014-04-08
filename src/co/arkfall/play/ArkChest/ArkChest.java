package co.arkfall.play.ArkChest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;


//Test
public class ArkChest extends JavaPlugin implements Listener {
	Logger log = getLogger();
	File dataFolder;
	List<Map> maps;
	
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		//getServer().getPluginCommand("arkchest").setTabCompleter(this);
		log = getLogger();

		dataFolder = getDataFolder();
		loadFromFile();
	}

	private void loadFromFile() {
		maps = new ArrayList<Map> ();
		if (getConfig().contains("Maps")) {
			Set<String> mapKeys = getConfig().getConfigurationSection("Maps").getKeys(false);
			Iterator<String> iterator = mapKeys.iterator();
			while (iterator.hasNext()) {
				String mapKey = iterator.next();
				World world = Bukkit.getWorld(getConfig().getString("Maps." + mapKey + ".Location.World")); 
				if (world != null) {
					Location negativeCorner = new Location(world,
							getConfig().getInt("Maps." + mapKey + ".Location.Negative Corner.X"),
							getConfig().getInt("Maps." + mapKey + ".Location.Negative Corner.Y"),
							getConfig().getInt("Maps." + mapKey + ".Location.Negative Corner.Z"));
					Location positiveCorner = new Location(world,
							getConfig().getInt("Maps." + mapKey + ".Location.Positive Corner.X"),
							getConfig().getInt("Maps." + mapKey + ".Location.Positive Corner.Y"),
							getConfig().getInt("Maps." + mapKey + ".Location.Positive Corner.Z"));

					List<Location> chests = new ArrayList<Location> ();
					if (getConfig().getConfigurationSection("Maps." + mapKey + ".Chests") != null) {
						Set<String> chestKeys = getConfig().getConfigurationSection("Maps." + mapKey + ".Chests").getKeys(false);
						for (String chestKey : chestKeys) {
							chests.add(new Location(world,
									getConfig().getInt("Maps." + mapKey + ".Chests." + chestKey + ".X"),
									getConfig().getInt("Maps." + mapKey + ".Chests." + chestKey + ".Y"),
									getConfig().getInt("Maps." + mapKey + ".Chests." + chestKey + ".Z")
									));
						}
					}
					
					int ItemsPerChest = getConfig().getInt("Maps." + mapKey + ".Items per chest");
					List<ItemStack> items = new ArrayList<ItemStack> ();
					if (getConfig().getConfigurationSection("Maps." + mapKey + ".Items") != null) {
						Set<String> itemKeys = getConfig().getConfigurationSection("Maps." + mapKey + ".Items").getKeys(false);
						for (String itemKey : itemKeys) {
							Material material = Material.getMaterial(getConfig().getString("Maps." + mapKey + ".Items." + itemKey + ".Type"));
							int amount = getConfig().getInt("Maps." + mapKey + ".Items." + itemKey + ".Amount", 1);
							short damage = (short) getConfig().getInt("Maps." + mapKey + ".Items." + itemKey + ".Durability", 0);
							ItemStack i = new ItemStack(material, amount, damage);
							ItemMeta meta = (ItemMeta) getConfig().get("Maps." + mapKey + ".Items." + itemKey + ".Meta");
							if (meta != null) {
								log.info("Meta is " + meta.serialize());
								i.setItemMeta(meta);
							}
							items.add(i);
						}
					}
					maps.add(new Map(mapKey, negativeCorner, positiveCorner, chests, ItemsPerChest, items.toArray(new ItemStack[0])));
					log.info("Loaded map " + mapKey);
					
					
				}
			}
		}
		if (getConfig().get("Blocks scanned per tick") == null) {
			getConfig().set("Blocks scanned per tick", 50000);
			saveToFile();
		}
	}
	
	public void saveToFile() {
		getConfig().set("Maps", null);
		for (Map map : maps) {
			getConfig().set("Maps." + map.getName() + ".Location.World", map.getNegativeCorner().getWorld().getName());
			getConfig().set("Maps." + map.getName() + ".Location.Negative Corner.X", map.getNegativeCorner().getBlockX());
			getConfig().set("Maps." + map.getName() + ".Location.Negative Corner.Y", map.getNegativeCorner().getBlockY());
			getConfig().set("Maps." + map.getName() + ".Location.Negative Corner.Z", map.getNegativeCorner().getBlockZ());
			getConfig().set("Maps." + map.getName() + ".Location.Positive Corner.X", map.getPositiveCorner().getBlockX());
			getConfig().set("Maps." + map.getName() + ".Location.Positive Corner.Y", map.getPositiveCorner().getBlockY());
			getConfig().set("Maps." + map.getName() + ".Location.Positive Corner.Z", map.getPositiveCorner().getBlockZ());
			getConfig().set("Maps." + map.getName() + ".Items per chest", map.getItemsPerChest());
			
			ItemStack[] items = map.getChestItems();
			for (int i = 0; i < items.length; i++) {
				getConfig().set("Maps." + map.getName() + ".Items." + i + ".Type", items[i].getType().toString());
				if (items[i].getDurability() != 0) {
					getConfig().set("Maps." + map.getName() + ".Items." + i + ".Durability", items[i].getDurability());
				}
				if (items[i].getAmount() != 1) {
					getConfig().set("Maps." + map.getName() + ".Items." + i + ".Amount", items[i].getAmount());
				}
				if (items[i].getItemMeta().serialize().size() > 1 || ! items[i].getItemMeta().serialize().get("meta-type").equals("UNSPECIFIC")) {
					log.info("saved meta: " + items[i].getItemMeta().serialize());
					getConfig().set("Maps." + map.getName() + ".Items." + i + ".Meta", items[i].getItemMeta());
				}
				else {
					log.info("skipped meta: " + items[i].getItemMeta().serialize());
				}
			}
			
			List<Location> chests = map.getChests();
			for (int i = 0; i < chests.size(); i++) {
				getConfig().set("Maps." + map.getName() + ".Chests." + i + ".X", chests.get(i).getBlockX());
				getConfig().set("Maps." + map.getName() + ".Chests." + i + ".Y", chests.get(i).getBlockY());
				getConfig().set("Maps." + map.getName() + ".Chests." + i + ".Z", chests.get(i).getBlockZ());
			}
		}
		
		try {
			getConfig().save(getDataFolder() + "/config.yml");
		} catch (IOException e) {
			log.severe("Could not save config.");
			e.printStackTrace();
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("arkchest")) {
			if (args.length == 0 || args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")) {
				sender.sendMessage("In this plugin, you can mark out areas in the world and then automatically refill the chests in those areas" +
						"/arkchest addmap <map> <x> <y> <z> <x> <y> <z> [world]\n" +
						"/arkchest scan <map>\n" +
						"/arkchest addchest <map> [<x> <y> <z> [world]]\n" +
						"/arkchest setitems <map> [<x> <y> <z> [world]]\n" +
						"/arkchest refill <map>\n" +
						"/arkchest details <map>\n" +
						"/arkchest delete <map>\n" +
						"For more details on a specific command, use the command with \"?\" as an argument. For example, \"/arkchest addmap ?\".");
				return false;
			}
			if (args[0].equalsIgnoreCase("addchest")) {
				if (args.length == 1) {
					sender.sendMessage(ChatColor.RED + "You must specify a map. For help on this command, use " + ChatColor.BOLD + "/arkchest addchest ?");
					return false;
				}
				else if (args[1].equals("?")) {
					sender.sendMessage(ChatColor.YELLOW + "Addchest is used to add a single chest to a map for refilling. It can be used if an additional " +
							"chest is wanted after a map has been scanned, but if you want to add many chests, it is probably faster to just rescan.\n" +
							"The synatax is:\n" + ChatColor.BOLD +
							"/arkchest addchest <map> [<x> <y> <z> [world]]\n" + ChatColor.RESET + ChatColor.YELLOW +
							"The coordinates of the chest are optional, but if not used, you must point your cursor at the chest you want to add. If you " +
							"do use coordinates but do not specify your world it will use the world you are in.\n" +
							"After you have added all of the chests you want to the map, you can use " + ChatColor.BOLD + "/arkchest setitems" + 
							ChatColor.RESET + ChatColor.YELLOW + " to set the items the chests will refill with.");
					return true;
				}
				Map map = getMap(args[1]);
				if (map == null) {
					sender.sendMessage(ChatColor.RED + "The specified map does not exist. For help on this command, use " + ChatColor.BOLD +
							"/arkchest addchest ?" + ChatColor.RESET + ChatColor.YELLOW + ".");
					return false;
				}
				Location chest;
				if (args.length == 2) {
					if (!(sender instanceof Player)) {
						sender.sendMessage(ChatColor.RED + "You must specify the location of the chest you wish to add if you use this command as anything " +
								"other than a player.");
					}
					Player player = (Player) sender;
					@SuppressWarnings("deprecation")
					Block target = player.getTargetBlock(null, 4);
					if (target == null || !(target.getState() instanceof Chest)) {
						player.sendMessage(ChatColor.RED + "You must point your cursor at an inventory or specify the x, y, and z of the inventory to use.");
						return false;
					}
					chest = target.getLocation();
				}
				else if (args.length == 5) {
					chest = new Location(((Player) sender).getWorld(), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
				}
				else {
					sender.sendMessage(ChatColor.RED + "Invalid number of arguments. For help on this command, use " + ChatColor.BOLD + "/arkchest addchest ?" +
							ChatColor.RESET + ChatColor.RED + ".");
					return false;
				}
				if (chest.getX() < map.getNegativeCorner().getX() || chest.getX() > map.getPositiveCorner().getX() ||
						chest.getY() < map.getNegativeCorner().getY() || chest.getY() > map.getPositiveCorner().getY() ||
						chest.getZ() < map.getNegativeCorner().getZ() || chest.getZ() > map.getPositiveCorner().getZ()) {
					sender.sendMessage(ChatColor.RED + "The specified chest is outside of the map. For help on this command, use " + ChatColor.BOLD +
							"/arkchest addchest ?" + ChatColor.RESET + ChatColor.RED + ".");
					return false;
				}
				map.addchest(chest);
				saveToFile();
				sender.sendMessage("Added chest at " + chest.getBlockX() + ", " + chest.getBlockY() + ", " + chest.getBlockZ());
				return true;
			}
			else if (args[0].equalsIgnoreCase("setnumber")) {
				if (args.length == 1) {
					sender.sendMessage(ChatColor.RED + "You must specify a map and a number of items for the chests to refill with. For help on this command, use " + ChatColor.BOLD +
							"/arkchest addchest ?" + ChatColor.RESET + ChatColor.RED + ".");
					return false;
				}
				else if (args[1].equals("?")) {
					sender.sendMessage(ChatColor.YELLOW + "This command is used to how many items each chest will refill with in a map. When refilling double chests, each half will refill separately, resulting in double the number of items. The syntax is\n" +
							ChatColor.BOLD + "/arkchest setnumber <map> <number>\n" + ChatColor.RESET +
							ChatColor.YELLOW + "After you have set how many items to refill each chest with, you can refill them with the " + ChatColor.BOLD + "/arkchest refill" + ChatColor.RESET + ChatColor.YELLOW + " command.");
					return true;
				}
				else if (args.length < 3) {
					sender.sendMessage(ChatColor.RED + "You must specify a map and a number of items for the chests to refill with. For help on this command, use " + ChatColor.BOLD +
							"/arkchest addchest ?" + ChatColor.RESET + ChatColor.RED + ".");
					return false;
				}
				else if (args.length > 3) {
					sender.sendMessage(ChatColor.RED + "Too many arguments. For help on this command, use " + ChatColor.BOLD +
							"/arkchest addchest ?" + ChatColor.RESET + ChatColor.RED + ".");
					return false;
				}
				Map map = getMap(args[1]);
				if (map == null) {
					sender.sendMessage(ChatColor.RED + "The specified map does not exist. For help on this command, use " + ChatColor.BOLD +
							"/arkchest addchest ?" + ChatColor.RESET + ChatColor.RED + ".");
					return false;
				}
				else if (args[2].matches("\\D")) {
					sender.sendMessage(ChatColor.RED + "The number of items per chest must be a positive integer. For help on this command, use " + ChatColor.BOLD +
							"/arkchest addchest ?" + ChatColor.RESET + ChatColor.RED + ".");
					return false;
				}
				int items = Integer.parseInt(args[2]);
				if ( items > 27) {
					sender.sendMessage(ChatColor.RED + "The number of items per chest cannot be greater than 27. For help on this command, use " + ChatColor.BOLD +
							"/arkchest addchest ?" + ChatColor.RESET + ChatColor.RED + ".");
					return false;
				}
				map.setItemsPerChest(Integer.parseInt(args[2]));
			}
			else if (args[0].equalsIgnoreCase("list")) {
				if (args.length == 1) {
					if (maps.size() > 0) {
					sender.sendMessage(ChatColor.YELLOW + "The currently existing maps are:");
					for (int i = 0; i < maps.size(); i++) {
						sender.sendMessage(ChatColor.YELLOW + " " + maps.get(i).getName());
					}
					}
					else {
						sender.sendMessage(ChatColor.YELLOW + "There are no maps in existence.");
					}
					return true;
				}
				else if (args.length > 2) {
					sender.sendMessage(ChatColor.RED + "Too many arguments. For help on this command, use " + ChatColor.BOLD +
							"/arkchest list ?" + ChatColor.RESET + ChatColor.RED + ".");
				}
				else if (args[1] == "?") {
					//sender.sendMessage(ChatColor.YELLOW + "This command is for listing all of the maps that currently exist. It takes no arguments, so syntax is simply"); //TODO finish help
				}
				else {
					sender.sendMessage(ChatColor.RED + "Too many arguments. For help on this command, use " + ChatColor.BOLD +
							"/arkchest list ?" + ChatColor.RESET + ChatColor.RED + ".");
				}
			}
			else if (args[0].equalsIgnoreCase("details")) {
				if (args.length == 1) {
					sender.sendMessage(ChatColor.RED + "You must specify a map. For help on this command, use " + ChatColor.BOLD +
							"/arkchest details ?" + ChatColor.RESET + ChatColor.RED + ".");
				}
				else if (args.length > 2) {
					sender.sendMessage(ChatColor.RED + "Too many arguments. For help on this command, use " + ChatColor.BOLD +
							"/arkchest details ?" + ChatColor.RESET + ChatColor.RED + ".");
				}
				else if (args[1] == "?") {
					//TODO print help
				}
				Map m = getMap(args[1]);
				if (m == null) {
					sender.sendMessage(ChatColor.RED + "The specified map does not exist. For help on this command, use " + ChatColor.BOLD +
							"/arkchest details ?" + ChatColor.RESET + ChatColor.RED + ".");
				}
				//TODO print details
			}
			else if (args[0].equalsIgnoreCase("delete")) {
				if (args.length == 1) {
					sender.sendMessage(ChatColor.RED + "You must specify a map. For help on this command, use " + ChatColor.BOLD +
							"/arkchest delete ?" + ChatColor.RESET + ChatColor.RED + ".");
				}
				else if (args.length > 2) {
					sender.sendMessage(ChatColor.RED + "Too many arguments. For help on this command, use " + ChatColor.BOLD +
							"/arkchest delete ?" + ChatColor.RESET + ChatColor.RED + ".");
				}
				else if (args[1] == "?") {
					//TODO print help
				}
				boolean exists = false;
				for (int i = 0; 0 < maps.size(); i++) {
					if (maps.get(0).getName().equalsIgnoreCase(args[1])) {
						maps.remove(i);
						exists = false;
					}
				}
				if (exists) {
					sender.sendMessage(ChatColor.AQUA + "Successfully deleted map \"" + args[1] + "\".");
				}
				else {
					sender.sendMessage(ChatColor.RED + "The specified map does not exist. For help on this command, use " + ChatColor.BOLD +
							"/arkchest delete ?" + ChatColor.RESET + ChatColor.RED + ".");
				}
			}
			else if (args[0].equalsIgnoreCase("refill")) {
				if (args.length == 1) {
					
				}
				//TODO add help
				if (args.length != 2) {
					sender.sendMessage(ChatColor.RED + "Not enough arguments. For help on this command, use " + ChatColor.BOLD + "/arkchest refill ?" +
							ChatColor.RESET + ChatColor.RED + ".");
					return false;
				}
				Map map = getMap(args[1]);
				if (map == null) {
					sender.sendMessage(ChatColor.RED + "Map does not exist. For help on this command, use " + ChatColor.BOLD + "/arkchest refill ?" + ChatColor.RESET + ChatColor.RED + ".");
					return false;
				}
				map.refill();
			}
			else if (args[0].equalsIgnoreCase("scan")) {
				//TODO add help
				if (args.length > 2) {
					sender.sendMessage(ChatColor.RED + "Too many arguments. for help on this command, use \"/arkchest scan ?\".");
					return false;
				}
				else if (args.length < 2) {
					sender.sendMessage(ChatColor.RED + "Need to specify map. for help on this command, use \"/arkchest scan ?\".");
					return false;
				}
				Map map = getMap(args[1]);
				if (map == null) {
					sender.sendMessage(ChatColor.RED + "The specified map does not exist. for help on this command, use \"/arkchest scan ?\".");
					return false;
				}
				int BPT = getConfig().getInt("Blocks scanned per tick");
				sender.sendMessage(ChatColor.YELLOW + "Beginning scan at " + BPT + " blocks per tick...");
				map.scan(this, BPT, sender);
				return true;
			}
			else if (args[0].equalsIgnoreCase("setItems")) {
				//TODO add help
				if (args.length < 2) {
					sender.sendMessage(ChatColor.RED + "You must specify a map. for help on this command, use \"/arkchest setitems ?\".");
					return false;
				}
				else if (args.length > 2) {
					sender.sendMessage(ChatColor.RED + "Too many arguments. for help on this command, use \"/arkchest setitems ?\".");
					return false;
				}
				Map map = getMap(args[1]);
				if (map == null) {
					sender.sendMessage(ChatColor.RED + "That map does not exist. for help on this command, use \"/arkchest setitems ?\".");
					return false;
				}
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "You must be a player to use this command.");
					return false;
					
				}
				Player player = (Player) sender;
				@SuppressWarnings("deprecation")		//There is no alternative in the current build
				Block target = player.getTargetBlock(null, 4);
				if (target == null || !(target.getState() instanceof InventoryHolder)) {
					player.sendMessage(ChatColor.RED + "You must point your cursor at an inventory to use this command.");
					return false;
				}
				List<ItemStack> inventory = new ArrayList<ItemStack> (Arrays.asList(((InventoryHolder) target.getState()).getInventory().getContents()));
				inventory.removeAll(Collections.singleton(null));
				map.setChestItems(inventory.toArray(new ItemStack[0]));
				saveToFile();
				player.sendMessage(ChatColor.GREEN + "Set items");
				return true;
			}
			else if (args[0].equalsIgnoreCase("addmap")) {
				if (args.length == 2 && args[1].equals("?")) {
					sender.sendMessage(ChatColor.YELLOW + "This command is used for creating areas where chests will refill, called \"maps\". Place one of " +
							"these around the entire area and then run the " + ChatColor.BOLD + "/arkchest scan" + ChatColor.RESET + ChatColor.YELLOW +
							" command to scan for chests in that area.\nThe syntax is:\n" + ChatColor.BOLD +
							"/arkchest addmap <name> <x> <y> <z> <x> <y> <z> [world]\n" + ChatColor.RESET + ChatColor.YELLOW + 
							" where the x's, y's, and z's specify two opposite corners of the map. If world is left off, it will use the world of the " +
							"player. If the command is not used by a player, then the world is required.");
					return true;
				}
				if (args.length < 8) {
					sender.sendMessage(ChatColor.RED + "Not enough arguments. For help on this command, use \"" + ChatColor.BOLD + "/arkchest ");
					return false;
				}
				else if (args.length > 9) {
					sender.sendMessage(ChatColor.RED + "Too many arguments.\n" + ChatColor.RESET + "/arkchest addmap <name> <x> <y> <z> <x> <y> <z> [world]");
					return false;
				}
				else if (args[1].matches("\\.")) {
					sender.sendMessage(ChatColor.RED + "Map name cannot contain a dot.");
					return false;
				}
				for (int i = 2; i < 6; i++) {
					if (args[i].matches("\\D")) {
						sender.sendMessage(ChatColor.RED + "Coordinates must be integers");
						return false;
					}
				}
				World world;
				if (args.length == 9) {
					world = getServer().getWorld(args[8]);
					if (world == null) {
						sender.sendMessage(ChatColor.RED + "The specified world is not found. For help on this command use \"/arkchest addmap ?\"");
						return false;
					}
				}
				else {
					if (!(sender instanceof Player)) {
						sender.sendMessage(ChatColor.RED + "You must specify a world if not running this command as a player.");
					}
				}
				Location negative = new Location(((Player) sender).getLocation().getWorld(), Integer.parseInt(args[2]),
						Integer.parseInt(args[3]), Integer.parseInt(args[4]));
				Location positive = new Location(((Player) sender).getLocation().getWorld(), Integer.parseInt(args[5]),
						Integer.parseInt(args[6]), Integer.parseInt(args[7]));
				if (negative.getX() > positive.getX()) {
					double tmp = negative.getX();
					negative.setX(positive.getX());
					positive.setX(tmp);
				}
				maps.add(new Map(args[1], negative, positive));
				saveToFile();
				sender.sendMessage("Created new map");
				return true;
			}
			else {
				sender.sendMessage(ChatColor.RED + "That is not a valid command. For a list of all of the ArkChest commands, use /arkchest");
			}
		}
		return false;
	}
	
	private Map getMap(String name) {
		for (Map map : maps) {
			if (map.getName().equals(name)) {
				return map;
			}
		}
		return null;
	}
}
