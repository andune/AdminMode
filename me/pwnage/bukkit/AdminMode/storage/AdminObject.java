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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Object created when an admin uses /am, stores their location, inventory and health and
 * serializes it to disk so it can survive through login/logout or a reboot. 
 * 
 * @author morganm
 *
 */
public class AdminObject {
	private transient static File dataFolder = null;
	
	private transient String playerName;
	
	private Location location;
	private ItemStack[] items;
	private int health;
	
	public AdminObject(Player p) {
		this.playerName = p.getName();
		this.location = p.getLocation();
		this.items = p.getInventory().getContents();
		this.health = p.getHealth();
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
    	
    	FileOutputStream fos = new FileOutputStream(saveFile);
    	XMLEncoder encoder = new XMLEncoder(fos);
    	encoder.writeObject(this);
    	encoder.close();
	}
	
	public static boolean hasSaveFile(String playerName) {
		if( playerName == null )
			throw new NullPointerException("playerName is null");

		File saveFile = getSaveFile(playerName);
		
		return saveFile.exists();		
	}
	
	/** Given a playerName, return a stored AdminObject that's on disk.  Will return null
	 * if no object exists.
	 * 
	 * @param playerName
	 * @return
	 * @throws IOException
	 */
	public static AdminObject load(String playerName) throws IOException {
		if( playerName == null )
			throw new NullPointerException("playerName is null");
		
		File saveFile = getSaveFile(playerName);
		
		if( !saveFile.exists() )
			return null;
    	
    	FileInputStream fis = new FileInputStream(saveFile);
    	XMLDecoder decoder = new XMLDecoder(fis);
    	AdminObject o = (AdminObject) decoder.readObject();
    	decoder.close();
    	
    	o.playerName = playerName;
    	
    	return o;
	}
	
	private static File getSaveFile(String playerName) {
		return new File(getDataFolder() + playerName + ".dat");
	}
	
	public String getPlayerName() {
		return playerName;
	}

	public Location getLocation() {
		return location;
	}

	public ItemStack[] getItems() {
		return items;
	}

	public int getHealth() {
		return health;
	}
}
