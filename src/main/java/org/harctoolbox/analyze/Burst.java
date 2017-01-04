/*
Copyright (C) 2016 Bengt Martensson.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program. If not, see http://www.gnu.org/licenses/.
*/

package org.harctoolbox.analyze;

import java.util.ArrayList;
import java.util.List;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.irp.BareIrStream;
import org.harctoolbox.irp.Duration;
import org.harctoolbox.irp.Extent;
import org.harctoolbox.irp.Flash;
import org.harctoolbox.irp.Gap;
import org.harctoolbox.irp.IrStreamItem;
import org.harctoolbox.irp.IrpUtils;

public class Burst {
    private static final double dafaultMaxRoundingError = 0.3f;
    private static final double defaultMaxUnits = 30f;
    private static final double defaultMaxUs = 10000f;

    private static double maxRoundingError = dafaultMaxRoundingError;
    private static double maxUnits = defaultMaxUnits;
    private static double maxUs = defaultMaxUs;

    private static Duration newFlashOrGap(boolean isFlash, double us, double timebase) {
        double units = timebase > 0 ? us/timebase : IrpUtils.invalid;
        double roundingError = Math.round(units) - units;
        String unit = (units > 0 && units < maxUnits && Math.abs(roundingError) < maxRoundingError) ? ""
                : us < maxUs ? "u"
                : "m";
        double duration = unit.isEmpty() ? Math.round(units)
                : unit.equals("m") ? Math.round(IrCoreUtils.microseconds2milliseconds(us))
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

    public static Flash newFlash(double duration, double timebase) {
        return (Flash) newFlashOrGap(true, duration, timebase);
    }

    public static Gap newGap(double duration, double timebase) {
        return (Gap) newFlashOrGap(false, duration, timebase);
    }

    /**
     * @param aMaxRoundingError the maxRoundingError to set
     */
    public static void setMaxRoundingError(double aMaxRoundingError) {
        maxRoundingError = aMaxRoundingError;
    }

    /**
     * @param aMaxUnits the maxUnits to set
     */
    public static void setMaxUnits(double aMaxUnits) {
        maxUnits = aMaxUnits;
    }

    /**
     * @param aMaxUs the maxUs to set
     */
    public static void setMaxUs(double aMaxUs) {
        maxUs = aMaxUs;
    }

    private final int gapDuration;
    private final int flashDuration;

    Burst(int flash, int gap) {
        gapDuration = gap;
        flashDuration = flash;
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
