package com.minehut.moderators.plotmoderation.listener.plotsquared;

import com.google.common.eventbus.Subscribe;
import com.minehut.moderators.plotmoderation.PlotModeration;
import com.minehut.moderators.plotmoderation.report.model.Report;
import com.plotsquared.core.events.PlotDeleteEvent;
import com.plotsquared.core.plot.PlotId;

import java.util.List;

public class PlotListener {

    @Subscribe
    public void onPlotDelete(PlotDeleteEvent event) {
        final PlotId plotId = event.getPlotId();
        final List<Report> reports = PlotModeration.getInstance().getReportManager().getReports().values().stream()
                .filter((report) -> report.plotX() == plotId.getX() && report.plotY() == plotId.getY())
                .toList();

        for (final Report report : reports) {
            PlotModeration.getInstance().getReportManager().deleteReport(report);
        }

        System.out.println("Plot (" + plotId.getX() + ";" + plotId.getY() + ") deleted!");
    }

}
