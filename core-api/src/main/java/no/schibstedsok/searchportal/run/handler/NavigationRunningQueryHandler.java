package no.schibstedsok.searchportal.run.handler;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import no.schibstedsok.searchportal.datamodel.navigation.NavigationDataObject;
import no.schibstedsok.searchportal.mode.NavigationConfig;
import no.schibstedsok.searchportal.mode.NavigationControllerSpiFactory;
import no.schibstedsok.searchportal.mode.navigation.NavigationControllerFactory;
import no.schibstedsok.searchportal.mode.navigation.NavigationController;
import no.schibstedsok.searchportal.result.NavigationItem;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.config.BytecodeLoader;
import no.schibstedsok.searchportal.site.config.DocumentLoader;
import no.schibstedsok.searchportal.site.config.PropertiesLoader;
import org.apache.log4j.Logger;

import javax.xml.parsers.DocumentBuilder;
import java.util.List;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.datamodel.navigation.NavigationDataObject;

import java.util.Properties;



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
   
    private static final String NAVIGATION_OPTION_VALUE = "NavigationOptionValue";
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

        final NavigationController.Context navCxt = new NavigationController.Context() {

            public DataModel getDataModel() {
                return context.getDataModel();
            }

            public DocumentLoader newDocumentLoader(SiteContext siteCxt, String resource, DocumentBuilder builder) {
                return context.newDocumentLoader(siteCxt, resource, builder);
            }

            public PropertiesLoader newPropertiesLoader(SiteContext siteCxt, String resource, Properties properties) {
                return context.newPropertiesLoader(siteCxt, resource, properties);
            }

            public BytecodeLoader newBytecodeLoader(SiteContext siteContext, String className, String jarFileName) {
                return context.newBytecodeLoader(siteContext, className, jarFileName);
            }

            public Site getSite() {
                return context.getSite();
            }
        };


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

        final NavigationItem items = factory.get(navEntry).getNavigationItems(navCxt);

        boolean selectionDone = false;

        final StringDataObject selectedValue = datamodel.getParameters().getValue(navEntry.getField());

        if (items != null) {
            for (final NavigationItem navigationItem : items.getResults()) {
                if (selectionDone)
                    break;

                if (selectedValue != null && selectedValue.getString().equals(navigationItem.getTitle())) {
                    navigationItem.setSelected(true);
                    selectionDone = true;
                }
//
// magnus had removed this <<<<<<< .mine
//                if (value != null) {
//                    final NavigationItem navigator = new BasicNavigationItem(
//                            option.getDisplayName(),
//                            NavigationHelper.getUrlFragment(datamodel, navEntry, value, null),
//                            -1);
//                    extendedNavigators.addResult(navigator);
//                    if (optionSelectedValue == null && isOptionDefaultSelected(option, searchResult)) {
//                        navigator.setSelected(true);
//                    } else if (optionSelectedValue != null && optionSelectedValue.getString().equals(value)) {
//                        navigator.setSelected(true);
//                    }
//                    if (option.isUseHitCount() && null != option.getCommandName()) {
//                        final SearchDataObject searchDO = datamodel.getSearch(option.getCommandName());
//                        if(null != searchDO){
//                            navigator.setHitCount(searchDO.getResults().getHitCount());
//                        }
//                    }
//                    navigator.addField(NAVIGATION_OPTION_VALUE, value);
//                }
//=======
//>>>>>>> .r5560
            }
        }
        return items;
    }
}
