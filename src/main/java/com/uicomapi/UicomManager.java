package com.uicomapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

import com.uicomapi.commands.CMDCommandException;
import com.uicomapi.commands.CMDFunction;
import com.uicomapi.commands.CMDStruct;
import com.uicomapi.commands.CMDTabLookup;
import com.uicomapi.commands.MissingPermissionHandle;
import com.uicomapi.commands.MissingPermissionsException;
import com.uicomapi.commands.PermissionCheck;
import com.uicomapi.gui.DispatchInformations;
import com.uicomapi.gui.GUIBase;
import com.uicomapi.util.Pair;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class UicomManager implements Listener, TabExecutor{

    private Plugin plugin = null;
    
	//GUI varibales
    private static Map<Player, GUIBase> GUIs = new HashMap<Player, GUIBase>();
    //---------------------

    //CMD variables
	private Map<String, CMDStruct> rootsToCMDS = new HashMap<>();
	private Map<String, String> aliasesToRoots = new HashMap<>();
	
	private String firstLabel = "[/]*[a-zA-Z0-9]*";
	private MissingPermissionHandle missingPermsHandle = (err) -> { 
		err.getPlayer().sendMessage(err.getMessage());
	};
    //--------------------

    public UicomManager(Plugin p) {
		this.plugin = p;
	}

	public UicomManager(){}


	public static void addGUI(GUIBase gui) {
		GUIs.put(gui.getPlayer(), gui);
	}
	
	public static void removeGUI(GUIBase gui) {
		GUIs.remove(gui.getPlayer());
	}
	
	public static GUIBase getGUIbyPlayer(Player player) {
		return GUIs.get(player);
	}
	
	public static GUIBase getGUIbyInventory(Inventory invent) {
		for(GUIBase inv : GUIs.values()) {
			if(inv.getInventory().equals(invent)) return inv;
		}
		return null;
	}
	
	public static void removeGUIofPlayer(Player p) {
		GUIs.remove(p);
	}
	
	public static Set<GUIBase> getGUIbyTag(String tag) {
		Set<GUIBase> out = new HashSet<>();
		for(GUIBase inv : GUIs.values()) {
			if(inv.getTag().equals(tag)) out.add(inv);
		}
		return out;
	}
	
	@EventHandler
	public void playerInventoryCloseEvent(InventoryCloseEvent e) {
		if(e.getPlayer() instanceof Player) {
			Player p = (Player)e.getPlayer();
			removeGUIofPlayer(p);
		}
	}
	
	@EventHandler
	public void onInventoryInteract(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		int rawClick = e.getRawSlot();
		
		if(rawClick < 0) return;
		
		GUIBase gui = getGUIbyPlayer(p);
		if(gui == null) return;
		if(e.getRawSlot() >= e.getInventory().getSize()) {
			InventoryAction action = e.getAction();
	        switch (action) {
	        case MOVE_TO_OTHER_INVENTORY:
	            e.setCancelled(true);
	            return;
	        case HOTBAR_MOVE_AND_READD:
	            e.setCancelled(true);
	            return;
	        case HOTBAR_SWAP:
	            e.setCancelled(true);
	            return;
	        default:
	            break;
	        }
		}
		if(gui.getAccessPermission().hasPermission(p) && e.getView().getTitle().equalsIgnoreCase(gui.getName()) && e.getCurrentItem() == null && e.getRawSlot() < gui.getSize()) {
			
			gui.dispatch(new DispatchInformations(e.getCurrentItem(), rawClick, gui.getButton(rawClick), gui, p, e));
		}else if(e.getCurrentItem() != null && e.getRawSlot() < gui.getSize()) {
            e.setCancelled(true);
            
			gui.dispatch(new DispatchInformations(e.getCurrentItem(), rawClick, gui.getButton(rawClick), gui, p, e));
		}
		
	}
	
	@EventHandler
	public void onDragDrop(InventoryDragEvent e) {
		Player p = (Player) e.getWhoClicked();
		
		GUIBase gui = getGUIbyPlayer(p);
		if(gui == null) return;
		if(gui.getAccessPermission().hasPermission(p) && e.getView().getTitle().equalsIgnoreCase(gui.getName())) {
			for(int i : e.getRawSlots()) {
				if(i < e.getInventory().getSize()) {
					if(e.getCursor() != null && !e.getCursor().equals(e.getOldCursor())) {
						e.setCancelled(true);
					}else if(e.getCursor() == null && e.getOldCursor() != null){
						e.setCancelled(true);
					}
				}
			}	
		}
	}

    //COMMANDS-------------------------

    private String[] preParse(String cmd) {
		String[] parts = cmd.split(" ");
		parts[0] = preParseLabel(parts[0]);
		if(this.plugin != null && !this.rootsToCMDS.containsKey(parts[0])){
			PluginCommand command = ((JavaPlugin)plugin).getCommand(parts[0]);
			if(command != null)
				command.setExecutor(this);
			else
				plugin.getLogger().log(Level.WARNING, "Could not register executor for: " + cmd);
		}
		if(!rootsToCMDS.containsKey(parts[0])) {
			rootsToCMDS.put(parts[0], new CMDStruct(parts[0], null));
		}
		return parts;
	}
	
	private String preParseLabel(String label) {
		return Pattern.matches(firstLabel, label) ? label : "/" + label;
	}
	
	public boolean register(String cmd, CMDFunction func) {
		String[] parts = preParse(cmd);
		try {
			rootsToCMDS.get(parts[0]).addCMD(parts, func);
		} catch (CMDCommandException e) {
			if(this.plugin != null) plugin.getLogger().log(java.util.logging.Level.WARNING, e.getMessage());
			return false;
		}
		return true;
	}

	public void registerAliase(String cmdRoot, String aliases){
		this.aliasesToRoots.put(preParseLabel(aliases), preParseLabel(cmdRoot));
	}
	
	public boolean registerTabLookup(String cmd, CMDTabLookup lookup) {
		String[] parts = preParse(cmd);
		try {
			rootsToCMDS.get(parts[0]).addCMDLookup(parts, lookup);
			return true;
		} catch (CMDCommandException e) {
			if(this.plugin != null) plugin.getLogger().log(Level.WARNING, e.getMessage());
		}
		return false;
	}
	
	public boolean registerPermissionCheck(String cmd, PermissionCheck check) {
		return registerPermissionCheck(cmd, check, null);
	}
	
	public boolean registerPermissionCheck(String cmd, PermissionCheck check, MissingPermissionHandle handle) {
		String[] parts = preParse(cmd);
		try {
			rootsToCMDS.get(parts[0]).addPermissionCheck(parts, check, handle);
			return true;
		} catch (CMDCommandException e) {
			if(this.plugin != null) plugin.getLogger().log(Level.WARNING, e.getMessage());
		}
		return false;
	}

	public void setMissingPermissionHandle(MissingPermissionHandle mph){
		this.missingPermsHandle = mph;
	}
	
	private boolean call(CommandSender sender, Command cmd, String label, String[] args) throws MissingPermissionsException, CMDCommandException {
		label = preParseLabel(label);
		String[] tempArr = new String[args.length + 1];
	    tempArr[0] = label;
	    System.arraycopy(args, 0, tempArr, 1, args.length);

		if(rootsToCMDS.containsKey(label) || aliasesToRoots.containsKey(label)) {
	    	try {
				CMDStruct root = rootsToCMDS.get(label);
				if(root == null) {
					root = rootsToCMDS.get(aliasesToRoots.get(label));
					tempArr[0] = aliasesToRoots.get(label);
				}
				if(root == null) return true;
				Pair<CMDStruct, Map<String, String>> pair = root.search(tempArr);

				CMDStruct struct = pair.getFirst();
				Map<String, String> wildCards = pair.getSecound();
				
				if(struct != null && struct.getFunc() != null) {
					if(sender instanceof Player){
						CMDStruct faildStruct = root.checkPermission(tempArr, (Player)sender);
						if(faildStruct != null)
							throw new MissingPermissionsException((Player)sender, faildStruct.getMissingPermissinHandle(), ChatColor.RED + "Missing Permissions", label, args);
					}
					return struct.getFunc().func(sender, cmd, label, args, wildCards);
				}
			} catch (CMDCommandException e) {
				if(e.getErrorReason().equals(CMDCommandException.ErrorReason.COMMAND_NOT_FOUND)){
					sender.sendMessage(ChatColor.RED + "Command: " + ChatColor.AQUA + String.join(" ", tempArr) + ChatColor.RED + " not found!");
					return true;
				}
				throw e;
			}
	    }
	    return true;
	}
	
	private List<String> tabComplete(CommandSender sender, Command cmd, String label, String[] args){
		label = preParseLabel(label);
		String[] tempArr = new String[args.length];
	    tempArr[0] = label;
	    System.arraycopy(args, 0, tempArr, 1, args.length-1);
		if(rootsToCMDS.containsKey(label)) {
			return rootsToCMDS.get(label).getTabList(tempArr, args[args.length-1], sender, cmd, label, args);
		}else {
			return new ArrayList<>();
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		try {
			return call(sender, cmd, label, args);
		} catch (MissingPermissionsException e) {
			if(e.getHandle() != null) {
				e.getHandle().handleMissingPermission(e);
			}else if(this.missingPermsHandle != null) {
				this.missingPermsHandle.handleMissingPermission(e);
			}
		} catch (CMDCommandException e) {
			sender.sendMessage(e.getMessage());
		} 
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return tabComplete(sender, cmd, label, args);
	}
    
}
