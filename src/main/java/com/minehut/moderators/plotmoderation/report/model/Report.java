package com.minehut.moderators.plotmoderation.report.model;

import java.util.UUID;

public record Report(UUID id, UUID reporterId, UUID targetId, String reason, long createdAt, int plotX, int plotY) {

}
