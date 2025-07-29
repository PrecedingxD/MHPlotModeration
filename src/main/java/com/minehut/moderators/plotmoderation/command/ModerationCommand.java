package com.minehut.moderators.plotmoderation.command;

import com.minehut.moderators.plotmoderation.PlotModeration;
import com.minehut.moderators.plotmoderation.flag.UnreviewedChangesFlag;
import com.minehut.moderators.plotmoderation.utils.pagination.Paginator;
import com.minehut.moderators.plotmoderation.utils.text.CC;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.query.PlotQuery;
import com.plotsquared.core.uuid.UUIDMapping;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;

import java.util.*;
import java.util.stream.Collectors;

@Command("plotmod")
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
                .min(Comparator.comparingLong(Plot::getTimestamp))
                .orElse(null);

        if (nextPlot == null) {
            player.sendMessage(CC.component("<prefix><red>There are currently no plots marked for moderator review.</red>"));
            return;
        }

        PlotSquared.get().getImpromptuUUIDPipeline().getSingle(Objects.requireNonNull(nextPlot.getOwner()), (username, t) -> nextPlot.getHome((location) -> {
            player.teleport(BukkitUtil.adapt(location));
            player.sendMessage(CC.component("<prefix><white>You have been teleported to <mh_blue>" + username + "</mh_blue>'s plot. Click this message to mark it as reviewed and in compliance with the rules.</white>")
                    .hoverEvent(HoverEvent.showText(CC.component("<gray>Click to mark this plot as reviewed and in compliance with the rules.</gray>")))
                    .clickEvent(ClickEvent.runCommand("/plotmod remove"))
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

    @Command("list [page]")
    @CommandDescription("View a list of all plots in the moderation queue")
    public void list(CommandSender sender, @Argument("page") Integer rawPage) {
        final int page = rawPage == null ? 1 : rawPage;
        final List<Plot> plots = PlotQuery.newQuery().allPlots().asList()
                .stream()
                .filter((plot) -> plot.getFlag(UnreviewedChangesFlag.class) && plot.getOwner() != null)
                .sorted(Comparator.comparingLong(Plot::getTimestamp))
                .toList();
        final Paginator<Plot> plotPaginator = new Paginator<>(9);
        final Paginator.PaginatedResult<Plot> paginatedResult = plotPaginator.paginate(plots, page);

        if (paginatedResult.totalPages() == 0) {
            sender.sendMessage(CC.component("<prefix><red>There are currently no plots marked for moderator review.</red>"));
            return;
        }

        if (page < 1 || page > paginatedResult.totalPages()) {
            sender.sendMessage(CC.component("<prefix><red>Invalid page index (1 - " + paginatedResult.totalPages() + ")"));
            return;
        }

        PlotSquared.get().getImpromptuUUIDPipeline().getNames(plots.stream()
                .filter(Plot::hasOwner)
                .map(Plot::getOwner)
                .distinct()
                .toList()
        ).thenAccept((mappings) -> {
            final Map<UUID, UUIDMapping> uuidMappings = mappings.stream()
                    .collect(Collectors.toMap(UUIDMapping::uuid, (mapping) -> mapping));

            sender.sendMessage(CC.component("<prefix><white>Queued Plots <gray>(Page " + page + "/" + paginatedResult.totalPages() + ")</white>"));

            for (final Plot plot : paginatedResult.items()) {
                final UUID ownerId = plot.getOwner();
                final UUIDMapping ownerMapping = uuidMappings.getOrDefault(ownerId, null);
                final String ownerUsername = ownerMapping == null ? "N/A" : ownerMapping.username();

                sender.sendMessage(CC.component("<gray>[<mh_blue>" + plot.getId().getX() + ";" + plot.getId().getY() + "</mh_blue>]</gray> <mh_blue>" + ownerUsername + "</mh_blue> ")
                        .append(CC.component("<dark_aqua>[Teleport]</dark_aqua>")
                                .hoverEvent(HoverEvent.showText(CC.component("<gray>Click here to teleport to this plot.</gray>")))
                                .clickEvent(ClickEvent.runCommand("/plot visit " + plot.getId().getX() + ";" + plot.getId().getY()))
                        )
                );
            }
        });
    }

}
