/*
 * Copyright (2007) Schibsted Søk
 *   This file is part of SESAT.
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
package no.sesat.search.view.navigation;

import no.sesat.search.datamodel.DataModel;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This implementation generates the URL components as parameters: param1=param1Value&param1=param2value. It is designed
 * for extension in a way that makes it easy to create a subclass that generates some of the url components as path
 * components (e.g. /search/param1Value/?param2=param2value).
 *
 * @author maek
 * @since 2.16
 */
public class BasicUrlGenerator extends AbstractUrlGenerator {

    private static final Logger LOG = Logger.getLogger(BasicUrlGenerator.class);

    private static final Pattern SEPARATOR_END = Pattern.compile("(&amp;|\\?|/)+$");

    private final StringBuilder urlBuilder;
    private final int prefixLength;

    /**
     * Creates a new url generator.
     *
     * @param dataModel the datamodel.
     * @param navigation the navigation to generate urls for.
     * @param state the state of the current navigations.
     */
    public BasicUrlGenerator(
            final DataModel dataModel,
            final NavigationConfig.Navigation navigation,
            final NavigationState state) {

        super(dataModel, navigation, state);
                            
        final String prefix = getPrefix();
        if (prefix == null || prefix.length() == 0) {
            throw new IllegalArgumentException("Prefix can't be empty");
        }

        this.urlBuilder = new StringBuilder(prefix).append(prefix.endsWith("/") ? "" : '/');
        this.prefixLength = urlBuilder.length();
    }

    /**
     * Returns the navigation URL for navigating nav to the given value. The navigation state is used to retrieve the
     * state of all the other navigators on the page.
     *
     * @param value the value.
     * @param nav the navigator to navigate.
     *
     * @return the URL for the state.
     */
    public final synchronized String getURL(final String value, final NavigationConfig.Nav nav) {
        return doGetURL(value, nav, Collections.<String, String>emptyMap());
    }

    /**
     * Returns the navigation URL for navigating nav to the given value. The navigation state is used to retrieve the
     * state of all the other navigators on the page.
     *
     * @param val the value.
     * @param nav the navigator to navigate.
     * @param extraComponents any extra components that should go into the URL.
     *
     * @return the URL for the state.
     */
    public String getURL(final String val, final NavigationConfig.Nav nav, final Map<String, String> extraComponents) {
        return doGetURL(val, nav ,extraComponents);
    }

    /**
     * Returns a list of components which are supposed to be generated as path components. This default implemementation
     * returns the empty set (which means that all components are generated as parameter components by default)
     *
     * Override this method to get some of your navigators components generated as path components.
     *
     * @param nav the nav.
     *
     * @return List of path components.
     */
    protected List<String> getPathComponents(final NavigationConfig.Nav nav) {
        return Collections.emptyList();
    }

    /**
     * Returns the list of components which are supposded to be generated as parameter components. The default is the
     * set of all components minus the list of path components.
     *
     * @param nav the nav.
     * @param extraUrlComponents any extra components for the URL.
     * @param value
     * @return the set of parameter components.
     */
    protected Set<String> getParameterComponents(
            final NavigationConfig.Nav nav,
            final Set<String> extraUrlComponents,
            final String value) {
        
        final Set<String> parameterComponents = getUrlComponentNames(nav, extraUrlComponents, value);

        // Remove components already handled as path components.
        parameterComponents.removeAll(getPathComponents(nav));

        return parameterComponents;
    }

    /**
     * Appends the component as a path component. This implementetion adds the value plus a slash. Value may be null or
     * empty.
     *
     * @param component the component.
     * @param value the value of the component.
     */
    protected void appendPathComponent(final String component, final String value) {
        if (null != value)
            urlBuilder.append(value).append("/");
    }

    /**
     * Appends the component and its value as a parameter component. This implementetion adds
     * <tt>component=value&</tt> if the value isn't empty.
     *
     * @param component the component.
     * @param value the value of the component.
     */
    protected void appendParameterComponent(final String component, final String value) {
        if (null != value && value.length() > 0) {
            urlBuilder.append(component);
            urlBuilder.append("=");
            urlBuilder.append(value);
            urlBuilder.append("&amp;");
        }
    }

    protected String getParameterValue(
            final String parameter,
            final String value,
            final NavigationConfig.Nav nav,
            final Map<String, String> extraParameters) {

        return parameter.equals(nav.getField()) ? enc(value) : getUrlComponentValue(nav, parameter, extraParameters);
    }

    /**
     * Returns the string builder used for generating the url.
     *
     * @return the string builder.
     */
    protected final StringBuilder getUrlBuilder() {
        return urlBuilder;
    }

    private synchronized String doGetURL(
            final String value,
            final NavigationConfig.Nav nav,
            final Map<String, String> extraParameters) {

        try {

            final List<String> pathComponents = getPathComponents(nav);
            final Set<String> parameterComponents = getParameterComponents(nav, extraParameters.keySet(), value);

            for (final String component : pathComponents) {
                appendPathComponent(component, getParameterValue(component, value, nav, extraParameters));
            }

            urlBuilder.append('?');

            for (final String component : parameterComponents) {
                appendParameterComponent(component, getParameterValue(component, value, nav, extraParameters));
            }

            if (!pathComponents.contains(nav.getField()) && !parameterComponents.contains(nav.getField())) {
                if (null != nav.getField())
                    appendParameterComponent(nav.getField(), getParameterValue(nav.getField(), value, nav, extraParameters));
            }

            return SEPARATOR_END.matcher(urlBuilder).replaceFirst("");

        } finally {
            urlBuilder.setLength(prefixLength);
        }
    }

    private String enc(final String str) {
        try {
            return str != null ? URLEncoder.encode(str, "UTF-8") : null;
        } catch (UnsupportedEncodingException e) {
            LOG.fatal("UTF-8 encoding not available");
        }
        return str;
    }
}
