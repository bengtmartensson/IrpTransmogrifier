package org.harctoolbox.analyze;

import java.util.ArrayList;
import java.util.List;
import org.harctoolbox.irp.BitDirection;
import org.harctoolbox.irp.IrStreamItem;

public class PwmDecoder extends AbstractDecoder {

    private final Burst zero;
    private final Burst one;

    public PwmDecoder(Analyzer analyzer, int timebase, Burst zero, Burst one) {
        super(analyzer, timebase, mkBitSpec(zero, one, timebase));
        this.zero = zero;
        this.one = one;
    }

    public PwmDecoder(Analyzer analyzer, int timebase, int zeroGap, int zeroFlash, int oneGap, int oneFlash) {
        this(analyzer, timebase, new Burst(zeroGap, zeroFlash), new Burst(oneGap, oneFlash));
    }

    public PwmDecoder(Analyzer analyzer, int timebase, int a, int b) {
        this(analyzer, timebase, a, a, a, b);
    }

    @Override
    protected List<IrStreamItem> process(int beg, int length, BitDirection bitDirection, boolean useExtents, List<Integer> parameterWidths) {
        List<IrStreamItem> items = new ArrayList<>(16);
        ParameterData data = new ParameterData();
        for (int i = beg; i < beg + length - 1; i += 2) {
            int noBitsLimit = getNoBitsLimit(parameterWidths);
            int mark = analyzer.getCleanedTime(i);
            int space = analyzer.getCleanedTime(i + 1);
            Burst burst = new Burst(mark, space);
            if (burst.equals(zero)) {
                data.update(0);
            } else if (burst.equals(one)) {
                data.update(1);
            } else {
                if (!data.isEmpty()) {
                    saveParameter(data, items, bitDirection);
                    data = new ParameterData();
                }

                items.add(newFlash(mark));
                if (i == beg + length - 2 && useExtents)
                    items.add(newExtent(analyzer.getTotalDuration(beg, length)));
                else
                    items.add(newGap(space));
            }

            if (data.getNoBits() >= noBitsLimit) {
                saveParameter(data, items, bitDirection);
                data = new ParameterData();
            }
        }
        if (!data.isEmpty())
            saveParameter(data, items, bitDirection);

        return items;
    }
}