package com.minehut.moderators.plotmoderation;

import com.minehut.moderators.plotmoderation.command.ModerationCommand;
import com.minehut.moderators.plotmoderation.command.ReportCommand;
import com.minehut.moderators.plotmoderation.flag.UnreviewedChangesFlag;
import com.minehut.moderators.plotmoderation.listener.paper.ModerationListener;
import com.minehut.moderators.plotmoderation.listener.plotsquared.PlotListener;
import com.minehut.moderators.plotmoderation.report.manager.ReportManager;
import com.minehut.moderators.plotmoderation.settings.Settings;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

@Getter
public class PlotModeration extends JavaPlugin {

    public static final String MODERATOR_PERMISSION = "mhplots.mod";

    @Getter
    private static PlotModeration instance;

    private Settings settings;
    private ReportManager reportManager;
    private LegacyPaperCommandManager<CommandSender> commandManager;

    @Override
    public void onEnable() {
        instance = this;

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        this.settings = new Settings(
                getConfig().getBoolean("features.queue"),
                getConfig().getBoolean("features.report")
        );
        this.reportManager = new ReportManager();
        this.commandManager = LegacyPaperCommandManager.createNative(
                this,
                ExecutionCoordinator.simpleCoordinator()
        );

        PlotSquared.get().getEventDispatcher().registerListener(new PlotListener());
        GlobalFlagContainer.getInstance().addFlag(UnreviewedChangesFlag.UNREVIEWED_CHANGES_FALSE);

        registerCommands();
        registerListeners();
    }

    private void registerCommands() {
        final AnnotationParser<CommandSender> parser = new AnnotationParser<>(
                commandManager,
                CommandSender.class
        );

        if(settings.queue()) {
            parser.parse(new ModerationCommand());
        }

        if(settings.report()) {
            parser.parse(new ReportCommand());
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ModerationListener(), this);
    }

}
