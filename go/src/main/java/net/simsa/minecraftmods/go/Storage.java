package net.simsa.minecraftmods.go;

import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;

public class Storage {
    final static String key = "net.simsa.minecraftmods.go";

    public Storage() {
	// TODO Auto-generated constructor stub
    }
    public static NamedLocations forWorld(World world) {
	// Create/retrieve the list of locations that are cross-dimensional
	//MapStorage globalStorage = world.getMapStorage();


	// Create/retrieve the list of locations for this dimension
	MapStorage storage = world.getPerWorldStorage();
	NamedLocations result = (NamedLocations) storage.loadData(NamedLocations.class, key);
	if (result == null) {
	    result = new NamedLocations(key);
	    storage.setData(key, result);
	    // default /go home to the spawn point
	    result.sharedLocations.put("home", new NamedLocation("home", world.getSpawnPoint(), 0, null, false, true));
	    result.markDirty();
	}
        result.sharedLocations.put("home", new NamedLocation("home", world.getSpawnPoint(), 0, null, false, true));
        result.markDirty();
	return result;
    }
}
