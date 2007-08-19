/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
*
* Jul 20, 2007 1:35:17 PM
*/
package no.sesat.searchportal.view.navigation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static no.sesat.searchportal.site.config.AbstractDocumentFactory.fillBeanProperty;
import static no.sesat.searchportal.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

import no.sesat.searchportal.view.navigation.NavigationConfig.Nav.ControllerFactory;
import no.sesat.searchportal.site.config.AbstractDocumentFactory;

/**
 * 
 * @version $Id$
 */
@ControllerFactory("no.sesat.searchportal.view.navigation.tab.TabNavigationControllerFactory")
public final class TabNavigationConfig extends NavigationConfig.Nav {
        
    private final List<String> commandNames;
    private final List<String> values;
    private final String image;
    private final String template;
    private final String urlSuffix;
    
    @SuppressWarnings("unchecked")
    public TabNavigationConfig(
            final NavigationConfig.Nav parent,
            final NavigationConfig.Navigation navigation,
            final Element navElement) {

        super(parent, navigation, navElement);

        final String commandNames = AbstractDocumentFactory.parseString(navElement.getAttribute("command-names"), null);
        this.commandNames =  null != commandNames 
                ? Collections.unmodifiableList(Arrays.asList(commandNames.split(",")))
                : Collections.EMPTY_LIST;
        
        final String values = AbstractDocumentFactory.parseString(navElement.getAttribute("values"), null);
        this.values = null != values 
                ? Collections.unmodifiableList(Arrays.asList(values.split(",")))
                : null;
        
        image = AbstractDocumentFactory.parseString(navElement.getAttribute("image"), null);
        template = AbstractDocumentFactory.parseString(navElement.getAttribute("template"), null);
        urlSuffix = AbstractDocumentFactory.parseString(navElement.getAttribute("url-suffix"), null);
    }

    public List<String> getCommandNames() {
        return commandNames;
    }

    @Override
    public String getField() {
        return "c";
    }
    
    public List<String> getValues(){
        // XXX expensive to create new array and list each call 
        return null != values ? values : Collections.unmodifiableList(Arrays.asList(new String[]{getTab()}));
    }
        
    public String getImage(){
        return image;
    }
    
    public String getTemplate(){
        return template;
    }

    public String getUrlSuffix(){
        return urlSuffix;
    }
    
    @Override
    public String toString() {
        return "Tab{ id=\"" + getId() + "\" tab=\"" + getTab() + "\"}";
    }
    
}
