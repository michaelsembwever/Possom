package no.sesat.mojo.modes;

/**
 * Function wrapper.
 *
 */
public interface NameFilter {

    /**
     * @param string
     *            String that we want to be filtered.
     * @return The filtered string.
     */
    String filter(String string);
}
