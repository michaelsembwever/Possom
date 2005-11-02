package no.schibstedsok.front.searchportal.util;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public interface Scorable {
    Float getScore();
    String getKey();
    void addScore(float f);
}
