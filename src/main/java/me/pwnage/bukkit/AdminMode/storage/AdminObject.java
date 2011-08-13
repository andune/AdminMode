/**
 * 
 */
package me.pwnage.bukkit.AdminMode.storage;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import me.pwnage.bukkit.AdminMode.AdminMode;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

/** Object created when an admin uses /am, stores their location, inventory and health and
 * serializes it to disk so it can survive through login/logout or a reboot. 
 * 
 * @author morganm
 *
 */
public class AdminObject {
	private static File dataFolder = null;
	
	private String playerName;
	private Location location;
	private ItemStack[] items;
	
	private AdminDataObject ado;
	
	/** For saving, this constructor should be used, it will copy all the relevant data
	 * from the player object.
	 * 
	 * @param p
	 */
	public AdminObject(Player p) {
		this(p.getName());
		
		ado = new AdminDataObject();
		setLocation(p.getLocation());
		setHealth(p.getHealth());
		setItems(p.getInventory().getContents());
	}
	/** For loading, this constructor should be used. The load() method should be called immediately
	 * after object instantiation to load the player data into this object.
	 * 
	 * @param playerName
	 */
	public AdminObject(String playerName) {
		this.playerName = playerName;
	}
	
	public static boolean hasSaveFile(String playerName) {
		if( playerName == null )
			throw new NullPointerException("playerName is null");

		File saveFile = getSaveFile(playerName);
		
		return saveFile.exists();		
	}
	
	private static File getDataFolder() {
		if( dataFolder == null )
			dataFolder = new File(AdminMode.getInstance().getDataFolder() + "/data/");
		
		if( !dataFolder.exists() )
			dataFolder.mkdirs();
		
		return dataFolder;
	}

	public void delete() {
		File saveFile = getSaveFile(playerName);
		saveFile.delete();
	}
	
	public void save() throws IOException {
		if( playerName == null )
			throw new NullPointerException("playerName is null");
		
		File saveFile = getSaveFile(playerName);
		saveFile.delete();
    	
		ClassLoader prevLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(AdminMode.getInstance().getClassLoader());
		
		try {
	    	FileOutputStream fos = new FileOutputStream(saveFile);
	    	XMLEncoder encoder = new XMLEncoder(fos);
	    	encoder.writeObject(ado);
	    	encoder.close();
		}
		finally {
			Thread.currentThread().setContextClassLoader(prevLoader);
		}
	}
	
	/** Load our data from the stored AdminDataObject that's on disk.
	 * 
	 * @param playerName
	 * @return
	 * @throws IOException
	 */
	public void load() throws IOException {
		if( playerName == null )
			throw new NullPointerException("playerName is null");
		
		File saveFile = getSaveFile(playerName);
		
		if( !saveFile.exists() )
			return;
    	
		ClassLoader prevLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(AdminMode.getInstance().getClassLoader());

		try {
			FileInputStream fis = new FileInputStream(saveFile);
	    	XMLDecoder decoder = new XMLDecoder(fis);
	    	ado = (AdminDataObject) decoder.readObject();
	    	decoder.close();
		}
		finally {
			Thread.currentThread().setContextClassLoader(prevLoader);
		}
	}
	
	private static File getSaveFile(String playerName) {
		return new File(getDataFolder(), playerName + ".dat");
	}
	
	public boolean hasData() { return ado != null; }
	
	public Location getLocation() {
		if( location == null ) {
			World world = AdminMode.getInstance().getServer().getWorld(ado.getWorldName());
			location = new Location(world, ado.getX(), ado.getY(), ado.getZ(), ado.getYaw(), ado.getPitch());
		}
		
		return location;
	}
	
	public String getPlayerName() {
		return playerName;
	}

	public void setLocation(Location l) {
		this.location = l;

		ado.setWorldName(location.getWorld().getName());
		ado.setX(location.getX());
		ado.setY(location.getY());
		ado.setZ(location.getZ());
		ado.setYaw(location.getYaw());
		ado.setPitch(location.getPitch());
	}
	
	public void setItems(ItemStack[] items) {
		this.items = items;
		
		StringBuilder sb = new StringBuilder();
		for(int i=0; i < items.length; i++) {
			if( items[i] != null ) {
				int typeId = items[i].getTypeId();
				int amount = items[i].getAmount();
				short durability = items[i].getDurability();

				byte data = 0;
				MaterialData md = items[i].getData();
				if( md != null )
					data = md.getData();

				sb.append(typeId);
				sb.append(",");
				sb.append(amount);
				sb.append(",");
				sb.append(data);
				sb.append(",");
				sb.append(durability);
			}
			
			sb.append(";");
		}
		
		ado.setItems(sb.toString());
	}

	public ItemStack[] getItems() {
		if( items == null ) {
			items = new ItemStack[36];
			
			String itemString = ado.getItems();
			if( itemString != null ) {
				String[] itemStrings = itemString.split(";");
				
				for(int i=0; i < itemStrings.length; i++) {
					if( itemStrings[i] == null )
						continue;
					
					String[] itemData = itemStrings[i].split(",");
					try {
						int typeId = Integer.parseInt(itemData[0]);
						int amount = Integer.parseInt(itemData[1]);
						byte data = Byte.parseByte(itemData[2]);
						short durability = Short.parseShort(itemData[3]);
						
						ItemStack stack = new ItemStack(typeId, amount, durability, data);
						items[i] = stack;
					}
					catch(NumberFormatException nfe) {
						AdminMode.log.info(AdminMode.logPrefix + " error loading item: "+itemData);
					}
					
				}
			}
		}
		
		return items;
	}

	public int getHealth() {
		return ado.getHealth();
	}
	
	public void setHealth(int health) {
		ado.setHealth(health);
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
}
