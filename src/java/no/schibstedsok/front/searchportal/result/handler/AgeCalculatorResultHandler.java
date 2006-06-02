// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result.handler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;
import no.schibstedsok.common.ioc.ContextWrapper;


import no.schibstedsok.front.searchportal.view.i18n.TextMessages;
import no.schibstedsok.front.searchportal.result.SearchResultItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class AgeCalculatorResultHandler implements ResultHandler {

    private static final String FAST_DATE_FMT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private String targetField;
    private String sourceField;
    private String dateFormat;
    private boolean asDate = false;
    private String ageMessageFormat;

    private static final Log LOG = LogFactory.getLog(AgeCalculatorResultHandler.class);

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

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Doctime is " + docTime);
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

                    //older than 3 days or source is Mediearkivet, show dd.mm.yyyy
                    if (dateParts[0].longValue() > 3 || s != null && s.equals("Mediearkivet") || asDate == true)
                        ageString = docTime.substring(8, 10) + "." + docTime.substring(5, 7) + "." + docTime.substring(0, 4);
                    //more than 1 day, show days
                    else if (dateParts[0].longValue() > 0) {
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
                    } else
                        ageString = docTime.substring(8, 10) + "." + docTime.substring(5, 7) + "." + docTime.substring(0, 4);

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Resulting age string is " + ageString);
                    }

                    item.addField(getTargetField(), ageString);
                } catch (ParseException e) {
                    LOG.warn("Unparsable date: " + docTime);
                }
            } else {
                LOG.warn(sourceField + " is null");
            }
        }

    }

    public String getTargetField() {
        return targetField;
    }

    public void setTargetField(final String targetField) {
        this.targetField = targetField;
    }

    public void setSourceField(final String string) {
        sourceField = string;
    }
}
