package no.schibstedsok.searchportal.site.config;

/**
 * @author magnuse
 */
public enum Spi {

    /** */
    SITE("site"),
    /** */
    QUERY_TRANSFORM_CONFIG("query-transform-config"),
    /** */
    QUERY_TRANSFORM_CONTROL("query-transform-control", QUERY_TRANSFORM_CONFIG),
    /** */
    RESULT_HANDLER_CONFIG("result-handler-config"),
    /** */
    RESULT_HANDLER_CONTROL("result-handler-control,", RESULT_HANDLER_CONFIG),
    /** */
    SEARCH_COMMAND_CONFIG("command-config"),
    /** */
    SEARCH_COMMAND_CONTROL("command-control", SEARCH_COMMAND_CONFIG),
    /** */
    RUN_HANDLER("run-handler"),
    /** */
    RUN_TRANSFORM("run-transform");

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

    public String toString() {
        return canonicalName;
    }
}
