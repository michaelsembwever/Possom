package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import static no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is a command to help generating navigation urls in the view. I got tired of all
 * the URL handling velocity code.
 * <p/>
 * This should be a multiResult resulthandler, but right now this just a waiting searchCommand.
 * Usually there will be no real waiting since the calls on the results occur from velocity.
 * <p/>
 * As a bonus from using this, you don't need to data-model the commands that only are
 * there for navigation.
 *
 * @author Geir H. Pettersen(T-Rank)
 */
@Controller("NavigationCommand")
public class NavigationCommandConfig extends CommandConfig {
    private static final Logger LOG = Logger.getLogger(NavigationCommandConfig.class);
    private ExtendedNavigationConfig extendedNavigationConfig;
    private static final String NAVIGATION_ELEMENT = "navigation";
    private static final String NAV_ELEMENT = "nav";

    public NavigationCommandConfig() {
    }

    public NavigationCommandConfig(SearchConfiguration sc) {
        if (sc instanceof NavigationCommandConfig) {
            extendedNavigationConfig = ((NavigationCommandConfig) sc).getExtendedNavigationConfig();
        }
    }

    public ExtendedNavigationConfig getExtendedNavigationConfig() {
        return extendedNavigationConfig;
    }

    public void setExtendedNavigationConfig(ExtendedNavigationConfig extendedNavigationConfig) {
        this.extendedNavigationConfig = extendedNavigationConfig;
    }

    private static List<Element> getDirectChildren(Element element, String elementName) {
        ArrayList<Element> children = new ArrayList<Element>();
        if (element != null) {
            NodeList childNodes = element.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);
                if (childNode instanceof Element && childNode.getNodeName().equals(elementName)) {
                    children.add((Element) childNode);
                }
            }
        }
        return children;
    }


    @Override
    public CommandConfig readSearchConfiguration(final Element element, final SearchConfiguration inherit) {
        super.readSearchConfiguration(element, inherit);
        LOG.debug("------------- Reading search configuration ------------");
        List<Element> navigationElements = getDirectChildren(element, NAVIGATION_ELEMENT);
        HashMap<String, Nav> navMap = new HashMap<String, Nav>();
        HashMap<String, Navigation> navigationMap = new HashMap<String, Navigation>();
        List<Navigation> navigationList = new ArrayList<Navigation>(navigationElements.size());
        for (Element navigationElement : navigationElements) {
            Navigation navigation = new Navigation(navigationElement, navMap);
            navigationList.add(navigation);
            if (navigation.getId() != null) {
                navigationMap.put(navigation.getId(), navigation);
            }
        }
        extendedNavigationConfig = new ExtendedNavigationConfig(navMap, navigationMap, navigationList);
        LOG.debug("-------------------------------------------------------");
        return this;
    }

    /**
     * Clients may want to access this config to get info on available navigations.
     */
    public static class ExtendedNavigationConfig {
        private HashMap<String, Nav> navMap;
        private HashMap<String, Navigation> navigationMap;
        private List<Navigation> navigationList;

        public ExtendedNavigationConfig(HashMap<String, Nav> navMap, HashMap<String, Navigation> navigationMap, List<Navigation> navigationList) {
            this.navigationMap = navigationMap;
            this.navMap = navMap;
            this.navigationList = navigationList;
        }

        public HashMap<String, Nav> getNavMap() {
            return navMap;
        }

        public HashMap<String, Navigation> getNavigationMap() {
            return navigationMap;
        }

        public List<Navigation> getNavigationList() {
            return navigationList;
        }
    }

    public static class Navigation {
        private String id;
        private String commandName;
        private String tab;
        private boolean out = false;
        private List<Nav> navList;
        private HashMap<String, Nav> navMap;
        private Set<String> resetNavSet;
        private static final String RESET_NAV_ELEMENT = "reset-nav";
        private Nav selectedNav;


        public Navigation() {
        }

        public Navigation(Element navigationElement, HashMap<String, Nav> navMap) {
            AbstractDocumentFactory.fillBeanProperty(this, null, "id", ParseType.String, navigationElement, null);
            AbstractDocumentFactory.fillBeanProperty(this, null, "commandName", ParseType.String, navigationElement, null);
            AbstractDocumentFactory.fillBeanProperty(this, null, "tab", ParseType.String, navigationElement, null);
            AbstractDocumentFactory.fillBeanProperty(this, null, "out", ParseType.Boolean, navigationElement, null);

            final List<Element> navElements = getDirectChildren(navigationElement, NAV_ELEMENT);
            navList = new ArrayList<Nav>(navElements.size());
            this.navMap = new HashMap<String, Nav>();
            addNavElements(navElements, navMap);
            final List<Element> resetNavElements = getDirectChildren(navigationElement, RESET_NAV_ELEMENT);
            resetNavSet = new HashSet<String>(resetNavElements.size());
            for (Element resetNavElement : resetNavElements) {
                String id = resetNavElement.getAttribute("id");
                if (id != null) {
                    resetNavSet.add(id);
                }
            }
        }

        private void addNavElements(List<Element> navElements, HashMap<String, Nav> navMap) {
            for (Element navElement : navElements) {
                Nav nav = new Nav(this, navElement);
                navList.add(nav);
                updateNavMap(nav, navMap);
                updateNavMap(nav, this.navMap);
                List<Element> childNavElements = getDirectChildren(navElement, NAV_ELEMENT);
                if (childNavElements.size() > 0) {
                    addNavElements(childNavElements, navMap);
                }
            }
        }

        private void updateNavMap(Nav nav, HashMap<String, Nav> navMap) {
            navMap.put(nav.getId(), nav);
            if (nav.getChildNavs() != null && nav.getChildNavs().size() > 0) {
                for (Nav subNav : nav.getChildNavs()) {
                    updateNavMap(subNav, navMap);
                }
            }
        }

        public Nav getSelectedNav() {
            return selectedNav;
        }

        public void setSelectedNav(Nav selectedNav) {
            this.selectedNav = selectedNav;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public HashMap<String, Nav> getNavMap() {
            return navMap;
        }

        public List<Nav> getNavList() {
            return navList;
        }

        public Set<String> getResetNavSet() {
            return resetNavSet;
        }

        public void setNavList(List<Nav> navList) {
            this.navList = navList;
        }

        public String getTab() {
            return tab;
        }

        public void setTab(String tab) {
            this.tab = tab;
        }

        public String getCommandName() {
            return commandName;
        }

        public void setCommandName(String commandName) {
            this.commandName = commandName;
        }

        public boolean isOut() {
            return out;
        }

        public void setOut(boolean out) {
            this.out = out;
        }

        public String toString() {
            return "\nNavigation{" +
                    "commandName='" + commandName + '\'' +
                    ", tab='" + tab + '\'' +
                    ", out=" + out +
                    ", navList=" + navList +
                    ", resetNavSet=" + resetNavSet +
                    '}';
        }
    }

    public static class Nav {
        private static final String OPTION_ELEMENT = "option";
        private static final String STATIC_PARAMETER_ELEMENT = "static-parameter";
        private String id;
        private String commandName;
        private String field;
        private String tab;
        private boolean out;
        private boolean realNavigator;

        private List<Option> options;
        private Map<String, String> staticParameters;
        private List<Nav> childNavs;
        private Navigation navigation;
        private Nav parentNav;
        private boolean excludeQuery = false;

        private Nav(Nav parentNav, Navigation navigation, Element navElement) {
            this.navigation = navigation;
            this.parentNav = parentNav;
            AbstractDocumentFactory.fillBeanProperty(this, null, "commandName", ParseType.String, navElement, navigation.getCommandName());
            AbstractDocumentFactory.fillBeanProperty(this, null, "id", ParseType.String, navElement, null);
            AbstractDocumentFactory.fillBeanProperty(this, null, "field", ParseType.String, navElement, id);
            AbstractDocumentFactory.fillBeanProperty(this, null, "tab", ParseType.String, navElement, navigation.getTab());
            AbstractDocumentFactory.fillBeanProperty(this, null, "out", ParseType.Boolean, navElement, Boolean.toString(navigation.isOut()));
            AbstractDocumentFactory.fillBeanProperty(this, null, "excludeQuery", ParseType.Boolean, navElement, "false");
            AbstractDocumentFactory.fillBeanProperty(this, null, "realNavigator", ParseType.Boolean, navElement, "true");

            final List<Element> childNavElements = getDirectChildren(navElement, NAV_ELEMENT);
            if (childNavElements.size() > 0) {
                childNavs = new ArrayList<Nav>(childNavElements.size());
                for (Element childNavElement : childNavElements) {
                    childNavs.add(new Nav(this, this.navigation, childNavElement));
                }
            }
            final List<Element> optionElements = getDirectChildren(navElement, OPTION_ELEMENT);
            options = new ArrayList<Option>(optionElements.size());
            for (Element optionElement : optionElements) {
                options.add(new Option(optionElement));
            }
            final List<Element> staticParamElements = getDirectChildren(navElement, STATIC_PARAMETER_ELEMENT);
            staticParameters = new HashMap<String, String>();
            for (Element staticParamElement : staticParamElements) {
                String name = staticParamElement.getAttribute("name");
                String value = staticParamElement.getAttribute("value");
                if (name != null && value != null) {
                    staticParameters.put(name, value);
                }
            }
            LOG.debug("Added " + this);
        }

        private Nav(Navigation navigation, Element navElement) {
            this(null, navigation, navElement);
        }

        public Nav getParentNav() {
            return parentNav;
        }

        public Navigation getNavigation() {
            return navigation;
        }

        public List<Nav> getChildNavs() {
            return childNavs;
        }

        public boolean isRealNavigator() {
            return realNavigator;
        }

        public void setRealNavigator(boolean realNavigator) {
            this.realNavigator = realNavigator;
        }

        public List<Option> getOptions() {
            return options;
        }

        public void setOptions(List<Option> options) {
            this.options = options;
        }

        public Map<String, String> getStaticParameters() {
            return staticParameters;
        }

        public void setStaticParameters(Map<String, String> staticParameters) {
            this.staticParameters = staticParameters;
        }

        public void setExcludeQuery(boolean excludeQuery) {
            this.excludeQuery = excludeQuery;
        }

        public boolean isExcludeQuery() {
            return excludeQuery;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTab() {
            return tab;
        }

        public void setTab(String tab) {
            this.tab = tab;
        }

        public boolean isOut() {
            return out;
        }

        public void setOut(boolean out) {
            this.out = out;
        }

        public String getCommandName() {
            return commandName;
        }

        public void setCommandName(String commandName) {
            this.commandName = commandName;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }


        public String toString() {
            return "Nav{" +
                    "id='" + id + '\'' +
                    ", commandName='" + commandName + '\'' +
                    ", field='" + field + '\'' +
                    ", options=" + options +
                    ", staticParameters=" + staticParameters +
                    '}';
        }
    }

    public static class Option {
        private String value;
        private String displayName;
        private String valueRef;
        private boolean realNavigator;
        private boolean defaultSelect;

        public Option() {
        }

        private Option(Element optionElement) {
            AbstractDocumentFactory.fillBeanProperty(this, null, "value", ParseType.String, optionElement, null);
            AbstractDocumentFactory.fillBeanProperty(this, null, "displayName", ParseType.String, optionElement, null);
            AbstractDocumentFactory.fillBeanProperty(this, null, "valueRef", ParseType.String, optionElement, null);
            AbstractDocumentFactory.fillBeanProperty(this, null, "realNavigator", ParseType.Boolean, optionElement, "false");
            AbstractDocumentFactory.fillBeanProperty(this, null, "defaultSelect", ParseType.Boolean, optionElement, "false");
        }

        public boolean isDefaultSelect() {
            return defaultSelect;
        }

        public void setDefaultSelect(boolean defaultSelect) {
            this.defaultSelect = defaultSelect;
        }

        public boolean isRealNavigator() {
            return realNavigator;
        }

        public void setRealNavigator(boolean realNavigator) {
            this.realNavigator = realNavigator;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getValueRef() {
            return valueRef;
        }

        public void setValueRef(String valueRef) {
            this.valueRef = valueRef;
        }

        public String toString() {
            return "Option{" +
                    "value='" + value + '\'' +
                    ", displayName='" + displayName + '\'' +
                    ", valueRef='" + valueRef + '\'' +
                    '}';
        }
    }

//    <navigation tab="nc" out="true" command-name="newsSearch">
//        <nav id="people" field="newsCase">
//            <static-parameter name="type" value="person"/>
//        </nav>
//        <nav id="cases" field="newsCase">
//            <static-parameter name="type" value="sak"/>
//        </nav>
//    </navigation>
//    <navigation command-name="newsSearch">
//        <nav id="year">
//            <nav id="yearmonth">
//                <nav id="yearmonthday"/>
//            </nav>
//        </nav>
//    </navigation>
//    <navigation command-name="newsSearch">
//        <nav id="publisher"/>
//    </navigation>
//    <navigation>
//        <nav id="clusterId"/>
//    </navigation>
//    <navigation>
//        <nav id="sort">
//            <option value="ascending" display-name="elst først"/>
//            <option value="descending" display-name="nyest først"/>
//            <option value="relevance" display-name="relevance"/>
//        </nav>
//    </navigation>
//    <navigation command-name="newsSearch">
//        <nav field="offset">
//            <option display-name="neste" value-ref="offset"/>
//        </nav>
//    </navigation>
//    <navigation command-name="newNewsSearchNavigator">
//        <nav id="medium" command-name="newNewsSearchNavigator"/>
//        <nav id="sources" command-name="newsSearchNavigator"/>
//        <nav id="countries" command-name="newsSearchNavigator"/>
//    </navigation>

}
