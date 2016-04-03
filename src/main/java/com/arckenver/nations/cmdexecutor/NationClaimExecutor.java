package com.arckenver.nations.cmdexecutor;

import java.math.BigDecimal;
import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.arckenver.nations.ConfigHandler;
import com.arckenver.nations.DataHandler;
import com.arckenver.nations.LanguageHandler;
import com.arckenver.nations.NationsPlugin;
import com.arckenver.nations.Utils;
import com.arckenver.nations.object.Nation;
import com.arckenver.nations.object.Rect;
import com.arckenver.nations.object.Region;
import com.flowpowered.math.vector.Vector2i;

public class NationClaimExecutor implements CommandExecutor
{
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			Player player = (Player) src;
			Nation nation = DataHandler.getNationOfPlayer(player.getUniqueId());
			if (nation == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.CI));
				return CommandResult.success();
			}
			if (!nation.isStaff(player.getUniqueId()))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.CK));
				return CommandResult.success();
			}
			Vector2i a = DataHandler.getFirstPoint(player.getUniqueId());
			Vector2i b = DataHandler.getSecondPoint(player.getUniqueId());
			if (a == null || b == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.EA));
				return CommandResult.success();
			}
			Rect rect = new Rect(player.getWorld().getUniqueId(), a, b);
			if (!nation.getRegion().isAdjacent(rect))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.EB));
				return CommandResult.success();
			}
			Region claimed = nation.getRegion().copy();
			claimed.addRect(rect);
			int toClaim = claimed.size() - nation.getRegion().size();
			
			if (NationsPlugin.getEcoService() == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.DC));
				return CommandResult.success();
			}
			Optional<Account> optAccount = NationsPlugin.getEcoService().getOrCreateAccount("nation-" + nation.getUUID().toString());
			if (!optAccount.isPresent())
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.DD));
				return CommandResult.success();
			}
			BigDecimal price = BigDecimal.valueOf(toClaim * ConfigHandler.getNode("prices").getNode("blockClaimPrice").getDouble());
			TransactionResult result = optAccount.get().withdraw(NationsPlugin.getEcoService().getDefaultCurrency(), price, NationsPlugin.getCause());
			if (result.getResult() == ResultType.ACCOUNT_NO_FUNDS)
			{
				src.sendMessage(Text.builder()
						.append(Text.of(TextColors.RED, LanguageHandler.DF.split("\\{AMOUNT\\}")[0]))
						.append(Utils.formatPrice(TextColors.RED, price))
						.append(Text.of(TextColors.RED, LanguageHandler.DF.split("\\{AMOUNT\\}")[1])).build());
				return CommandResult.success();
			}
			else if (result.getResult() != ResultType.SUCCESS)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.DN));
				return CommandResult.success();
			}
			
			if (toClaim + nation.getRegion().size() > nation.maxBlockSize())
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.EG));
				return CommandResult.success();
			}
			
			nation.setRegion(claimed);
			DataHandler.addToWorldChunks(nation);
			DataHandler.saveNation(nation.getUUID());
			src.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.EH));
		}
		else
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.CA));
		}
		return CommandResult.success();
	}
}