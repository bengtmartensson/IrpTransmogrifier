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

package org.harctoolbox.irp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.IrSignal.Pass;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements Irstream as of Chapter 6.
 *
 */
public class IrStream extends BareIrStream implements AggregateLister {

    private static final Logger logger = Logger.getLogger(IrStream.class.getName());

    private RepeatMarker repeatMarker; // must not be null!

    public IrStream(String str) throws IrpSyntaxException, InvalidRepeatException{
        this(new ParserDriver(str).getParser().irstream());
    }

    public IrStream(IrpParser.IrstreamContext ctx) throws IrpSyntaxException, InvalidRepeatException {
        super(ctx.bare_irstream());
        IrpParser.Repeat_markerContext ctxRepeatMarker = ctx.repeat_marker();
        repeatMarker = ctxRepeatMarker != null ? new RepeatMarker(ctxRepeatMarker) : new RepeatMarker();
    }

    public IrStream(List<IrStreamItem> irStreamItems, RepeatMarker repeatMarker) {
        super(irStreamItems);
        this.repeatMarker = repeatMarker;
    }

    public IrStream(List<IrStreamItem> irStreamItems) {
        this(irStreamItems, new RepeatMarker());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IrStream))
            return false;

        IrStream other = (IrStream) obj;

        return super.equals(obj) && repeatMarker.equals(other.repeatMarker);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.repeatMarker);
        return hash;
    }

    public RepeatMarker getRepeatMarker() {
        return repeatMarker;
    }

    private int getMinRepeats() {
        return repeatMarker.getMin();
    }

    private boolean isInfiniteRepeat() {
        return repeatMarker.isInfinite();
    }

    @Override
    EvaluatedIrStream evaluate(IrSignal.Pass state, IrSignal.Pass pass, NameEngine nameEngine, GeneralSpec generalSpec)
            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        IrpUtils.entering(logger, "evaluate", this);
        int repetitions = evaluateTheRepeat(pass) ? 1 : getMinRepeats();
        EvaluatedIrStream result = evaluate(evaluateTheRepeat(pass) ? IrSignal.Pass.repeat : state, pass, nameEngine, generalSpec, repetitions);
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
        return hasVariation(false) ? pass
                : (pass == IrSignal.Pass.repeat && isInfiniteRepeat()) ? IrSignal.Pass.repeat
                : null;
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
        return "(" + super.toIrpString() + ")" + repeatMarker.toIrpString();
    }

    public boolean isRepeatSequence() {
        return repeatMarker.isInfinite();
    }

    @Override
    public Element toElement(Document document) {
        //Element element = document.createElement(getClass().getSimpleName());
        Element element = super.toElement(document);
        //element.appendChild(bareIrStream);
        if (repeatMarker.getMin() != 1)
            element.setAttribute("repeatMin", Integer.toString(repeatMarker.getMin()));
        if (repeatMarker.getMax() != 1)
            element.setAttribute("repeatMax", repeatMarker.isInfinite() ? "infinite" : Integer.toString(repeatMarker.getMax()));
        element.setAttribute("isRepeat", Boolean.toString(isRepeatSequence()));
        element.setAttribute("numberOfBitSpecs", Integer.toString(numberOfBitSpecs()));
        if (numberOfBits() >= 0)
            element.setAttribute("numberOfBits", Integer.toString(numberOfBits()));
        element.setAttribute("numberOfBareDurations", Integer.toString(numberOfBareDurations()));
//        for (IrStreamItem item : irStreamItems)
//            element.appendChild(item.toElement(document));

        //Element intro = document.createElement("Intro");
        //element.appendChild(intro);
        //Element repeat = document.createElement("Repeat");
        //element.appendChild(repeat);
        //Element ending = document.createElement("Ending");
        //element.appendChild(ending);

//        if (!isRepeatSequence()) {
//            Element current = intro;
//            int bareDurations = 0;
//            int bits = 0;
//            for (IrStreamItem item : irStreamItems) {
//                if (item instanceof IrStream && ((IrStream) item).isRepeatSequence()) {
//                    intro.setAttribute("numberOfBits", Integer.toString(bits));
//                    intro.setAttribute("numberOfBareDurations", Integer.toString(bareDurations));
//                    bits = 0;
//                    bareDurations = 0;
//                    repeat.appendChild(item.toElement(document));
//                    repeat.setAttribute("numberOfBareDurations", Integer.toString(item.numberOfBareDurations()));
//                    repeat.setAttribute("numberOfBits", Integer.toString(item.numberOfBits()));
//                    current = ending;
//
//                } else {
//                    current.appendChild(item.toElement(document));
//                    bareDurations += item.numberOfBareDurations();
//                    bits += item.numberOfBits();
//                }
//            }
//            ending.setAttribute("numberOfBits", Integer.toString(bits));
//            ending.setAttribute("numberOfBareDurations", Integer.toString(bareDurations));
//        } else {
//            for (IrStreamItem item : irStreamItems)
//                repeat.appendChild(item.toElement(document));
//        }

        if (!repeatMarker.isTrivial())
            element.appendChild(repeatMarker.toElement(document));

        return element;
    }

    public Element toElement(Document document, Pass pass) {
        Element element = super.toElement(document);
//        boolean evaluateTheRepeat = pass == IrSignal.Pass.repeat && isInfiniteRepeat();
//        int repetitions = evaluateTheRepeat ? 1 : getMinRepeats();
//        if (evaluateTheRepeat)
//            recognizeData.setState(IrSignal.Pass.repeat);
//        boolean status = recognize(recognizeData, pass, bitSpecs, repetitions);
        return element;
    }

    @Override
    int numberOfBareDurations() {
        int sum = 0;
        sum = getIrStreamItems().stream().map((item) -> item.numberOfBareDurations()).reduce(sum, Integer::sum);
        return sum;
    }

    @Override
    int numberOfBits() {
        int sum = 0;
        sum = getIrStreamItems().stream().map((item) -> item.numberOfBits()).reduce(sum, Integer::sum);
        return sum;
    }

    @Override
    public int numberOfInfiniteRepeats() {
        int noir = super.numberOfInfiniteRepeats();
        return repeatMarker.isInfinite() ? noir + 1
                : repeatMarker.getMin() * noir;
    }

    private boolean evaluateTheRepeat(IrSignal.Pass pass) {
        return pass == IrSignal.Pass.repeat && isInfiniteRepeat();
    }

    @Override
    public boolean recognize(RecognizeData recognizeData, IrSignal.Pass pass, List<BitSpec> bitSpecs)
            throws NameConflictException {
        IrpUtils.entering(logger, "recognize " + pass, this);
        //boolean evaluateTheRepeat = pass == IrSignal.Pass.repeat && isInfiniteRepeat();
        int repetitions = evaluateTheRepeat(pass) ? 1 : getMinRepeats();
        if (evaluateTheRepeat(pass))
            recognizeData.setState(IrSignal.Pass.repeat);
        boolean status = recognize(recognizeData, pass, bitSpecs, repetitions);
        IrpUtils.exiting(logger, "recognize " + pass, status ? "pass" : "fail");
        return status;
    }

    private boolean recognize(RecognizeData recognizeData, IrSignal.Pass pass, List<BitSpec> bitSpecs, int repeats)
            throws NameConflictException {
        for (int i = 0; i < repeats; i++) {
            boolean status = super.recognize(recognizeData, pass, bitSpecs);
            if (!status)
                return false;
        }
        return true;
    }

    boolean isRPlus() {
        return repeatMarker.isInfinite() && repeatMarker.getMin() > 0 && ! hasVariation(true);
    }

    @Override
    public int weight() {
        return super.weight() + repeatMarker.weight();
    }
/*
    @Override
    public String code(IrSignal.Pass state, IrSignal.Pass pass, CodeGenerator codeGenerator) {
        //ItemCodeGenerator template = codeGenerator.newItemCodeGenerator("SetOfStatements");
        int repetitions = evaluateTheRepeat(pass) ? 1 : getMinRepeats();
        if (repetitions == 0)
            return null;
        List<String> body = super.codeList(state, pass, codeGenerator);

        //if (repetitions == 1)
        //    return template.render();
        ItemCodeGenerator template = codeGenerator.newItemCodeGenerator(repetitions == 1 ? "SetOfStatements" : "Repeat");
        for (String s : body)
            //template.addAttribute("body", s);
            template.addAttribute("body", s);
        if (repetitions > 1)
            template.addAttribute("repeats", repetitions);
        return template.render();
    }*/

//    public List<String> codeList(IrSignal.Pass state, IrSignal.Pass pass, CodeGenerator codeGenerator) {
//        //ItemCodeGenerator template = codeGenerator.newItemCodeGenerator("SetOfStatements");
//        int repetitions = evaluateTheRepeat(pass) ? 1 : getMinRepeats();
//        if (repetitions == 0)
//            return new ArrayList<>(0);
//
//        String body = super.code(state, pass, codeGenerator);
//        template.addAttribute("body", body);
//        if (repetitions == 1)
//            return template.render();
//
//        ItemCodeGenerator repeatTemplate = codeGenerator.newItemCodeGenerator("Repeat");
//        repeatTemplate.addAttribute("body", template.render());
//        repeatTemplate.addAttribute("repeats", repetitions);
//        return repeatTemplate.render();
//    }
/*
    public String code(Pass pass, CodeGenerator codeGenerator) {
        ItemCodeGenerator template = codeGenerator.newItemCodeGenerator("FunctionBody");
        Pass state = stateWhenEntering(pass) != null ? stateWhenEntering(pass) : IrSignal.Pass.intro;
        String body = code(state, pass, codeGenerator);
        if (body != null && !body.isEmpty())
            template.addAttribute("body", body);
        if (body != null && !body.isEmpty() && hasExtent())
            template.addAttribute("reset", hasExtent());
        return template.render();
    }

    public Map<String, Object> codeMap(Pass pass, CodeGenerator codeGenerator) {
        //ItemCodeGenerator template = codeGenerator.newItemCodeGenerator("FunctionBody");
        Pass state = stateWhenEntering(pass) != null ? stateWhenEntering(pass) : IrSignal.Pass.intro;
        String body = code(state, pass, codeGenerator);
        Map<String, Object> map = new HashMap<>(2);
        if (body != null && !body.isEmpty())
            map.put("body", body);
            //template.addAttribute("body", body);
        if (body != null && !body.isEmpty() && hasExtent())
            //template.addAttribute("reset", hasExtent());
            map.put("reset", hasExtent());
        return map;
        //return template.render();
    }*/

//    public List<String> codeList(Pass pass, CodeGenerator codeGenerator) {
//        // ItemCodeGenerator template = codeGenerator.newItemCodeGenerator("FunctionBody");
//        Pass state = stateWhenEntering(pass) != null ? stateWhenEntering(pass) : IrSignal.Pass.intro;
//        List<String> body = codeList(state, pass, codeGenerator);
////        template.addAttribute("body", body);
////        if (!body.isEmpty() && hasExtent())
////            template.addAttribute("reset", hasExtent());
////        return template.render();
//        return body;
//    }

    @Override
    public Map<String, Object> propertiesMap(GeneralSpec generalSpec) {
        Map<String, Object> m = new HashMap<>(3);
        m.put("intro", propertiesMap(IrSignal.Pass.intro, generalSpec));
        m.put("repeat", propertiesMap(IrSignal.Pass.repeat, generalSpec));
        m.put("ending", propertiesMap(IrSignal.Pass.ending, generalSpec));
        return m;
    }

    private Map<String, Object> propertiesMap(Pass pass, GeneralSpec generalSpec) {
        Map<String, Object> m = new HashMap<>(3);
        m.put("kind", "FunktionBody");

        Pass state = stateWhenEntering(pass) != null ? stateWhenEntering(pass) : IrSignal.Pass.intro;
        Map<String, Object> body = propertiesMap(state, pass, generalSpec);
        m.put("irStream", body);
//        template.addAttribute("body", body);
//        if (!body.isEmpty() && hasExtent())
//            template.addAttribute("reset", hasExtent());
//        return template.render();
        //if (!body.isEmpty() && hasExtent())
        m.put("reset", hasExtent());
        return m;
    }

    @Override
    public Map<String, Object> propertiesMap(Pass state, Pass pass, GeneralSpec generalSpec) {
        Map<String, Object> m = new HashMap<>(2);
        m.put("kind", "Repeat");

        //ItemCodeGenerator template = codeGenerator.newItemCodeGenerator("SetOfStatements");
        int repetitions = evaluateTheRepeat(pass) ? 1 : getMinRepeats();
        if (repetitions == 0)
            return new HashMap<>(0);
        Map<String, Object> body = super.propertiesMap(state, pass, generalSpec);
//        template.addAttribute("body", body);
//        if (repetitions == 1)
//            return template.render();

        //ItemCodeGenerator repeatTemplate = codeGenerator.newItemCodeGenerator("Repeat");
        m.put("repeatBody", body);
        if (repetitions > 1)
            m.put("repeats", repetitions);
        return m;
    }
}
