/*
 * Copyright (2005) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.view.spell;

public class RelevantQuery implements Comparable {

    private String query;
    private Integer weight;

    public RelevantQuery(String query, Integer weight) {
        this.query = query;
        this.weight = weight;
    }

    public int compareTo(Object o) {
        RelevantQuery q = (RelevantQuery) o;
        
        return q.getWeight().compareTo(weight);
        
    }

    /**
     * Get the query.
     *
     * @return the query.
     */
    public String getQuery() {
        return query;
    }

    /**
     * Get the weight.
     *
     * @return the weight.
     */
    public Integer getWeight() {
        return weight;
    }
}
