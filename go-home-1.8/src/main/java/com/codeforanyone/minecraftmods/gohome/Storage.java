package com.codeforanyone.minecraftmods.gohome;

import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;

public class Storage {
	final static String globalkey = "com.codeforanyone.minecraftmods.go:global";
	final static String dimkey = "com.codeforanyone.minecraftmods.go:dim";

	public Storage() {
	}

	public static NamedLocations forGlobal(World world) {
		// Create/retrieve the list of locations that are cross-dimensional
		MapStorage storage = world.getMapStorage();
		NamedLocations result = (NamedLocations) storage.loadData(NamedLocations.class, globalkey);
		if (result == null) {
			result = new NamedLocations(globalkey);
			storage.setData(globalkey, result);
			// default /go home to the spawn point
		}
		result.locations.put("home", new NamedLocation("home", world.getSpawnPoint(), 0, null, false, true));
		result.markDirty();
		return result;
	}

	public static NamedLocations forWorld(World world) {
		// Create/retrieve the list of locations for this dimension
		MapStorage storage = world.getPerWorldStorage();
		NamedLocations result = (NamedLocations) storage.loadData(NamedLocations.class, dimkey);
		if (result == null) {
			result = new NamedLocations(dimkey);
			storage.setData(dimkey, result);
		}
		return result;
	}
}
