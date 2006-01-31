/*
 * Copyright (2005) Schibsted S�k AS
 */
package no.schibstedsok.front.searchportal.query.token;

public class TokenMatch
 implements Comparable {

    private String token;
    private String match;
    private Integer start;
    private Integer end;

    public TokenMatch(String token, String match, int start, int end) {
        this.token = token;
        this.match = match;
        this.start = new Integer(start);
        this.end = new Integer(end);
    }

    public int compareTo(Object o) {
        TokenMatch other = (TokenMatch) o;
        
        return start.compareTo(other.getStart());
    }

    /**
     * Get the start index.
     *
     * @return the end index.
     */
    public Integer getStart() {
        return start;
    }

    /**
     * Get the match.
     *
     * @return the match.
     */
    public String getMatch() {
        return match;
    }

    /**
     * Get the token.
     *
     * @return the token.
     */
    public String getToken() {
        return token;
    }

    /**
     * Get the end.
     *
     * @return the end.
     */
    public Integer getEnd() {
        return end;
    }
}
