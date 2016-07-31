/*
Copyright (C) 2011, 2016 Bengt Martensson.

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
package org.harctoolbox.irp;

import java.util.ArrayList;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.IrSignal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements Irstream as of Chapter 6.
 *
 * @author Bengt Martensson
 */
public class IrStream extends BareIrStream {

    private static final Logger logger = Logger.getLogger(IrStream.class.getName());

    private RepeatMarker repeatMarker;
    // I hate the missing default arguments in Java!!!
//    public IrStream(Protocol env) {
//        this(env, null);
//    }

//    public IrStream(Protocol env, ArrayList<IrStreamItem>items) {
//        this(env, items, null, null);
//    }
//
//    public IrStream(Protocol env, ArrayList<IrStreamItem>items, RepeatMarker repeatMarker) {
//        this(env, items, repeatMarker, null);
//    }

    //public IrStream(Protocol env, BareIrStream bareIrStream, RepeatMarker repeatMarker, BitSpec bitSpec) {
    //this(env, bareIrStream != null ? bareIrStream.irStreamItems : null, repeatMarker, bitSpec);
    //}

    //public IrStream(Protocol env, IrStream src, BitSpec bitSpec) {
    //    this(env, src, src != null ? src.repeatMarker : null, bitSpec);
    //}

//    public IrStream(Protocol env, ArrayList<IrStreamItem>items, RepeatMarker repeatMarker, BitSpec bitSpec) {
//        super(env, items, bitSpec, 0);
//        this.repeatMarker = repeatMarker != null ? repeatMarker : new RepeatMarker();
//    }

    public IrStream(String str) throws IrpSyntaxException, InvalidRepeatException{
        this(new ParserDriver(str).getParser().irstream());
    }
    public IrStream(IrpParser.IrstreamContext ctx) throws IrpSyntaxException, InvalidRepeatException {
        super(ctx.bare_irstream());
        IrpParser.Repeat_markerContext ctxRepeatMarker = ctx.repeat_marker();
        repeatMarker = ctxRepeatMarker != null ? new RepeatMarker(ctxRepeatMarker) : null;
    }

    //private ArrayList<PrimaryIrStreamItem> toPrimaryIrStreamItems() {
    //    return toPrimaryIrStreamItems(environment, irStreamItems);
    //}

    /*private static ArrayList<PrimaryIrStreamItem> toPrimaryIrStreamItems(Protocol environment, ArrayList<IrStreamItem> irstreamItems) {
        ArrayList<PrimaryIrStreamItem> primaryItems = new ArrayList<PrimaryIrStreamItem>();
        for (IrStreamItem item : irstreamItems) {
            BitStream bitStream = null;
            String type = item.getClass().getSimpleName();
            if (type.equals("Bitfield")) {
                bitStream = new BitStream(environment);

                bitStream.add((BitField)item, environment.getBitDirection());
            } else if (type.equals("Duration") || type.equals("Extent") || type.equals("IRStream")) {
                primaryItems.add((PrimaryIrStreamItem)item);
            } else {
                throw new RuntimeException("This-cannot-happen-item found: " + type);
            }
            if (bitStream != null) {
                    primaryItems.add(bitStream);
                    bitStream = null;
            }
        }
        return primaryItems;
    }*/

    public RepeatMarker getRepeatMarker() {
        return repeatMarker;
    }

    private int getMinRepeats() {
        return repeatMarker == null ? 1 : repeatMarker.getMin();
    }

    private boolean isInfiniteRepeat() {
        return repeatMarker != null && repeatMarker.isInfinite();
    }

    @Override
    EvaluatedIrStream evaluate(IrSignal.Pass state, IrSignal.Pass pass, NameEngine nameEngine, GeneralSpec generalSpec)
            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        IrpUtils.entering(logger, "evaluate", this);
        boolean evaluateTheRepeat = pass == IrSignal.Pass.repeat && isInfiniteRepeat();
        int repetitions = evaluateTheRepeat ? 1 : getMinRepeats();
        EvaluatedIrStream result = evaluate(evaluateTheRepeat ? IrSignal.Pass.repeat : state, pass, nameEngine, generalSpec, repetitions);
        IrpUtils.exiting(logger, "evaluate", result);
        return result;
    }

    private EvaluatedIrStream evaluate(IrSignal.Pass state, IrSignal.Pass pass, NameEngine nameEngine, GeneralSpec generalSpec, int repeats)
            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        IrSignal.Pass actualState = state;
        EvaluatedIrStream result = new EvaluatedIrStream(nameEngine, generalSpec, pass);
        for (int i = 0; i < repeats; i++) {
            EvaluatedIrStream irSequence = super.evaluate(actualState, pass, nameEngine, generalSpec);
            if (irSequence.getState() != null) {
                actualState = irSequence.getState();
                result.setState(actualState);
            }
            result.add(irSequence);
        }
        return result;
    }

    @Override
    public IrSignal.Pass stateWhenEntering(IrSignal.Pass pass) {
        return (pass == IrSignal.Pass.repeat && isInfiniteRepeat()) ? IrSignal.Pass.repeat : null;
    }

    @Override
    public IrSignal.Pass stateWhenExiting(IrSignal.Pass pass) {
        return isInfiniteRepeat() ? IrSignal.Pass.ending : null;
    }


    @Override
    public String toString() {
        return toIrpString();
    }

    @Override
    public String toIrpString() {
        return "(" + super.toIrpString() + ")" + (repeatMarker != null ? repeatMarker.toIrpString() : "");
    }

    public boolean isRepeatSequence() {
        return repeatMarker != null && repeatMarker.isInfinite();
    }

    @Override
    public Element toElement(Document document) throws IrpSyntaxException {
        Element element = document.createElement("irstream");
        element.setAttribute("is_repeat", Boolean.toString(isRepeatSequence()));
        element.setAttribute("numberOfBitSpecs", Integer.toString(numberOfBitSpecs()));
        element.setAttribute("numberOfBits", Integer.toString(numberOfBits()));
        element.setAttribute("numberOfBareDurations", Integer.toString(numberOfBareDurations()));
        Element intro = document.createElement("intro");
        element.appendChild(intro);
        Element repeat = document.createElement("repeat");
        element.appendChild(repeat);
        Element ending = document.createElement("ending");
        element.appendChild(ending);

        if (!isRepeatSequence()) {
            Element current = intro;
            int bareDurations = 0;
            int bits = 0;
            for (IrStreamItem item : irStreamItems) {
                if (item instanceof IrStream && ((IrStream) item).isRepeatSequence()) {
                    intro.setAttribute("numberOfBits", Integer.toString(bits));
                    intro.setAttribute("numberOfBareDurations", Integer.toString(bareDurations));
                    bits = 0;
                    bareDurations = 0;
                    repeat.appendChild(item.toElement(document));
                    repeat.setAttribute("numberOfBareDurations", Integer.toString(item.numberOfBareDurations()));
                    repeat.setAttribute("numberOfBits", Integer.toString(item.numberOfBits()));
                    current = ending;

                } else {
                    current.appendChild(item.toElement(document));
                    bareDurations += item.numberOfBareDurations();
                    bits += item.numberOfBits();
                }
            }
            ending.setAttribute("numberOfBits", Integer.toString(bits));
            ending.setAttribute("numberOfBareDurations", Integer.toString(bareDurations));
        } else {
            for (IrStreamItem item : irStreamItems)
                repeat.appendChild(item.toElement(document));
        }

        if (repeatMarker != null)
            element.appendChild(repeatMarker.toElement(document));
        return element;
    }

    @Override
    int numberOfBareDurations() {
        int sum = 0;
        sum = irStreamItems.stream().map((item) -> item.numberOfBareDurations()).reduce(sum, Integer::sum);
        return sum;
    }

    @Override
    int numberOfBits() {
        int sum = 0;
        sum = irStreamItems.stream().map((item) -> item.numberOfBits()).reduce(sum, Integer::sum);
        return sum;
    }

    @Override
    public int numberOfInfiniteRepeats() {
        int noir = super.numberOfInfiniteRepeats();
        return repeatMarker == null ? noir
                : repeatMarker.isInfinite() ? noir + 1
                : repeatMarker.getMin() * noir;
    }

    @Override
    public RecognizeData recognize(RecognizeData initData, IrSignal.Pass pass, GeneralSpec generalSpec, ArrayList<BitSpec> bitSpecs) throws NameConflictException {
        //IrSignal.Pass actualState = state;
        IrpUtils.entering(logger, "recognize", this);
        boolean evaluateTheRepeat = pass == IrSignal.Pass.repeat && isInfiniteRepeat();
        int repetitions = evaluateTheRepeat ? 1 : getMinRepeats();
        RecognizeData actualData = initData.clone();
        if (evaluateTheRepeat)
            actualData.setState(IrSignal.Pass.repeat);
        RecognizeData recognizeData = recognize(actualData, pass, generalSpec, bitSpecs, repetitions);
        IrpUtils.exiting(logger, "recognize", recognizeData != null ? recognizeData.toString() : "null");
        return recognizeData;
    }

    private RecognizeData recognize(RecognizeData initData, IrSignal.Pass pass,
            GeneralSpec generalSpec, ArrayList<BitSpec> bitSpec, int repeats) throws NameConflictException {
        IrSignal.Pass state = initData.getState();
        int position = initData.getStart();
        NameEngine nameEngine = initData.getNameEngine();
        //EvaluatedIrStream result = new EvaluatedIrStream(nameEngine, generalSpec, pass);
        for (int i = 0; i < repeats; i++) {
//            RecognizeData data = initData.clone();
//            data.setStart(position);
//            data.setState(state);
//            data.set
            RecognizeData data = new RecognizeData(initData.getIrSequence(), position, 0, state, nameEngine);
            RecognizeData recognizeData = super.recognize(data, pass, generalSpec, bitSpec);
            if (recognizeData == null)
                return null;
            nameEngine.addBarfByConflicts(recognizeData.getNameEngine());
            position += recognizeData.getLength();
            //if (irSequence.getState() != null) {
            //    actualState = irSequence.getState();
            //    result.setState(actualState);
            //}
            //result.add(irSequence);
        }
        return new RecognizeData(initData.getIrSequence(), initData.getStart(), position - initData.getStart(), state, nameEngine);
    }
}
