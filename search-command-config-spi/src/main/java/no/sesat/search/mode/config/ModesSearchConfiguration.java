package no.sesat.search.mode.config;

import org.w3c.dom.Element;

public interface ModesSearchConfiguration {

    /**
     * This method will apply the attributes found in element.
     *
     * @param element
     *            The xml element where the attribues are found.
     * @param inherit
     *            The configuration that we inherit from.
     */
    public void readSearchConfiguration(final Element element, final SearchConfiguration inherit);
}
