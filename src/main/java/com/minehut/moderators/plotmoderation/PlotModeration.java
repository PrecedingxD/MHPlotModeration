package com.minehut.moderators.plotmoderation;

import com.minehut.moderators.plotmoderation.command.ModerationCommand;
import com.minehut.moderators.plotmoderation.flag.UnreviewedChangesFlag;
import com.minehut.moderators.plotmoderation.listener.ModerationListener;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

public class PlotModeration extends JavaPlugin {

    public static final String MODERATOR_PERMISSION = "mhplots.mod";

    @Getter
    private static PlotModeration instance;

    private LegacyPaperCommandManager<CommandSender> commandManager;

    @Override
    public void onEnable() {
        instance = this;

        this.commandManager = LegacyPaperCommandManager.createNative(
                this,
                ExecutionCoordinator.simpleCoordinator()
        );

        GlobalFlagContainer.getInstance().addFlag(UnreviewedChangesFlag.UNREVIEWED_CHANGES_FALSE);

        registerCommands();
        registerListeners();
    }

    private void registerCommands() {
        final AnnotationParser<CommandSender> parser = new AnnotationParser<>(
                commandManager,
                CommandSender.class
        );

        parser.parse(new ModerationCommand());
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ModerationListener(), this);
    }

}
