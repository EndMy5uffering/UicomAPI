package com.uicomapi.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.uicomapi.commands.CMDCommandException.ErrorReason;
import com.uicomapi.util.Pair;
import com.google.common.collect.Lists;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMDStruct {

	private final String wildCardPattern = "<([a-zA-Z0-9]+|\\.\\.\\.)>";
	private final String varargPattern = "<(\\.\\.\\.)>";
	
	private final String part;
	private final boolean isWildCard, isVarArg;
	private CMDFunction func;
	private Map<String, CMDStruct> next = new HashMap<>();
	private CMDTabLookup lookup;
	private PermissionCheck permissionCheck;
	private MissingPermissionHandle missingPermissinHandle = null;
	private CMDStruct nextWildCard = null;
		
	public CMDStruct(String part) {
		this(part, null);
	}
	
	public CMDStruct(String part, CMDFunction func) {
		this.isWildCard = Pattern.matches(wildCardPattern, part);
		this.isVarArg = Pattern.matches(varargPattern, part);
		this.part = part;
		this.func = func;
	}
	
	public Pair<CMDStruct, Map<String, String>> search(String[] cmd) throws CMDCommandException {
		return this.search(cmd, new HashMap<>(), 0);
	}
	
	private Pair<CMDStruct, Map<String, String>> search(String[] parts, Map<String,String> wildCards, int c) throws CMDCommandException {
		if(this.isVarArg){
			return new Pair<CMDStruct,Map<String,String>>(this, wildCards);
		}
		
		if(!this.isWildCard && !parts[c].equalsIgnoreCase(this.part)) 
			throw new CMDCommandException("Pattern mismatch while searching!\nExpected: \"" + this.part + "\" got \"" + parts[c] + "\" in command: " + String.join(" ", parts), ErrorReason.COMMAND_NOT_FOUND);

		if(this.isWildCard)
			wildCards.put(this.part.replace("<", "").replace(">", ""), parts[c]);
		
		if(c < parts.length-1) {
			if(hasNext(parts[c+1])) {
				return getNext(parts[c+1]).search(parts, wildCards, ++c);
			}else if(!hasNext(parts[c+1]) && this.nextWildCard != null){
				return this.nextWildCard.search(parts, wildCards, ++c);
			}else{
				throw new CMDCommandException("Pattern mismatch while searching! No part in the command matches: \""+ parts[c+1] + "\" in command: " + String.join(" ", parts), ErrorReason.COMMAND_NOT_FOUND);
			}
		}
		return new Pair<CMDStruct,Map<String,String>>(this, wildCards);
	}
	
	public List<String> getTabList(String[] parts, String partial, CommandSender sender, Command cmd, String str, String[] args){
		try {
			Pair<CMDStruct, Map<String, String>> res = search(parts);
			CMDStruct struct = res.getFirst();
			Map<String, String> wildCards = res.getSecound();
			if(struct != null) {
				Set<String> output = new HashSet<>();
				for(CMDStruct element : struct.next.values()) {
					if(element.part.contains(partial))
						output.add(element.part);
				}
				if(struct.nextWildCard != null && struct.nextWildCard.lookup != null){
					for(String item : struct.nextWildCard.lookup.get(sender, cmd, str, args, wildCards)){
						if(item.contains(partial))
							output.add(item);
					}
				}
				return Lists.newArrayList(output);
			}
		} catch (CMDCommandException e) {
			return new ArrayList<>();
		}
		return new ArrayList<>();
	}
	
	public List<CMDStruct> createPath(String[] parts) throws CMDCommandException{
		return createPath(parts, 1, new ArrayList<CMDStruct>());
	}

	private List<CMDStruct> createPath(String[] parts, int c, List<CMDStruct> list) throws CMDCommandException{
		if(this.isVarArg && c <= parts.length -1){
			throw new CMDCommandException("Can not get path after variable argument! For command: " + String.join(" ", parts), ErrorReason.OTHER);
		}
		list.add(this);
		if(c > parts.length - 1) {
			return list;
		}

		boolean isNextWildCard = Pattern.matches(wildCardPattern, parts[c]);
		if(isNextWildCard && this.nextWildCard != null && !this.nextWildCard.part.equals(parts[c])){
			throw new CMDCommandException("Can not get path with more then one wildcard in a branch! For command: " + String.join(" ", parts), ErrorReason.OTHER);
		}else if(isNextWildCard && this.nextWildCard != null && this.nextWildCard.part.equals(parts[c])){
			return this.nextWildCard.createPath(parts, ++c, list);
		}else if(isNextWildCard && this.nextWildCard == null){
			this.nextWildCard = new CMDStruct(parts[c], null);
			return this.nextWildCard.createPath(parts, ++c, list);
		}
		if(!this.hasNext(parts[c])) {
			this.next.put(parts[c], new CMDStruct(parts[c], null));
		}
		return this.getNext(parts[c]).createPath(parts, ++c, list);
	}

	public List<CMDStruct> getPath(String[] parts) throws CMDCommandException{
		return getPath(parts, 1, new ArrayList<CMDStruct>());
	}

	private List<CMDStruct> getPath(String[] parts, int c, List<CMDStruct> list) throws CMDCommandException{
		if(this.isVarArg && c <= parts.length -1){
			throw new CMDCommandException("Can not get path after variable argument! For command: " + String.join(" ", parts), ErrorReason.OTHER);
		}
		list.add(this);
		if(c > parts.length - 1) {
			return list;
		}

		if(!this.hasNext(parts[c]) && this.nextWildCard == null)
			throw new CMDCommandException("Pattern mismatch! No sub function found for: \""+ parts[c] + "\" in command: " + String.join(" ", parts), ErrorReason.CMD_MISSMATCH);
		else if(!this.hasNext(parts[c]) && this.nextWildCard != null)
			return this.nextWildCard.getPath(parts, ++c, list);
		if(!this.hasNext(parts[c]))
			throw new CMDCommandException("Pattern mismatch! No sub function found for: \""+ parts[c] + "\" in command: " + String.join(" ", parts), ErrorReason.CMD_MISSMATCH);
		else
			return this.getNext(parts[c]).getPath(parts, ++c, list);
	}
 

	public void addCMD(String[] parts, CMDFunction func) throws CMDCommandException {
		List<CMDStruct> path = createPath(parts);
		path.get(path.size()-1).setFunc(func);
	}

	public void addCMDLookup(String[] parts, CMDTabLookup tablookup) throws CMDCommandException {
		List<CMDStruct> path = createPath(parts);
		path.get(path.size()-1).setLookup(tablookup);
	}
	
	public void addPermissionCheck(String[] parts, PermissionCheck permissionCheck, MissingPermissionHandle handle) throws CMDCommandException {
		List<CMDStruct> path = createPath(parts);
		CMDStruct cstruct = path.get(path.size()-1);
		cstruct.setPermissionCheck(permissionCheck);
		cstruct.setMissingPermissinHandle(handle);
	}

	public CMDStruct checkPermission(String[] parts, Player p) throws CMDCommandException{
		List<CMDStruct> path = getPath(parts);
		boolean hasPermission = true;
		for(CMDStruct s : path){
			hasPermission &= s.checkPermission(p);
			if(!hasPermission) return s;
		}
		return null;
	}

	private boolean checkPermission(Player p){
		return hasPermissionCheck() ? this.permissionCheck.check(p) : true;
	}
	
	public String getPart() {
		return this.part;
	}
	
	public boolean hasFunc() {
		return func != null;
	}
	
	public boolean hasPermissionCheck() {
		return this.permissionCheck != null;
	}
	
	public boolean hasNext(String part) {
		return next.containsKey(part);
	}
	
	public CMDStruct getNext(String part) {
		return next.get(part);
	}
	
	public CMDFunction getFunc() {
		return this.func;
	}

	public PermissionCheck getPermissionCheck() {
		return permissionCheck;
	}

	public void setPermissionCheck(PermissionCheck permissionCheck) {
		this.permissionCheck = permissionCheck;
	}

	public MissingPermissionHandle getMissingPermissinHandle() {
		return missingPermissinHandle;
	}

	public void setMissingPermissinHandle(MissingPermissionHandle missingPermissinHandle) {
		this.missingPermissinHandle = missingPermissinHandle;
	}

	public CMDStruct getNextWildCard() {
		return nextWildCard;
	}

	public void setNextWildCard(CMDStruct nextWildCard) {
		this.nextWildCard = nextWildCard;
	}

	private void setFunc(CMDFunction func) {
		this.func = func;
	}

	private void setLookup(CMDTabLookup lookup) {
		this.lookup = lookup;
	}

}
