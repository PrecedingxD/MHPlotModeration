package com.minehut.moderators.plotmoderation.flag;

import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import org.checkerframework.checker.nullness.qual.NonNull;

public class UnreviewedChangesFlag extends BooleanFlag<UnreviewedChangesFlag> {

    public static final UnreviewedChangesFlag UNREVIEWED_CHANGES_TRUE = new UnreviewedChangesFlag(true);
    public static final UnreviewedChangesFlag UNREVIEWED_CHANGES_FALSE = new UnreviewedChangesFlag(false);

    protected UnreviewedChangesFlag(boolean value) {
        super(value, StaticCaption.of("<gray>Set to 'true' to mark whether this plot has been reviewed since it's last edit.</gray>"));
    }

    @Override
    protected UnreviewedChangesFlag flagOf(@NonNull Boolean value) {
        return value ? UNREVIEWED_CHANGES_TRUE : UNREVIEWED_CHANGES_FALSE;
    }

}
