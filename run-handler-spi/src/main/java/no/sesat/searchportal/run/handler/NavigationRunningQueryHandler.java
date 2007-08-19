package no.sesat.searchportal.run.handler;

import no.sesat.searchportal.datamodel.DataModel;
import no.sesat.searchportal.datamodel.generic.StringDataObject;
import no.sesat.searchportal.datamodel.navigation.NavigationDataObject;
import no.sesat.searchportal.view.navigation.NavigationConfig;
import no.sesat.searchportal.view.NavigationControllerSpiFactory;
import no.sesat.searchportal.view.navigation.NavigationControllerFactory;
import no.sesat.searchportal.view.navigation.NavigationController;
import no.sesat.searchportal.result.NavigationItem;
import no.sesat.searchportal.site.Site;
import no.sesat.searchportal.site.SiteContext;
import no.sesat.searchportal.site.config.BytecodeLoader;
import org.apache.log4j.Logger;

import java.util.List;
import no.sesat.searchportal.datamodel.DataModel;
import no.sesat.searchportal.datamodel.navigation.NavigationDataObject;

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
