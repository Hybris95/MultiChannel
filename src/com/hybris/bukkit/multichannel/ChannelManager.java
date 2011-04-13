package com.hybris.bukkit.multichannel;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.nijikokun.bukkit.Permissions.Permissions;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;

import com.hybris.bukkit.party.api.GroupManager;
import com.hybris.bukkit.party.api.Group;

/**
* ChannelManager tool for MultiChannel
* Compatible with Permissions
* @version 1.0
* @author Hybris95
 */
public class ChannelManager extends PlayerListener {
	
	private final MultiChannel plugin;
	private HashMap<String,Channel> publicChannels;
	private HashMap<String,Channel> privateChannels;
	private HashMap<String,LinkedHashMap<String,Integer>> usersChannels;
	private GroupManager groupManager;

	public ChannelManager(MultiChannel instance, GroupManager groupManager) {
		this.plugin = instance;
		this.groupManager = groupManager;
		this.publicChannels = new HashMap<String,Channel>();
		this.privateChannels = new HashMap<String,Channel>();
		this.usersChannels = new HashMap<String, LinkedHashMap<String,Integer>>();
	}
    
	@Override
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		String[] split = event.getMessage().split(" ", 2);
		Player player = event.getPlayer();
		boolean cancelEvent = true;
		
		if(split[0].equalsIgnoreCase("/chan")){
			if(split.length > 1){
				split = split[1].split(" ", 2);
				if(split[0].equalsIgnoreCase("admin") && split.length > 1){
					split = split[1].split(" ", 2);
					if(split[0].equalsIgnoreCase("create") && split.length > 1){
						if(!this.plugin.isAllowed(player, "admin.create")){return;}
						
						split = split[1].split(" ", 2);
						String channelName = split[0];
						String channelPassword = "";
						if(split.length == 2){
							channelPassword = split[1];
						}
						
						Channel c = this.publicChannels.get(channelName);
						if(c == null){
							c = this.privateChannels.get(channelName);
						}
						
						if(c == null){
							if(channelPassword.compareTo("") == 0){
								this.publicChannels.put(channelName, new Channel(this, channelName, player, "", true));
								player.sendMessage("Public Channel successfully created.");
							}
							else{
								this.privateChannels.put(channelName, new Channel(this, channelName, player, channelPassword, true));
								player.sendMessage("Private Channel successfully created.");
							}
							this.joinedChannel(player, channelName);
						}
						else{
							player.sendMessage("This channel already exists.");
						}
					}
					else if(split[0].equalsIgnoreCase("delete") && split.length > 1){
						if(!this.plugin.isAllowed(player, "admin.delete")){return;}
						
						// Channel removing
						String channelName = split[1];
						Channel c = this.publicChannels.get(channelName);
						if(c == null){
							c = this.privateChannels.get(channelName);
						}
						
						if(c != null){
							c.closeChannel(player, true);
						}
						else{
							player.sendMessage("Unknown channel.");
						}
					}
					else if(split[0].equalsIgnoreCase("join") && split.length > 1){
						if(!this.plugin.isAllowed(player, "admin.reqjoin")){return;}
						
						String channelName = split[1];
						
						Channel c = this.publicChannels.get(channelName);
						if(c == null){
							c = this.privateChannels.get(channelName);
						}
						
						if(c != null){
							if(c.joinChannel(player, "", true)){
								this.joinedChannel(player, channelName);
							}
						}
						else{
							player.sendMessage("Unknown channel.");
						}
					}
					else if(split[0].equalsIgnoreCase("changepwd") && split.length > 1){
						if(!this.plugin.isAllowed(player, "admin.changepwd")){return;}
						
						split = split[1].split(" ", 2);
						// Change a channel password
						String channelName = split[0];
						String newPassword = "";
						if(split.length == 2){
							newPassword = split[1];
						}
						
						Channel c = this.publicChannels.get(channelName);
						if(c == null){
							c = this.privateChannels.get(channelName);
							if(c != null){
								c.changePwd(player, newPassword, true);
								player.sendMessage("Password successfully changed.");
								if(newPassword.compareTo("") == 0){
									this.privateChannels.remove(channelName);
									this.publicChannels.put(channelName, c);
									player.sendMessage("Your channel is now public.");
								}
							}
							else{
								player.sendMessage("Unknown channel.");
							}
						}
						else{
							c.changePwd(player, newPassword, true);
							player.sendMessage("Password successfully changed.");
							if(newPassword.compareTo("") != 0){
								this.publicChannels.remove(channelName);
								this.privateChannels.put(channelName, c);
								player.sendMessage("Your channel is now private.");
							}
						}
					}
					else if(split[0].equalsIgnoreCase("mute") && split.length > 1 && split[1].split(" ", 2).length > 1){
						if(!this.plugin.isAllowed(player, "admin.mute")){return;}
						
						split = split[1].split(" ", 2);
						String channelName = split[0];
						String playerToMute = split[1];
						
						Channel c = this.publicChannels.get(channelName);
						if(c == null){
							c = this.privateChannels.get(channelName);
						}
						
						if(c != null){
							if(c.mutePlayer(player, playerToMute, true)){
								player.sendMessage(playerToMute + " has been muted in " + channelName);
							}
							else{
								player.sendMessage("Could not mute " + playerToMute + " from " + channelName);
							}
						}
						else{
							player.sendMessage("Unknown channel.");
						}
					}
					else if(split[0].equalsIgnoreCase("unmute") && split.length > 1 && split[1].split(" ", 2).length > 1){
						if(!this.plugin.isAllowed(player, "admin.mute")){return;}
						
						split = split[1].split(" ", 2);
						
						String channelName = split[0];
						String playerToUnmute = split[1];
						
						Channel c = this.publicChannels.get(channelName);
						if(c == null){
							c = this.privateChannels.get(channelName);
						}
						
						if(c != null){
							if(c.unmutePlayer(player, playerToUnmute, true)){
								player.sendMessage(playerToUnmute + " has been unmuted in " + channelName);
							}
							else{
								player.sendMessage("Could not unmute " + playerToUnmute + " from " + channelName);
							}
						}
						else{
							player.sendMessage("Unknown channel.");
						}
					}
					else if(split[0].equalsIgnoreCase("kick") && split.length > 1 && split[1].split(" ", 2).length > 1){
						if(!this.plugin.isAllowed(player, "admin.kick")){return;}
						
						split = split[1].split(" ", 2);
						
						String channelName = split[0];
						String playerToKick = split[1];
						
						Channel c = this.publicChannels.get(channelName);
						if(c == null){
							c = this.privateChannels.get(channelName);
						}
						
						if(c != null){
							if(c.kickPlayer(player, playerToKick, true)){
								player.sendMessage(playerToKick + " has been kicked from " + channelName);
							}
							else{
								player.sendMessage("Could not kick " + playerToKick + " from " + channelName);
							}
						}
						else{
							player.sendMessage("Unknown channel.");
						}
					}
					else if(split[0].equalsIgnoreCase("ban") && split.length > 1 && split[1].split(" ", 2).length > 1){
						if(!this.plugin.isAllowed(player, "admin.ban")){return;}
						
						split = split[1].split(" ", 2);
						
						String channelName = split[0];
						String playerToBan = split[1];
						
						Channel c = this.publicChannels.get(channelName);
						if(c == null){
							c = this.privateChannels.get(channelName);
						}
						
						if(c != null){
							if(c.banPlayer(player, playerToBan, true)){
								player.sendMessage(playerToBan + " has been banned from " + channelName);
							}
							else{
								player.sendMessage("Could not ban " + playerToBan + " from " + channelName);
							}
						}
						else{
							player.sendMessage("Unknown channel.");
						}
					}
					else if(split[0].equalsIgnoreCase("unban") && split.length > 1 && split[1].split(" ", 2).length > 1){
						if(!this.plugin.isAllowed(player, "admin.ban")){return;}
						
						split = split[1].split(" ", 2);
						
						String channelName = split[0];
						String playerToUnban = split[1];
						
						Channel c = this.publicChannels.get(channelName);
						if(c == null){
							c = this.privateChannels.get(channelName);
						}
						
						if(c != null){
							if(c.unbanPlayer(player, playerToUnban, true)){
								player.sendMessage(playerToUnban + " has been unbanned from " + channelName);
							}
							else{
								player.sendMessage("Could not unban " + playerToUnban + " from " + channelName);
							}
						}
						else{
							player.sendMessage("Unknown channel.");
						}
					}
					else if(split[0].equalsIgnoreCase("listchans")){
						if(!this.plugin.isAllowed(player, "admin.listchans")){return;}
						
						if(this.publicChannels.isEmpty() && this.privateChannels.isEmpty()){
							player.sendMessage("No channels available.");
						}
						else{
							player.sendMessage("List of available channels :");
							Object[] array = this.publicChannels.keySet().toArray();
							for(Object channelName_O : array){
								player.sendMessage(channelName_O.toString());
							}
							array = this.privateChannels.keySet().toArray();
							for(Object channelName_O : array){
								player.sendMessage("[PRIVATE]" + channelName_O.toString());
							}
						}
					}
					else if(split[0].equalsIgnoreCase("listusers") && split.length > 1){
						if(!this.plugin.isAllowed(player, "admin.listusers")){return;}
						
						String channelName = split[1];
						Channel c = this.publicChannels.get(channelName);
						if(c == null){
							c = this.privateChannels.get(channelName);
						}
						
						if(c != null){
							c.listUsers(player, true);
						}
						else{
							player.sendMessage("Channel " + channelName + " doesn't exist.");
						}
					}
					else{
						this.printUsage(player);
					}
				}
				else if(split[0].equalsIgnoreCase("create") && split.length > 1){
					if(!this.plugin.isAllowed(player, "user.create")){return;}
					
					split = split[1].split(" ", 2);
					String channelName = split[0];
					String channelPassword = "";
					if(split.length == 2){
						channelPassword = split[1];
					}
					
					Channel c = this.publicChannels.get(channelName);
					if(c == null){
						c = this.privateChannels.get(channelName);
					}
					
					if(c == null){
						if(channelPassword.compareTo("") == 0){
							this.publicChannels.put(channelName, new Channel(this, channelName, player, "", false));
							player.sendMessage("Public Channel successfully created.");
						}
						else{
							this.privateChannels.put(channelName, new Channel(this, channelName, player, channelPassword, false));
							player.sendMessage("Private Channel successfully created.");
						}
						this.joinedChannel(player, channelName);
					}
					else{
						player.sendMessage("This channel already exists.");
					}
				}
				else if(split[0].equalsIgnoreCase("delete") && split.length > 1){
					if(!this.plugin.isAllowed(player, "user.delete")){return;}
					
					// Channel removing
					String channelName = split[1];
					Channel c = this.publicChannels.get(channelName);
					if(c == null){
						c = this.privateChannels.get(channelName);
					}
					
					if(c != null){
						if(!c.closeChannel(player, false)){
							player.sendMessage("You cannot close this channel.");
						}
					}
					else{
						player.sendMessage("Unknown channel.");
					}
				}
				else if(split[0].equalsIgnoreCase("join") && split.length > 1){
					if(!this.plugin.isAllowed(player, "user.reqjoin")){return;}
					
					split = split[1].split(" ", 2);
					String channelName = split[0];
					String channelPassword = "";
					if(split.length == 2){
						channelPassword = split[1];
					}
					
					Channel c = this.publicChannels.get(channelName);
					if(c == null){
						c = this.privateChannels.get(channelName);
					}
					
					if(c != null){
						if(c.joinChannel(player, channelPassword, false)){
							this.joinedChannel(player, channelName);
						}
					}
					else{
						player.sendMessage("Unknown channel.");
					}
				}
				else if(split[0].equalsIgnoreCase("leave") && split.length > 1){
					if(!this.plugin.isAllowed(player, "user.reqjoin")){return;}
					
					// Leave a channel
					String channelName = split[1];
					Channel c = this.publicChannels.get(channelName);
					if(c == null){
						c = this.privateChannels.get(channelName);
					}
					
					if(c != null){
						if(c.leaveChannel(player)){
							this.leftChannel(player, channelName);
						}
					}
					else{
						player.sendMessage("Unknown channel.");
					}
				}
				else if(split[0].equalsIgnoreCase("changepwd") && split.length > 1){
					if(!this.plugin.isAllowed(player, "user.changepwd")){return;}
					
					split = split[1].split(" ", 2);
					// Change a channel password
					String channelName = split[0];
					String newPassword = "";
					if(split.length == 2){
						newPassword = split[1];
					}
					
					Channel c = this.publicChannels.get(channelName);
					if(c == null){
						c = this.privateChannels.get(channelName);
						if(c != null){
							if(c.changePwd(player, newPassword, false)){
								player.sendMessage("Password successfully changed.");
								if(newPassword.compareTo("") == 0){
									this.privateChannels.remove(channelName);
									this.publicChannels.put(channelName, c);
									player.sendMessage("Your channel is now public.");
								}
							}
						}
						else{
							player.sendMessage("Unknown channel.");
						}
					}
					else{
						if(c.changePwd(player, newPassword, false)){
							player.sendMessage("Password successfully changed.");
							if(newPassword.compareTo("") != 0){
								this.publicChannels.remove(channelName);
								this.privateChannels.put(channelName, c);
								player.sendMessage("Your channel is now private.");
							}
						}
					}
				}
				else if(split[0].equalsIgnoreCase("mute") && split.length > 1 && split[1].split(" ", 2).length > 1){
					if(!this.plugin.isAllowed(player, "user.mute")){return;}
					
					split = split[1].split(" ", 2);
					String channelName = split[0];
					String playerToMute = split[1];
					
					Channel c = this.publicChannels.get(channelName);
					if(c == null){
						c = this.privateChannels.get(channelName);
					}
					
					if(c != null){
						if(c.mutePlayer(player, playerToMute, false)){
							player.sendMessage(playerToMute + " has been muted in " + channelName);
						}
						else{
							player.sendMessage("Could not mute " + playerToMute + " from " + channelName);
						}
					}
					else{
						player.sendMessage("Unknown channel.");
					}
				}
				else if(split[0].equalsIgnoreCase("unmute") && split.length > 1 && split[1].split(" ", 2).length > 1){
					if(!this.plugin.isAllowed(player, "user.mute")){return;}
					
					split = split[1].split(" ", 2);
					
					String channelName = split[0];
					String playerToUnmute = split[1];
					
					Channel c = this.publicChannels.get(channelName);
					if(c == null){
						c = this.privateChannels.get(channelName);
					}
					
					if(c != null){
						if(c.unmutePlayer(player, playerToUnmute, false)){
							player.sendMessage(playerToUnmute + " has been unmuted in " + channelName);
						}
						else{
							player.sendMessage("Could not unmute " + playerToUnmute + " from " + channelName);
						}
					}
					else{
						player.sendMessage("Unknown channel.");
					}
				}
				else if(split[0].equalsIgnoreCase("kick") && split.length > 1 && split[1].split(" ", 2).length > 1){
					if(!this.plugin.isAllowed(player, "user.kick")){return;}
					
					split = split[1].split(" ", 2);
					
					String channelName = split[0];
					String playerToKick = split[1];
					
					Channel c = this.publicChannels.get(channelName);
					if(c == null){
						c = this.privateChannels.get(channelName);
					}
					
					if(c != null){
						if(c.kickPlayer(player, playerToKick, false)){
							player.sendMessage(playerToKick + " has been kicked from " + channelName);
						}
						else{
							player.sendMessage("Could not kick " + playerToKick + " from " + channelName);
						}
					}
					else{
						player.sendMessage("Unknown channel.");
					}
				}
				else if(split[0].equalsIgnoreCase("ban") && split.length > 1 && split[1].split(" ", 2).length > 1){
					if(!this.plugin.isAllowed(player, "user.ban")){return;}
					
					split = split[1].split(" ", 2);
					
					String channelName = split[0];
					String playerToBan = split[1];
					
					Channel c = this.publicChannels.get(channelName);
					if(c == null){
						c = this.privateChannels.get(channelName);
					}
					
					if(c != null){
						if(c.banPlayer(player, playerToBan, false)){
							player.sendMessage(playerToBan + " has been banned from " + channelName);
						}
						else{
							player.sendMessage("Could not ban " + playerToBan + " from " + channelName);
						}
					}
					else{
						player.sendMessage("Unknown channel.");
					}
				}
				else if(split[0].equalsIgnoreCase("unban") && split.length > 1 && split[1].split(" ", 2).length > 1){
					if(!this.plugin.isAllowed(player, "user.ban")){return;}
					
					split = split[1].split(" ", 2);
					
					String channelName = split[0];
					String playerToUnban = split[1];
					
					Channel c = this.publicChannels.get(channelName);
					if(c == null){
						c = this.privateChannels.get(channelName);
					}
					
					if(c != null){
						if(c.unbanPlayer(player, playerToUnban, false)){
							player.sendMessage(playerToUnban + " has been unbanned from " + channelName);
						}
						else{
							player.sendMessage("Could not unban " + playerToUnban + " from " + channelName);
						}
					}
					else{
						player.sendMessage("Unknown channel.");
					}
				}
				else if(split[0].equalsIgnoreCase("listchans")){
					if(!this.plugin.isAllowed(player, "user.listchans")){return;}
					
					if(this.publicChannels.isEmpty()){
						player.sendMessage("No channels available.");
					}
					else{
						player.sendMessage("List of available channels :");
						Object[] array = this.publicChannels.keySet().toArray();
						for(Object channelName_O : array){
							player.sendMessage(channelName_O.toString());
						}
					}
				}
				else if(split[0].equalsIgnoreCase("listusers") && split.length > 1){
					if(!this.plugin.isAllowed(player, "user.listusers")){return;}
					
					String channelName = split[1];
					Channel c = this.publicChannels.get(channelName);
					if(c == null){
						c = this.privateChannels.get(channelName);
					}
					
					if(c != null){
						c.listUsers(player, false);
					}
					else{
						player.sendMessage("Channel " + channelName + " doesn't exist.");
					}
				}
				else if(split[0].equalsIgnoreCase("listjoined")){
					if(!this.plugin.isAllowed(player, "user.reqjoin")){return;}
					
					HashMap<String, Integer> channels = this.usersChannels.get(player.getName());
					if(channels != null){
						Object[] keys = channels.keySet().toArray();
						Object[] values = channels.values().toArray();
						if(values.length > 0){
							player.sendMessage("List of joined channels :");
							for(int i = 0; i < values.length; i++){
								player.sendMessage("/" + (Integer)values[i] + " aka " + keys[i].toString());
							}
						}
						else{
							player.sendMessage("You have joined no channels");
						}
					}
					else{
						player.sendMessage("You have joined no channels");
					}
				}
				else if(split[0].equalsIgnoreCase("help")){
					this.printUsage(player);
					// TODO : Help for each commands
				}
				else{
					this.printUsage(player);
				}
			}
			else{
				this.printUsage(player);
			}
		}
		else if(split[0].equalsIgnoreCase("/gr")){
			if(this.groupManager != null){
				Group playerGroup = this.groupManager.getGroup(player);
				if(playerGroup != null){
					if(split.length > 1){
						String message = split[1];
						ArrayList<Player> members = playerGroup.getMembers();
						for(Player member : members){
							member.sendMessage("[PARTY]" + player.getName() + ": " + message);
						}
					}
					else{
						player.sendMessage("Say something!");
					}
				}
				else{
					player.sendMessage("You are not in a group");
				}
			}
			cancelEvent = false;
		}
		else if(split[0].startsWith("/")){
			if(split.length > 1){
				try{
					int index = Integer.parseInt(split[0].split("/", 2)[1]);
					
					String registeredChannel = this.getWithPlayerIndex(player, index);
					if(registeredChannel.compareTo("") != 0){
						if(!this.plugin.isAllowed(player, "user.speak")){return;}
						
						// Speak in a channel
						String message = split[1];
						Channel c = this.publicChannels.get(registeredChannel);
						if(c == null){
							c = this.privateChannels.get(registeredChannel);
						}
						
						if(c != null){
							c.speak(player, message);
						}
						else{
							player.sendMessage("Could not find the registered channel.");
						}
					}
				}
				catch(NumberFormatException e){
					cancelEvent = false;
				}
			}
			else{
				cancelEvent = false;
			}
		}
		else{
			cancelEvent = false;
		}
		event.setCancelled(cancelEvent);
	}
	
	private void printUsage(Player toPrint){
		toPrint.sendMessage("/chan [admin] <create|delete|join|leave|changepwd|mute|unmute|listchans|listusers|listjoined|kick|ban|unban|help>");
	}
	
	private void joinedChannel(Player justJoined, String channelName){
		LinkedHashMap<String,Integer> channels = this.usersChannels.get(justJoined.getName());
		if(channels == null){
			channels = new LinkedHashMap<String,Integer>();
			this.usersChannels.put(justJoined.getName(), channels);
		}
		
		Object[] values = channels.values().toArray();
		if(values.length == 0){
			channels.put(channelName, 1);
			justJoined.sendMessage("Channel registered as : /1");
		}
		else if(!channels.containsKey(channelName)){
			channels.put(channelName, (Integer)values[values.length - 1] + 1);
			justJoined.sendMessage("Channel registered as : /" + ((Integer)values[values.length - 1] + 1));
		}
		else{
			justJoined.sendMessage("Channel already registered as : /" + channels.get(channelName));
		}
	}
	
	private void leftChannel(Player justLeft, String channelName){
		HashMap<String, Integer> channels = this.usersChannels.get(justLeft.getName());
		if(channels != null){
			Object value = channels.remove(channelName);
			if(value != null){
				justLeft.sendMessage("Unregistered channel /" + value);
			}
		}
	}
	
	private String getWithPlayerIndex(Player p, int index){
		String toReturn = "";
		HashMap<String, Integer> channels = this.usersChannels.get(p.getName());
		if(channels != null){
			if(channels.containsValue(index)){
				Object[] keys = channels.keySet().toArray();
				Object[] values = channels.values().toArray();
				for(int i = 0; i < values.length; i++){
					if((Integer)values[i] == index){
						toReturn = keys[i].toString();
						return toReturn;
					}
				}
			}
		}
		return toReturn;
	}
	
	void removeChannel(String toRemove, ArrayList<String> usersContained){
		if(this.publicChannels.containsKey(toRemove)){
			this.publicChannels.remove(toRemove);
		}
		
		if(this.privateChannels.containsKey(toRemove)){
			this.privateChannels.remove(toRemove);
		}
		
		for(String name : usersContained){
			this.leftChannel(this.getPlayer(name), toRemove);
		}
	}
	
	Player getPlayer(String name){
		return this.plugin.getPlayer(name);
	}

}

