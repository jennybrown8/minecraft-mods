package net.simsa.minecraftmods.go;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

/**
 * Primary command for the "go" mod; manages a list of named locations and lets a user send themselves to a named location.
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
	return "/go";
    }

    @Override
    public void execute(ICommandSender sender, String[] args) throws CommandException {
	System.out.println("Wouldnt it be neat if this worked.");
    }

}
