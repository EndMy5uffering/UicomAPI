package com.uicomapi.gui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.uicomapi.util.PermissionGroup;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class GUIBase {
	
	protected final String name;
	private Map<Integer, InventoryButton> items = new HashMap<>();
	private String defaultErrorMessage = "";
	protected final Player opendBy;
	protected final int size;
	private final String Tag;
	protected Inventory inventory;
	private Set<Integer> noEventCancle = new HashSet<>();
	private GUIBase parent;
	private PermissionGroup accessPermissions = new PermissionGroup();
	
	private GUIFunction generalFunction;
	
	private boolean filterUnaccassable = true;
	
	public GUIBase(Player p, int size, String name, String Tag) {
		this.name = name;
		this.opendBy = p;
		this.size = size;
		this.Tag = Tag;
	}
	
	public boolean OpenGUI() {
		inventory = Bukkit.createInventory(this.opendBy, this.size, this.name);
		refreshItems();
		opendBy.openInventory(inventory);
		return true;
	}
	
	public void addItem(int index, ItemStack item) {
		if(items.containsKey(index)){
			items.get(index).addItem(item);
		}else{
			items.put(index, new InventoryButton(index, item, ""));
		}
		
	}
	
	public void addItem(int index, Material m, String name, List<String> lore) {
		ItemStack item = new ItemStack(m);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		if(lore != null && lore.size() > 0) meta.setLore(lore);
		item.setItemMeta(meta);
		addItem(index, item);
	}
	
	public void addItems(List<ItemStack> items) {
		for(int i = 0; i < items.size(); i++) {
			if(i < this.size) {
				addItem(i, items.get(i));
			}
		}
	}

	public InventoryButton getButton(int idx){
		return items.get(idx);
	}
	
	public ItemStack createItem(String itemName, List<String> lore, Material m) {
		ItemStack item = new ItemStack(m);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(itemName);
		if(lore != null && lore.size() > 0) meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
	
	public void setDefaultErrorMessage(String msg) {
		this.defaultErrorMessage = msg;
	}
	
	public void addNoCancleRange(int from, int to) {
		for(int i = from; i <= to; i++) {
			noEventCancle.add(i);
		}
	}
	
	public void addNoCancleRange(int... index) {
		for(int i : index) {
			noEventCancle.add(i);
		}
	}
	
	public void ClearNoCancle() {
		if(noEventCancle != null) noEventCancle.clear();
	}
	
	public void ClearUI() {
		if(inventory != null && items != null) {
			items.clear();
			inventory.clear();
		}
	}
	
	public void refreshItems() {
		if(inventory != null) {
			inventory.clear();
			items.values().stream().filter(x -> {
				return x.getPermissions().hasPermission(opendBy);
			}).forEach(x -> inventory.setItem(x.getInventoryIndex(), x.getActiveItem()));
		}
	}
	
	public void dispatch(DispatchInformations info){
		if(!items.containsKey(info.getIndex())) return;

		info.event.setCancelled(!(noEventCancle.contains(info.index) && info.event.getClickedInventory().equals(this.inventory)));
		
		if(items.get(info.index).getPermissions().hasPermission(info.getPlayer())) {
			items.get(info.index).getActiveFunction().dispatch(info);
		}
		
		if(generalFunction != null) {
			generalFunction.dispatch(info);
		}
		
	}
	
	public ItemStack getItem(int index) {
		if(items.get(index) != null && items.get(index).equals(this.inventory.getItem(index))) {
			return items.get(index).getActiveItem();
		}else {
			return null;
		}
	}

	public String getName() {
		return name;
	}

	public Player getPlayer() {
		return opendBy;
	}

	public String getTag() {
		return Tag;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public void setGeneralFunction(GUIFunction f) {
		this.generalFunction = f;
	}
	
	public Set<Integer> getNoEventCancle() {
		return noEventCancle;
	}

	public boolean isFilterUnaccassable() {
		return filterUnaccassable;
	}

	public void setFilterUnaccassable(boolean filterUnaccassable) {
		this.filterUnaccassable = filterUnaccassable;
	}

	public int getSize() {
		return size;
	}

	public String getDefaultErrorMessage() {
		return defaultErrorMessage;
	}

	public GUIFunction getGeneralFunction() {
		return generalFunction;
	}

	public GUIBase getParent(){
		return this.parent;
	}

	public boolean hasParent(){
		return this.parent != null;
	}

	public void setParent(GUIBase parent){
		this.parent = parent;
	}

	public void setAccessPermission(PermissionGroup permissionGroup){
		this.accessPermissions = permissionGroup;
	}

	public PermissionGroup getAccessPermission(){
		return this.accessPermissions;
	}

	public void addAccessPermission(String[]... permissions){
		this.accessPermissions.addPermissions(permissions);
	}

	public void setAccessPermission(String[]... permissions){
		this.accessPermissions.setPermissions(permissions);
	}

	public GUIBase addAccessPermission(String... permissions){
		this.accessPermissions.addPermissions(permissions);
		return this;
	}
	
}
