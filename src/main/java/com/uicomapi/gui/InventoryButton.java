package com.uicomapi.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.uicomapi.util.PermissionGroup;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryButton {
    
    private List<ItemStack> itemStacks = new ArrayList<>();
    private Object attachedObjects;
    private String errorMessage;
    private Map<ItemStack, GUIFunction> itemsTofunctions = new HashMap<>();
    private int activeItem = 0;
    private PermissionGroup permission = new PermissionGroup();
    private int inventoryIndex = 0;

    public InventoryButton(int inventoryIndex, ItemStack itemStack, String errorMessage){
        this.itemStacks.add(itemStack);
        this.errorMessage = errorMessage;
    }

    public InventoryButton(int inventoryIndex, Material m, String name, List<String> lore) {
		ItemStack item = new ItemStack(m);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		if(lore != null && lore.size() > 0) meta.setLore(lore);
		item.setItemMeta(meta);
        this.itemStacks.add(item);
    }

    public ItemStack next(){
        this.activeItem = (this.activeItem + 1) % this.itemStacks.size();
        return this.itemStacks.get(this.activeItem);
    }

    public ItemStack previous(){
        this.activeItem = this.activeItem - 1;
        if(this.activeItem < 0) this.activeItem += this.itemStacks.size();
        return this.itemStacks.get(this.activeItem);
    }

    public boolean addFunction(int idx, GUIFunction f){
        if(idx < 0 || idx >= itemStacks.size()) return false;
        this.itemsTofunctions.put(itemStacks.get(idx), f);
        return true;
    }

    public void addItem(ItemStack item){
        this.itemStacks.add(item);
    }

    public void addItems(ItemStack... item){
        for(ItemStack stack : item)
            this.itemStacks.add(stack);
    }

    public ItemStack getActiveItem(){
        return this.itemStacks.get(activeItem);
    }

    public GUIFunction getActiveFunction(){
        GUIFunction f = this.itemsTofunctions.get(getActiveItem());
        if(f == null) return (i) -> {};
        return f;
    }

    public void setPermissions(PermissionGroup permission){
        if(permission == null) {
            this.permission = new PermissionGroup();
            return;
        }
        this.permission = permission;
    }

    public PermissionGroup getPermissions(){
        return this.permission;
    }

    public int getInventoryIndex(){
        return this.inventoryIndex;
    }

}
