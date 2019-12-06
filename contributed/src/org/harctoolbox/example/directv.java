/*
Copyright (C) 2019 Bengt Martensson.

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
package org.harctoolbox.example;

import java.util.List;
import java.util.Map;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.Pronto;
import org.harctoolbox.irp.Decoder;
import org.harctoolbox.irp.Decoder.Decode;
import org.harctoolbox.irp.Expression;
import org.harctoolbox.irp.IrpDatabase;
import org.harctoolbox.irp.NameEngine;
import org.harctoolbox.irp.NamedProtocol;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class directv {

    private static final String RM_NAMESPACE = "http://www.hifi-remote.com/forums/viewtopic.php?t=101943";

    public static void main(String[] args) {
        try {
            IrSignal irSignal = Pronto.parse("0000 006D 000A 000A 00E4 002E 002E 002E 0017 0017 0017 0017 002E 0017 0017 0017 002E 0017 002E 002E 0017 0017 0017 0474 0072 002E 002E 002E 0017 0017 0017 0017 002E 0017 0017 0017 002E 0017 002E 002E 0017 0017 0017 0474");
            Decoder.DecoderParameters decoderParameters = new Decoder.DecoderParameters();
            IrpDatabase irpDatabase = new IrpDatabase((String) null);
            Decoder decoder = new Decoder(irpDatabase);
            //Map<String, Decode> sigDecodes;
            Decoder.SimpleDecodesSet sigDecodes = decoder.decodeIrSignal(irSignal, decoderParameters);
            for (Decode decode : sigDecodes) {
                NamedProtocol namedProtocol = decode.getNamedProtocol();
                String name = namedProtocol.getName();
                List<DocumentFragment> properties = irpDatabase.getXmlProperties(name, "uei-executor");
                DocumentFragment executor = properties.get(0);
                NodeList children = executor.getChildNodes();

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

                Map<String, Long> names = decode.getMap();
                NameEngine nameEngine = new NameEngine(names);

                // Getting the assignments
                NodeList assignments = deployment.getElementsByTagNameNS(RM_NAMESPACE, "assignment");
                for (int i = 0; i < assignments.getLength(); i++) {
                    Element e = (Element) assignments.item(i);

                    // Get and parse the Expression contained therein
                    Expression exp = Expression.newExpression(e.getTextContent());

                    // evaluate wrt the known variable values
                    long value = exp.toLong(nameEngine);

                    // and stuff it into the name engine
                    String paramName = e.getAttribute("target");
                    nameEngine.define(paramName, value);
                }

                NodeList nl = deployment.getElementsByTagNameNS(RM_NAMESPACE, "protocolName");
                String protocolName = nl.getLength() > 0 ? ((Element) nl.item(0)).getTextContent() : name;
                NamedProtocol np = new NamedProtocol(protocolName, namedProtocol.getIrp(), null);
                Decode fixedDecode = new Decode(np, nameEngine.toMap(), -1, -1, 0);
                System.out.println(fixedDecode);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
