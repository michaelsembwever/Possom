/*
 * Copyright (2005) Schibsted S¿k AS
 * 
 */
package no.schibstedsok.front.searchportal.response;

import no.fast.ds.search.IDocumentSummary;

/**
 *
 * This class holds a search result element for the "yellow" collection.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class FastCompaniesSearchResult extends FastSearchResult {

    private String companyName;

    private String address;
    private String address2;
    private String postalCode;
    private String postalCode2;
    private String city;
    private String city2;
    private String phoneNumber;

    public FastCompaniesSearchResult(IDocumentSummary summary) {
        super(summary);
    }

    protected void populateFields(IDocumentSummary summary) {
        companyName =  getSummaryField(summary, "ypnavn");
        address = getSummaryField(summary, "ypadresse");
        address2 = getSummaryField(summary, "ypadresse2");
        postalCode = getSummaryField(summary, "yppostnr");
        postalCode2 = getSummaryField(summary, "yppostnr2");
        city = getSummaryField(summary, "yppoststed");
        city2 = getSummaryField(summary, "yppoststed");
        phoneNumber = getSummaryField(summary, "yptelefon");
    }

    /**
     *
     * Returns the visiting address (street name) of the company.
     *
     * @return The visiting address.
     */
    public String getVisitingAddress() {
        return address2 != null ? address2 : address;
    }

    /**
     *
     * Returns the zip code of the visiting address.
     *
     * @return The zip code.
     */
    public String getVisitingZip() {
        return postalCode2 != null ? postalCode2 : postalCode;
    }

    /**
     *
     * Returns the city part of the visiting address.
     *
     * @return The city name.
     */
    public String getVisitingCity() {
        return city2 != null ? city2 : city;
    }

    /**
     *
     * Returns the company name.
     *
     * @return The company name.
     */
    public String getCompanyName() {
        return companyName;
    }


    /**
     *
     * Returns the phone number.
     *
     * @return The phone number.
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     *
     * Sets the phone number.
     *
     * @param phoneNumber
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

}
