/* Copyright (2007) Schibsted Søk AS
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
 */
/*
 * NavigatatableAdvancedFastSearchCommand.java
 *
 * Created on July 20, 2006, 11:57 AM
 *
 */

package no.sesat.search.mode.command;


import com.fastsearch.esp.search.result.IModifier;
import com.fastsearch.esp.search.result.INavigator;
import com.fastsearch.esp.search.result.IQueryResult;
import no.sesat.search.mode.config.NavigatableEspFastCommandConfig;
import no.sesat.search.result.FastSearchResult;
import no.sesat.search.result.Modifier;
import no.sesat.search.result.Navigator;
import no.sesat.search.result.ModifierDateComparator;
import no.sesat.search.result.ModifierStringComparator;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import no.sesat.search.datamodel.generic.StringDataObject;

/**
 * This class provies an advanced fast search command with navigation
 * capabilities.
 *
 * @author <a href="mailto:magnus.eklund@gmail.com">Magnus Eklund</a>
 * @version $Id$
 */
public class NavigatableESPFastCommand extends ESPFastSearchCommand {

    // Attributes ----------------------------------------------------
    private final Map<String, Navigator> navigatedTo = new HashMap<String, Navigator>();
    private final Map<String, String[]> navigatedValues = new HashMap<String, String[]>();


    public NavigatableESPFastCommand(final Context cxt) {

        super(cxt);
    }

    public Collection createNavigationFilterStrings() {
        final Collection<String> filterStrings = new ArrayList<String>();

        for (String field : navigatedValues.keySet()) {
            final String modifiers[] = navigatedValues.get(field);

            for (String modifier : modifiers) {
                if (!field.equals("contentsource") || !modifier.equals("Norske nyheter"))
                    filterStrings.add("+" + field + ":\"" + modifier + "\"");
            }
        }

        for (final Navigator navigator : getSearchConfiguration().getNavigators().values()) {
            final StringDataObject navigatedValue = datamodel.getParameters().getValue(navigator.getId());


            if (navigatedValue != null) {
                final String value =  navigator.isBoundaryMatch() ? "^\"" + navigatedValue.getString() + "\"$" : "\"" + navigatedValue.getString() + "\"";

                if ("adv".equals(getSearchConfiguration().getFiltertype()))
                    filterStrings.add(" AND " + navigator.getField() + ':'  + value );
                else
                    filterStrings.add("+" + navigator.getField() + ':'  + value);
            }
        }

        return filterStrings;
    }

    @Override
    public ResultList<? extends ResultItem> execute() {
        if (!getSearchConfiguration().isIgnoreNavigation() && getNavigators() != null) {
            for (String navigatorKey : getNavigators().keySet()) {
                addNavigatedTo(navigatorKey);
            }
        }

        final ResultList<? extends ResultItem> searchResult = super.execute();

        // We want to collect modifiers even if we ignore navigation
        if (getNavigators() != null) {
            for (String navigatorKey : getNavigators().keySet()) {
                addNavigatedTo(navigatorKey);
            }
        }

        if (null != getNavigators() && searchResult instanceof FastSearchResult) {
            collectModifiers(getIQueryResult(), (FastSearchResult<? extends ResultItem>)searchResult);
        }

        return searchResult;
    }

    public void addNavigatedTo(final String navigatorKey) {
        navigatedTo.put(navigatorKey, getNavigators().get(navigatorKey));
    }

    public Navigator getNavigatedTo(final String navigatorKey) {
        return navigatedTo.get(navigatorKey);
    }


    public Map getNavigatedValues() {
        return navigatedValues;
    }

    public String getNavigatedValue(final String fieldName) {
        final String[] singleValue = navigatedValues.get(fieldName);

        if (singleValue != null) {
            return (singleValue[0]);
        } else {
            return null;
        }
    }

    public Map getNavigatedTo() {
        return navigatedTo;
    }

    /**
     * Assured associated search configuration will always be of this type. *
     */
    @Override
    public NavigatableEspFastCommandConfig getSearchConfiguration() {
        return (NavigatableEspFastCommandConfig) super.getSearchConfiguration();
    }

    protected Map<String, Navigator> getNavigators() {
        return getSearchConfiguration().getNavigators();
    }

    private void collectModifiers(IQueryResult result, FastSearchResult<? extends ResultItem> searchResult) {

        for (String navigatorKey : navigatedTo.keySet()) {

            collectModifier(navigatorKey, result, searchResult);
        }
    }

    private void collectModifier(String navigatorKey, IQueryResult result, FastSearchResult<? extends ResultItem> searchResult) {

        final Navigator nav = navigatedTo.get(navigatorKey);
        INavigator navigator = null;

        if (result != null) {
            navigator = result.getNavigator(nav.getName());
        }

        if (navigator != null) {

            Iterator modifers = navigator.modifiers();

            while (modifers.hasNext()) {
                IModifier modifier = (IModifier) modifers.next();
                if (!navigatedValues.containsKey(nav.getField()) || modifier.getName().equals(navigatedValues.get(nav.getField())[0])) {
                    Modifier mod = new Modifier(modifier.getName(), modifier.getCount(), nav);
                    searchResult.addModifier(navigatorKey, mod);
                }
            }


            if (searchResult.getModifiers(navigatorKey) != null) {
                switch (nav.getSort()) {
                    case DAY_MONTH_YEAR:
                        Collections.sort(searchResult.getModifiers(navigatorKey), ModifierDateComparator.DAY_MONTH_YEAR);
                        break;
                    case DAY_MONTH_YEAR_DESCENDING:
                        Collections.sort(searchResult.getModifiers(navigatorKey), ModifierDateComparator.DAY_MONTH_YEAR_DESCENDING);
                        break;
                    case YEAR_MONTH_DAY_DESCENDING:
                        Collections.sort(searchResult.getModifiers(navigatorKey), ModifierDateComparator.YEAR_MONTH_DAY_DESCENDING);
                        break;                        
                    case YEAR:
                        Collections.sort(searchResult.getModifiers(navigatorKey), ModifierDateComparator.YEAR);
                        break;
                    case MONTH_YEAR:
                        Collections.sort(searchResult.getModifiers(navigatorKey), ModifierDateComparator.MONTH_YEAR);
                        break;
                    case YEAR_MONTH:
                        Collections.sort(searchResult.getModifiers(navigatorKey), ModifierDateComparator.YEAR_MONTH);
                        break;
                    case ALPHABETICAL:
                        Collections.sort(searchResult.getModifiers(navigatorKey), ModifierStringComparator.ALPHABETICAL);
                        break;
                    case ALPHABETICAL_DESCENDING:
                        Collections.sort(searchResult.getModifiers(navigatorKey), Collections.reverseOrder(ModifierStringComparator.ALPHABETICAL));
                        break;
                    case CUSTOM:
                        Collections.sort(searchResult.getModifiers(navigatorKey), getModifierComparator(nav));
                        break;
                    case NONE:
                        // Use the soting the index returns
                        break;
                    case COUNT:
                        /* Fall through */
                    default:
                        Collections.sort(searchResult.getModifiers(navigatorKey));
                        break;
                }
            }

        } else if (nav.getChildNavigator() != null) {
            navigatedTo.put(navigatorKey, nav.getChildNavigator());
            collectModifier(navigatorKey, result, searchResult);
        }
    }

    protected Comparator getModifierComparator(final Navigator nav) {
        return null;
    }
    
    @Override
    protected String getAdditionalFilter() {
        
        final StringBuilder result = new StringBuilder();
        if (!getSearchConfiguration().isIgnoreNavigation() && getNavigators() != null) {
            final Collection navStrings = createNavigationFilterStrings();
            result.append( StringUtils.join(navStrings.iterator(), " "));
        }
        return result.append(' ' + super.getAdditionalFilter()).toString().trim();
    }

    protected boolean isNavigatable() { return true; }
}
