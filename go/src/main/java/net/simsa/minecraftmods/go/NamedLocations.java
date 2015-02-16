package net.simsa.minecraftmods.go;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSavedData;

public class NamedLocations extends WorldSavedData {

    Map<String, NamedLocation> locations = new HashMap<String, NamedLocation>();

    public NamedLocations(String mapName) {
	super(mapName);
    }

    public void clear() {
	locations.clear();
	markDirty();
    }

    public void saveSharedLocation(ICommandSender commandSender, NamedLocation nl) {
	if (!MCUtil.isOp(commandSender) && !MCUtil.isSinglePlayer()) {
	    return;
	}
	locations.put(nl.getName(), nl);
	markDirty();
    }

    public NamedLocation getByName(String locName, ICommandSender commandSender) {
	boolean playerIsOp = MCUtil.isOp(commandSender);
	int playerCurrentDimension = commandSender.getCommandSenderEntity().dimension;

	if (locations.containsKey(locName)
		&& (playerIsOp || !locations.get(locName).isOperatorOnly())
		&& (locations.get(locName).isCrossDimensional() || locations.get(locName).getDimension() == playerCurrentDimension)) {
	    return locations.get(locName);
	}
	return null;
    }

    public SortedSet<String> list(ICommandSender commandSender, boolean includeDimensionName) {
	SortedSet<String> results = new TreeSet<String>();
	for (String name : locations.keySet()) {
	    if (locations.get(name).isVisibleToPlayer(commandSender)) {
		results.add(name
			+ (includeDimensionName
				&& locations.get(name).getDimension() != commandSender
					.getCommandSenderEntity().dimension ? " ("
				+ MCUtil.dimensionName(locations.get(name).getDimension()) + ")" : ""));
	    }
	}
	return results;
    }

    public boolean deleteSharedLocation(ICommandSender commandSender, String name) {
	if (!MCUtil.isOp(commandSender) && !MCUtil.isSinglePlayer()) {
	    return false;
	}
	name = name.toLowerCase();
	if (!locations.containsKey(name)) {
	    return false;
	}
	locations.remove(name);
	markDirty();
	return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
	for (Object okey : nbt.getKeySet()) {
	    String key = (String) okey;
	    NamedLocation nl = NamedLocation.deserialize(nbt.getString(key));
	    locations.put(nl.getName(), nl);
	}
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
	for (String locationName : locations.keySet()) {
	    nbt.setString(locationName, locations.get(locationName).serialize());
	}
    }

}