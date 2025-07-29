package com.minehut.moderators.plotmoderation.command;

import com.minehut.moderators.plotmoderation.PlotModeration;
import com.minehut.moderators.plotmoderation.report.model.Report;
import com.minehut.moderators.plotmoderation.utils.text.CC;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.uuid.UUIDMapping;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReportCommand {

    private static final Map<UUID, Long> LAST_REPORT_MAP = new HashMap<>();
    private static final long REPORT_COOLDOWN_TIME = TimeUnit.MINUTES.toMillis(1);

    @Command("plotreport <reason>")
    @CommandDescription("Report a plot to staff")
    public void report(CommandSender sender, @Greedy @Argument("reason") String reason) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.component("<prefix><red>This command may only be performed by a player.</red>"));
            return;
        }

        final Plot plot = BukkitUtil.adapt(player.getLocation()).getPlotAbs();

        if (plot == null) {
            player.sendMessage(CC.component("<prefix><red>You are not in a plot.</red>"));
            return;
        }

        if (!plot.hasOwner()) {
            player.sendMessage(CC.component("<prefix><red>This plot does not have an owner.</red>"));
            return;
        }

        final long lastReportedAt = LAST_REPORT_MAP.getOrDefault(player.getUniqueId(), 0L);
        final long elapsedTime = System.currentTimeMillis() - lastReportedAt;

        if (elapsedTime < REPORT_COOLDOWN_TIME) {
            player.sendMessage(CC.component("<prefix><red>You are currently on plot report cooldown.</red>"));
            return;
        }

        PlotSquared.get().getImpromptuUUIDPipeline().getSingle(Objects.requireNonNull(plot.getOwner()), (username, t) -> {
            player.sendMessage(CC.component("<prefix><green>Your report has been sent!</green>")
                    .appendNewline()
                    .append(CC.component("<red><i>Warning: Abuse of the report system may result in a ban.</i></red>"))
            );

            final Component component = CC.component("<prefix><white><mh_blue>" + player.getName() + "</mh_blue> has reported <mh_blue>" + username + "</mh_blue>'s plot for </white>")
                    .append(Component.text(reason, CC.MH_BLUE_COLOR))
                    .append(CC.component("<white>.</white>"));

            for (final Player staff : Bukkit.getServer().getOnlinePlayers()) {
                if (!staff.hasPermission(PlotModeration.MODERATOR_PERMISSION)) {
                    continue;
                }

                staff.sendMessage(component);
            }

            PlotModeration.getInstance().getReportManager().saveReport(new Report(
                    UUID.randomUUID(),
                    player.getUniqueId(),
                    plot.getOwner(),
                    reason,
                    System.currentTimeMillis(),
                    plot.getId().getX(),
                    plot.getId().getY()
            ));
        });


        LAST_REPORT_MAP.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @Command("plotmod report list")
    @Permission(PlotModeration.MODERATOR_PERMISSION)
    @CommandDescription("View a list of all reports")
    public void list(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.component("<prefix><red>This command may only be performed by a player.</red>"));
            return;
        }

        final List<Report> reports = PlotModeration.getInstance().getReportManager().getReports().values().stream()
                .sorted((o1, o2) -> Long.compare(o2.createdAt(), o1.createdAt()))
                .toList();

        if (reports.isEmpty()) {
            player.sendMessage(CC.component("<prefix><red>There are currently no plot reports.</red>"));
            return;
        }

        PlotSquared.get().getImpromptuUUIDPipeline().getNames(
                Stream.concat(
                        reports.stream().map(Report::reporterId),
                        reports.stream().map(Report::targetId)
                ).toList()
        ).thenAccept((mappings) -> {
            final Map<UUID, UUIDMapping> uuidMappings = mappings.stream()
                    .collect(Collectors.toMap(UUIDMapping::uuid, (mapping) -> mapping));
            final Book.Builder builder = Book.builder();

            for (final Report report : reports) {
                final String reporterName = uuidMappings.get(report.reporterId()).username();
                final String targetName = uuidMappings.get(report.targetId()).username();

                final Component page = Component.empty()
                        .append(CC.component("<blue>By:</blue>"))
                        .appendNewline()
                        .append(Component.text(reporterName))
                        .appendNewline()
                        .appendNewline()
                        .append(CC.component("<blue>Owner:</blue>"))
                        .appendNewline()
                        .append(Component.text(targetName))
                        .appendNewline()
                        .appendNewline()
                        .append(CC.component("<blue>Reason:</blue>"))
                        .appendNewline()
                        .append(Component.text(report.reason()))
                        .appendNewline()
                        .appendNewline()
                        .append(CC.component("<#5dc9bd>[REVIEW]</#5dc9bd>")
                                .hoverEvent(HoverEvent.showText(CC.component("<gray>Click here to teleport to this plot.</gray>")))
                                .clickEvent(ClickEvent.callback((audience) -> player.performCommand("plot visit " + report.plotX() + ";" + report.plotY())))
                        )
                        .appendNewline()
                        .append(CC.component("<red>[DELETE]</red>")
                                .hoverEvent(HoverEvent.showText(CC.component("<gray>Click here to delete this report.</gray>")))
                                .clickEvent(ClickEvent.callback((audience) -> {
                                    PlotModeration.getInstance().getReportManager().deleteReport(report);
                                    player.sendMessage(CC.component("<prefix><green>Plot report successfully deleted.</green>"));

                                    if(reports.size() > 1) {
                                        list(sender);
                                    }
                                }))
                        );

                builder.addPage(page);
            }

            Bukkit.getServer().getScheduler().runTask(PlotModeration.getInstance(), () -> player.openBook(builder));
        });
    }

}
