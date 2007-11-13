package no.sesat.search.sitemap;

import java.util.Iterator;

/**
 * Base class for sitemap page providers. Extends this class to provide your own URLs that should be a part of the
 * installation-wide sitemap. Your implementation will be instantiated using the default constructor.
 *
 * @author maek
 */
public abstract class PageProvider implements Iterable<Page> {

    /**
     *
     */
    public PageProvider() {
    }

    /**
     * The name of the sitemap. Note that this is not the final filename but a descriptive name like sesam-nyheter
     * or katalog-infopages
     *
     * @return the name of the sitemap.
     */
    public abstract String getName();

    /**
     * Iterator for the pages to be put in sitemap.
     *
     * @return iterator for site map pages.
     */
    public abstract Iterator<no.sesat.search.sitemap.Page> iterator();
}
