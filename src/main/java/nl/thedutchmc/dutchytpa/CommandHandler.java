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
    private final Tpa plugin;

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
            handleTpaCommand(sender, args);
            return true;
        }

        if (command.getName().equals("tpaccept") || command.getName().equals("tpyes")) {
            handleTpaAcceptCommad(sender);
            return true;
        }

        if (command.getName().equals("tpdeny") || command.getName().equals("tpno")) {
            handleTpaDenyCommand(sender);
            return true;
        }

        return false;
    }

    private void handleTpaDenyCommand(CommandSender sender) {
        if (!sender.hasPermission("tpa.deny")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return;
        }

        final Player senderP = (Player) sender;

        // Check if a request exists
        if (!targetMap.containsValue(senderP.getUniqueId())) {
            sender.sendMessage(ChatColor.GOLD + "You don't have any pending requests!");
            return;
        }

        for (Map.Entry<UUID, UUID> entry : targetMap.entrySet()) {
            if (entry.getValue().equals(senderP.getUniqueId())) {
                targetMap.remove(entry.getKey());
                Player originalSender = Bukkit.getPlayer(entry.getKey());
                originalSender.sendMessage(ChatColor.GOLD + "Your TPA request was denied!");
                sender.sendMessage(ChatColor.GOLD + "Denied TPA request.");
                break;
            }
        }
    }

    private void handleTpaAcceptCommad(CommandSender sender) {
        // Check permission
        if (!sender.hasPermission("tpa.accept")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return;
        }

        final Player senderP = (Player) sender;

        // Check if there's a pending TPA request
        if (!targetMap.containsValue(senderP.getUniqueId())) {
            sender.sendMessage(ChatColor.GOLD + "You don't have any pending requests!");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "TPA request accepted!");

        for (Map.Entry<UUID, UUID> entry : targetMap.entrySet()) {
            if (entry.getValue().equals(senderP.getUniqueId())) {
                Player tpRequester = Bukkit.getPlayer(entry.getKey());

                // Fire the successful TPA event.
                // Used for integration with other plugins
                SuccessfulTpaEvent event = new SuccessfulTpaEvent(tpRequester, tpRequester.getLocation());
                Bukkit.getPluginManager().callEvent(event);

                // Teleport the player
                tpRequester.teleport(senderP);

                // Remove the pending request
                targetMap.remove(entry.getKey());
                break;
            }
        }
    }

    private void handleTpaCommand(CommandSender sender, String[] args) {
        // Check permissions
        if (!sender.hasPermission("tpa.tpa")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return;
        }

        // Check if a player name was provided
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Invalid syntax!");
            return;
        }

        // Check if the target player exists
        if (!Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(args[0]))) {
            sender.sendMessage(ChatColor.RED + "Player is not online!");
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        final Player senderP = (Player) sender;

        // Check if the target player is the requesting player
        if (target.getUniqueId().equals(senderP.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "You may not teleport to yourself!");
            return;
        }

        // Check if there's a pending request
        if (targetMap.containsKey(senderP.getUniqueId())) {
            sender.sendMessage(ChatColor.GOLD + "You already have a pending request!");
            return;
        }

        // Send the target a message informing them of the request
        target.sendMessage(ChatColor.RED + senderP.getName() + ChatColor.GOLD + " wants to teleport to you. \nType " + ChatColor.RED + "/tpaccept" + ChatColor.GOLD + " to accept this request.\nType " + ChatColor.RED + "/tpdeny" + ChatColor.GOLD + " to deny this request.\nYou have 5 minutes to respond.");
        targetMap.put(senderP.getUniqueId(), target.getUniqueId());

        // Inform the requester that the request was sent
        sender.sendMessage(ChatColor.GOLD + "Send TPA request to " + ChatColor.RED + target.getName());

        // Remove the request after a certain time
        (new BukkitRunnable() {
            public void run() {
                CommandHandler.targetMap.remove(senderP.getUniqueId());
            }
        }).runTaskLaterAsynchronously(this.plugin, 6000L);
    }
}