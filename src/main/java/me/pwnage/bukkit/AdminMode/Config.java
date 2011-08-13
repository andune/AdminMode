/**
 * 
 */
package me.pwnage.bukkit.AdminMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.config.Configuration;

/**
 * @author morganm
 *
 */
public class Config {
	private static Logger log = AdminMode.log;
    public static final String logPrefix = AdminMode.logPrefix;
    
	private static final String CONFIG_FILE = "config.yml";
	
    private ItemStack[] adminStack = null;
	private Configuration pluginConfig;
	private File configFile;
	
	public boolean isResetLoc() { return pluginConfig.getBoolean("resetLoc", true); }
	public boolean isResetItems() { return pluginConfig.getBoolean("resetItems", true); }
	public boolean isResetHealth() { return pluginConfig.getBoolean("resetHealth", true); }
	
	public ItemStack[] getAdminItems() {
		if( adminStack == null )
		{
			String itemsString = pluginConfig.getString("items", null);

			if( itemsString == null ) {
				adminStack = new ItemStack[] {};
				return adminStack;
			}

			String[] Items4Admins = itemsString.split(",");
			adminStack = new ItemStack[Items4Admins.length];

			for(int i=0; i < Items4Admins.length; i++)
			{
				Items4Admins[i].replaceAll("/", "");

				String[] info = Items4Admins[i].split(":");
				int itemid = Integer.parseInt(info[0]);
				int amount = 1;

				if(info.length > 1)
				{
					amount = Integer.parseInt(info[1]);
				}

				adminStack[i] = new ItemStack(itemid, amount);
			}
		}
		
		return adminStack;
	}
	
    public void load() {
		pluginConfig = AdminMode.getInstance().getConfiguration();
		configFile = new File(AdminMode.getInstance().getDataFolder() + File.separator + CONFIG_FILE);
		
		// if no config exists, copy the default one out of the JAR file
		if( !configFile.exists() )
			copyConfigFromJar(configFile);
    }

	public boolean save() {
		if( pluginConfig == null )
			throw new NullPointerException("config is null");
		
		return pluginConfig.save();
	}
	
	/** Code adapted from Puckerpluck's MultiInv plugin.
	 * 
	 * @param string
	 * @return
	 */
    private void copyConfigFromJar(File file) {
        if (!file.canRead()) {
            try {
            	JarFile jar = new JarFile(AdminMode.getInstance().getJarFile());
            	
                file.getParentFile().mkdirs();
                JarEntry entry = jar.getJarEntry(CONFIG_FILE);
                InputStream is = jar.getInputStream(entry);
                FileOutputStream os = new FileOutputStream(file);
                byte[] buf = new byte[(int) entry.getSize()];
                is.read(buf, 0, (int) entry.getSize());
                os.write(buf);
                os.close();
            } catch (Exception e) {
                log.warning(logPrefix + " Could not copy config file "+CONFIG_FILE+" to default location");
            }
        }
    }
}
