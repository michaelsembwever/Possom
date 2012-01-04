/* Copyright (2005-2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Jul 26, 2007 9:12:23 AM
 */
package no.sesat.search.view.navigation;

import static no.sesat.search.site.config.AbstractDocumentFactory.fillBeanProperty;
import no.sesat.search.mode.config.SearchConfiguration;
import no.sesat.search.site.config.AbstractDocumentFactory.ParseType;
import no.sesat.search.view.navigation.NavigationConfig.Nav.ControllerFactory;

import org.w3c.dom.Element;

/** Configuration for result paging.
 * Makes it possible to configure pageSize, number of page number thumbnails, and jump forward multiple pages options.
 *
 * @version $Id$
 */
@ControllerFactory("no.sesat.search.view.navigation.ResultPagingNavigationController")
public final class ResultPagingNavigationConfig extends NavigationConfig.Nav {

    /** Used for both the id of the configuration AND the parameter name. **/
    public static final String OFFSET_KEY = SearchConfiguration.DEFAULT_PAGING_PARAMETER;

    private int multiplePageSize;
    private int pageSize;
    private int numberOfPages;
    private String commandName;
    private String hitcountSource;

    public ResultPagingNavigationConfig(
            final NavigationConfig.Nav parent,
            final NavigationConfig.Navigation navigation,
            final Element e) {

        super(parent, navigation, e);

        fillBeanProperty(this, null, "multiplePageSize", ParseType.Int, e, "0");
        fillBeanProperty(this, null, "pageSize", ParseType.Int, e, "10");
        fillBeanProperty(this, null, "numberOfPages", ParseType.Int, e, "10");
        fillBeanProperty(this, null, "commandName", ParseType.String, e, null);
        fillBeanProperty(this, null, "hitcountSource", ParseType.String, e, "");
    }

    public int getMultiplePageSize() {
        return multiplePageSize;
    }

    public void setMultiplePageSize(final int multiplePageSize) {
        this.multiplePageSize = multiplePageSize;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(final int pageSize) {
        this.pageSize = pageSize;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public void setNumberOfPages(final int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public void setCommandName(final String commandName) {
        this.commandName = commandName;
    }

    public String getHitcountSource() {
        return hitcountSource;
    }

    public void setHitcountSource(final String hitcountSource) {
        this.hitcountSource = hitcountSource;
    }
}
