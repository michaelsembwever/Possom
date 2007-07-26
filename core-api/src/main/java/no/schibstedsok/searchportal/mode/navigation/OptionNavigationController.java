/* Copyright (2005-2007) Schibsted Søk AS
 *
 * Jul 25, 2007 11:02:02 AM
 */
package no.schibstedsok.searchportal.mode.navigation;

import no.schibstedsok.searchportal.result.NavigationItem;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
import no.schibstedsok.searchportal.result.BasicNavigationItem;
import no.schibstedsok.searchportal.result.NavigationHelper;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObjectSupport;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Geir H. Pettersen(T-Rank)
 * @author <a href="mailto:magnus.eklund@sesam.no">Magnus Eklund</a>
 */
public class OptionNavigationController
        implements NavigationController, NavigationControllerFactory<OptionsNavigationConfig> {

    private OptionsNavigationConfig config;
    private String commandName;

    public NavigationController get(final OptionsNavigationConfig navigationConfig) {
        this.config = navigationConfig;
        this.commandName = navigationConfig.getCommandName();
        return this;
    }

    public NavigationItem getNavigationItems(final DataModel dataModel) {

        final ResultList<? extends ResultItem> searchResult = commandName != null
                ? dataModel.getSearch(commandName).getResults()
                : null;

        if (! config.getOptionsToKeep().isEmpty()) {
            removeAllBut(config.getOptionsToKeep(), searchResult, dataModel);
        }

//        removeAll(config.getOptionsToDelete(), dataModel, searchResult, dataModel);
        addAll(config.getOptionsToAdd(), searchResult, dataModel);

        // Only modifies the result of the parent. Return null.
        return null;
    }


    private void removeAllBut(
            final Collection<OptionsNavigationConfig.Option> optionsToKeep,
            final ResultList<? extends ResultItem> searchResult,
            final DataModel dataModel) {
        final NavigationItem parentResult = dataModel.getNavigation().getNavigation(config.getParent().getId());
        final List<NavigationItem> toRemove = new ArrayList<NavigationItem>();
        StringDataObject selectedValue = dataModel.getParameters().getValue(config.getParent().getField());

          // Navigators already collected. Options is override
          for (NavigationItem navigator : parentResult.getResults()) {
              boolean match = false;

              // Double loop to find match in two lists. Not nice, but it works.
              for (OptionsNavigationConfig.Option option : optionsToKeep) {
                  final String value = option.getValue();
                  if (navigator.getTitle().equals(value)) {
                      match = true;


                      if (selectedValue == null && isOptionDefaultSelected(searchResult, option)) {
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
              parentResult.removeResult(item);
          }
    }

    private void addAll(
            final Collection<OptionsNavigationConfig.Option> optionsToAdd,
            final ResultList<? extends ResultItem> searchResult, DataModel dataModel) {

        final NavigationItem parentResult = dataModel.getNavigation().getNavigation(config.getParent().getId());
        final StringDataObject optionSelectedValue = dataModel.getParameters().getValue(config.getParent().getField());

        for (OptionsNavigationConfig.Option option : optionsToAdd) {

            String value = option.getValue();
            if (option.getValueRef() != null && searchResult != null) {
                final String tmp = searchResult.getField(option.getValueRef());
                if (tmp != null && tmp.length() > 0) {
                    value = tmp;
                }
            }
            if (value != null) {
                final NavigationItem navigator = new BasicNavigationItem(
                        option.getDisplayName(),
                        NavigationHelper.getUrlFragment(dataModel, config.getParent(), value, null),
                        -1);
                parentResult.addResult(navigator);
                if (optionSelectedValue == null && isOptionDefaultSelected(searchResult, option)) {
                    navigator.setSelected(true);
                } else if (optionSelectedValue != null && optionSelectedValue.getString().equals(value)) {
                    navigator.setSelected(true);
                }
                if (option.isUseHitCount() && option.getCommandName() != null) {
                    navigator.setHitCount(dataModel.getSearch(option.getCommandName()).getResults().getHitCount());
                }
            }
        }
    }

    private boolean isOptionDefaultSelected(
            final ResultList<? extends ResultItem> result, 
            final OptionsNavigationConfig.Option option) {
        final String valueRef = option.getDefaultSelectValueRef();

        return option.isDefaultSelect()
                || (result != null &&  valueRef != null && option.getValue().equals(result.getField(valueRef)));
    }
}
