/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.query.token;

/**
 **/
public class TokenMatch
 implements Comparable {

    private String token;
    private String match;
    private Integer start;
    private Integer end;
    /**
     * Holds value of property _touched.
     */
    private boolean touched = false;

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

    /**
     * Getter for property touched.
     * @return Value of property touched.
     */
    public boolean isTouched() {
        return touched;
    }

    /**
     * Setter for property touched.
     * @param touched New value of property touched.
     */
    public void setTouched(final boolean touched) {
        this.touched = touched;
    }
}
