package me.pwnage.bukkit.AdminMode;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class amPlayer extends PlayerListener
{
    private AdminMode plugin;
    public amPlayer(AdminMode p)
    {
        plugin = p;
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player p = event.getPlayer();
        if(plugin.isInAdminMode(p))
        {
        	p.sendMessage(ChatColor.YELLOW + "You are still in admin mode!");
        }
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        Player p = event.getPlayer();
        if(plugin.isInAdminMode(p))
        {
        	p.sendMessage(ChatColor.YELLOW + "You are still in admin mode!");
        }
    }
}
