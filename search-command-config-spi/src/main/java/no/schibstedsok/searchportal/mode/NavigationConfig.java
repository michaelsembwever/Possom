package no.schibstedsok.searchportal.mode;

import java.io.Serializable;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import static no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
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
 * @version $Id$
 */
public final class NavigationConfig implements Serializable {

    private static final Logger LOG = Logger.getLogger(NavigationConfig.class);

    private static final String NAVIGATION_ELEMENT = "navigation";
    private static final String NAV_ELEMENT = "nav";

    private final Map<String, Nav> navMap = new HashMap<String, Nav>();
    private final Map<String, Navigation> navigationMap = new HashMap<String, Navigation>();
    private final List<Navigation> navigationList = new ArrayList<Navigation>();

    public NavigationConfig() {}

    public NavigationConfig readNavigationConfig(final Element element, final NavigationConfig inherit) {

        // inheritence first so that self-configuration can override
        if(null != inherit){
            navMap.putAll(inherit.getNavMap());
            navigationMap.putAll(inherit.getNavigationMap());
            navigationList.addAll(inherit.getNavigationList());
        }

        final List<Element> navigationElements = getDirectChildren(element, NAVIGATION_ELEMENT);

        for (Element navigationElement : navigationElements) {
            final Navigation navigation = new Navigation(navigationElement, navMap);
            navigationList.add(navigation);
            if (navigation.getId() != null) {
                navigationMap.put(navigation.getId(), navigation);
            }
        }

        return this;
    }

    public Map<String, Nav> getNavMap() {
        return Collections.unmodifiableMap(navMap);
    }

    public Map<String, Navigation> getNavigationMap() {
        return Collections.unmodifiableMap(navigationMap);
    }

    public List<Navigation> getNavigationList() {
        return Collections.unmodifiableList(navigationList);
    }

    private static List<Element> getDirectChildren(final Element element, final String elementName) {

        final List<Element> children = new ArrayList<Element>();
        if (element != null) {
            final NodeList childNodes = element.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node childNode = childNodes.item(i);
                if (childNode instanceof Element && childNode.getNodeName().equals(elementName)) {
                    children.add((Element) childNode);
                }
            }
        }
        return children;
    }


    public static final class Navigation {

        private String id;
        private String commandName;
        private String tab;
        private boolean out = false;
        private List<Nav> navList;
        private Map<String, Nav> navMap;
        private Set<String> resetNavSet;
        private static final String RESET_NAV_ELEMENT = "reset-nav";

        public Navigation() {
        }

        public Navigation(final Element navigationElement, final Map<String, Nav> navMap) {

            AbstractDocumentFactory.fillBeanProperty(this, null, "id", ParseType.String, navigationElement, null);
            AbstractDocumentFactory.fillBeanProperty(this, null, "commandName", ParseType.String, navigationElement, null);
            AbstractDocumentFactory.fillBeanProperty(this, null, "tab", ParseType.String, navigationElement, null);
            AbstractDocumentFactory.fillBeanProperty(this, null, "out", ParseType.Boolean, navigationElement, null);

            final List<Element> navElements = getDirectChildren(navigationElement, NAV_ELEMENT);
            navList = new ArrayList<Nav>(navElements.size());
            this.navMap = new HashMap<String, Nav>();
            for (Element navElement : navElements) {
                Nav nav = new Nav(this, navElement);
                navList.add(nav);
                updateNavMap(nav, navMap);
                updateNavMap(nav, this.navMap);
            }
            final List<Element> resetNavElements = getDirectChildren(navigationElement, RESET_NAV_ELEMENT);
            resetNavSet = new HashSet<String>(resetNavElements.size());
            for (Element resetNavElement : resetNavElements) {
                String id = resetNavElement.getAttribute("id");
                if (id != null) {
                    Nav nav = navMap.get(id);
                    if (nav != null) {
                        addReset(nav);
                    } else {
                        LOG.error("Error in config, <reset-nav id=\"" + id + "\" />, nav with id=" + id + " not found");
                    }
                }
            }
        }

        private void addReset(final Nav nav) {
            if (nav != null) {
                resetNavSet.add(nav.getField());
                if (nav.getChildNavs() != null) {
                    for (Nav childNav : nav.getChildNavs()) {
                        addReset(childNav);
                    }
                }
            }
        }

        private void updateNavMap(final Nav nav, final Map<String, Nav> navMap) {
            navMap.put(nav.getId(), nav);
            if (nav.getChildNavs() != null && nav.getChildNavs().size() > 0) {
                for (Nav subNav : nav.getChildNavs()) {
                    updateNavMap(subNav, navMap);
                }
            }
        }

        public String getId() {
            return id;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public Map<String, Nav> getNavMap() {
            return navMap;
        }

        public List<Nav> getNavList() {
            return navList;
        }

        public Set<String> getResetNavSet() {
            return resetNavSet;
        }

        public void setNavList(final List<Nav> navList) {
            this.navList = navList;
        }

        public String getTab() {
            return tab;
        }

        public void setTab(final String tab) {
            this.tab = tab;
        }

        public String getCommandName() {
            return commandName;
        }

        public void setCommandName(final String commandName) {
            this.commandName = commandName;
        }

        public boolean isOut() {
            return out;
        }

        public void setOut(final boolean out) {
            this.out = out;
        }

        @Override
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

    public static final class Nav {

        private static final String OPTION_ELEMENT = "option";
        private static final String STATIC_PARAMETER_ELEMENT = "static-parameter";
        private String id;
        private String commandName;
        private String field;
        private String tab;
        private String backText;
        private boolean out;
        private boolean realNavigator;

        private List<Option> options;
        private Map<String, String> staticParameters;
        private List<Nav> childNavs;
        private final Navigation navigation;
        private final Nav parentNav;
        private boolean excludeQuery = false;

        private Nav(final Nav parentNav, final Navigation navigation, final Element navElement) {

            this.navigation = navigation;
            this.parentNav = parentNav;

            AbstractDocumentFactory.fillBeanProperty(
                    this,
                    null,
                    "commandName",
                    ParseType.String,
                    navElement,
                    navigation.getCommandName());

            AbstractDocumentFactory
                    .fillBeanProperty(this, null, "id", ParseType.String, navElement, null);

            AbstractDocumentFactory
                    .fillBeanProperty(this, null, "field", ParseType.String, navElement, id);

            AbstractDocumentFactory
                    .fillBeanProperty(this, null, "tab", ParseType.String, navElement, navigation.getTab());

            AbstractDocumentFactory.fillBeanProperty(
                    this,
                    null,
                    "out",
                    ParseType.Boolean,
                    navElement,
                    Boolean.toString(navigation.isOut()));

            AbstractDocumentFactory
                    .fillBeanProperty(this, null, "excludeQuery", ParseType.Boolean, navElement, "false");

            AbstractDocumentFactory
                    .fillBeanProperty(this, null, "realNavigator", ParseType.Boolean, navElement, "true");

            AbstractDocumentFactory
                    .fillBeanProperty(this, null, "backText", ParseType.String, navElement, "");
            

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
                options.add(new Option(optionElement, this));
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
        }

        private Nav(final Navigation navigation, final Element navElement) {
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

        public void setRealNavigator(final boolean realNavigator) {
            this.realNavigator = realNavigator;
        }

        public List<Option> getOptions() {
            return options;
        }

        public void setOptions(final List<Option> options) {
            this.options = options;
        }

        public Map<String, String> getStaticParameters() {
            return staticParameters;
        }

        public void setStaticParameters(final Map<String, String> staticParameters) {
            this.staticParameters = staticParameters;
        }

        public void setExcludeQuery(final boolean excludeQuery) {
            this.excludeQuery = excludeQuery;
        }

        public boolean isExcludeQuery() {
            return excludeQuery;
        }

        public String getId() {
            return id;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public String getTabByValue(String value) {
            if (options != null) {
                for (Option option : options) {
                    if (option.getValue() != null && option.getValue().equals(value)) {
                        return option.getTab();
                    }
                }
            }
            return null;
        }

        public String getTab() {
            return tab;
        }

        public void setTab(final String tab) {
            this.tab = tab;
        }

        public boolean isOut() {
            return out;
        }

        public void setOut(final boolean out) {
            this.out = out;
        }

        public String getCommandName() {
            return commandName;
        }

        public void setCommandName(final String commandName) {
            this.commandName = commandName;
        }

        public String getField() {
            return field;
        }

        public void setField(final String field) {
            this.field = field;
        }

        public String getBackText() {
            return backText;
        }

        public void setBackText(String backText) {
            this.backText = backText;
        }

        @Override
        public String toString() {

            return "Nav{"
                    + "id='" + id + '\''
                    + ", commandName='" + commandName + '\''
                    + ", field='" + field + '\''
                    + ", options=" + options
                    + ", staticParameters=" + staticParameters
                    + '}';
        }
    }

    public static final class Option {

        private String value;
        private String displayName;
        private String valueRef;
        private boolean realNavigator;
        private boolean defaultSelect;
        private String defaultSelectValueRef;
        private String tab;
        private boolean useHitCount;
        private String commandName;

        private Option(Element optionElement, Nav parentNav) {

            AbstractDocumentFactory
                    .fillBeanProperty(this, null, "value", ParseType.String, optionElement, null);
            AbstractDocumentFactory
                    .fillBeanProperty(this, null, "displayName", ParseType.String, optionElement, null);
            AbstractDocumentFactory
                    .fillBeanProperty(this, null, "valueRef", ParseType.String, optionElement, null);
            AbstractDocumentFactory
                    .fillBeanProperty(this, null, "realNavigator", ParseType.Boolean, optionElement, "false");
            AbstractDocumentFactory
                    .fillBeanProperty(this, null, "defaultSelect", ParseType.Boolean, optionElement, "false");
            AbstractDocumentFactory
                    .fillBeanProperty(this, null, "defaultSelectValueRef", ParseType.String, optionElement, null);
            AbstractDocumentFactory
                    .fillBeanProperty(this, null, "tab", ParseType.String, optionElement, parentNav.getTab());
            AbstractDocumentFactory
                    .fillBeanProperty(this, null, "useHitCount", ParseType.Boolean, optionElement, "false");
            AbstractDocumentFactory
                    .fillBeanProperty(this, null, "commandName", ParseType.String, optionElement, parentNav.getCommandName());
        }

        public boolean isDefaultSelect() {
            return defaultSelect;
        }

        public void setDefaultSelect(final boolean defaultSelect) {
            this.defaultSelect = defaultSelect;
        }

        public String getDefaultSelectValueRef() {
            return defaultSelectValueRef;
        }

        public void setDefaultSelectValueRef(String defaultSelectValueRef) {
            this.defaultSelectValueRef = defaultSelectValueRef;
        }

        public String getTab() {
            return tab;
        }

        public void setTab(String tab) {
            this.tab = tab;
        }

        public boolean isUseHitCount() {
            return useHitCount;
        }

        public void setUseHitCount(boolean useHitCount) {
            this.useHitCount = useHitCount;
        }

        public String getCommandName() {
            return commandName;
        }

        public void setCommandName(String commandName) {
            this.commandName = commandName;
        }

        public boolean isRealNavigator() {
            return realNavigator;
        }

        public void setRealNavigator(final boolean realNavigator) {
            this.realNavigator = realNavigator;
        }

        public String getValue() {
            return value;
        }

        public void setValue(final String value) {
            this.value = value;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(final String displayName) {
            this.displayName = displayName;
        }

        public String getValueRef() {
            return valueRef;
        }

        public void setValueRef(final String valueRef) {
            this.valueRef = valueRef;
        }

        @Override
        public String toString() {

            return "Option{"
                    + "value='" + value + '\''
                    + ", displayName='" + displayName + '\''
                    + ", valueRef='" + valueRef + '\''
                    + '}';
        }
    }
}