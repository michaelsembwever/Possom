package no.schibstedsok.front.searchportal.result;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class Enrichment implements Comparable {

    private int analysisResult;
    private String name;

    public Enrichment(int analysisResult, String name) {
        this.analysisResult = analysisResult;
        this.name = name;
    }

    public int getAnalysisResult() {
        return analysisResult;
    }

    public String getName() {
        return name;
    }

    public int compareTo(Object o) {
        Enrichment e = (Enrichment) o;
        Integer otherScore = new Integer(e.getAnalysisResult());
        Integer thisScore = new Integer(analysisResult);
        return otherScore.compareTo(thisScore);
    }
}
