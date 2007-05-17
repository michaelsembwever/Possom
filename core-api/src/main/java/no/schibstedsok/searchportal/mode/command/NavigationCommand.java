package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObjectSupport;
import no.schibstedsok.searchportal.mode.config.NavigationCommandConfig;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.Modifier;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;

/**
 * This is a command to help generating navigation urls in the view. I got tired of all
 * the URL handling velocity code. Some of the effects from this is virtually impossible to
 * code in velocity.
 * <p/>
 * TODO This should be a multiResult resulthandler, but right now this just a waiting searchCommand.
 * Usually there will be no real waiting since the calls on the results occur from velocity.
 * <p/>
 * As a bonus from using this, you don't need to data-model the commands that only are
 * there for navigation.
 *
 * @author Geir H. Pettersen(T-Rank)
 */
public final class NavigationCommand extends AbstractSearchCommand {
    
    private static final Logger LOG = Logger.getLogger(NavigationCommand.class);

    /**
     * @param cxt The context to execute in.
     */
    public NavigationCommand(Context cxt) {
        super(cxt);
    }

    public ResultList<? extends ResultItem> execute() {
        NavigationCommandConfig config = getSearchConfiguration();
        return new ExtendedNavigationSearchResult(this, config.getExtendedNavigationConfig(), context);
    }

    @Override
    public NavigationCommandConfig getSearchConfiguration() {
        return (NavigationCommandConfig) super.getSearchConfiguration();
    }

    /**
     * 
     * @param T 
     */
    public static class ExtendedNavigationSearchResult<T extends ResultItem> extends BasicSearchResult<T> {
        
        private final ExtendedNavigation extendedNavigation;


        /**
         * 
         * @param command 
         * @param extendedNavigationConfig 
         * @param context 
         */
        public ExtendedNavigationSearchResult(
                final NavigationCommand command, 
                final NavigationCommandConfig.ExtendedNavigationConfig extendedNavigationConfig, 
                final Context context) {
            
            super();
            this.extendedNavigation = new ExtendedNavigation(extendedNavigationConfig, context);
        }

        /**
         * 
         * @return 
         */
        public ExtendedNavigation getExtendedNavigation() {
            return extendedNavigation;
        }
    }

    /**
     * 
     */
    public static final class ExtendedNavigation {
        
        private final NavigationCommandConfig.ExtendedNavigationConfig extendedNavigationConfig;
        private final Context context;


        /**
         * 
         * @param extendedNavigationConfig 
         * @param context 
         */
        public ExtendedNavigation(
                final NavigationCommandConfig.ExtendedNavigationConfig extendedNavigationConfig, 
                final Context context) {
            
            this.extendedNavigationConfig = extendedNavigationConfig;
            this.context = context;
        }

        /**
         * 
         * @param id 
         * @return 
         */
        public NavigationCommandConfig.Navigation getNavigation(String id) {
            return extendedNavigationConfig.getNavigationMap().get(id);
        }

        /**
         * Returns extended navigators for a name(id)
         *
         * @param name the id of the navigator to get.
         * @return a list with extended navigators
         */
        public NavigatorList getNavigators(String name) {
            NavigationCommandConfig.Nav navEntry = extendedNavigationConfig.getNavMap().get(name);
            try {
                if (navEntry != null) {
                    boolean selectionDone = false;
                    StringDataObject selectedValue = context.getDataModel().getParameters().getValue(name);
                    NavigatorList extendedNavigators = new NavigatorList(new ArrayList<ExtendedNavigator>());
                    FastSearchResult fsr = null;
                    if (navEntry.getCommandName() != null) {
                        ResultList<? extends ResultItem> searchResult = context.getRunningQuery().getSearchResult(navEntry.getCommandName());
                        if (searchResult instanceof FastSearchResult) {
                            fsr = (FastSearchResult) searchResult;
                            List<Modifier> modifiers = fsr.getModifiers(navEntry.isRealNavigator() ? navEntry.getField() : name);
                            if (modifiers != null && modifiers.size() > 0) {
                                for (Modifier modifier : modifiers) {
                                    final String navigatorName = modifier.getNavigator() == null ? null : modifier.getNavigator().getName();
                                    final String urlFragment = getUrlFragment(navEntry, modifier.getName(), navigatorName);
                                    final ExtendedNavigator navigator = new ExtendedNavigator(modifier.getName(), urlFragment, modifier.getCount());
                                    if (!selectionDone) {
                                        selectedValue = context.getDataModel().getParameters().getValue(navEntry.getField());
                                        if (selectedValue != null && selectedValue.getString().equals(modifier.getName())) {
                                            navigator.setSelected(true);
                                            selectionDone = true;
                                        }
                                    }
                                    extendedNavigators.add(navigator);
                                }
                            }
                        }
                    }
                    getOptionNavigators(navEntry, fsr, extendedNavigators, selectedValue);
                    return extendedNavigators;
                }

            } catch (InterruptedException e) {
                LOG.error("Could not get searchResult for " + navEntry.getCommandName(), e);
            } catch (ExecutionException e) {
                LOG.error("Could not get searchResult for " + navEntry.getCommandName(), e);
            }
            return null;
        }


        private void getOptionNavigators(
                final NavigationCommandConfig.Nav navEntry, 
                final FastSearchResult fsr, 
                final List<ExtendedNavigator> extendedNavigators, 
                StringDataObject selectedValue) {
            
            // Only used by getNavigators. Mainly to split code.
            if (extendedNavigators.size() > 0 && navEntry.getOptions().size() > 0) {
                // Navigators already collected. Options is override
                Iterator<ExtendedNavigator> it = extendedNavigators.iterator();
                while (it.hasNext()) {
                    boolean match = false;
                    ExtendedNavigator navigator = it.next();
                    // Double loop to find match in two lists. Not nice, but it works.
                    for (NavigationCommandConfig.Option option : navEntry.getOptions()) {
                        String value = option.getValue();
                        if (navigator.name.equals(value)) {
                            match = true;
                            if (selectedValue == null && option.isDefaultSelect()) {
                                navigator.setSelected(true);
                                selectedValue = new StringDataObjectSupport("dummy");
                            }
                            if (option.getDisplayName() != null) {
                                navigator.setName(option.getDisplayName());
                            }
                        }
                    }
                    if (!match) {
                        it.remove();
                    }
                }
            } else {
                final StringDataObject optionSelectedValue = context.getDataModel().getParameters().getValue(navEntry.getField());
                for (NavigationCommandConfig.Option option : navEntry.getOptions()) {
                    String value = option.getValue();
                    if (option.getValueRef() != null && fsr != null) {
                        String tmp = fsr.getField(option.getValueRef());
                        if (tmp != null && tmp.length() > 0) {
                            value = tmp;
                        }
                    }
                    if (value != null) {
                        ExtendedNavigator navigator = new ExtendedNavigator(option.getDisplayName(), getUrlFragment(navEntry, value), -1);
                        extendedNavigators.add(navigator);
                        if (optionSelectedValue == null && option.isDefaultSelect()) {
                            navigator.setSelected(true);
                        } else if (optionSelectedValue != null && optionSelectedValue.getString().equals(value)) {
                            navigator.setSelected(true);
                        }
                    }
                }
            }
        }

        /**
         * Gets the url fragment you should use when navigating on this value.
         *
         * @param navigator the navigator you are using
         * @param value     the specific field you are using
         * @return a url fragemt to use
         */
        public String getUrlFragment(final String navigator, final String value) {
            NavigationCommandConfig.Nav navEntry = extendedNavigationConfig.getNavMap().get(navigator);
            return getUrlFragment(navEntry, value);
        }

        private String getUrlFragment(final NavigationCommandConfig.Nav navEntry, final String value) {
            return getUrlFragment(navEntry, value, null);
        }

        /**
         * 
         * @param navEntry 
         * @param value 
         * @param navigatorName 
         * @return 
         */
        public String getUrlFragment(
                final NavigationCommandConfig.Nav navEntry, 
                final String value, 
                final String navigatorName) {
            
            final StringBuilder sb = new StringBuilder();
            String tab = navEntry.getTab();
            if (tab == null) {
                tab = context.getDataModel().getParameters().getValue("c").getUtf8UrlEncoded();
            }
            sb.append("c=").append(tab);
            if (!navEntry.isExcludeQuery()) {
                sb.append("&q=").append(context.getDataModel().getQuery().getUtf8UrlEncoded());
            }
            if (value != null && value.length() > 0) {
                sb.append('&').append(enc(navEntry.getField())).append('=').append(enc(value));
                if (navEntry.isRealNavigator() && navigatorName != null) {
                    sb.append('&').append("nav_").append(enc(navEntry.getField())).append('=').append(enc(navigatorName));
                }
            }
            if (!navEntry.isOut()) {
                addParentFragment(sb, navEntry);
                for (NavigationCommandConfig.Navigation navigation : extendedNavigationConfig.getNavigationList()) {
                    if (navigation != navEntry.getNavigation()) {
                        addNavigationFragments(navigation, sb, navEntry);
                    }
                }
            }
            for (String key : navEntry.getStaticParameters().keySet()) {
                addFragment(sb, navEntry, key, navEntry.getStaticParameters().get(key));
            }
            return sb.toString();
        }

        private void addNavigationFragments(
                final NavigationCommandConfig.Navigation navigation, 
                final StringBuilder sb, 
                final NavigationCommandConfig.Nav navEntry) {
            
            final Set<String> fieldFilterSet = new HashSet<String>();
            for (NavigationCommandConfig.Nav nav : navigation.getNavList()) {
                addNavigationFragment(fieldFilterSet, nav, sb, navEntry);
            }
        }

        private void addNavigationFragment(
                final Set<String> fieldFilterSet, 
                final NavigationCommandConfig.Nav nav, 
                final StringBuilder sb, 
                final NavigationCommandConfig.Nav navEntry) {
            
            StringDataObject fieldValue = context.getDataModel().getParameters().getValue(nav.getField());
            if (!fieldFilterSet.contains(nav.getField())) {
                addPreviousField(fieldValue, sb, navEntry, nav.getField());
                fieldFilterSet.add(nav.getField());
                for (String staticKey : nav.getStaticParameters().keySet()) {
                    fieldValue = context.getDataModel().getParameters().getValue(staticKey);
                    if (!fieldFilterSet.contains(staticKey)) {
                        addPreviousField(fieldValue, sb, navEntry, staticKey);
                    }
                }
                if (nav.getChildNavs() != null) {
                    for (NavigationCommandConfig.Nav childNav : nav.getChildNavs()) {
                        addNavigationFragment(fieldFilterSet, childNav, sb, navEntry);
                    }
                }
            }
        }

        private void addPreviousField(
                StringDataObject fieldValue, 
                final StringBuilder sb, 
                final NavigationCommandConfig.Nav navEntry, 
                final String fieldName) {
            
            if (fieldValue != null && addFragment(sb, navEntry, fieldName, fieldValue.getString())) {
                fieldValue = context.getDataModel().getParameters().getValue("nav_" + fieldName);
                if (fieldValue != null) {
                    addFragment(sb, navEntry, "nav_" + fieldName, fieldValue.getString());
                }
            }
        }

        private boolean addFragment(
                final StringBuilder sb, 
                final NavigationCommandConfig.Nav nav, 
                final String id, 
                final String value) {
            
            if (!nav.getNavigation().getResetNavSet().contains(id)) {
                sb.append('&').append(enc(id)).append('=').append(enc(value));
                return true;
            }
            return false;
        }

        private void addParentFragment(final StringBuilder sb, final NavigationCommandConfig.Nav navEntry) {
            NavigationCommandConfig.Nav parentNav = navEntry.getParentNav();
            if (parentNav != null) {
                StringDataObject fieldValue = context.getDataModel().getParameters().getValue(parentNav.getField());
                addPreviousField(fieldValue, sb, navEntry, parentNav.getField());
                addParentFragment(sb, parentNav);
            }
        }

        private String enc(final String str) {
            
            try {
                return URLEncoder.encode(str, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                LOG.fatal("UTF-8 encoding not available");
            }
            return str;
        }
    }

    public static class NavigatorList implements List<ExtendedNavigator> {
        private List<ExtendedNavigator> proxiedList;
        private boolean dirty = true;
        private ExtendedNavigator selectedItem;

        public NavigatorList(List<ExtendedNavigator> proxiedList) {
            this.proxiedList = proxiedList;
        }

        public ExtendedNavigator getSelected() {
            findSelection();
            return selectedItem;
        }

        private void findSelection() {
            if (dirty) {
                selectedItem = null;
                for (ExtendedNavigator extendedNavigator : proxiedList) {
                    if (extendedNavigator.isSelected()) {
                        selectedItem = extendedNavigator;
                        break;
                    }
                }
            }
            dirty = false;
        }

        public boolean hasSelection() {
            findSelection();
            return selectedItem != null;
        }

        public int size() {
            return proxiedList.size();
        }

        public boolean isEmpty() {
            return proxiedList.isEmpty();
        }

        public boolean contains(Object o) {
            return proxiedList.contains(o);
        }

        public Iterator<ExtendedNavigator> iterator() {
            dirty = true;
            return proxiedList.iterator();
        }

        public Object[] toArray() {
            return proxiedList.toArray();
        }

        public <T> T[] toArray(T[] a) {
            return proxiedList.toArray(a);
        }

        public boolean add(ExtendedNavigator t) {
            dirty = true;
            return proxiedList.add(t);
        }

        public boolean remove(Object o) {
            return proxiedList.remove(o);
        }

        public boolean containsAll(Collection<?> c) {
            return proxiedList.containsAll(c);
        }

        public boolean addAll(Collection<? extends ExtendedNavigator> c) {
            dirty = true;
            return proxiedList.addAll(c);
        }

        public boolean addAll(int index, Collection<? extends ExtendedNavigator> c) {
            dirty = true;
            return proxiedList.addAll(index, c);
        }

        public boolean removeAll(Collection<?> c) {
            dirty = true;
            return proxiedList.removeAll(c);
        }

        public boolean retainAll(Collection<?> c) {
            dirty = true;
            return proxiedList.retainAll(c);
        }

        public void clear() {
            dirty = true;
            proxiedList.clear();
        }

        public ExtendedNavigator get(int index) {
            return proxiedList.get(index);
        }

        public ExtendedNavigator set(int index, ExtendedNavigator element) {
            dirty = true;
            return proxiedList.set(index, element);
        }

        public void add(int index, ExtendedNavigator element) {
            dirty = true;
            proxiedList.add(index, element);
        }

        public ExtendedNavigator remove(int index) {
            dirty = true;
            return proxiedList.remove(index);
        }

        public int indexOf(Object o) {
            return proxiedList.indexOf(o);
        }

        public int lastIndexOf(Object o) {
            return proxiedList.lastIndexOf(o);
        }

        public ListIterator<ExtendedNavigator> listIterator() {
            dirty = true;
            return proxiedList.listIterator();
        }

        public ListIterator<ExtendedNavigator> listIterator(int index) {
            dirty = true;
            return proxiedList.listIterator(index);
        }

        public List<ExtendedNavigator> subList(int fromIndex, int toIndex) {
            return proxiedList.subList(fromIndex, toIndex);
        }
    }

    /**
     * This is the interface class to velocity.
     */
    public static final class ExtendedNavigator {
        
        private String name;
        private String urlFragment;
        private int count;
        private boolean selected = false;

        /**
         * 
         * @param displayName 
         * @param urlFragment 
         * @param count 
         */
        public ExtendedNavigator(final String displayName, final String urlFragment, final int count) {
            this.name = displayName;
            this.urlFragment = urlFragment;
            this.count = count;
        }

        /**
         * 
         * @param selected 
         */
        public void setSelected(final boolean selected) {
            this.selected = selected;
        }

        /**
         * 
         * @return 
         */
        public boolean isSelected() {
            return selected;
        }

        /**
         * 
         * @return 
         */
        public String getName() {
            return name;
        }

        /**
         * 
         * @param name 
         */
        public void setName(final String name) {
            this.name = name;
        }

        /**
         * 
         * @return 
         */
        public String getUrlFragment() {
            return urlFragment;
        }

        /**
         * 
         * @return 
         */
        public int getCount() {
            return count;
        }
    }
}
