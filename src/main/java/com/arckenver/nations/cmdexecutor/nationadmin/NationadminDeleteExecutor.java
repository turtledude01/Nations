package com.arckenver.nations.cmdexecutor.nationadmin;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;

import com.arckenver.nations.DataHandler;
import com.arckenver.nations.LanguageHandler;
import com.arckenver.nations.object.Nation;

public class NationadminDeleteExecutor implements CommandExecutor
{
	@Override
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (!ctx.<String>getOne("nation").isPresent())
		{
			src.sendMessage(Text.of(TextColors.YELLOW, "/na delete <nation>"));
			return CommandResult.success();
		}
		String nationName = ctx.<String>getOne("nation").get();
		Nation nation = DataHandler.getNation(nationName);
		if (nation == null)
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.CB));
			return CommandResult.success();
		}
		DataHandler.removeNation(nation.getUUID());
		MessageChannel.TO_ALL.send(Text.of(TextColors.AQUA, LanguageHandler.CN.replaceAll("\\{NATION\\}", nation.getName())));
		return CommandResult.success();
	}
}
