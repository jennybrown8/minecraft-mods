package com.codeforanyone.minecraftmods.gohome;

import java.util.SortedSet;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

/**
 * Primary command for the "go" mod; manages a list of named locations and lets
 * a user send themselves to a named location.
 * 
 */
public class CommandGo extends CommandBase {

	private int condenseAtCount = 10;

	public CommandGo() {
	}

	@Override
	public String getName() {
		return "go";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		if (!MCUtil.isOp(sender) && !MCUtil.isSinglePlayer()) {
			return "/go\n/go [locationName]";
		}
		return "/go\n/go [locationName]\n/go add [locationName]\n/go rm [locationName]\n/go add-global [locationName]\n/go rm-global [locationName]";
	}

	public String join(SortedSet<String> locations) {
		StringBuffer sb = new StringBuffer();
		for (String s : locations) {
			sb.append(s);
			if (locations.size() > condenseAtCount) {
				sb.append(" ");
			} else {
				sb.append("\n");
			}
		}
		if (locations.size() > condenseAtCount) {
			sb.append("\n");
		}
		return sb.toString();
	}

	@Override
	public void execute(ICommandSender sender, String[] args) throws CommandException {
		EntityPlayer player = (EntityPlayer) sender;
		// System.out.println("Player " + player.getName() + " is running /go");
		// System.out.println("Is player an op? " + MCUtil.isOp(player));

		// TODO - Handle operator-only locations and single-player mode
		NamedLocations globalLocations = Storage.forGlobal(sender.getEntityWorld());
		NamedLocations sameDimension = Storage.forWorld(sender.getEntityWorld());

		if (args.length < 1) {
			listNamdLocationNames(globalLocations, sameDimension, player);
		} else if (args.length == 1) {
			teleport(sender, args[0], sameDimension, globalLocations);
		} else if (args.length == 2 && args[0].equals("add")) {
			add(player, args[1], sameDimension, false);
		} else if (args.length == 2 && args[0].equals("add-global")) {
			add(player, args[1], globalLocations, true);
		} else if (args.length == 2 && ("rm".equals(args[0]) || "del".equals(args[0]) || "delete".equals(args[0]))) {
			remove(sender, args[1], sameDimension, player);
		} else if (args.length == 2 && "rm-global".equals(args[0])) {
			remove(sender, args[1], globalLocations, player);
		} else {
			player.addChatComponentMessage(new ChatComponentText("Error: Wrong number of arguments to /go"));
		}
	}

	private void remove(ICommandSender sender, String loc, NamedLocations locations, EntityPlayer player) {
		if (!MCUtil.isOp(player) && !MCUtil.isSinglePlayer()) {
			player.addChatComponentMessage(new ChatComponentText("Error: You must be an operator."));
		}
		loc = loc.toLowerCase();
		if ("home".equals(loc)) {
			player.addChatComponentMessage(new ChatComponentText(
					"Error: You may not override the /go home definition which always points to Overworld spawn."));
		} else {
			// delete a location
			boolean success = locations.deleteSharedLocation(sender, loc);
			if (success) {
				player.addChatComponentMessage(new ChatComponentText("Removed location " + loc));
			} else {
				player.addChatComponentMessage(
						new ChatComponentText("Permission denied or invalid name; not removing " + loc));
			}
		}
	}

	private void add(EntityPlayer player, String loc, NamedLocations locations, boolean crossDimensional) {
		if (!MCUtil.isOp(player) && !MCUtil.isSinglePlayer()) {
			player.addChatComponentMessage(new ChatComponentText("Error: You must be an operator."));
		}

		loc = loc.toLowerCase();
		if ("home".equals(loc.toLowerCase())) {
			player.addChatComponentMessage(new ChatComponentText(
					"Error: You may not override the /go home definition which always points to Overworld spawn."));
		} else {
			NamedLocation nl = new NamedLocation(loc, player.getPosition(), player.dimension);
			nl.setOperatorOnly(false);
			nl.setCrossDimensional(crossDimensional);
			if (!nl.isSafeLanding(player.getEntityWorld())) {
				player.addChatComponentMessage(new ChatComponentText(
						"Cannot add: Location is not safe.  Make sure you have a solid floor and air above your head."));
			} else {
				locations.saveSharedLocation(player, nl);
				player.addChatComponentMessage(new ChatComponentText("Added location " + loc + " as "
						+ prettyPlaceString(crossDimensional, player.dimension, player.getPosition().getX(),
								player.getPosition().getY(), player.getPosition().getZ())));
			}
		}
	}

	private NamedLocation get(ICommandSender sender, String location, NamedLocations inDimension,
			NamedLocations globalLocations) {
		NamedLocation nl = globalLocations.getByName(location, sender);
		if (nl == null) {
			nl = inDimension.getByName(location, sender);
		}
		if (nl == null) {
			return null;
		}
		if (!nl.isVisibleToPlayer(sender)
				|| (!nl.isCrossDimensional() && sender.getCommandSenderEntity().dimension != nl.getDimension())) {
			return null;
		}
		return nl;
	}

	private void teleport(ICommandSender sender, String loc, NamedLocations inDimension,
			NamedLocations globalLocations) {
		EntityPlayer player = (EntityPlayer) sender;

		NamedLocation namedLocation = this.get(sender, loc, inDimension, globalLocations);
		if (namedLocation == null) {
			player.addChatComponentMessage(new ChatComponentText("Unknown location name: " + loc));
			return;
		}
		BlockPos destination = namedLocation.getBlockPos();
		World playerWorld = player.getEntityWorld();
		if (!namedLocation.isSafeLanding(playerWorld)) {
			player.addChatComponentMessage(new ChatComponentText(
					"Uh-oh! Location " + loc + " is not a safe landing!  Canceling teleport to " + namedLocation));
			return;
		}

		boolean playerWasRidingAHorse = player.isRiding() && player.ridingEntity instanceof EntityHorse;
		if (player.isRiding()) {
			// Theoretically, just dismounting should work. But it doesn't. The
			// rider gets stuck on the mount.
			player.dismountEntity(player.ridingEntity);

			// Calling mountEntity(null) seems to trigger an update packet to
			// the server, or maybe an EntityMountEvent, so both agree the rider
			// is fully dismounted, solving the bug.
			player.mountEntity(null);
		}
		// System.out.println("Is player still riding an entity? " +
		// player.ridingEntity + " isRiding " + player.isRiding());

		int comingFromDimension = player.dimension;
		int destinationDimension = namedLocation.getDimension();
		boolean sameDimension = (destinationDimension == player.dimension);
		if (destinationDimension != player.dimension) {
			player.travelToDimension(destinationDimension);
		}
		BlockPos comingFrom = player.getPosition();
		player.setPositionAndUpdate(destination.getX() + 0.5, destination.getY() + 0.1, destination.getZ() + 0.5);
		String msg = "Teleporting " + player.getName() + " from "
				+ prettyPlaceString(!sameDimension, comingFromDimension, comingFrom.getX(), comingFrom.getY(),
						comingFrom.getZ())
				+ " to " + loc + " " + prettyPlaceString(!sameDimension, destinationDimension, destination.getX(),
						destination.getY(), destination.getZ());
		player.addChatComponentMessage(new ChatComponentText(msg));

		if (playerWasRidingAHorse) {
			announceToOperators(player, "Warning: Player teleported while riding, leaving a horse behind in "
					+ MCUtil.dimensionName(comingFromDimension) + ". " + msg);
		}

	}

	private String prettyPlaceString(boolean showDim, int dimension, int x, int y, int z) {
		return "(" + (showDim ? MCUtil.dimensionName(dimension) + " " : "") + x + ", " + y + ", " + z + ")";

	}

	private void announceToOperators(EntityPlayer sender, String teleportMessage) {
		String modMessage = "[go-home mod] " + teleportMessage;
		System.out.println(modMessage); // for the server logs.
		String[] opers = MinecraftServer.getServer().getConfigurationManager().getOppedPlayerNames();
		for (int i = 0; i < opers.length; i++) {
			EntityPlayer opPlayer = ((EntityPlayerMP) sender).mcServer.getConfigurationManager()
					.getPlayerByUsername(opers[i]);
			// any logged in operators might be able to help retrieve a horse.
			opPlayer.addChatComponentMessage(new ChatComponentText(modMessage));
		}
	}

	// public static void Whisper(EntityPlayer fromPlayer, EntityPlayer
	// toPlayer, String theMessage) {
	// CommandMessage cm = new CommandMessage();
	// String[] args = new String[] { toPlayer.getDisplayNameString(),
	// theMessage };
	// try {
	// cm.execute(fromPlayer, args);
	// } catch (CommandException e) {
	// e.printStackTrace();
	// return;
	// }
	// }

	private void listNamdLocationNames(NamedLocations globalLocations, NamedLocations inDimension,
			EntityPlayer player) {
		SortedSet<String> globalNames = globalLocations.list(player, true);
		SortedSet<String> names = inDimension.list(player, false);
		player.addChatComponentMessage(new ChatComponentText("Where to?\n" + join(globalNames) + join(names)));
	}

}
