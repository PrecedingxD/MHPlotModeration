package com.minehut.moderators.plotmoderation.report.manager;

import com.minehut.moderators.plotmoderation.PlotModeration;
import com.minehut.moderators.plotmoderation.report.model.Report;
import com.minehut.moderators.plotmoderation.utils.config.ConfigUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

@Getter
public class ReportManager {

    private static final File REPORTS_FILE = new File(
            PlotModeration.getInstance().getDataFolder(),
            "reports.yml"
    );

    private final Map<UUID, Report> reports = new HashMap<>();
    private final YamlConfiguration config;

    @SneakyThrows
    public ReportManager() {
        if (!REPORTS_FILE.getParentFile().exists()) {
            REPORTS_FILE.getParentFile().mkdirs();
        }

        if (!REPORTS_FILE.exists()) {
            REPORTS_FILE.createNewFile();
        }

        this.config = YamlConfiguration.loadConfiguration(REPORTS_FILE);

        final ConfigurationSection reportsSection = ConfigUtils.getOrCreateConfigurationSection(config, "reports");

        for (final String strId : reportsSection.getKeys(false)) {
            try {
                final ConfigurationSection section = reportsSection.getConfigurationSection(strId);
                final UUID reportId = UUID.fromString(strId);
                final UUID reporterId = UUID.fromString(section.getString("reporter"));
                final UUID targetId = UUID.fromString(section.getString("target"));
                final String reason = section.getString("reason");
                final long createdAt = section.getLong("created-at");
                final int plotX = section.getInt("plot-x");
                final int plotY = section.getInt("plot-y");
                final Report report = new Report(
                        reportId,
                        reporterId,
                        targetId,
                        reason,
                        createdAt,
                        plotX,
                        plotY
                );

                reports.put(report.id(), report);
            } catch (Exception e) {
                PlotModeration.getInstance().getLogger().log(
                        Level.SEVERE,
                        "Failed to load report with ID " + strId,
                        e
                );
            }
        }
    }

    @SneakyThrows
    public void saveReport(Report report) {
        final ConfigurationSection section = ConfigUtils.getOrCreateConfigurationSection(config, "reports." + report.id());

        section.set("reporter", report.reporterId().toString());
        section.set("target", report.targetId().toString());
        section.set("reason", report.reason());
        section.set("created-at", report.createdAt());
        section.set("plot-x", report.plotX());
        section.set("plot-y", report.plotY());

        config.save(REPORTS_FILE);
        reports.put(report.id(), report);
    }

    @SneakyThrows
    public void deleteReport(Report report) {
        config.set("reports." + report.id(), null);
        config.save(REPORTS_FILE);

        reports.remove(report.id());
    }

}
