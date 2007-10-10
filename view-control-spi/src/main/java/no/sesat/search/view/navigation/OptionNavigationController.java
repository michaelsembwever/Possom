/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Jul 25, 2007 11:02:02 AM
 */
package no.sesat.search.view.navigation;

import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.datamodel.generic.StringDataObjectSupport;
import no.sesat.search.result.BasicNavigationItem;
import no.sesat.search.result.NavigationItem;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

    public NavigationItem getNavigationItems(Context context) {
        final ResultList<? extends ResultItem> searchResult = commandName != null
                ? context.getDataModel().getSearch(commandName).getResults()
                : null;

        if (! config.getOptionsToKeep().isEmpty()) {
            removeAllBut(config.getOptionsToKeep(), searchResult, context.getDataModel());
        }

        removeAll(config.getOptionsToDelete(), context.getDataModel());
        addAll(config.getOptionsToAdd(), context.getDataModel());

        // Only modifies the result of the parent. Return null.
        return null;
    }
                                            
    private void removeAll(final Collection<OptionsNavigationConfig.Option> options, final DataModel dataModel) {
        final NavigationItem parentResult = dataModel.getNavigation().getNavigation(config.getParent().getId());
        for (final Iterator<NavigationItem> iterator = parentResult.getResults().iterator(); iterator.hasNext();) {
            final NavigationItem item = iterator.next();
            for (final OptionsNavigationConfig.Option option : options) {
                if (item.getTitle().equals(option.getValue())) {
                    iterator.remove();
                }
            }
        }
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

    private void addAll(final Collection<OptionsNavigationConfig.Option> optionsToAdd, final DataModel dataModel) {

        final NavigationItem parentResult = dataModel.getNavigation().getNavigation(config.getParent().getId());
        final StringDataObject optionSelectedValue = dataModel.getParameters().getValue(config.getParent().getField());


        boolean selectionDone = false;

        for (final OptionsNavigationConfig.Option option : optionsToAdd) {

            final ResultList<? extends ResultItem> searchResult = option.getCommandName() != null
                    ? dataModel.getSearch(option.getCommandName()).getResults()
                    : null;

            String value = option.getValue();
            if (option.getValueRef() != null && searchResult != null) {
                final String tmp = searchResult.getField(option.getValueRef());
                if (tmp != null && tmp.length() > 0) {
                    value = tmp;
                }
            }
            if (value != null) {
                final Map<String, String> urlParameters = NavigationHelper.getUrlParameters(dataModel, config.getParent(), value, null);

                if (option.getTab() != null) {
                    urlParameters.put("c", option.getTab());
                }

                final NavigationItem navigator = new BasicNavigationItem(
                        option.getDisplayName(),
                        NavigationHelper.getUrlFragment(urlParameters),
                        -1);
                parentResult.addResult(navigator);
                if (!selectionDone && optionSelectedValue == null && isOptionDefaultSelected(searchResult, option)) {
                    navigator.setSelected(true);
                    selectionDone = true;
                } else if (optionSelectedValue != null && optionSelectedValue.getString().equals(value)) {
                    navigator.setSelected(true);
                    selectionDone = true;
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
