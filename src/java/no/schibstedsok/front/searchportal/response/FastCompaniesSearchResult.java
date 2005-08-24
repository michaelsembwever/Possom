/*
 * Copyright (2005) Schibsted S¿k AS
 * 
 */
package no.schibstedsok.front.searchportal.response;

import no.fast.ds.search.IDocumentSummary;

import java.util.StringTokenizer;

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
    private String postalCode;
    private String city;
    private String phoneNumber;

    public FastCompaniesSearchResult(IDocumentSummary summary) {
        super(summary);
    }

    protected void populateFields(IDocumentSummary summary) {
        companyName =  getSummaryField(summary, "ypnavn");
        address = getSummaryField(summary, "ypadresse");
        postalCode = getSummaryField(summary, "ywpostnr");
        city = getSummaryField(summary, "ywpoststed");
        phoneNumber = getSummaryField(summary, "yptelefon");
    }

    /**
     *
     * Returns the visiting address (street name) of the company.
     *
     * @return The visiting address.
     */
    public String getVisitingAddress() {
        return address;
    }

    /**
     *
     * Returns the zip code of the visiting address.
     *
     * @return The zip code.
     */
    public String getVisitingZip() {
        return postalCode;
    }

    /**
     *
     * Returns the city part of the visiting address.
     *
     * @return The city name.
     */
    public String getVisitingCity() {
        return city;
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
