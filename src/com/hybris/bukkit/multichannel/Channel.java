package com.hybris.bukkit.multichannel;

import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
* Channel container for MultiChannel
* @version 1.0
* @author Hybris95
 */
class Channel{
	
	private String channelName = "";
	private String owner = "";
	private String password = "";
	private ArrayList<String> subscribedPlayers = new ArrayList<String>();
	private ArrayList<String> mutedPlayers = new ArrayList<String>();
	private ArrayList<String> hiddenPlayers = new ArrayList<String>();
	private ArrayList<String> bannedPlayers = new ArrayList<String>();
	private ChannelManager cM = null;
	
	Channel(ChannelManager cM, String channelName, Player owner, String password, boolean adminRequest){
		this.cM = cM;
		this.channelName = channelName;
		this.owner = owner.getName();
		this.password = password;
		this.subscribedPlayers.add(this.owner);
		if(adminRequest){
			this.hiddenPlayers.add(this.owner);
		}
	}
	
	boolean closeChannel(Player requestClosing, boolean adminRequest){
		if(adminRequest || (requestClosing.getName().compareTo(this.owner) == 0)){
			this.sendToConnected(this.channelName + ") This channel is being closed.", "");
			this.owner = "";
			this.password = "";
			this.mutedPlayers.clear();
			this.hiddenPlayers.clear();
			this.cM.removeChannel(this.channelName, this.subscribedPlayers);
			this.subscribedPlayers.clear();
			this.channelName = "";
			this.cM = null;
			return true;
		}
		else{
			return false;
		}
	}
	
	boolean joinChannel(Player requireEntering, String password, boolean adminRequest){
		if((this.password.equals(password) && !this.bannedPlayers.contains(requireEntering.getName())) || adminRequest){
			String playerName = requireEntering.getName();
			if(!this.subscribedPlayers.contains(playerName)){
				this.subscribedPlayers.add(playerName);
				if(!adminRequest){
					sendToConnected(this.channelName + ") " + playerName + " has entered the channel.", "");
				}
				else{
					requireEntering.sendMessage("You have joined " + this.channelName);
					this.hiddenPlayers.add(playerName);
				}
			}
			return true;
		}
		else{
			requireEntering.sendMessage("Either wrong password or banned from this channel");
			return false;
		}
	}
	
	boolean leaveChannel(Player requireLeaving){
		String playerName = requireLeaving.getName();
		if(this.subscribedPlayers.contains(playerName)){
			if(playerName.compareTo(this.owner) == 0){
				this.closeChannel(null, true);
			}
			else{
				if(!this.hiddenPlayers.contains(playerName)){
					sendToConnected(this.channelName + ") " + playerName + " has left the channel.", "");
				}
				else{
					requireLeaving.sendMessage("You have left " + this.channelName);
					this.hiddenPlayers.remove(this.hiddenPlayers.indexOf(playerName));
				}
				
				this.subscribedPlayers.remove(this.subscribedPlayers.indexOf(playerName));
				
				if(this.subscribedPlayers.isEmpty()){
					this.closeChannel(null, true);
				}
			}
			return true;
		}
		else{
			requireLeaving.sendMessage("You are not in this channel.");
			return false;
		}
	}
	
	boolean speak(Player speaker, String message){
		String playerName = speaker.getName();
		if(this.subscribedPlayers.contains(playerName) && (!this.mutedPlayers.contains(playerName) || this.hiddenPlayers.contains(playerName))){
			if(this.hiddenPlayers.contains(playerName)){
				sendToConnected(this.channelName + ") [HIDDEN]: " + message, "");
			}
			else{
				sendToConnected(this.channelName + ") " + playerName + ": " + message, "");
			}
			return true;
		}
		else{
			speaker.sendMessage("You are either not in this channel or muted from it.");
			return false;
		}
	}
	
	boolean changePwd(Player requestChangePwd, String newPassword, boolean adminRequest){
		String playerName = requestChangePwd.getName();
		if((playerName.compareTo(this.owner) == 0) || adminRequest){
			this.password = newPassword;
			return true;
		}
		else{
			requestChangePwd.sendMessage("You are not authorized to change this channel's password.");
			return false;
		}
	}
	
	boolean mutePlayer(Player requestMute, String toMute, boolean adminRequest){
		if(adminRequest || requestMute.getName().equals(this.owner)){
			if(!this.mutedPlayers.contains(toMute)){
				this.mutedPlayers.add(toMute);
				return true;
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}
	
	boolean unmutePlayer(Player requestUnMute, String toUnMute, boolean adminRequest){
		if(adminRequest || requestUnMute.getName().equals(this.owner)){
			if(this.mutedPlayers.contains(toUnMute)){
				this.mutedPlayers.remove(toUnMute);
				return true;
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}
	
	boolean kickPlayer(Player requestKick, String toKick, boolean adminRequest){
		if(adminRequest || requestKick.getName().equals(this.owner)){
			return this.leaveChannel(this.cM.getPlayer(toKick));
		}
		else{
			return false;
		}
	}
	
	boolean banPlayer(Player requestBan, String toBan, boolean adminRequest){
		if(adminRequest || requestBan.getName().equals(this.owner)){
			if(!this.bannedPlayers.contains(toBan)){
				this.bannedPlayers.add(toBan);
				return true;
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}
	
	boolean unbanPlayer(Player requestUnBan, String toUnBan, boolean adminRequest){
		if(adminRequest || requestUnBan.getName().equals(this.owner)){
			if(this.bannedPlayers.contains(toUnBan)){
				this.bannedPlayers.remove(toUnBan);
				return true;
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}
	
	void listUsers(Player requestListUsers, boolean adminRequest){
		requestListUsers.sendMessage("List of connected players :");
		
		if(adminRequest){
			for(Object o : this.hiddenPlayers.toArray()){
				Player p = this.cM.getPlayer(o.toString());
				if(p != null && p.isOnline()){
					if(this.mutedPlayers.contains(p.getName())){
						requestListUsers.sendMessage("[HIDDEN][MUTED]" + p.getName());
					}
					else{
						requestListUsers.sendMessage("[HIDDEN]" + p.getName());
					}
				}
			}
		}
		
		for(Object o : this.subscribedPlayers.toArray()){
			Player p = this.cM.getPlayer(o.toString());
			if(p != null && p.isOnline()){
				if(!this.hiddenPlayers.contains(p.getName())){
					if(this.mutedPlayers.contains(p.getName())){
						requestListUsers.sendMessage("[MUTED]" + p.getName());
					}
					else{
						requestListUsers.sendMessage(p.getName());
					}
				}
			}
		}
	}
	
	private void sendToConnected(String message, String exceptSender){
		for(Object n : this.subscribedPlayers.toArray()){
			if(n.toString().compareTo(exceptSender) != 0){
				Player p = this.cM.getPlayer(n.toString());
				if(p != null && p.isOnline()){
					p.sendMessage(message);
				}
			}
		}
	}
	
}