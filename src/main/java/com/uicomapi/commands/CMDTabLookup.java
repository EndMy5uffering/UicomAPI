package com.uicomapi.commands;

import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface CMDTabLookup {

	public List<String> get(CommandSender sender, Command cmd, String str, String[] args, Map<String, String> wildCards);
	
}
