package nl.thedutchmc.dutchytpa;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandHandler implements CommandExecutor {
	
	private Tpa plugin;
	
	public CommandHandler(Tpa plugin) {
		this.plugin = plugin;
	}
	
	static HashMap<UUID, UUID> targetMap = new HashMap<>();

	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		// TPA command
		if(command.getName().equals("tpa")) {
			
			// tpa <playername>
			if(args.length == 1) {
				
				Player target = Bukkit.getPlayer(args[0]);
				Player senderP = (Player) sender;
				
				if(targetMap.containsKey(senderP.getUniqueId())) {
					sender.sendMessage(ChatColor.GOLD + "You already have a pending request!");
					return false;
				}
				
				target.sendMessage(ChatColor.RED + senderP.getName() + ChatColor.GOLD + " wants to teleport to you. \n"
						+ "Type " + ChatColor.RED + "/tpaccept" + ChatColor.GOLD + " to accept this request.\n"
						+ "Type " + ChatColor.RED + "/tpdeny" + ChatColor.GOLD + " to deny this request.\n"
						+ "You have 5 minutes to respond.");
				
				targetMap.put(senderP.getUniqueId(), target.getUniqueId());
				
				sender.sendMessage(ChatColor.GOLD + "Send TPA request to " + ChatColor.RED + target.getName());
				
				//After 5 minutes remove the request from the list
				new BukkitRunnable() {
					
					@Override
					public void run() {
						targetMap.remove(senderP.getUniqueId());
					}
					
				}.runTaskLaterAsynchronously(plugin, 5*60*20);
				
			// tpa <noargs>
			} else {
				sender.sendMessage(ChatColor.RED + "Invalid synax!");
			}
		
			return true;
			
			
		// tpaccept command
		} else if(command.getName().equals("tpaccept")) {
			
			Player senderP = (Player) sender;
			
			if(targetMap.containsValue(senderP.getUniqueId())) {
				
				sender.sendMessage(ChatColor.GOLD + "TPA request accepted!");
				
				for(Map.Entry<UUID, UUID> entry : targetMap.entrySet()) {
					
					if(entry.getValue().equals(senderP.getUniqueId())) {
						
						Player tpRequester = Bukkit.getPlayer(entry.getKey());
						tpRequester.teleport(senderP);
						targetMap.remove(entry.getKey());
						
						break;		
					}
				}
				
			} else {
				sender.sendMessage(ChatColor.GOLD + "You don't have any pending requests!");
				
			}
			
			return true;
			
			
		// tpdeny command
		} else if(command.getName().equals("tpdeny")) {
			
			Player senderP = (Player) sender;

			if(targetMap.containsValue(senderP.getUniqueId())) {
				
				for(Map.Entry<UUID, UUID> entry : targetMap.entrySet()) {
					
					if(entry.getValue().equals(senderP.getUniqueId())) {	
						
						targetMap.remove(entry.getKey());
						
						Player originalSender = Bukkit.getPlayer(entry.getKey());
						
						originalSender.sendMessage(ChatColor.GOLD + "Your TPA request was denied!");
						sender.sendMessage(ChatColor.GOLD + "Denied TPA request.");
						
						break;
					}
				}
			} else {
				sender.sendMessage(ChatColor.GOLD + "You don't have any pending requests!");

			}

			return true;
		}
		
		return false;
	}

}