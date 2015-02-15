package net.simsa.minecraftmods.go;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

public class NamedLocations extends WorldSavedData {

    Map<String, BlockPos> positions = new HashMap<String, BlockPos>();
    Map<String, Boolean> opRequired = new HashMap<String, Boolean>();

    public NamedLocations(String mapName) {
	super(mapName);
    }

    final static String key = "net.simsa.minecraftmods.go";

    public boolean isOpOnly(String name) {
	name = name.toLowerCase();
	return (opRequired.get(name) != null && opRequired.get(name).equals(Boolean.TRUE));
    }

    public void savePosition(String name, BlockPos pos, boolean op) {
	name = name.toLowerCase();
	positions.put(name, pos);
	opRequired.put(name, new Boolean(op));
	markDirty();
    }

    public SortedSet<String> listPositions(boolean includeOp) {
	SortedSet<String> results = new TreeSet<String>();
	for (String name : positions.keySet()) {
	    if ((opRequired.get(name).equals(Boolean.TRUE) && includeOp)
		    || opRequired.get(name).equals(Boolean.FALSE)) {
		results.add(name);
	    }
	}
	return results;
    }

    public void deletePosition(String name) {
	name = name.toLowerCase();
	positions.remove(name);
	opRequired.remove(name);
	markDirty();
    }

    public BlockPos getPosition(String name) {
	name = name.toLowerCase();
	return positions.get(name);
    }

    public static NamedLocations forWorld(World world) {
	// Create/retrieve the MyWorldData instance for the given world
	MapStorage storage = world.getPerWorldStorage();
	NamedLocations result = (NamedLocations) storage.loadData(NamedLocations.class, key);
	if (result == null) {
	    result = new NamedLocations(key);
	    storage.setData(key, result);
	    // default /go home to the spawn point but op can override later.
	    result.savePosition("home", world.getSpawnPoint(), false);
	}
	return result;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
	for (Object okey : nbt.getKeySet()) {
	    String key = (String) okey;
	    if (key.endsWith(":pos")) {
		String location = key.substring(0, key.length() - ":pos".length());
		int[] pos = nbt.getIntArray(key);
		positions.put(location, new BlockPos(pos[0], pos[1], pos[2]));
	    } else if (key.endsWith(":op")) {
		String location = key.substring(0, key.length() - ":op".length());
		opRequired.put(location, nbt.getBoolean(key));
	    } else {
		System.err.println("Unrecognized key format " + key);
	    }
	}
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
	for (String locationName : positions.keySet()) {
	    int[] pos = new int[3];
	    pos[0] = positions.get(locationName).getX();
	    pos[1] = positions.get(locationName).getY();
	    pos[2] = positions.get(locationName).getZ();
	    nbt.setIntArray(locationName + ":pos", pos);
	    nbt.setBoolean(locationName + ":op", isOpOnly(locationName));
	}

    }

}