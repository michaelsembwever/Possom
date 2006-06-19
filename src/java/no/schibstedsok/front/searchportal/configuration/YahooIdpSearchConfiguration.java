// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.configuration;


/**
 * @author mick
 * @version <tt>$Id$</tt>
 */
public final class YahooIdpSearchConfiguration extends AbstractYahooSearchConfiguration {

    public YahooIdpSearchConfiguration(){
        super(null);
    }

    public YahooIdpSearchConfiguration(final SearchConfiguration asc){
        super(asc);
        if(asc != null && asc instanceof YahooIdpSearchConfiguration){
            final YahooIdpSearchConfiguration ysc = (YahooIdpSearchConfiguration) asc;
            database = ysc.database;
            dateRange = ysc.dateRange;
            regionMix = ysc.regionMix;
            spellState = ysc.spellState;
            unique = ysc.unique;
        }
    }

    /**
     * Holds value of property database.
     */
    private String database;

    /**
     * Getter for property database.
     * @return Value of property database.
     */
    public String getDatabase() {
        return this.database;
    }

    /**
     * Setter for property database.
     * @param database New value of property database.
     */
    public void setDatabase(final String database) {
        this.database = database;
    }

    /**
     * Holds value of property dateRange.
     */
    private String dateRange;

    /**
     * Getter for property dateRange.
     * @return Value of property dateRange.
     */
    public String getDateRange() {
        return this.dateRange;
    }

    /**
     * Setter for property dateRange.
     * @param dateRange New value of property dateRange.
     */
    public void setDateRange(final String dateRange) {
        this.dateRange = dateRange;
    }

    /**
     * Holds value of property spellState.
     */
    private String spellState;

    /**
     * Getter for property spellstate.
     * @return Value of property spellstate.
     */
    public String getSpellState() {
        return this.spellState;
    }

    /**
     * Setter for property spellstate.
     * @param spellstate New value of property spellstate.
     */
    public void setSpellState(final String spellState) {
        this.spellState = spellState;
    }

    /**
     * Holds value of property regionMix.
     */
    private String regionMix;

    /**
     * Getter for property regionMix.
     * @return Value of property regionMix.
     */
    public String getRegionMix() {
        return this.regionMix;
    }

    /**
     * Setter for property regionMix.
     * @param regionMix New value of property regionMix.
     */
    public void setRegionMix(final String regionMix) {
        this.regionMix = regionMix;
    }

    /**
     * Holds value of property unique.
     */
    private String unique;

    /**
     * Getter for property unique.
     * @return Value of property unique.
     */
    public String getUnique() {
        return this.unique;
    }

    /**
     * Setter for property unique.
     * @param unique New value of property unique.
     */
    public void setUnique(final String unique) {
        this.unique = unique;
    }

    /**
     * Holds value of property region.
     */
    private String region;

    /**
     * Getter for property region.
     * @return Value of property region.
     */
    public String getRegion() {
        return this.region;
    }

    /**
     * Setter for property region.
     * @param region New value of property region.
     */
    public void setRegion(String region) {
        this.region = region;
    }



}
