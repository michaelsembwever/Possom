/* Copyright (2005-2007) Schibsted Søk AS
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
 * Jul 26, 2007 9:12:23 AM
 */
package no.sesat.search.view.navigation;

import static no.sesat.search.site.config.AbstractDocumentFactory.fillBeanProperty;
import static no.sesat.search.site.config.AbstractDocumentFactory.ParseType;
import no.sesat.search.view.navigation.NavigationConfig.Nav.ControllerFactory;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:magnus.eklund@sesam.no">Magnus Eklund</a>
 */
@ControllerFactory("no.sesat.search.view.navigation.ResultPagingNavigationController")
public final class ResultPagingNavigationConfig extends NavigationConfig.Nav {

    private int pageSize;
    private int numberOfPages;
    private String commandName;
    private String hitcountSource;

    public ResultPagingNavigationConfig(final NavigationConfig.Nav parent, final NavigationConfig.Navigation navigation, final Element e) {
        super(parent, navigation, e);

        fillBeanProperty(this, null, "pageSize", ParseType.Int, e, "10");
        fillBeanProperty(this, null, "numberOfPages", ParseType.Int, e, "10");
        fillBeanProperty(this, null, "commandName", ParseType.String, e, null);
        fillBeanProperty(this, null, "hitcountSource", ParseType.String, e, "");
    }
    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public String getHitcountSource() {
        return hitcountSource;
}
    
    public void setHitcountSource(String hitcountSource) {
        this.hitcountSource = hitcountSource;
    }
}
