/*
 * Copyright (2005) Schibsted Søk AS
 * 
 */
package no.schibstedsok.front.searchportal.response;

import no.fast.ds.search.IDocumentSummary;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class FastPersonsSearchResult extends FastSearchResult {

    private String firstName;
    private String lastName;
    private String middleName;
    private String phoneNumber;
    private String address;
    private String postalCode;
    private String city;
    private String cellPhone;

    public FastPersonsSearchResult(IDocumentSummary summary) {
        super(summary);
    }

    protected void populateFields(IDocumentSummary summary) {
        this.firstName = getSummaryField(summary, "wpfornavn");
        this.lastName = getSummaryField(summary, "wpetternavn");
        this.middleName = getSummaryField(summary, "wpmellomnavn");
        this.phoneNumber = getSummaryField(summary, "wptelefon");
        this.address = getSummaryField(summary, "wpadresse");
        this.postalCode = getSummaryField(summary, "ywpostnr");
        this.city = getSummaryField(summary, "ywpoststed");
        this.cellPhone = getSummaryField(summary, "wpmobiltelefon");

        super.populateFields(summary);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public String getName() {

        StringBuffer buf = new StringBuffer();

        buf.append(this.firstName);

        if (this.middleName != null) {
            buf.append(" ");
            buf.append(this.middleName);
        }

        buf.append(" ");
        buf.append(this.lastName);

        return buf.toString();
    }

    public String getCity() {
        return this.city;
    }

    public String getZip() {
        return postalCode;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber != null ? phoneNumber : cellPhone;
    }
}
