/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
*
* Jul 20, 2007 1:35:17 PM
*/
package no.schibstedsok.searchportal.mode.navigation;

import static no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.fillBeanProperty;
import static no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

import no.schibstedsok.searchportal.mode.NavigationConfig;

/**
 * 
 * @author Geir H. Pettersen(T-Rank)
 * @author <a href="mailto:magnus.eklund@sesam.no">Magnus Eklund</a>
 */
@NavigationConfig.Nav.ControllerFactory("no.schibstedsok.searchportal.mode.navigation.fast.FastNavigationControllerFactory")
public class FastNavigationConfig extends NavigationConfig.Nav {
    
    private String commandName;
    
    public FastNavigationConfig(
            final NavigationConfig.Nav parent,
            final NavigationConfig.Navigation navigation,
            final Element navElement) {

        super(parent, navigation, navElement);

        /* TODO: temporarily inherit from navigation to provide backward compatibility. remove command-name from navigation */ 
        fillBeanProperty(this,navigation, "commandName", ParseType.String, navElement, null);
    }

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }
}
