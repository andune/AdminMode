package me.pwnage.bukkit.AdminMode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.pwnage.bukkit.AdminMode.storage.AdminObject;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class AdminMode extends JavaPlugin
{
    public static final Logger log = Logger.getLogger("Minecraft");
    public static String logPrefix = "[AdminMode]";
    private static AdminMode instance;
    
    private amEntity amEntity = new amEntity(this);
    private amPlayer amPlayer = new amPlayer(this);
    private PermissionHandler permh;
    private Config config;
    
    private String name;
    private String ver;
    
    private HashMap<String, Boolean> adminModeHash = new HashMap<String, Boolean>();

    /** Unlike traditional Singleton pattern, this method *could* potentially return null, since
     * we depend on Bukkit to initialize our object.  In theory, this should be impossible since
     * no code from our plugin should ever run until after onEnable() has been invoked, and the
     * instance is assigned as the first line of onEnable().
     * 
     * @return
     */
    static public AdminMode getInstance() {
    	return instance;
    }
    
    @Override
    public void onEnable()
    {
    	instance = this;
    	
    	name = getDescription().getName();
    	ver = getDescription().getVersion();
    	logPrefix = "[" + name + "]";
    	
        config = new Config();
        config.load();
        
        registerPermissions();
        
        getServer().getPluginManager().registerEvent(Type.ENTITY_DAMAGE, amEntity, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Type.ENTITY_TARGET, amEntity, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Type.PLAYER_JOIN, amPlayer, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Type.PLAYER_QUIT, amPlayer, Priority.Normal, this);
        
        log.log(Level.INFO, logPrefix + " Version " + ver + " Enabled.");
    }

    @Override
    public void onDisable()
    {
        log.log(Level.INFO, logPrefix + " Version " + ver + " Disabled.");
        instance = null;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command c, String name, String[] args)
    {
    	if( !(cs instanceof Player) )
    		return false;
    	
    	Player p = (Player) cs;
    	
    	if(name.equals("adminmode") || name.equals("am"))
    	{
    		if( args.length > 0 ) {
    			if( args[0].equals("reload") ) {
    				if( hasPermission(p, "adminmode.reload") ) {
    					config.load();
    					p.sendMessage(ChatColor.YELLOW + logPrefix + " Config file reloaded");
    				}
    				else
    					p.sendMessage(ChatColor.RED + "You do not have permission.");
    				
    				return true;
    			}
    		}
    		
    		if(hasPermission(p, "adminmode.use"))
    		{
    			if( AdminObject.hasSaveFile(p.getName()) ) {
    				resetInv(p);
    				p.sendMessage(ChatColor.YELLOW + "Disabled Admin Mode.");
    			} else {
    				pullInv(p);
    				p.sendMessage(ChatColor.YELLOW + "Enabled Admin Mode. Type " + ChatColor.BLUE + "/" + name + " off " + ChatColor.YELLOW + " to disable.");
    			}
    		} else {
    			p.sendMessage(ChatColor.RED + "You do not have permission to run this command!");
    		}
    		return true;
    	}
    	
    	return false;
    }
    
    /** This works by looking to see if we have a cached state for the player in question.  If not,
     * we get the state from disk and cache it.
     * 
     * @param p
     * @return true if the player is in AdminMode, false if not
     */
    public boolean isInAdminMode(Player p) {
    	Boolean b = adminModeHash.get(p.getName());
    	
    	if( b == null ) {
    		b = AdminObject.hasSaveFile(p.getName());
    		setAdminMode(p, b.booleanValue());
    	}
    	
    	return b.booleanValue();
    }
    
    private void setAdminMode(Player p, boolean status) {
    	adminModeHash.put(p.getName(), Boolean.valueOf(status));
    }

    private void resetInv(Player p)
    {
    	AdminObject ao = null;
    	try {
    		ao = AdminObject.load(p.getName());
    	}
    	catch(IOException e) {
    		e.printStackTrace();
    	}
    	
    	if( ao == null ) {
    		p.sendMessage("Error retrieving stored data");
    		return;
    	}
    	
        if(config.isResetItems())
        	p.getInventory().setContents(ao.getItems());
        if(config.isResetLoc())
        	p.teleport(ao.getLocation());
        if(config.isResetHealth())
        	p.setHealth(ao.getHealth());
        
        ao.delete();	// delete the saveFile once we're fully restored
        setAdminMode(p, false);
    }

    private void pullInv(Player p)
    {
    	AdminObject ao = new AdminObject(p);
    	try {
    		ao.save();
            setAdminMode(p, true);
            p.getInventory().setContents(config.getAdminItems());
    	}
    	catch(IOException e) {
    		e.printStackTrace();
    		p.sendMessage("Error saving data");
    	}
    }

    /** Initialize permission system.
     * 
     */
    private void registerPermissions() {
        Plugin permissionsPlugin = getServer().getPluginManager().getPlugin("Permissions");
        if( permissionsPlugin != null )
        	permh = ((Permissions) permissionsPlugin).getHandler();
    }
    
    /** Detect if a given CommandSender (includes Player objects) has access to a Permission.
     * Uses a Perm 2/3-compatible permission system if one was found, otherwise defaults to
     * Bukkit Superperms (introduced in RB#1000).
     * 
	 * @author morganm
     * @param sender
     * @param permissionNode
     * @return
     */
    public boolean hasPermission(CommandSender sender, String permissionNode) {
    	if( sender instanceof ConsoleCommandSender )		// console always has full rights
			return true;
		
		if( sender instanceof Player ) {
	    	if( permh != null ) 
	    		return permh.has((Player) sender, permissionNode);
	    	else
	    		return sender.hasPermission(permissionNode);		// default to superPerms
		}
		
		return false;
    }
    
    public File getJarFile() { return super.getFile(); }
}