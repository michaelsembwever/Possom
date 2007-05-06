package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import no.schibstedsok.searchportal.mode.config.NavigationCommandConfig;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.result.SearchResult;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * This is a command to help generating navigation urls in the view. I got tired of all
 * the URL handling velocity code. Some of the effects from this is virtually impossible to
 * code in velocity.
 * <p/>
 * This should be a multiResult resulthandler, but right now this just a waiting searchCommand.
 * Usually there will be no real waiting since the calls on the results occur from velocity.
 * <p/>
 * As a bonus from using this, you don't need to data-model the commands that only are
 * there for navigation.
 *
 * @author Geir H. Pettersen(T-Rank)
 */
public class NavigationCommand extends AbstractSearchCommand {
    private static final Logger LOG = Logger.getLogger(NavigationCommand.class);

    /**
     * @param cxt The context to execute in.
     */
    public NavigationCommand(Context cxt) {
        super(cxt);
    }

    public SearchResult execute() {
        NavigationCommandConfig config = getSearchConfiguration();
        return new ExtendedNavigationSearchResult(this, config.getExtendedNavigationConfig(), context);
    }

    @Override
    public NavigationCommandConfig getSearchConfiguration() {
        return (NavigationCommandConfig) super.getSearchConfiguration();
    }

    public static class ExtendedNavigationSearchResult extends FastSearchResult {
        private ExtendedNavigation extendedNavigation;


        public ExtendedNavigationSearchResult(NavigationCommand command, NavigationCommandConfig.ExtendedNavigationConfig extendedNavigationConfig, Context context) {
            super(command);
            this.extendedNavigation = new ExtendedNavigation(extendedNavigationConfig, context);
        }

        public ExtendedNavigation getExtendedNavigation() {
            return extendedNavigation;
        }
    }

    public static class ExtendedNavigation {
        private NavigationCommandConfig.ExtendedNavigationConfig extendedNavigationConfig;
        private Context context;


        public ExtendedNavigation(NavigationCommandConfig.ExtendedNavigationConfig extendedNavigationConfig, Context context) {
            this.extendedNavigationConfig = extendedNavigationConfig;
            this.context = context;
        }

        public NavigationCommandConfig.Navigation getNavigation(String id) {
            return extendedNavigationConfig.getNavigationMap().get(id);
        }

        /**
         * Returns extended navigators for a name(id)
         *
         * @param name the id of the navigator to get.
         * @return a list with extended navigators
         */
        public List<ExtendedNavigator> getNavigators(String name) {
            NavigationCommandConfig.Nav navEntry = extendedNavigationConfig.getNavMap().get(name);
            try {
                if (navEntry != null) {
                    boolean selectionDone = false;
                    StringDataObject selectedValue = context.getDataModel().getParameters().getValue(name);
                    List<ExtendedNavigator> extendedNavigators = new ArrayList<ExtendedNavigator>();
                    FastSearchResult fsr = null;
                    if (navEntry.getCommandName() != null) {
                        SearchResult searchResult = context.getRunningQuery().getSearchResult(navEntry.getCommandName());
                        if (searchResult instanceof FastSearchResult) {
                            fsr = (FastSearchResult) searchResult;
                            List<Modifier> modifiers = fsr.getModifiers(navEntry.isRealNavigator() ? navEntry.getField() : name);
                            if (modifiers != null && modifiers.size() > 0) {
                                for (Modifier modifier : modifiers) {
                                    final String navigatorName = modifier.getNavigator() == null ? null : modifier.getNavigator().getName();
                                    final String value = navEntry.isRealNavigator() && navigatorName != null ? navigatorName : modifier.getName();
                                    final String urlFragment = getUrlFragment(navEntry, value);
                                    ExtendedNavigator navigator = new ExtendedNavigator(modifier.getName(), urlFragment, modifier.getCount());
                                    if (!selectionDone) {
                                        selectedValue = context.getDataModel().getParameters().getValue(navEntry.getField());
                                        LOG.debug("Value = " + navEntry.getField() + ",Comparing " + (selectedValue == null ? "" : selectedValue.getString()) + " with " + value);
                                        if (selectedValue != null && selectedValue.getString().equals(value)) {
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


        private void getOptionNavigators(NavigationCommandConfig.Nav navEntry, FastSearchResult fsr, List<ExtendedNavigator> extendedNavigators, StringDataObject selectedValue) {
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
        public String getUrlFragment(String navigator, String value) {
            NavigationCommandConfig.Nav navEntry = extendedNavigationConfig.getNavMap().get(navigator);
            return getUrlFragment(navEntry, value);
        }

        public String getUrlFragment(NavigationCommandConfig.Nav navEntry, String value) {
            StringBuilder sb = new StringBuilder();
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

        private void addNavigationFragments(NavigationCommandConfig.Navigation navigation, StringBuilder sb, NavigationCommandConfig.Nav navEntry) {
            final Set<String> fieldFilterSet = new HashSet<String>();
            for (NavigationCommandConfig.Nav nav : navigation.getNavList()) {
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
                }
            }
        }

        private void addPreviousField(StringDataObject fieldValue, StringBuilder sb, NavigationCommandConfig.Nav navEntry, String fieldName) {
            if (fieldValue != null && addFragment(sb, navEntry, fieldName, fieldValue.getString())) {
                fieldValue = context.getDataModel().getParameters().getValue("nav_" + fieldName);
                if (fieldValue != null) {
                    addFragment(sb, navEntry, "nav_" + fieldName, fieldValue.getString());
                }
            }
        }

        private boolean addFragment(StringBuilder sb, NavigationCommandConfig.Nav nav, String id, String value) {
            if (!nav.getNavigation().getResetNavSet().contains(id)) {
                sb.append('&').append(enc(id)).append('=').append(enc(value));
                return true;
            }
            return false;
        }

        private void addParentFragment(StringBuilder sb, NavigationCommandConfig.Nav navEntry) {
            NavigationCommandConfig.Nav parentNav = navEntry.getParentNav();
            if (parentNav != null) {
                StringDataObject fieldValue = context.getDataModel().getParameters().getValue(parentNav.getField());
                addPreviousField(fieldValue, sb, navEntry, parentNav.getField());
                addParentFragment(sb, parentNav);
            }
        }

        private String enc(String str) {
            try {
                return URLEncoder.encode(str, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                LOG.fatal("UTF-8 encoding not available");
            }
            return str;
        }
    }

    /**
     * This is the interface class to velocity.
     */
    public static class ExtendedNavigator {
        private String name;
        private String urlFragment;
        private int count;
        private boolean selected = false;

        public ExtendedNavigator(String displayName, String urlFragment, int count) {
            this.name = displayName;
            this.urlFragment = urlFragment;
            this.count = count;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isSelected() {
            return selected;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrlFragment() {
            return urlFragment;
        }

        public int getCount() {
            return count;
        }
    }
}
