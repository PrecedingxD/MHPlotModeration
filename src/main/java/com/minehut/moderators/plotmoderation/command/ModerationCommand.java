package com.minehut.moderators.plotmoderation.command;

import com.minehut.moderators.plotmoderation.PlotModeration;
import com.minehut.moderators.plotmoderation.flag.UnreviewedChangesFlag;
import com.minehut.moderators.plotmoderation.utils.CC;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.query.PlotQuery;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;

import java.util.Objects;

@Command("moderation")
@Permission(PlotModeration.MODERATOR_PERMISSION)
@CommandDescription("Manage the plot moderation queue")
public class ModerationCommand {

    @Command("next")
    @CommandDescription("Teleport to the next plot in the moderation queue")
    public void next(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.component("<prefix><red>This command may only be performed by a player.</red>"));
            return;
        }

        final Plot nextPlot = PlotQuery.newQuery().allPlots().asList()
                .stream()
                .filter((plot) -> plot.getFlag(UnreviewedChangesFlag.class) && plot.getOwner() != null)
                .min((o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()))
                .orElse(null);

        if (nextPlot == null) {
            player.sendMessage(CC.component("<prefix><red>There are currently no plots marked for moderator review.</red>"));
            return;
        }

        PlotSquared.get().getImpromptuUUIDPipeline().getSingle(Objects.requireNonNull(nextPlot.getOwner()), (username, t) -> nextPlot.getCenter((location) -> {
            player.teleport(BukkitUtil.adapt(location));
            player.sendMessage(CC.component("<prefix><white>You have been teleported to <mh_blue>" + username + "</mh_blue>'s plot. Click this message to mark it as reviewed and in compliance with the rules.</white>")
                    .hoverEvent(HoverEvent.showText(CC.component("<gray>Click to mark this plot as reviewed and in compliance with the rules.</gray>")))
                    .clickEvent(ClickEvent.runCommand("/moderation remove"))
            );
        }));
    }

    @Command("remove")
    @CommandDescription("Remove a plot from the moderation queue")
    public void remove(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.component("<prefix><red>This command may only be performed by a player.</red>"));
            return;
        }

        final Plot plot = BukkitUtil.adapt(player.getLocation()).getPlotAbs();

        if (plot == null) {
            player.sendMessage(CC.component("<prefix><red>You are not in a plot.</red>"));
            return;
        }

        if (!plot.getFlag(UnreviewedChangesFlag.class)) {
            player.sendMessage(CC.component("<prefix><red>This plot is not currently queued for moderator review.</red>"));
            return;
        }

        plot.setFlag(UnreviewedChangesFlag.class, "false");
        player.sendMessage(CC.component("<prefix><green>You have marked this plot as reviewed and in compliance with the rules. It will be added to the moderation queue again if changes are contributed.</green>"));
    }

}
