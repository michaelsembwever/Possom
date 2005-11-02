package no.schibstedsok.front.searchportal.result;

import no.schibstedsok.front.searchportal.util.Scorable;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class KeywordCluster implements Scorable {

    private Float score;
    private String key;

    public KeywordCluster(String key, Float score) {
        this.score = score;
        this.key = key;
    }

    public Float getScore() {
        return score;
    }

    public String getKey() {
        return key;
    }

    public void addScore(float f) {
        score = new Float(score.floatValue() + f);
    }

    public int getClusterWidth() {
        float f = score.floatValue() * 10;
        int intScore = (int) f;
        int width = 0;
        if (intScore >= 10 && intScore <= 13)
            width = 10;
        else if (intScore >= 14 && intScore <= 16)
            width = 20;
        else if (intScore > 16)
            width = 30;
        
        return width;
    }
}
