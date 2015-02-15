package net.simsa.minecraftmods.go;

import java.util.SortedSet;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import scala.actors.threadpool.Arrays;

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

    public boolean isOp(String player) {
	return Arrays.asList(MinecraftServer.getServer().getConfigurationManager().getOppedPlayerNames())
		.contains(player.toLowerCase().trim());
    }

    public boolean isSinglePlayer() {
	return MinecraftServer.getServer().isSinglePlayer();
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
	NamedLocations namedLocations = NamedLocations.forWorld(sender.getEntityWorld());
	EntityPlayer player = (EntityPlayer) sender;
	System.out.println("Player " + player.getName() + " is running /go now.");
	System.out.println("Is player an op? " + isOp(player.getName()));

	// TODO - Handle operator-only locations and single-player mode
	// TODO - Handle safe-landing checks prior to teleporting

	if (args.length < 1) {
	    // print a list of options
	    SortedSet<String> names = namedLocations
		    .listPositions(isOp(player.getName()) || isSinglePlayer());
	    player.addChatComponentMessage(new ChatComponentText("Where to?\n" + join(names)));
	} else if (args.length == 1) {
	    // go to the specified location if it's valid
	    BlockPos pos = namedLocations.getPosition(args[0]);
	    if (pos != null) {
		int destinationDimension = namedLocations.getDimension(args[0]);
		String dimName = GoMod.dimensionName(destinationDimension);
		boolean sameDimension = (destinationDimension == player.dimension);

		if (destinationDimension != player.dimension) {
		    player.travelToDimension(destinationDimension);
		}
		player.setPositionAndUpdate(pos.getX(), pos.getY(), pos.getZ());
		player.addChatComponentMessage(new ChatComponentText("Teleporting you to " + args[0] + " "
			+ " (" + (!sameDimension ? dimName + " " : "") + pos.getX() + ", " + pos.getY()
			+ ", " + pos.getZ() + ")"));
	    } else {
		player.addChatComponentMessage(new ChatComponentText(args[0]
			+ " is not the name of any saved location in this dimension."));
	    }
	} else if (args.length == 2 && args[0].equals("add")) {
	    // add a location
	    if ("home".equals(args[1].toLowerCase())) {
		player.addChatComponentMessage(new ChatComponentText(
			"Error: You may not override the /go home definition which always points to Overworld spawn."));
	    } else {
		namedLocations.savePosition(args[1], player.getPosition(), player.dimension, false);
		player.addChatComponentMessage(new ChatComponentText("Added location " + args[1] + " as ("
			+ GoMod.dimensionName(player.dimension) + " " + player.getPosition().getX() + ","
			+ player.getPosition().getY() + "," + player.getPosition().getZ() + ")"));
	    }
	} else if (args.length == 2 && args[0].equals("rm")) {
	    if ("home".equals(args[1].toLowerCase())) {
		player.addChatComponentMessage(new ChatComponentText(
			"Error: You may not override the /go home definition which always points to Overworld spawn."));
	    } else {
		// delete a location
		namedLocations.deletePosition(args[1]);
		player.addChatComponentMessage(new ChatComponentText("Removed location " + args[1]));
	    }
	}
    }

}
