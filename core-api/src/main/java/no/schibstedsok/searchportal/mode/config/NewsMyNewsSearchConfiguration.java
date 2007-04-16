// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

public class NewsMyNewsSearchConfiguration extends AbstractSearchConfiguration {

    public NewsMyNewsSearchConfiguration(SearchConfiguration sc) {
        super(sc);
        if (sc instanceof NewsMyNewsSearchConfiguration) {
            NewsMyNewsSearchConfiguration nmsc = (NewsMyNewsSearchConfiguration) sc;
        }
    }
}
