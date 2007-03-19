// Copyright (2007) Schibsted Søk AS
/*
 * YellowGeoSearchCommand.java
 *
 * Created on 17. august 2006, 10:41
 *
 */

package no.schibstedsok.searchportal.mode.command;


/**
 *
 * @author ssthkjer
 */
public class YellowGeoSearchCommand extends YellowSearchCommand {

    private String additionalFilter;

    /** Creates a new instance of YellowGeoSearchCommand */
    public YellowGeoSearchCommand(final Context cxt) {

        super(cxt);
    }

    protected String getSortBy() {
        return getSearchConfiguration().getSortBy();
    }

}
