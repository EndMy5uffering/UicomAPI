package com.uicomapi.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class DispatchInformations {

	public final ItemStack item;
	public final int index;
	public final GUIBase base;
	public final Player player;
	public final InventoryClickEvent event;
	public final Object attachedObject;
	
	public DispatchInformations(ItemStack item, int index, Object attachedObject, GUIBase base, Player player, InventoryClickEvent e){
		this.item = item;
		this.index = index;
		this.base = base;
		this.player = player;
		this.event = e;
		this.attachedObject = attachedObject;
	}

	public ItemStack getItem() {
		return this.item;
	}

	public int getIndex() {
		return this.index;
	}

	public GUIBase getBase() {
		return this.base;
	}

	public Player getPlayer() {
		return this.player;
	}

	public InventoryClickEvent getEvent() {
		return this.event;
	}

	public Object getAttachedObject() {
		return this.attachedObject;
	}
	
}
