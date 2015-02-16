package net.simsa.minecraftmods.go;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSavedData;

public class NamedLocations extends WorldSavedData {

    Map<String, NamedLocation> sharedLocations = new HashMap<String, NamedLocation>();

    // Map<String, Map<String, NamedLocation>> playerLocations = new
    // HashMap<String, Map<String, NamedLocation>>();

    public NamedLocations(String mapName) {
	super(mapName);
    }

    public void saveSharedLocation(ICommandSender commandSender, NamedLocation nl) {
	if (!MCUtil.isOp(commandSender.getCommandSenderEntity().getName()) && !MCUtil.isSinglePlayer()) {
	    return;
	}
	sharedLocations.put(nl.getName(), nl);
	markDirty();
    }

    //@formatter:off
    /*
    public void savePlayer(NamedLocation nl) {
	Map<String, NamedLocation> map = playerLocations.get(nl.getPlayerUsername());
	if (map == null) {
	    map = new HashMap<String, NamedLocation>();
	    playerLocations.put(nl.getPlayerUsername(), map);
	}
	map.put(nl.getName(), nl);
	markDirty();
    }
    */
    //@formatter:on

    public NamedLocation getByName(String locName, ICommandSender commandSender) {
	boolean playerIsOp = MCUtil.isOp(commandSender.getName());
	int playerCurrentDimension = commandSender.getCommandSenderEntity().dimension;

	if (sharedLocations.containsKey(locName)
		&& (playerIsOp || !sharedLocations.get(locName).isOperatorOnly())
		&& (sharedLocations.get(locName).isCrossDimensional() || sharedLocations.get(locName)
			.getDimension() == playerCurrentDimension)) {
	    return sharedLocations.get(locName);
	}
	// Map<String, NamedLocation> map =
	// playerLocations.get(requestingPlayer);
	// if (map.containsKey(locName)) {
	// return map.get(locName);
	// }
	return null;
    }

    public SortedSet<String> list(ICommandSender commandSender) {
	SortedSet<String> results = new TreeSet<String>();
	for (String name : sharedLocations.keySet()) {
	    if (sharedLocations.get(name).isVisibleToPlayer(commandSender)) {
		results.add(name);
	    }
	}
	return results;
    }

    public void deleteSharedLocation(ICommandSender commandSender, String name) {
	if (!MCUtil.isOp(commandSender.getCommandSenderEntity().getName())) {
	    return;
	}
	name = name.toLowerCase();
	sharedLocations.remove(name);
	markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
	for (Object okey : nbt.getKeySet()) {
	    String key = (String) okey;
	    NamedLocation nl = NamedLocation.deserialize(nbt.getString(key));
	    sharedLocations.put(nl.getName(), nl);
	}
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
	for (String locationName : sharedLocations.keySet()) {
	    nbt.setString(locationName, sharedLocations.get(locationName).serialize());
	}

    }

}