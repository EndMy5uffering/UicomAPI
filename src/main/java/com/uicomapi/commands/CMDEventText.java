package com.uicomapi.commands;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class CMDEventText {

    public static TextComponent getInteractComponent(String displayText, String value, ClickEvent.Action type, ChatColor c, boolean bold, String font) {
		TextComponent message = getTextComponent(displayText, c, bold);
		message.setClickEvent(new ClickEvent(type, value));
		return message;
	}

    public static TextComponent getInteractComponent(String displayText, String value, ClickEvent.Action type, ChatColor c, boolean bold) {
		return getInteractComponent(displayText, value, type, c, bold, null);
	}

	public static TextComponent getInteractComponent(String displayText, String value, ClickEvent.Action type, ChatColor c) {
		return getInteractComponent(displayText, value, type, c, false, null);
	}

	public static TextComponent getInteractComponent(String displayText, String value, ClickEvent.Action type) {
		return getInteractComponent(displayText, value,type, ChatColor.WHITE, false, null);
	}

	public static TextComponent getTextComponent(String text, ChatColor c, boolean bold, String font){
		TextComponent message = new TextComponent(text);
		message.setColor(c);
		message.setBold(bold);
		if(font != null) message.setFont(font);
		return message;
	}

	public static TextComponent getTextComponent(String text, ChatColor c, boolean bold){
		return getTextComponent(text, c, bold, null);
	}

	public static TextComponent getTextComponent(String text, ChatColor c){
		return getTextComponent(text, c, false);
	}

	public static TextComponent getTextComponent(String text){
		return getTextComponent(text, ChatColor.WHITE);
	}

	public static void sendEventMessage(Player p, TextComponent... m){
		p.spigot().sendMessage(m);
	}

}
