/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 */
package no.sesat.search.result.handler;

import no.sesat.search.result.handler.AbstractResultHandlerConfig.Controller;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to combine modifiers into new ones in a new navigator.
 *
 * @author Geir H. Pettersen
 * @version $Id$
 */
@Controller("CombineModifiersResultHandler")
public class CombineModifiersResultHandlerConfig  extends AbstractResultHandlerConfig  {
    private String sourceNavigatorName;
    private String targetNavigatorName;
    private String allModifierName;
    private String defaultModifierName;
    private Map<String, String> modifierMap = new HashMap<String, String>();

    public String getSourceNavigatorName() {
        return sourceNavigatorName;
    }

    public void setSourceNavigatorName(String sourceNavigatorName) {
        this.sourceNavigatorName = sourceNavigatorName;
    }

    public String getTargetNavigatorName() {
        return targetNavigatorName;
    }

    public void setTargetNavigatorName(String targetNavigatorName) {
        this.targetNavigatorName = targetNavigatorName;
    }

    public String getAllModifierName() {
        return allModifierName;
    }

    public void setAllModifierName(String allModifierName) {
        this.allModifierName = allModifierName;
    }

    public String getDefaultModifierName() {
        return defaultModifierName;
    }

    public void setDefaultModifierName(String defaultModifierName) {
        this.defaultModifierName = defaultModifierName;
    }

    public Map<String, String> getModifierMap() {
        return modifierMap;
    }

    public void setModifierMap(Map<String, String> modifierMap) {
        this.modifierMap = modifierMap;
    }

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {
        sourceNavigatorName = element.getAttribute("source-navigator-name");
        targetNavigatorName = element.getAttribute("target-navigator-name");
        String attr = element.getAttribute("all-modifier-name");
        if (attr != null && attr.length() > 0) {
            allModifierName = attr;
        }
        attr = element.getAttribute("default-modifier-name");
        if (attr != null && attr.length() > 0) {
            defaultModifierName = attr;
        }
        List<Element> modifierList = getDirectChildren(element, "modifier");
        for (Element modifierElement : modifierList) {
            final String modifierTargetName = modifierElement.getAttribute("name");
            List<Element> modifierSourceList = getDirectChildren(modifierElement, "source");
            for (Element modifierSource : modifierSourceList) {
                final String modifierSourceName = modifierSource.getAttribute("name");
                modifierMap.put(modifierSourceName, modifierTargetName);
            }
        }
        return this;
    }

    private static List<Element> getDirectChildren(final Element element, final String elementName) {
        final List<Element> children = new ArrayList<Element>();
        if (element != null) {
            final NodeList childNodes = element.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node childNode = childNodes.item(i);
                if (childNode instanceof Element && childNode.getNodeName().equals(elementName)) {
                    children.add((Element) childNode);
                }
            }
        }
        return children;
    }

}
