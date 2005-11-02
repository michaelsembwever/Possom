package no.schibstedsok.front.searchportal.query;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public interface StopWordRemover {

    public String removeStopWords(String originalQuery);

}
