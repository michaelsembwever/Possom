/*
 * NavigatatableAdvancedFastSearchCommand.java
 *
 * Created on July 20, 2006, 11:57 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.mode.command;

import com.fastsearch.esp.search.result.IModifier;
import com.fastsearch.esp.search.result.INavigator;
import com.fastsearch.esp.search.result.IQueryResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import no.schibstedsok.searchportal.mode.command.AdvancedFastSearchCommand;
import no.schibstedsok.searchportal.mode.command.SearchCommand.Context;
import no.schibstedsok.searchportal.mode.config.NavigatableAdvancedFastConfiguration;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.result.Navigator;
import no.schibstedsok.searchportal.result.SearchResult;
import org.apache.commons.lang.StringUtils;

/**
 * This class provies an advanced fast search command with navigation 
 * capabilities.
 * 
 * @author maek
 */
public class NavigatableAdvancedFastCommand extends AdvancedFastSearchCommand {
    
    // Attributes ----------------------------------------------------
    private final Map<String,Navigator> navigatedTo = new HashMap<String,Navigator>();
    private final Map<String,String[]> navigatedValues = new HashMap<String,String[]>();
    
    
    
    public NavigatableAdvancedFastCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);
    }
    
    public Collection createNavigationFilterStrings() {
        final Collection filterStrings = new ArrayList();
        
        for (final Iterator iterator = navigatedValues.keySet().iterator(); iterator.hasNext();) {
            final String field = (String) iterator.next();
            
            final String modifiers[] = (String[]) navigatedValues.get(field);
            
            
            for (int i = 0; i < modifiers.length; i++) {
                if (!field.equals("contentsource") || !modifiers[i].equals("Norske nyheter"))
                    filterStrings.add("+" + field + ":\"" + modifiers[i] + "\"");
            }
        }
        
        return filterStrings;
    }
    
    public SearchResult execute() {
        if (getNavigators() != null) {
            for (String navigatorKey : getNavigators().keySet()) {
                
                addNavigatedTo(navigatorKey, getParameters().containsKey("nav_" + navigatorKey)
                ? getParameter("nav_" + navigatorKey)
                : null);
            }
        }

        final FastSearchResult searchResult = (FastSearchResult) super.execute();
        
        if (getNavigators() != null) {
            collectModifiers(getIQueryResult(), searchResult);
        }
        
        return searchResult;
    }
    
    public Map getOtherNavigators(final String navigatorKey) {
        
        final Map<String,String> otherNavigators = new HashMap<String,String>();
        
        for (String parameterName : (Set<String>)getParameters().keySet()) {
            
            if (parameterName.startsWith("nav_") && !parameterName.substring(parameterName.indexOf('_') + 1).equals(navigatorKey)) {
                final String paramValue = getParameter(parameterName);
                otherNavigators.put(parameterName.substring(parameterName.indexOf('_') + 1), paramValue);
            }
        }
        return otherNavigators;
    }
    public void addNavigatedTo(final String navigatorKey, final String navigatorName) {
        
        final Navigator navigator = (Navigator) getNavigators().get(navigatorKey);
        
        if (navigatorName == null) {
            navigatedTo.put(navigatorKey, navigator);
        } else {
            navigatedTo.put(navigatorKey, findChildNavigator(navigator, navigatorName));
        }
    }
    
    public Navigator getNavigatedTo(final String navigatorKey) {
        return (Navigator) navigatedTo.get(navigatorKey);
    }
    
    
    public Navigator getParentNavigator(final String navigatorKey) {
        if (getParameters().containsKey("nav_" + navigatorKey)) {
            final String navName =  getParameter("nav_" + navigatorKey);
            
            return findParentNavigator((Navigator) getNavigators().get(navigatorKey), navName);
            
        } else {
            return null;
        }
    }
    
    public Navigator getParentNavigator(final String navigatorKey, final String name) {
        if (getParameters().containsKey("nav_" + navigatorKey)) {
            
            return findParentNavigator((Navigator) getNavigators().get(navigatorKey), name);
            
        } else {
            return null;
        }
    }
    
    public Navigator findParentNavigator(final Navigator navigator, final String navigatorName) {
        if (navigator.getChildNavigator() == null) {
            return null;
        } else if (navigator.getChildNavigator().getName().equals(navigatorName)) {
            
            if (true) {
                return navigator;
            } else {
                return findParentNavigator(navigator.getChildNavigator(), navigatorName);
                
            }
        } else {
            return findParentNavigator(navigator.getChildNavigator(), navigatorName);
        }
    }
    
    public Map getNavigatedValues() {
        return navigatedValues;
    }
    
    public String getNavigatedValue(final String fieldName) {
        final String[] singleValue = (String[]) navigatedValues.get(fieldName);
        
        if (singleValue != null) {
            return (singleValue[0]);
        } else {
            return null;
        }
    }
    
    public boolean isTopLevelNavigator(final String navigatorKey) {
        return !getParameters().containsKey("nav_" + navigatorKey);
    }
    
    public Map getNavigatedTo() {
        return navigatedTo;
    }
    
    public String getNavigatorTitle(final String navigatorKey) {
        final Navigator nav = getNavigatedTo(navigatorKey);
        
        Navigator parent = findParentNavigator((Navigator) getNavigators().get(navigatorKey), nav.getName());
        
        String value = getNavigatedValue(nav.getField());
        
        if (value == null && parent != null) {
            
            value = getNavigatedValue(parent.getField());
            
            if (value == null) {
                
                parent = findParentNavigator((Navigator) getNavigators().get(navigatorKey), parent.getName());
                
                
                if (parent != null) {
                    value = getNavigatedValue(parent.getField());
                }
                return value;
            } else {
                return value;
            }
        }
        
        if (value == null) {
            return nav.getDisplayName();
        } else {
            return value;
        }
        
    }
    
    public String getNavigatorTitle(final Navigator navigator) {
        final String value = getNavigatedValue(navigator.getField());
        
        if (value == null) {
            return navigator.getDisplayName();
        } else {
            return value;
        }
    }
    
    /** Assured associated search configuration will always be of this type. **/
    public NavigatableAdvancedFastConfiguration getSearchConfiguration() {
        return (NavigatableAdvancedFastConfiguration) super.getSearchConfiguration();
    }
    
    public List getNavigatorBackLinks(final String navigatorKey) {
        
        final List backLinks = addNavigatorBackLinks(getSearchConfiguration().getNavigator(navigatorKey), new ArrayList(), navigatorKey);
        
        if (backLinks.size() > 0) {
            backLinks.remove(backLinks.size() - 1);
        }
        
        return backLinks;
    }
    
    public List addNavigatorBackLinks(final Navigator navigator, final List links, final String navigatorKey) {
        
        final String a = getParameter(navigator.getField());
        
        if (a != null) {
            if (!(navigator.getName().equals("ywfylkesnavigator") && a.equals("Oslo"))) {
                if (!(navigator.getName().equals("ywkommunenavigator") && a.equals("Oslo"))) {
                    links.add(navigator);
                }
            }
        }
        
        if (navigator.getChildNavigator() != null) {
            final String n = getParameter("nav_" + navigatorKey);
            
            if (n != null && navigator.getName().equals(n)) {
                return links;
            }
            
            addNavigatorBackLinks(navigator.getChildNavigator(), links, navigatorKey);
        }
        
        return links;
    }
    
    protected Map<String, Navigator> getNavigators() {
        return getSearchConfiguration().getNavigators();
    }
    
    private String getNavigatorsString() {
        
        if (getNavigators() != null) {
            
            Collection allFlattened = new ArrayList();
            
            
            for (Navigator navigator : getNavigators().values()) {
                
                allFlattened.addAll(flattenNavigators(new ArrayList(), navigator));
            }
            
            return StringUtils.join(allFlattened.iterator(), ',');
        } else {
            return "";
        }
    }
    
    private Collection flattenNavigators(Collection soFar, Navigator nav) {
        
        soFar.add(nav);
        
        if (nav.getChildNavigator() != null) {
            flattenNavigators(soFar, nav.getChildNavigator());
        }
        
        return soFar;
    }
    
    private void collectModifiers(IQueryResult result, FastSearchResult searchResult) {
        
        for (String navigatorKey : navigatedTo.keySet()) {
            
            collectModifier(navigatorKey, result, searchResult);
        }
    }
    
    private void collectModifier(String navigatorKey, IQueryResult result, FastSearchResult searchResult) {
        
        final Navigator nav = (Navigator) navigatedTo.get(navigatorKey);
        
        INavigator navigator = result.getNavigator(nav.getName());
        
        if (navigator != null) {
            
            Iterator modifers = navigator.modifiers();
            
            while (modifers.hasNext()) {
                IModifier modifier = (IModifier) modifers.next();
                Modifier mod = new Modifier(modifier.getName(), modifier.getCount(), nav);
                searchResult.addModifier(navigatorKey, mod);
            }
            
            if (searchResult.getModifiers(navigatorKey) != null) {
                Collections.sort(searchResult.getModifiers(navigatorKey));
            }
            
        } else if (nav.getChildNavigator() != null) {
            navigatedTo.put(navigatorKey, nav.getChildNavigator());
            collectModifier(navigatorKey, result, searchResult);
        }
    }
    
    private Navigator findChildNavigator(Navigator nav, String nameToFind) {
        
        if (getParameters().containsKey(nav.getField())) {
            
            navigatedValues.put(nav.getField(), getParameters().get(nav.getField()) instanceof String[]
                    ? (String[])getParameters().get(nav.getField())
                    : new String[]{getParameter(nav.getField())});
        }
        
        if (nav.getName().equals(nameToFind)) {
            if (nav.getChildNavigator() != null) {
                return nav.getChildNavigator();
            } else {
                return nav;
            }
        }
        
        if (nav.getChildNavigator() == null) {
            throw new RuntimeException("Navigator " + nameToFind + " not found.");
        }
        
        return findChildNavigator(nav.getChildNavigator(), nameToFind);
    }

    
    protected String getAdditionalFilter() {
        final Collection navStrings = createNavigationFilterStrings();
        if (getNavigators() != null) {
            return StringUtils.join(navStrings.iterator(), " ");
        } else {
            return null;
        }
    }
}
