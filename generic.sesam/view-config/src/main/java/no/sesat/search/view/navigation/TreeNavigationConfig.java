/**
 * Copyright (2008) Schibsted Søk AS
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
 *
 */
package no.sesat.search.view.navigation;

import org.w3c.dom.Element;
import no.sesat.search.site.config.AbstractDocumentFactory;
import static no.sesat.search.site.config.AbstractDocumentFactory.fillBeanProperty;

import java.util.Set;
import java.util.HashSet;

/**
 * This class represent a tree element in the views.xml file.
 *
 * Usage:
 *  <navigation url-generator="no.sesat.search.view.navigation.TreeUrlGenerator" prefix="/search/">
 *              <tree id="geonav" hide-parameter="True" name="Hele norge">
 *                  <tree field="countryregion" name="Nord-Norge">
 *                      <tree field="county" name="Finnmark"/>
 *                      <tree field="county" name="Nordland"/>
 *                      <tree field="county" name="Svalbard"/>
 *                      <tree field="county" name="Troms"/>
 *                  </tree>
 *                  <tree field="countryregion" name="Sørlandet">
 *                      <tree field="county" name="Aust-Agder"/>
 *                      <tree field="county" name="Vest-Agder"/>
 *                  </tree>
 *                  ...
 */
@NavigationConfig.Nav.ControllerFactory("no.sesat.search.view.navigation.TreeNavigationController$Factory")
public class TreeNavigationConfig extends NavigationConfig.Nav {

    private String name;
    private String value;
    private boolean hideParameter;
    private Set resetParameter;

    /**
     *
     * @param parent The parent
     * @param navigation The navigation
     * @param navElement
     */
    public TreeNavigationConfig(
            final NavigationConfig.Nav parent,
            final NavigationConfig.Navigation navigation,
            final Element navElement) {
        super(parent, navigation, navElement);

        fillBeanProperty(this, navigation, "name", AbstractDocumentFactory.ParseType.String, navElement, null);
        fillBeanProperty(this, navigation, "value", AbstractDocumentFactory.ParseType.String, navElement, name);
        fillBeanProperty(this, navigation, "hideParameter", AbstractDocumentFactory.ParseType.Boolean, navElement, "false");

        
        if (parent == null) {
            resetParameter = new HashSet<String>();
        } else {
            if(parent instanceof TreeNavigationConfig) {
                resetParameter = ((TreeNavigationConfig)parent).resetParameter;
            }
        }
        
        if(resetParameter!=null) {
            resetParameter.add(getField());
        }
    }

    /**
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name  Name used when displaying this element.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * If not specified the name will be used as default.
     *
     * @return Value used for parameter
     */
    public String getValue() {
        return value;
    }

    /**
     *  If not specified the name will be used as default.
     *
     * @param value Value used for parameter.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     *
     * @return Set of parameter names that should be reset when generating url
     */
    public Set getResetParameter() {
        return resetParameter;
    }

    /**
     * If this element should hide it's parameter or not. Default is false.
     *
     * @return true if this element should not generate a parameter.
     */
    public boolean isHideParameter() {
        return hideParameter;
    }

    /**
     * Set if this element should hide it's parameter. If this is true,
     * then the elements field and value will be skipped when generating
     * url.
     *
     * @param hideParameter
     */
    public void setHideParameter(boolean hideParameter) {
        this.hideParameter = hideParameter;
    }
}
