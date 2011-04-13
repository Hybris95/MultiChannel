package com.hybris.bukkit.multichannel;

import org.bukkit.entity.Player;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import com.hybris.bukkit.party.api.OptionalPartyPlugin;

/**
* MultiChannel plugin for Bukkit
* Compatible with Permissions
* @version 0.5a
* @author Hybris95
*/
public class MultiChannel extends OptionalPartyPlugin
{

	private ChannelManager cM;
	public static PermissionHandler Permissions = null;
	//private String baseDir = "plugins/MultiChannel/";
	
	public void onLoad()
	{
		super.onLoad();
	}

	public void onDisable()
	{
		this.log.info("[MultiChannel] unloading...");
		super.onDisable();
		// Unload channels ?
		this.cM = null;
		// TODO : Save every channels
		this.log.info("[MultiChannel] successfully unloaded");
	}

	public void onEnable() {
		this.log.info("[MultiChannel] enabling...");
		// Load (or not) Party plugin
		super.onEnable();
		this.cM = new ChannelManager(this, this.getManager());
		// Load the Permissions plugin
		this.loadPermissions();
		// Register our event
		this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, cM, Priority.Normal, this);
		this.log.info("[MultiChannel] successfully enabled");
	}
    
	private void loadPermissions() {
		Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
		PluginDescriptionFile pdfFile = this.getDescription();
			
		if (this.Permissions == null) {
			if (test!= null) {
				this.getServer().getPluginManager().enablePlugin(test);
				this.Permissions = ((Permissions) test).getHandler();
				this.log.info("[MultiChannel] successfully loaded Permissions.");
			}
			else {
				this.log.info("[MultiChannel] not using Permissions. Permissions not detected");
			}
		}
	}
	
	boolean usesPermissions(){
		if(this.Permissions == null){
			return false;
		}
		else{
			return true;
		}
	}
	
	boolean isAllowed(Player player, String node){
		boolean toReturn = false;
		String realNode = "multichannel." + node;
		if(usesPermissions()){
			toReturn = this.Permissions.has(player, realNode);
		}
		else{
			toReturn = (!node.startsWith("admin.") || player.isOp());
		}
		
		if(!toReturn){
			player.sendMessage("Unauthorized command call");
		}
		
		return toReturn;
	}
	
	Player getPlayer(String name){
		return this.getServer().getPlayer(name);
	}
	
}