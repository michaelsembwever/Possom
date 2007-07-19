package no.schibstedsok.searchportal.run.handler;

import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObjectSupport;
import no.schibstedsok.searchportal.mode.NavigationConfig;
import no.schibstedsok.searchportal.result.BasicNavigationItem;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.result.NavigationItem;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.datamodel.navigation.NavigationDataObject;
import no.schibstedsok.searchportal.result.NavigationHelper;

/**
 * To help generating navigation urls in the view. I got tired of all
 * the URL handling velocity code. Some of the effects from this is virtually impossible to
 * code in velocity.
 * <p/>
 * As a bonus from using this, you don't need to data-model the commands that only are
 * there for navigation.
 *
 * @author Geir H. Pettersen(T-Rank)
 * @version $Id$
 */
public final class NavigationRunningQueryHandler implements RunningQueryHandler{

    private static final Logger LOG = Logger.getLogger(NavigationRunningQueryHandler.class);

    public void handleRunningQuery(final Context context) {

        // Update the datamodel
        final NavigationDataObject navDO = context.getDataModel().getNavigation();
        if(null != navDO.getConfiguration()){
            for(String id : navDO.getConfiguration().getNavMap().keySet()){
                navDO.setNavigation(id, getNavigators(context.getDataModel(), id));
            }
        }
    }

    /**
     * Returns extended navigators for a name(id)
     *
     * @param name the id of the navigator to get.
     * @return a list with extended navigators
     */
    private static NavigationItem getNavigators(final DataModel datamodel, final String name) {

        final NavigationConfig.Nav navEntry = NavigationHelper.getConfig(datamodel).getNavMap().get(name);

        if (navEntry != null) {
            boolean selectionDone = false;
            StringDataObject selectedValue = datamodel.getParameters().getValue(name);
            final NavigationItem extendedNavigators = new BasicNavigationItem();
            FastSearchResult fsr = null;

            if (navEntry.getCommandName() != null && datamodel.getSearch(navEntry.getCommandName()) != null) {
                final ResultList<? extends ResultItem> searchResult
                        = datamodel.getSearch(navEntry.getCommandName()).getResults();

                if (searchResult instanceof FastSearchResult) {
                    fsr = (FastSearchResult) searchResult;
                    final List<Modifier> modifiers = fsr.getModifiers(name);

                    if (modifiers != null && modifiers.size() > 0) {
                        for (Modifier modifier : modifiers) {

                            final String navigatorName = modifier.getNavigator() == null
                                    ? null
                                    : modifier.getNavigator().getName();

                            final String urlFragment = NavigationHelper
                                    .getUrlFragment(datamodel, navEntry, modifier.getName(), navigatorName);

                            final NavigationItem navigator
                                    = new BasicNavigationItem(modifier.getName(), urlFragment, modifier.getCount());

                            if (!selectionDone) {
                                selectedValue = datamodel.getParameters().getValue(navEntry.getField());
                                if (selectedValue != null && selectedValue.getString().equals(modifier.getName())) {
                                    navigator.setSelected(true);
                                    selectionDone = true;
                                }
                            }
                            extendedNavigators.addResult(navigator);
                        }
                    }
                }
            }
            getOptionNavigators(datamodel, navEntry, fsr, extendedNavigators, selectedValue);
            return extendedNavigators;
        }

        return null;
    }

    private static void getOptionNavigators(

            final DataModel datamodel,final NavigationConfig.Nav navEntry,
            final FastSearchResult fsr,
            final NavigationItem extendedNavigators,
            StringDataObject selectedValue) {

        // Only used by getNavigators. Mainly to split code.
        if (extendedNavigators.getResults().size() > 0 && navEntry.getOptions().size() > 0) {

            final List<NavigationItem> toRemove = new ArrayList<NavigationItem>();

            // Navigators already collected. Options is override
            for (NavigationItem navigator : extendedNavigators.getResults()) {
                boolean match = false;

                // Double loop to find match in two lists. Not nice, but it works.
                for (NavigationConfig.Option option : navEntry.getOptions()) {
                    final String value = option.getValue();
                    if (navigator.getTitle().equals(value)) {
                        match = true;
                        if (selectedValue == null && isOptionDefaultSelected(option, fsr)) {
                            navigator.setSelected(true);
                            selectedValue = new StringDataObjectSupport("dummy");
                        }
                        if (option.getDisplayName() != null) {
                            navigator.setTitle(option.getDisplayName());
                        }
                    }
                }
                if (!match) {
                    toRemove.add(navigator);
                }
            }
            for(NavigationItem item : toRemove){
                extendedNavigators.removeResult(item);
            }
        } else {
            final StringDataObject optionSelectedValue
                    = datamodel.getParameters().getValue(navEntry.getField());

            for (NavigationConfig.Option option : navEntry.getOptions()) {

                String value = option.getValue();
                if (option.getValueRef() != null && fsr != null) {
                    final String tmp = fsr.getField(option.getValueRef());
                    if (tmp != null && tmp.length() > 0) {
                        value = tmp;
                    }
                }
                if (value != null) {
                    final NavigationItem navigator = new BasicNavigationItem(
                            option.getDisplayName(),
                            NavigationHelper.getUrlFragment(datamodel, navEntry, value, null),
                            -1);
                    extendedNavigators.addResult(navigator);
                    if (optionSelectedValue == null && isOptionDefaultSelected(option, fsr)) {
                        navigator.setSelected(true);
                    } else if (optionSelectedValue != null && optionSelectedValue.getString().equals(value)) {
                        navigator.setSelected(true);
                    }
                    if (option.isUseHitCount() && option.getCommandName() != null) {
                        navigator.setHitCount(datamodel.getSearch(option.getCommandName()).getResults().getHitCount());
                    }
                }
            }
        }
    }

    private static boolean isOptionDefaultSelected(NavigationConfig.Option option, FastSearchResult fsr) {
        final String valueRef = option.getDefaultSelectValueRef();

        return option.isDefaultSelect()
                || (fsr != null &&  valueRef != null && option.getValue().equals(fsr.getField(valueRef)));
    }

}
