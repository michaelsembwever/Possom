// Copyright (2007) Schibsted Søk AS
/*
 * ESPFastSearchCommand.java
 *
 * Created on May 30, 2006, 3:01 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;


/**
 * A search command used for querrying FAST ESP 5.0 query servers.
 */
public class ESPFastSearchCommand extends AbstractESPFastSearchCommand {

    /** Creates a new instance of FastSearchCommand
     *
     * @param cxt Search command context.
     * @param parameters Search command parameters.
     */
    public ESPFastSearchCommand(final Context cxt) {

        super(cxt);
    }
}
