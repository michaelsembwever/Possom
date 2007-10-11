/* Copyright (2007) Schibsted Søk AS
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
 */
package no.sesat.search.run.handler;

import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.datamodel.navigation.NavigationDataObject;
import no.sesat.search.view.navigation.NavigationConfig;
import no.sesat.search.view.NavigationControllerSpiFactory;
import no.sesat.search.view.navigation.NavigationControllerFactory;
import no.sesat.search.view.navigation.NavigationController;
import no.sesat.search.result.NavigationItem;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.config.BytecodeLoader;
import org.apache.log4j.Logger;

import java.util.List;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.navigation.NavigationDataObject;

import no.schibstedsok.commons.ioc.ContextWrapper;



/**
 * To help generating navigation urls in the view. I got tired of all
 * the URL handling velocity code. Some of the effects from this is virtually impossible to
 * code in velocity.
 * <p/>
 * As a bonus from using this, you don't need to data-model the commands that only are
 * there for navigation.
 *
 * @author Geir H. Pettersen(T-Rank)
 * @author <a href="mailto:magnus.eklund@sesam.no">Magnus Eklund</a>
 * @version $Id$
 */
public final class NavigationRunningQueryHandler implements RunningQueryHandler{

    private static final Logger LOG = Logger.getLogger(NavigationRunningQueryHandler.class);
   
    private NavigationControllerSpiFactory controllerFactoryFactory;


    public void handleRunningQuery(final Context context) {

        final NavigationControllerSpiFactory.Context cxt = new NavigationControllerSpiFactory.Context() {

            public Site getSite() {
                return context.getSite();
            }

            public BytecodeLoader newBytecodeLoader(SiteContext siteContext, String className, String jarFileName) {
                return context.newBytecodeLoader(siteContext, className, jarFileName);
            }
        };

        this.controllerFactoryFactory = new NavigationControllerSpiFactory(cxt);

        final NavigationController.Context navCxt = ContextWrapper.wrap(NavigationController.Context.class, context);

        // Update the datamodel
        final NavigationDataObject navDO = context.getDataModel().getNavigation();

        if (navDO.getConfiguration() != null) {
            for (final NavigationConfig.Navigation navigation : navDO.getConfiguration().getNavigationList()) {
                processNavs(navigation.getNavList(), context.getDataModel(), navCxt);
            }
        }
    }

    /**
     * Process the navs in a top-down fashion so that children can use the result of their parents.
     *
     * @param navs
     * @param dataModel
     * @param navCxt
     */
    private void processNavs(
            final List<NavigationConfig.Nav> navs,
            final DataModel dataModel,
            final NavigationController.Context navCxt) {

        final NavigationDataObject navDO = dataModel.getNavigation();

        for (final NavigationConfig.Nav nav : navs) {
            final NavigationItem items = getNavigators(dataModel, nav, navCxt);

            // Navs with null id are considered anonymous. These navs typically just modify the result of their
            // parent and will not be found in the navmap.
            if (items != null && nav.getId() != null) {
                navDO.setNavigation(nav.getId(), items);
            }

            processNavs(nav.getChildNavs(), dataModel, navCxt);
        }
    }

    private NavigationItem getNavigators(
            final DataModel datamodel,
            final NavigationConfig.Nav navEntry,
            final NavigationController.Context navCxt) {

        final NavigationControllerFactory<NavigationConfig.Nav> factory
                = controllerFactoryFactory.getController(navEntry);

        return factory.get(navEntry).getNavigationItems(navCxt);
    }
}
