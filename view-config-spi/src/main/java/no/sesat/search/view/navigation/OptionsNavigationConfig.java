/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Jul 25, 2007 11:03:04 AM
 */
package no.sesat.search.view.navigation;

import static no.sesat.search.site.config.AbstractDocumentFactory.fillBeanProperty;
import static no.sesat.search.site.config.AbstractDocumentFactory.ParseType;
import no.sesat.search.view.navigation.NavigationConfig.Nav.ControllerFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathConstants;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.io.Serializable;

/**
 *
 * @author Geir H. Pettersen(T-Rank)
 * @author <a href="mailto:magnus.eklund@sesam.no">Magnus Eklund</a>
 */
@ControllerFactory("no.sesat.search.view.navigation.OptionNavigationController")
public final class OptionsNavigationConfig extends NavigationConfig.Nav {

    private final List<Option> optionsToKeep = new ArrayList<Option>();
    private final List<Option> optionsToDelete = new ArrayList<Option>();
    private final List<Option> optionsToAdd = new ArrayList<Option>();

    private enum Operation {
        ADD,
        KEEP,
        DELETE
    }

    public OptionsNavigationConfig(
            final NavigationConfig.Nav parent,
            final NavigationConfig.Navigation navigation,
            final Element element)
    {
        super(parent, navigation, element);
        try {
            final XPath xPath = XPathFactory.newInstance().newXPath();
            final NodeList optionNodes = (NodeList) xPath.evaluate("config/*", element, XPathConstants.NODESET);
            processOptions(optionNodes);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isVirtual() {
        return true;
    }

    private void processOptions(NodeList optionNodes) {
        for (int i = 0; i < optionNodes.getLength(); i++) {
            final Element optionElement = (Element) optionNodes.item(i);

            final Option option = new Option(optionElement);
            final Operation operation = Enum.valueOf(Operation.class, optionElement.getNodeName().toUpperCase());

            switch (operation) {
                case ADD:
                    optionsToAdd.add(option);
                    break;
                case KEEP:
                    optionsToKeep.add(option);
                    break;
                case DELETE:
                    optionsToDelete.add(option);
                    break;
                default:
                    break;
            }
        }
    }

    public Collection<Option> getOptionsToKeep() {
        return Collections.unmodifiableCollection(optionsToKeep);
    }

    public Collection<Option> getOptionsToDelete() {
        return Collections.unmodifiableCollection(optionsToDelete);
    }

    public Collection<Option> getOptionsToAdd() {
        return Collections.unmodifiableCollection(optionsToAdd);
    }

    public class Option implements Serializable {

        private String value;
        private String displayName;
        private String valueRef;
        private boolean defaultSelect;
        private String defaultSelectValueRef;
        private String tab;
        private boolean useHitCount;
        private String commandName;

        public Option(final Element e) {
            fillBeanProperty(this, null, "value", ParseType.String, e, null);
            fillBeanProperty(this, null, "valueRef", ParseType.String, e, null);
            fillBeanProperty(this, null, "defaultSelect", ParseType.Boolean, e, "false");
            fillBeanProperty(this, null, "defaultSelectValueRef", ParseType.String, e, null);
            fillBeanProperty(this, null, "useHitCount", ParseType.Boolean, e, "false");
            fillBeanProperty(this, getParent(), "commandName", ParseType.String, e, null);
            fillBeanProperty(this, null, "tab", ParseType.String, e, getParent().getTab());
            fillBeanProperty(this, null, "displayName", ParseType.String, e, getValue());
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getValueRef() {
            return valueRef;
        }

        public void setValueRef(String valueRef) {
            this.valueRef = valueRef;
        }

        public boolean isDefaultSelect() {
            return defaultSelect;
        }

        public void setDefaultSelect(boolean defaultSelect) {
            this.defaultSelect = defaultSelect;
        }

        public String getDefaultSelectValueRef() {
            return defaultSelectValueRef;
        }

        public void setDefaultSelectValueRef(String defaultSelectValueRef) {
            this.defaultSelectValueRef = defaultSelectValueRef;
        }

        public String getTab() {
            return tab;
        }

        public void setTab(String tab) {
            this.tab = tab;
        }

        public boolean isUseHitCount() {
            return useHitCount;
        }

        public void setUseHitCount(boolean useHitCount) {
            this.useHitCount = useHitCount;
        }

        public String getCommandName() {
            return commandName;
        }

        public void setCommandName(String commandName) {
            this.commandName = commandName;
        }
    }
}
