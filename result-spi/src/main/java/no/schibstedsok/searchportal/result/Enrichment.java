/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License

 */
package no.schibstedsok.searchportal.result;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class Enrichment implements Comparable<Enrichment> {

    private float analysisResult;
    private String name;

    public Enrichment(final float analysisResult, final String name) {
        this.analysisResult = analysisResult;
        this.name = name;
    }

    public int getAnalysisResult() {
        return (int)analysisResult;
    }

    public void setAnalysisResult(final float analysisResult) {
        this.analysisResult = analysisResult;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int compareTo(final Enrichment e) {
        return (int)(e.analysisResult - analysisResult);
    }
}
