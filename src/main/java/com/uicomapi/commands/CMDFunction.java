package com.uicomapi.commands;

import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface CMDFunction {

	public boolean func(CommandSender sender, Command cmd, String str, String[] args, Map<String, String> wildCards);
    
}
