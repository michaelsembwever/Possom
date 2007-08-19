/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 *
 * Jul 26, 2007 9:12:23 AM
 */
package no.sesat.searchportal.view.navigation;

import static no.sesat.searchportal.site.config.AbstractDocumentFactory.fillBeanProperty;
import static no.sesat.searchportal.site.config.AbstractDocumentFactory.ParseType;
import no.sesat.searchportal.view.navigation.NavigationConfig.Nav.ControllerFactory;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:magnus.eklund@sesam.no">Magnus Eklund</a>
 */
@ControllerFactory("no.sesat.searchportal.view.navigation.ResultPagingNavigationController")
public final class ResultPagingNavigationConfig extends NavigationConfig.Nav {

    private int pageSize;
    private int numberOfPages;
    private String commandName;

    public ResultPagingNavigationConfig(final NavigationConfig.Nav parent, final NavigationConfig.Navigation navigation, final Element e) {
        super(parent, navigation, e);

        fillBeanProperty(this, null, "pageSize", ParseType.Int, e, "10");
        fillBeanProperty(this, null, "numberOfPages", ParseType.Int, e, "10");
        fillBeanProperty(this, null, "commandName", ParseType.String, e, null);
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

}
