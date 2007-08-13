package no.schibstedsok.searchportal.site.config;

/**
 * @author magnuse
 * @version $Id$
 */
public enum Spi {

    /** */
    SITE("site"),
    /** */
    QUERY_TRANSFORM_CONFIG("query-transform-config"),
    /** */
    QUERY_TRANSFORM_CONTROL("query-transform-control", QUERY_TRANSFORM_CONFIG),
    /** */
    RESULT("result"),
    /** */
    RESULT_HANDLER_CONFIG("result-handler-config", RESULT),
    /** */
    RESULT_HANDLER_CONTROL("result-handler-control", RESULT_HANDLER_CONFIG),
    /** */
    SEARCH_COMMAND_CONFIG("search-command-config", RESULT),
    /** */
    SEARCH_COMMAND_CONTROL("search-command-control", SEARCH_COMMAND_CONFIG),
    /** */
    VIEW_CONFIG("view-config", RESULT),
    /** */
    VIEW_CONTROL("view-control", VIEW_CONFIG),
    /** */
    RUN_HANDLER("run-handler"),
    /** */
    RUN_TRANSFORM("run-transform"),
    /** */
    VELOCITY_DIRECTIVES("velocity-directives");

    private final Spi parent;
    private final String canonicalName;

    Spi(final String canonicalName, final Spi parent) {
        this.canonicalName = canonicalName;
        this.parent = parent;
    }

    /**
     *
     */
    Spi(final String canonicalName) {
        parent = null;
        this.canonicalName = canonicalName;
    }

    /**
     *
     * @return
     */
    public Spi getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return canonicalName;
    }
}
