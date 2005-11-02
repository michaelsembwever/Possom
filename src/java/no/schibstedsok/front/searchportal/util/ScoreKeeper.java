package no.schibstedsok.front.searchportal.util;

import java.util.*;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class ScoreKeeper {

    private static class ScoreComparator implements Comparator
     {
         public int compare(Object o1, Object o2)
         {
             Scorable s1 = (Scorable) o1;
             Scorable s2 = (Scorable) o2;

             return s2.getScore().compareTo(s1.getScore());
         }
     }

    private static Comparator comparator = new ScoreComparator();

    private HashMap map = new HashMap();

    public void addScore(Scorable scorable) {
        if (map.get(scorable.getKey()) == null) {
            map.put(scorable.getKey(), scorable);
        } else {
            Scorable oldScore = (Scorable) map.get(scorable.getKey());
            oldScore.addScore(scorable.getScore().floatValue());
        }
    }

    public List getSortedByScore() {
        List all = new ArrayList();
        all.addAll(map.values());
        Collections.sort(all, comparator);
        return all;
    }
}
