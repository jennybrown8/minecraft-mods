package com.codeforanyone.minecraftmods.go;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

/**
 * Represents a saved named location which can be a teleport destination
 * 
 * @author jenny
 * 
 */
public class NamedLocation implements Comparable<NamedLocation> {

	private String name;
	private BlockPos blockPos;
	private String playerUsername;
	private int dimension;
	private boolean operatorOnly;
	private boolean crossDimension;

	private NamedLocation() {
	}

	public NamedLocation(String name, BlockPos blockPos, int dimension) {
		this(name, blockPos, dimension, null);
	}

	public NamedLocation(String name, BlockPos blockPos, int dimension, String player) {
		this(name, blockPos, dimension, null, false, false);
	}

	public NamedLocation(String name, BlockPos blockPos, int dimension, String player, boolean operatorOnly,
			boolean crossDimension) {
		this.name = name;
		this.blockPos = blockPos;
		this.dimension = dimension;
		this.playerUsername = player;
		this.operatorOnly = operatorOnly;
		this.crossDimension = crossDimension;
	}

	public String toString() {
		return this.name + " as " + MCUtil.dimensionName(this.dimension) + " (" + blockPos.getX() + ", " + blockPos.getY()
				+ ", " + blockPos.getZ() + ")";
	}

	public boolean isVisibleToPlayer(ICommandSender player) {
		if (this.operatorOnly && !MCUtil.isOp(player))
			return false;
		if (!this.crossDimension && player.getCommandSenderEntity().dimension != this.dimension)
			return false;
		if (this.playerUsername != null && !this.playerUsername.equals(player.getCommandSenderEntity().getName()))
			return false;

		return true;
	}

	public String serialize() {
		StringBuffer sb = new StringBuffer();
		sb.append(blockPos.getX());
		sb.append(":");
		sb.append(blockPos.getY());
		sb.append(":");
		sb.append(blockPos.getZ());
		sb.append(":");
		sb.append(dimension);
		sb.append(":");
		sb.append(crossDimension);
		sb.append(":");
		sb.append(operatorOnly);
		sb.append(":");
		sb.append(name);
		sb.append(":");
		sb.append(playerUsername == null ? "" : playerUsername);
		return sb.toString();
	}

	public static NamedLocation deserialize(String serial) {
		String[] pieces = serial.split(":");
		NamedLocation nl = new NamedLocation();
		nl.setBlockPos(
				new BlockPos(Integer.parseInt(pieces[0]), Integer.parseInt(pieces[1]), Integer.parseInt(pieces[2])));
		nl.setDimension(Integer.parseInt(pieces[3]));
		nl.setCrossDimensional(Boolean.parseBoolean(pieces[4]));
		nl.setOperatorOnly(Boolean.parseBoolean(pieces[5]));
		nl.setName(pieces[6]);
		if (pieces.length == 8 && !"".equals(pieces[7])) {
			nl.setPlayerUsername(pieces[7]);
		}
		return nl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BlockPos getBlockPos() {
		return blockPos;
	}

	public void setBlockPos(BlockPos blockPos) {
		this.blockPos = blockPos;
	}

	public int getDimension() {
		return dimension;
	}

	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

	public boolean isOperatorOnly() {
		return operatorOnly;
	}

	public void setOperatorOnly(boolean operatorOnly) {
		this.operatorOnly = operatorOnly;
	}

	public boolean isCrossDimensional() {
		return crossDimension;
	}

	public void setCrossDimensional(boolean crossDimension) {
		this.crossDimension = crossDimension;
	}

	public String getPlayerUsername() {
		return playerUsername;
	}

	public void setPlayerUsername(String playerUsername) {
		this.playerUsername = playerUsername;
	}

	/**
	 * Determines whether the BlockPos would make a safe landing point for a
	 * player. At minimum, a solid block to stand on, no lava at foot level
	 * (water is okay), a breathable block at head level (usually air but
	 * torches ok too), and at least one block of breathable space above the
	 * head (due to Minecraft bugs with Y position that can sometimes be fixed
	 * by jumping). It's not a super rigorous check but it reduces the
	 * likelihood of insta-deaths.
	 * 
	 * @return true if the landing is probably survive-able
	 */
	public boolean isSafeLanding(World world) {
		WorldServer worldserver = MinecraftServer.getServer().worldServerForDimension(this.dimension);

		// System.out.println("Check blocks for dimension " + this.dimension);
		// System.out.println(" at location: " + blockPos.getX() + ", " +
		// blockPos.getY() + ", " + blockPos.getZ());
		Material head_mat = worldserver.getBlockState(blockPos.up(1)).getBlock().getMaterial();
		Block head_block = worldserver.getBlockState(blockPos.up(1)).getBlock();
		Block foot_block = worldserver.getBlockState(blockPos).getBlock();
		Block floor_block = worldserver.getBlockState(blockPos.down(1)).getBlock();
		Block abovehead_block = worldserver.getBlockState(blockPos.up(2)).getBlock();

		if (!floor_block.isPassable(world, blockPos.down(1)) && foot_block.isPassable(world, blockPos)
				&& head_block.isPassable(world, blockPos.up(1)) && abovehead_block.isPassable(world, blockPos.up(2))
				&& !head_mat.isLiquid()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Order by crossDimension/non, dimension Overworld/Nether/End/other, then
	 * by player-specific/all, then by name alphabetical.
	 */
	@Override
	public int compareTo(NamedLocation other) {
		if (this.isCrossDimensional() && !other.isCrossDimensional()) {
			return -1;
		} else if (!this.isCrossDimensional() && other.isCrossDimensional()) {
			return 1;
		}
		if (this.dimension != other.dimension) {
			if (this.dimension == 0 && (other.dimension == -1) || (other.dimension == 1)) {
				return -1;
			} else if (this.dimension == 0 && other.dimension > 0) {
				return -1;
			} else if (other.dimension == 0 && (this.dimension == -1) || (this.dimension == 1)) {
				return 1;
			} else if (other.dimension == 0 && this.dimension > 0) {
				return 1;
			} else {
				return (new Integer(this.dimension).compareTo(new Integer(other.dimension)));
			}
		}
		if (this.playerUsername != null && other.playerUsername == null) {
			return -1;
		} else if (this.playerUsername == null && other.playerUsername != null) {
			return 1;
		} else if (!this.playerUsername.equals(other.playerUsername)) {
			return this.playerUsername.compareTo(other.playerUsername);
		}
		return this.name.compareTo(other.name);
	}

}
