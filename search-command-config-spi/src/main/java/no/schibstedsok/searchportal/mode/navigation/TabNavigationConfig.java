/* Copyright (2005-2007) Schibsted Søk AS
*
* Jul 20, 2007 1:35:17 PM
*/
package no.schibstedsok.searchportal.mode.navigation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.fillBeanProperty;
import static no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

import no.schibstedsok.searchportal.mode.NavigationConfig;
import no.schibstedsok.searchportal.mode.NavigationConfig.Nav.ControllerFactory;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;

/**
 * 
 * @version $Id$
 */
@ControllerFactory("no.schibstedsok.searchportal.mode.navigation.tab.TabNavigationControllerFactory")
public final class TabNavigationConfig extends NavigationConfig.Nav {
        
    private final List<String> commandNames;
    private final List<String> values;
    private final String image;
    private final String template;
    
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

    @Override
    public String toString() {
        return "Tab{ id=\"" + getId() + "\" tab=\"" + getTab() + "\"}";
    }
    
}
