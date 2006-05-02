// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class Enrichment implements Comparable {

    private int analysisResult;
    private String name;

    public Enrichment(final int analysisResult, final String name) {
        this.analysisResult = analysisResult;
        this.name = name;
    }

    public int getAnalysisResult() {
        return analysisResult;
    }

    public String getName() {
        return name;
    }

    public int compareTo(final Object o) {
        final Enrichment e = (Enrichment) o;
        final Integer otherScore = Integer.valueOf(e.getAnalysisResult());
        final Integer thisScore = Integer.valueOf(analysisResult);
        return otherScore.compareTo(thisScore);
    }
}
