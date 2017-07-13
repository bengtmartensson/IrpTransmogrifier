/*
Copyright (C) 2017 Bengt Martensson.

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

public final class Burst {

    static Integer multiplier(double us, Double timebase, Preferences prefs) {
        if (timebase == null)
            return null;

        double units = us/timebase;
        int rounded = (int) Math.round(units);
        double roundingError = Math.round(units) - units;
        boolean ok = units < prefs.getMaxUnits() && Math.abs(roundingError) < prefs.getMaxRoundingError();
        return ok ? rounded : null;
    }

    private static String unitString(Integer mult, double us, Preferences prefs) {
        return mult != null ? ""
                : us < prefs.getMaxMicroSeconds() ? "u"
                : "m";
    }

    private static Duration newFlashOrGap(boolean isFlash, double us, Double timebase, Preferences prefs) {
        Integer mult = multiplier(us, timebase, prefs);
        String unit = unitString(mult, us, prefs);
        double duration = mult != null ? mult.doubleValue()
                : unit.equals("m") ? IrCoreUtils.microseconds2milliseconds(us)
                : us;
        return isFlash ? new Flash(duration, unit) : new Gap(duration, unit);
    }

    public static Extent newExtent(int us, Double timebase, Preferences prefs) {
        Integer mult = multiplier(us, timebase, prefs);
        String unit = unitString(mult, us, prefs);
        double duration = unit.isEmpty() && mult != null ? mult.doubleValue()
                : unit.equals("m") ? Math.round(IrCoreUtils.microseconds2milliseconds(us))
                : us;
        return new Extent(duration, unit);
    }

    public static Flash newFlash(double duration, Double timebase, Preferences prefs) {
        return (Flash) newFlashOrGap(true, duration, timebase, prefs);
    }

    public static Gap newGap(double duration, Double timebase, Preferences prefs) {
        return (Gap) newFlashOrGap(false, duration, timebase, prefs);
    }

    public static int compare(Burst burst1, Burst burst2) {
        return burst1.compare(burst2);
    }

    private final int gapDuration;
    private final int flashDuration;

    Burst(int flash, int gap) {
        gapDuration = gap;
        flashDuration = flash;
    }

    /**
     * @return the gapDuration
     */
    public int getGapDuration() {
        return gapDuration;
    }

    /**
     * @return the flashDuration
     */
    public int getFlashDuration() {
        return flashDuration;
    }

    public BareIrStream toBareIrStream(double timebase, Preferences prefs) {
        List<IrStreamItem> items = new ArrayList<>(2);
        Flash flash = newFlash(flashDuration, timebase, prefs);
        items.add(flash);
        Gap gap = newGap(gapDuration, timebase, prefs);
        items.add(gap);
        return new BareIrStream(items);
    }

    @Override
    public String toString() {
        return toBareIrStream(0, new Preferences()).toIrpString(10);
    }

    public String toString(Preferences prefs) {
        return toBareIrStream(0, prefs).toIrpString(10);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Burst))
            return false;

        return flashDuration == ((Burst) obj).flashDuration
                && gapDuration == ((Burst) obj).gapDuration;
    }

    public boolean equalsWithLongGap(Burst burst) {
        return flashDuration == burst.flashDuration && gapDuration > burst.gapDuration;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + this.gapDuration;
        hash = 67 * hash + this.flashDuration;
        return hash;
    }

    private int sum() {
        return flashDuration + gapDuration;
    }

    public int compare(Burst burst) {
        int sumCompare = sum() - burst.sum();
        return sumCompare != 0 ? sumCompare : flashDuration - burst.flashDuration;
    }

    public int overhang(Burst burst) {
         return gapDuration - burst.gapDuration;
    }

    public final static class Preferences {
        public static final double DEFAULTMAXROUNDINGERROR = 0.3;
        public static final double DEFAULTMAXUNITS = 30.0;
        public static final double DEFAULTMAXMICROSECONDS = 10000.0;

        private double maxRoundingError = DEFAULTMAXROUNDINGERROR;
        private double maxUnits = DEFAULTMAXUNITS;
        private double maxMicroSeconds = DEFAULTMAXMICROSECONDS;

        public Preferences(double maxRoundingError, double maxUnits, double maxMicroSeconds) {
            this.maxRoundingError = maxRoundingError;
            this.maxUnits = maxUnits;
            this.maxMicroSeconds = maxMicroSeconds;
        }

        public Preferences() {
            this.maxRoundingError = DEFAULTMAXROUNDINGERROR;
            this.maxUnits = DEFAULTMAXUNITS;
            this.maxMicroSeconds = DEFAULTMAXMICROSECONDS;
        }

        @Override
        public String toString() {
            return "{"
                    + Double.toString(maxRoundingError) + ", "
                    + Double.toString(maxUnits) + ", "
                    + Double.toString(maxMicroSeconds) + "}";
        }

        /**
         * @return the maxRoundingError
         */
        public double getMaxRoundingError() {
            return maxRoundingError;
        }

        /**
         * @return the maxUnits
         */
        public double getMaxUnits() {
            return maxUnits;
        }

        /**
         * @return the maxMicroSeconds
         */
        public double getMaxMicroSeconds() {
            return maxMicroSeconds;
        }
    }
}
