package nl.thedutchmc.dutchytpa;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandHandler implements CommandExecutor {
  private Tpa plugin;

  public CommandHandler(Tpa plugin) {
	this.plugin = plugin;
  }

  static HashMap<UUID, UUID> targetMap = new HashMap<>();

  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	if (!(sender instanceof Player)) {
	  sender.sendMessage(ChatColor.RED + "Only players may use this command!");
	  return true;
	}
	if (command.getName().equals("tpa")) {
	  if (!sender.hasPermission("tpa.tpa"))
		sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
	  if (args.length == 1) {
		if (!Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(args[0]))) {
		  sender.sendMessage(ChatColor.RED + "Player is not online!");
		  return true;
		}
		Player target = Bukkit.getPlayer(args[0]);
		final Player senderP = (Player)sender;
		if (target.getUniqueId().equals(senderP.getUniqueId())) {
		  sender.sendMessage(ChatColor.RED + "You may not teleport to yourself!");
		  return true;
		}
		if (targetMap.containsKey(senderP.getUniqueId())) {
		  sender.sendMessage(ChatColor.GOLD + "You already have a pending request!");
		  return false;
		}
		target.sendMessage(ChatColor.RED + senderP.getName() + ChatColor.GOLD + " wants to teleport to you. \nType " + ChatColor.RED + "/tpaccept" + ChatColor.GOLD + " to accept this request.\nType " + ChatColor.RED + "/tpdeny" + ChatColor.GOLD + " to deny this request.\nYou have 5 minutes to respond.");
		targetMap.put(senderP.getUniqueId(), target.getUniqueId());
		sender.sendMessage(ChatColor.GOLD + "Send TPA request to " + ChatColor.RED + target.getName());
		(new BukkitRunnable() {
			public void run() {
			  CommandHandler.targetMap.remove(senderP.getUniqueId());
			}
		  }).runTaskLaterAsynchronously((Plugin)this.plugin, 6000L);
	  } else {
		sender.sendMessage(ChatColor.RED + "Invalid synax!");
	  }
	  return true;
	}
	if (command.getName().equals("tpaccept") || command.getName().equals("tpyes")) {
	  if (!sender.hasPermission("tpa.accept"))
		sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
	  final Player senderP = (Player)sender;
	  if (targetMap.containsValue(senderP.getUniqueId())) {
		sender.sendMessage(ChatColor.GOLD + "TPA request accepted!");
		for (Map.Entry<UUID, UUID> entry : targetMap.entrySet()) {
		  if (((UUID)entry.getValue()).equals(senderP.getUniqueId())) {
			Player tpRequester = Bukkit.getPlayer(entry.getKey());
			SuccessfulTpaEvent event = new SuccessfulTpaEvent(tpRequester, tpRequester.getLocation());
			Bukkit.getPluginManager().callEvent(event);
			tpRequester.teleport((Entity)senderP);
			targetMap.remove(entry.getKey());
			break;
		  }
		}
	  } else {
		sender.sendMessage(ChatColor.GOLD + "You don't have any pending requests!");
	  }
	  return true;
	}
	if (command.getName().equals("tpdeny") || command.getName().equals("tpno")) {
	  if (!sender.hasPermission("tpa.deny"))
		sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
	  final Player senderP = (Player)sender;
	  if (targetMap.containsValue(senderP.getUniqueId())) {
		for (Map.Entry<UUID, UUID> entry : targetMap.entrySet()) {
		  if (((UUID)entry.getValue()).equals(senderP.getUniqueId())) {
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