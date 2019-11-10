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
package org.harctoolbox.ircore;

import java.util.Locale;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DumbHtmlRenderer {

    private static final int CELLSIZE = 12;

    public static String render(DocumentFragment fragment) {
        if (fragment == null)
            return "";

        String str = render((Node) fragment, true);
        return str.trim();
    }

    private static String render(Node node, boolean preserveSpace) {
        short nodeType = node.getNodeType();
        switch (nodeType) {
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
                return node.getTextContent().replaceAll("\\s+", preserveSpace ? " " : "");

            case Node.ELEMENT_NODE:
                return render((Element) node);

            case Node.DOCUMENT_FRAGMENT_NODE:
            case Node.DOCUMENT_NODE:
                return render(node.getChildNodes(), true);

            default:
                throw new ThisCannotHappenException();
        }
    }

    private static String render(Element element) {
        String tagName = element.getTagName().toLowerCase(Locale.US);
        switch (tagName) {
            case "a":
                return renderA(element);
            case "p":
            case "div":
            case "ul":
                return renderP(element);
            case "li":
                return renderLi(element);
            case "table":
                return renderTable(element);
//            case "thead":
//                return renderThead(element);
            case "tr":
                return renderTr(element);
            case "td":
            case "th":
                return renderTd(element);
            case "br":
                return renderBr(element);
            default:
                return render(element.getChildNodes(), false);
        }
    }

    private static String render(NodeList nodeList, boolean preserveSpace) {
        StringBuilder sb = new StringBuilder(1024);
        for (int i = 0; i < nodeList.getLength(); i++) {
            String str = render(nodeList.item(i), preserveSpace);
            sb.append(str);
        }
        return sb.toString();
    }

    private static String renderA(Element element) {
        String href = element.getAttribute("href");
        String inner = render(element.getChildNodes(), true);
        boolean isLocal = href.startsWith("#");
        return isLocal ? inner : "[" + inner + "](" + href + ")";
    }

    private static String renderP(Element element) {
        return "\n\n" + render(element.getChildNodes(), true).trim();
    }

    private static String renderUl(Element element) {
        return "\n" + render(element.getChildNodes(), true) + "\n";
    }

    private static String renderLi(Element element) {
        return "\n* " + render(element.getChildNodes(), true).trim();
    }

    private static String renderTable(Element element) {
        return "\n" + IrCoreUtils.chars(3 * CELLSIZE + 1, '-') + "\n" + render(element.getChildNodes(), false) + "\n";
    }

    private static String renderTr(Element element) {
        return render(element.getChildNodes(), false) + "|\n" + IrCoreUtils.chars(3 * CELLSIZE + 1, '-') + "\n";
    }

    private static String renderTd(Element element) {
        String inner = render(element.getChildNodes(), false);
        return "| " + IrCoreUtils.spaces(CELLSIZE - 3 - inner.length()) + inner + " ";
    }

    private static String renderBr(Element element) {
        return "\n";
    }

    private DumbHtmlRenderer() {
    }
}
