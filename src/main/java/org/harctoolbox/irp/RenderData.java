package org.harctoolbox.irp;

import java.util.ArrayList;
import java.util.List;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;

public class RenderData extends Traverser {

    private final NameEngine nameEngine;
    private final GeneralSpec generalSpec;
    private final List<EvaluatedIrStream> evaluatedIrStreamList;

    public RenderData(NameEngine nameEngine, GeneralSpec generalSpec) {
        super(IrSignal.Pass.intro);
        this.nameEngine = nameEngine;
        this.generalSpec = generalSpec;
        evaluatedIrStreamList = new ArrayList<>(2);
    }

    private EvaluatedIrStream currentEvaluatedIrStream() {
        return evaluatedIrStreamList.get(evaluatedIrStreamList.size() - 1);
    }

    IrSequence toIrSequence() throws UnassignedException, IrpSemanticException {
        return currentEvaluatedIrStream().toIrSequence();
    }

    void add(Duration duration) {
        currentEvaluatedIrStream().add(duration);
    }

    void add(BitStream bitStream) {
        currentEvaluatedIrStream().add(bitStream);
    }

    void add(EvaluatedIrStream evalStream) {
        currentEvaluatedIrStream().add(evalStream);
    }

    public final void push() {
        EvaluatedIrStream evalIrStream = evaluatedIrStreamList.isEmpty()
                ? new EvaluatedIrStream(nameEngine, generalSpec, IrSignal.Pass.intro)
                : new EvaluatedIrStream(currentEvaluatedIrStream());
        evaluatedIrStreamList.add(evalIrStream);
    }

    public void pop() {
        if (evaluatedIrStreamList.size() > 1) {
            EvaluatedIrStream evalIrStream = currentEvaluatedIrStream();
            evaluatedIrStreamList.remove(evaluatedIrStreamList.size() - 1);
            currentEvaluatedIrStream().add(evalIrStream);
        }
    }

    /**
     * @return the nameEngine
     */
    public NameEngine getNameEngine() {
        return nameEngine;
    }

    /**
     * @return the generalSpec
     */
    public GeneralSpec getGeneralSpec() {
        return generalSpec;
    }

    void reduce(BitSpec bitStream) throws UnassignedException, InvalidNameException {
        currentEvaluatedIrStream().reduce(bitStream);
    }

    @Override
    public void preprocess(IrStreamItem item, IrSignal.Pass pass, List<BitSpec> bitSpecs) throws IrpSignalParseException, NameConflictException, IrpSemanticException, InvalidNameException, UnassignedException {
        item.prerender(this, pass, bitSpecs);
    }

    @Override
    public void postprocess(IrStreamItem item, IrSignal.Pass pass, List<BitSpec> bitSpecs) throws IrpSignalParseException, NameConflictException, IrpSemanticException, InvalidNameException, UnassignedException {
        item.render(this, pass, bitSpecs);
    }
}
