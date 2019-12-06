package org.harctoolbox.example;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.*;
import org.harctoolbox.irp.*;
import org.harctoolbox.irp.Decoder.Decode;
import org.harctoolbox.irp.Decoder.DecoderParameters;
import org.w3c.dom.*;

public class directv2 {

    public static void main(String[] args) {
        try {
            IrSignal irSignal = Pronto.parse("0000 006D 000A 000A 00E4 002E 002E 002E 0017 0017 0017 0017 002E 0017 0017 0017 002E 0017 002E 002E 0017 0017 0017 0474 0072 002E 002E 002E 0017 0017 0017 0017 002E 0017 0017 0017 002E 0017 002E 002E 0017 0017 0017 0474");
            DecoderParameters decoderParameters = new DecoderParameters();
            IrpDatabase irpDatabase = new IrpDatabase((String) null);
            ExecutorWrapperDatabase executorWrapperDatabase = new ExecutorWrapperDatabase(irpDatabase);
            Decoder decoder = new Decoder(irpDatabase);
            Decoder.SimpleDecodesSet sigDecodes = decoder.decodeIrSignal(irSignal, decoderParameters);
            for (Decode decode : sigDecodes) {
                System.out.println("Orig: " + decode);
                NamedProtocol namedProtocol = decode.getNamedProtocol();
                String protocolName = namedProtocol.getName();
                Map<String, Long> names = decode.getMap();
                NameEngine nameEngine = new NameEngine(names);
                List<ExecutorWrapper> wrapperList = executorWrapperDatabase.get(protocolName);
                if (wrapperList == null)
                    continue;
                for (ExecutorWrapper ew : wrapperList) {
                    Decode newDecode = ew.fixDecode(protocolName, namedProtocol, nameEngine);
                    System.out.println("Fixed: " + newDecode);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static class ExecutorWrapper {

        private static final String RM_NAMESPACE = "http://www.hifi-remote.com/forums/viewtopic.php?t=101943";

        static List<ExecutorWrapper> parseList(List<DocumentFragment> exec) {
            List<ExecutorWrapper> result = new ArrayList<>(16);
            exec.forEach((fragment) -> {
                result.add(new ExecutorWrapper(fragment));
            });
            return result;
        }

        private String protocolName;
        private final Map<String, Expression> assignments;

        ExecutorWrapper(DocumentFragment fragment) {
            assignments = new LinkedHashMap<>(4); // Important: keeps the order things are put in.
            protocolName = null;
            NodeList children = fragment.getChildNodes();

            // Finding the deployment element
            Element deployment = null;
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE)
                    continue;
                Element e = (Element) node;
                if (e.getLocalName().equals("deployment")) {
                    deployment = e;
                    break;
                }
            }
            if (deployment == null)
                return;

            // Getting the assignments
            NodeList assignmentNodes = deployment.getElementsByTagNameNS(RM_NAMESPACE, "assignment");
            for (int i = 0; i < assignmentNodes.getLength(); i++) {
                Element e = (Element) assignmentNodes.item(i);

                // Get and parse the Expression contained therein
                Expression exp = Expression.newExpression(e.getTextContent());

                // and stuff it into the map
                String paramName = e.getAttribute("target");
                assignments.put(paramName, exp);
            }

            NodeList nl = deployment.getElementsByTagNameNS(RM_NAMESPACE, "protocolName");
            if (nl.getLength() > 0)
                protocolName = nl.item(0).getTextContent();
        }

        void fixParameters(NameEngine nameEngine) {
            assignments.entrySet().forEach((assgnmnt) -> {
                try {
                    String paramName = assgnmnt.getKey();
                    long value = assgnmnt.getValue().toLong(nameEngine);
                    nameEngine.define(paramName, value);
                } catch (NameUnassignedException | InvalidNameException ex) {
                    Logger.getLogger(directv2.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }

        Decode fixDecode(String protocolName, NamedProtocol namedProtocol, NameEngine nameEngine) throws InvalidNameException, UnsupportedRepeatException, NameUnassignedException, IrpInvalidArgumentException {
            if (this.protocolName != null)
                protocolName = this.protocolName;
            fixParameters(nameEngine);
            NamedProtocol np = new NamedProtocol(protocolName, namedProtocol.getIrp(), null); // inefficient...
            Decode fixedDecode = new Decode(np, nameEngine.toMap(), -1, -1, 0);
            return fixedDecode;
        }
    }

    private static class ExecutorWrapperDatabase {

        private final Map<String, List<ExecutorWrapper>> map;

        ExecutorWrapperDatabase(IrpDatabase irpDatabase) {
            map = new HashMap<>(16);
            irpDatabase.getKeys().forEach((protName) -> {
                List<DocumentFragment> exec = irpDatabase.getXmlProperties(protName, "uei-executor");
                if (exec != null) {
                    List<ExecutorWrapper> lies = ExecutorWrapper.parseList(exec);
                    map.put(protName, lies);
                }
            });
        }

        List<ExecutorWrapper> get(String name) {
            return map.get(name.toLowerCase(Locale.US));
        }
    }
}
