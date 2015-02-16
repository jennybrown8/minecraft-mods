package net.simsa.minecraftmods.go;

import java.util.SortedSet;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

/**
 * Primary command for the "go" mod; manages a list of named locations and lets
 * a user send themselves to a named location.
 * 
 */
public class CommandGo extends CommandBase {

    public CommandGo() {
    }

    @Override
    public String getName() {
	return "go";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
	return "/go home\n/go [locationName]\n/go add [locationName]\n/go rm [locationName]";
    }

    public String join(SortedSet<String> locations) {
	StringBuffer sb = new StringBuffer();
	for (String s : locations) {
	    sb.append(s);
	    sb.append("\n");
	}
	if (locations.size() == 0) {
	    sb.append("Use /go add locationname  to name where you stand as a teleport destination. /go home is reserved for spawn point.");
	}
	return sb.toString();
    }

    @Override
    public void execute(ICommandSender sender, String[] args) throws CommandException {
	NamedLocations namedLocations = Storage.forWorld(sender.getEntityWorld());
	EntityPlayer player = (EntityPlayer) sender;
	System.out.println("Player " + player.getName() + " is running /go");
	System.out.println("Is player an op? " + MCUtil.isOp(player.getName()));

	// TODO - Handle operator-only locations and single-player mode
	// TODO - Handle safe-landing checks prior to teleporting
	// TODO - Improve cross-dimensional saving
	// TODO - QA

	if (args.length < 1) {
	    listNames(namedLocations, player);
	} else if (args.length == 1) {
	    teleport(sender, args, namedLocations, player);
	} else if (args.length == 2 && args[0].equals("add")) {
	    add(player, args, namedLocations);
	} else if (args.length == 2
		&& ("rm".equals(args[0]) || "del".equals(args[0]) || "delete".equals(args[0]))) {
	    remove(sender, args, namedLocations, player);
	}
    }

    private void remove(ICommandSender sender, String[] args, NamedLocations namedLocations,
	    EntityPlayer player) {
	if ("home".equals(args[1].toLowerCase())) {
	    player.addChatComponentMessage(new ChatComponentText(
		    "Error: You may not override the /go home definition which always points to Overworld spawn."));
	} else {
	    // delete a location
	    namedLocations.deleteSharedLocation(sender, args[1]);
	    player.addChatComponentMessage(new ChatComponentText("Removed location " + args[1]));
	}
    }

    private void add(EntityPlayer player, String[] args, NamedLocations namedLocations) {
	if ("home".equals(args[1].toLowerCase())) {
	    player.addChatComponentMessage(new ChatComponentText(
		    "Error: You may not override the /go home definition which always points to Overworld spawn."));
	} else {
	    NamedLocation nl = new NamedLocation(args[1], player.getPosition(), player.dimension);
	    nl.setOperatorOnly(false);
	    nl.setCrossDimensional(false);
	    namedLocations.saveSharedLocation(player, nl);
	    player.addChatComponentMessage(new ChatComponentText("Added location " + args[1] + " as ("
		    + GoMod.dimensionName(player.dimension) + " " + player.getPosition().getX() + ","
		    + player.getPosition().getY() + "," + player.getPosition().getZ() + ")"));
	}
    }

    private void teleport(ICommandSender sender, String[] args, NamedLocations namedLocations,
	    EntityPlayer player) {
	if (namedLocations.getByName(args[0], sender) == null
		|| !namedLocations.getByName(args[0], sender).isVisibleToPlayer(sender)) {
	    player.addChatComponentMessage(new ChatComponentText("Invalid location " + args[0]));
	    return;
	}

	BlockPos pos = namedLocations.getByName(args[0], sender).getBlockPos();
	if (pos != null) {
	    int destinationDimension = namedLocations.getByName(args[0], sender).getDimension();
	    String dimName = GoMod.dimensionName(destinationDimension);
	    boolean sameDimension = (destinationDimension == player.dimension);

	    if (destinationDimension != player.dimension) {
		player.travelToDimension(destinationDimension);
	    }
	    player.setPositionAndUpdate(pos.getX(), pos.getY(), pos.getZ());
	    player.addChatComponentMessage(new ChatComponentText("Teleporting you to " + args[0] + " " + " ("
		    + (!sameDimension ? dimName + " " : "") + pos.getX() + ", " + pos.getY() + ", "
		    + pos.getZ() + ")"));
	} else {
	    player.addChatComponentMessage(new ChatComponentText(args[0]
		    + " is not the name of any saved location in this dimension."));
	}
    }

    private void listNames(NamedLocations namedLocations, EntityPlayer player) {
	SortedSet<String> names = namedLocations.list(player);
	player.addChatComponentMessage(new ChatComponentText("Where to?\n" + join(names)));
    }

}
