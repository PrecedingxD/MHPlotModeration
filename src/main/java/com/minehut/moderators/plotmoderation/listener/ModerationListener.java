package com.minehut.moderators.plotmoderation.listener;

import com.minehut.moderators.plotmoderation.PlotModeration;
import com.minehut.moderators.plotmoderation.flag.UnreviewedChangesFlag;
import com.minehut.moderators.plotmoderation.utils.text.CC;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.query.PlotQuery;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class ModerationListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        if (player.hasPermission(PlotModeration.MODERATOR_PERMISSION)) {
            final int totalUnreviewedPlots = (int) PlotQuery.newQuery().allPlots().asList()
                    .stream()
                    .filter((plot) -> plot.getFlag(UnreviewedChangesFlag.class) && plot.getOwner() != null)
                    .count();

            if(totalUnreviewedPlots > 0) {
                Bukkit.getServer().getScheduler().runTaskLater(PlotModeration.getInstance(), () -> player.sendMessage(CC.component("<prefix><white>There are currently <mh_blue>" + totalUnreviewedPlots + "</mh_blue> plots awaiting review. Type <mh_blue>/plotmod next</mh_blue> to teleport to the next plot in the queue.</white>")), 20L);
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final Location location = BukkitUtil.adapt(event.getBlock().getLocation());
        final Plot plot = location.getPlotAbs();

        if (plot != null && plot.isAdded(player.getUniqueId()) && !plot.getFlag(UnreviewedChangesFlag.class)) {
            plot.setFlag(UnreviewedChangesFlag.class, "true");
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final Location location = BukkitUtil.adapt(event.getBlock().getLocation());
        final Plot plot = location.getPlotAbs();

        if (plot != null && plot.isAdded(player.getUniqueId()) && !plot.getFlag(UnreviewedChangesFlag.class)) {
            plot.setFlag(UnreviewedChangesFlag.class, "true");
        }
    }

}
