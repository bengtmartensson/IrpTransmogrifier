package org.harctoolbox.analyze;

import java.util.ArrayList;
import java.util.List;
import org.harctoolbox.irp.BareIrStream;
import org.harctoolbox.irp.Duration;
import org.harctoolbox.irp.Extent;
import org.harctoolbox.irp.Flash;
import org.harctoolbox.irp.Gap;
import org.harctoolbox.irp.IrStreamItem;

public class Burst {
    private static final double maxRoundingError = 0.3f;
    private static final double maxUnits = 20f;
    private static final double maxUs = 10000f;

    private static Duration newFlashOrGap(boolean isFlash, int us, double timebase) {
        double units = us/timebase;
        double roundingError = Math.round(units) - units;
        String unit = (units < maxUnits && Math.abs(roundingError) < maxRoundingError) ? ""
                : us < maxUs ? "u"
                : "m";
        double duration = unit.isEmpty() ? Math.round(units)
                : unit.equals("m") ? Math.round(us/1000f)
                : us;
        return isFlash ? new Flash(duration, unit) : new Gap(duration, unit);
    }

    public static Extent newExtent(int us, double timebase) {
        double units = us/timebase;
        double roundingError = Math.round(units) - units;
        String unit = (units < maxUnits && Math.abs(roundingError) < maxRoundingError) ? ""
                : us < maxUs ? "u"
                : "m";
        double duration = unit.isEmpty() ? Math.round(units)
                : unit.equals("m") ? Math.round(us/1000f)
                : us;
        return new Extent(duration, unit);
    }

    public static Flash newFlash(int duration, double timebase) {
        return (Flash) newFlashOrGap(true, duration, timebase);
    }

    public static Gap newGap(int duration, double timebase) {
        return (Gap) newFlashOrGap(false, duration, timebase);
    }

    private final int gapDuration;
    private final int flashDuration;

    Burst(int mark, int space) {
        gapDuration = space;
        flashDuration = mark;
    }

    /**
     * @return the spaceDuration
     */
    public int getGapDuration() {
        return gapDuration;
    }

    /**
     * @return the markDuration
     */
    public int getFlashDuration() {
        return flashDuration;
    }

    public BareIrStream toBareIrStream(double timebase) {
        List<IrStreamItem> items = new ArrayList<>(2);
        Flash flash = newFlash(flashDuration, timebase);
        items.add(flash);
        Gap gap = newGap(gapDuration, timebase);
        items.add(gap);
        return new BareIrStream(items);
    }

    @Override
    public String toString() {
        return toBareIrStream(0).toIrpString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Burst))
            return false;

        return flashDuration == ((Burst) obj).flashDuration
                && gapDuration == ((Burst) obj).gapDuration;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + this.gapDuration;
        hash = 67 * hash + this.flashDuration;
        return hash;
    }
}
