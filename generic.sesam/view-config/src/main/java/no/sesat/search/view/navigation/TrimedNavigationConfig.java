/** Copyright (2005-2007) Schibsted Søk
 *   This file is part of SESAT.
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
**/
package no.sesat.search.view.navigation;

import static no.sesat.search.site.config.AbstractDocumentFactory.fillBeanProperty;
import no.sesat.search.site.config.AbstractDocumentFactory;
import org.w3c.dom.Element;

/**
 * User: anthor
 * Date: 05.sep.2007
 * Time: 11:51:45
 */

@NavigationConfig.Nav.ControllerFactory("no.sesat.search.view.navigation.TrimedNavigationController")
public class TrimedNavigationConfig extends NavigationConfig.Nav {
   private String commandName;
   private String separator;

    public TrimedNavigationConfig(
            final NavigationConfig.Nav parent,
            final NavigationConfig.Navigation navigation,
            final Element navElement) {

        super(parent, navigation, navElement);

        /* TODO: temporarily inherit from navigation to provide backward compatibility. remove command-name from navigation */
        fillBeanProperty(this,navigation, "commandName", AbstractDocumentFactory.ParseType.String, navElement, null);
        fillBeanProperty(this,navigation, "separator", AbstractDocumentFactory.ParseType.String, navElement, null);
    }

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }


    public String getSeparator() {
        return separator;
    }


    public void setSeparator(String separator) {
        this.separator = separator;
    }
}
