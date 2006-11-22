// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;
import java.util.Date;

import no.schibstedsok.common.ioc.ContextWrapper;


import no.schibstedsok.searchportal.view.i18n.TextMessages;
import no.schibstedsok.searchportal.result.SearchResultItem;
import org.apache.log4j.Logger;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class AgeCalculatorResultHandler implements ResultHandler {

    private static final String FAST_DATE_FMT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private String targetField;
    private String sourceField;
    private String dateFormat;
    private Boolean asDate = Boolean.FALSE;
    private String ageMessageFormat;

    private static final Logger LOG = Logger.getLogger(AgeCalculatorResultHandler.class);

    /** @inherit **/
    public void handleResult(final Context cxt, final Map parameters) {

        final String fmt = dateFormat != null ? dateFormat : FAST_DATE_FMT;
        final String ageFormatKey = ageMessageFormat != null ? ageMessageFormat : "age";
        final DateFormat df = new SimpleDateFormat(fmt);

        // Zulu time is UTC. But java doesn't know that.
        if (fmt.endsWith("'Z'")) {
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        for (final SearchResultItem item : cxt.getSearchResult().getResults()) {
            final String docTime = item.getField(sourceField);

            if (docTime != null) {

                try {
                    final long stamp = df.parse(docTime).getTime();

                    final long age = System.currentTimeMillis() - stamp;

                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Doctime is " + docTime);
                    }

                    final Long dateParts[] = new Long[3];

                    dateParts[0] = Long.valueOf(age / (60 * 60 * 24 * 1000));
                    dateParts[1] = Long.valueOf(age / (60 * 60 * 1000) % 24);
                    dateParts[2] = Long.valueOf(age / (60 * 1000) % 60);

                    String ageString = ""; // = TextMessages.getMessages().getMessage(currentLocale, ageFormatKey, dateParts);
                    final String  s = parameters.get("contentsource") instanceof String[]
                            ? ((String[])parameters.get("contentsource"))[0]
                            : (String)parameters.get("contentsource");

                    final TextMessages txtMsgs = TextMessages.valueOf(
                            ContextWrapper.wrap(TextMessages.Context.class, cxt));

                    //older than 3 days or source is Mediearkivet, show short date format.
                    if (dateParts[0].longValue() > 3 || s != null && s.equals("Mediearkivet") || asDate.booleanValue()){
                        final DateFormat shortFmt = DateFormat.getDateInstance(DateFormat.SHORT, cxt.getSite().getLocale());
                        ageString = shortFmt.format(new Date(stamp));
                    //more than 1 day, show days
                    }else if (dateParts[0].longValue() > 0) {
                        dateParts[1] = Long.valueOf(0);
                        dateParts[2] = Long.valueOf(0);
                        ageString = txtMsgs.getMessage(ageFormatKey, (Object[])  dateParts);
                    //more than 1 hour, show hours
                    } else if (dateParts[1].longValue() > 0) {
                        dateParts[2] = Long.valueOf(0);
                        ageString = txtMsgs.getMessage(ageFormatKey, (Object[]) dateParts);
                    //if less than 1 hour, show minutes
                    } else if (dateParts[2].longValue() > 0) {
                        dateParts[0] = Long.valueOf(0);
                        dateParts[1] = Long.valueOf(0);
                        ageString = txtMsgs.getMessage(ageFormatKey, (Object[]) dateParts);
                    } else{
                        ageString = docTime.substring(8, 10) + "." + docTime.substring(5, 7) + "." + docTime.substring(0, 4);
                    }
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Resulting age string is " + ageString);
                    }         

                    if (stamp > 0) {
                        item.addField(getTargetField(), ageString);
                    }

                } catch (ParseException e) {
                    LOG.warn("Unparsable date: " + docTime);
                }
            } else {
                LOG.warn(sourceField + " is null");
            }
        }

    }

    /** TODO comment me. **/
    public String getTargetField() {
        return targetField;
    }

    /** TODO comment me. **/
    public void setTargetField(final String targetField) {
        this.targetField = targetField;
    }

    /** TODO comment me. **/
    public void setSourceField(final String string) {
        sourceField = string;
    }

    /** TODO comment me. **/
    public void setAsDate(final Boolean asDate) {
        this.asDate = asDate;
    }
}
