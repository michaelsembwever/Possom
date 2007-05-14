// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.commons.ioc.ContextWrapper;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.view.i18n.TextMessages;
import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;


/**
 * Calculate Age.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public final class AgeCalculatorResultHandler implements ResultHandler {

    private static final Logger LOG = Logger.getLogger(AgeCalculatorResultHandler.class);
    private static final String FAST_DATE_FMT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private final AgeCalculatorResultHandlerConfig config;

    /**
     * @param config
     */
    public AgeCalculatorResultHandler(final ResultHandlerConfig config) {
        this.config = (AgeCalculatorResultHandlerConfig) config;
    }

    /**
     * @inherit *
     */
    public void handleResult(final Context cxt, final DataModel datamodel) {

        final String fmt = /*dateFormat != null ? dateFormat :*/ FAST_DATE_FMT;
        final String ageFormatKey = config.getAgeFormatKey();
        final DateFormat df = new SimpleDateFormat(fmt);

        // Zulu time is UTC. But java doesn't know that.
        if (fmt.endsWith("'Z'")) {
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        setAgeForAll(cxt.getSearchResult(), df, datamodel, cxt, ageFormatKey);
    }

    private void setAgeForAll(
            final ResultList<ResultItem> searchResult,
            final DateFormat df,
            final DataModel datamodel,
            final Context cxt,
            final String ageFormatKey) {

        for (final ResultItem item : searchResult.getResults()) {
            if (item instanceof ResultList<?>) {
                ResultList<ResultItem> subResult = (ResultList<ResultItem>)item;
                if (subResult != null) {
                    setAgeForAll(subResult, df, datamodel, cxt, ageFormatKey);
                }
            }
            searchResult.replaceResult(item, setAge(item, df, datamodel, cxt, ageFormatKey));
        }
    }

    private ResultItem setAge(
            ResultItem item,
            final DateFormat df,
            final DataModel datamodel,
            final Context cxt,
            final String ageFormatKey) {

        final String docTime = item.getField(config.getSourceField());

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

                final Map<String, Object> parameters = datamodel.getJunkYard().getValues();
                String ageString = "";
                // = TextMessages.getMessages().getMessage(currentLocale, ageFormatKey, dateParts);
                final String s = parameters.get("contentsource") instanceof String[]
                        ? ((String[]) parameters.get("contentsource"))[0]
                        : (String) parameters.get("contentsource");

                final TextMessages txtMsgs = TextMessages.valueOf(ContextWrapper.wrap(
                        TextMessages.Context.class,
                        cxt,
                        new SiteContext() {
                            public Site getSite() {
                                return datamodel.getSite().getSite();
                            }
                        }));

                //older than 3 days or source is Mediearkivet, show short date format.
                if (dateParts[0].longValue() > 3 || s != null && s.equals("Mediearkivet") || config.getAsDate()) {

                    final DateFormat shortFmt = DateFormat.getDateInstance(
                            DateFormat.SHORT,
                            datamodel.getSite().getSite().getLocale());

                    ageString = shortFmt.format(new Date(stamp));
                    //more than 1 day, show days
                } else if (dateParts[0].longValue() > 0) {
                    dateParts[1] = Long.valueOf(0);
                    dateParts[2] = Long.valueOf(0);
                    ageString = txtMsgs.getMessage(ageFormatKey, (Object[]) dateParts);
                    //more than 1 hour, show hours
                } else if (dateParts[1].longValue() > 0) {
                    dateParts[2] = Long.valueOf(0);
                    ageString = txtMsgs.getMessage(ageFormatKey, (Object[]) dateParts);
                    //if less than 1 hour, show minutes
                } else if (dateParts[2].longValue() > 0) {
                    dateParts[0] = Long.valueOf(0);
                    dateParts[1] = Long.valueOf(0);
                    ageString = txtMsgs.getMessage(ageFormatKey, (Object[]) dateParts);
                } else {
                    ageString = docTime.substring(8, 10) + "."
                            + docTime.substring(5, 7) + "." + docTime.substring(0, 4);
                }
                LOG.trace("Resulting age string is " + ageString);

                if (stamp > 0) {
                    item = item.addField(config.getTargetField(), ageString);
                }

            } catch (ParseException e) {
                LOG.warn("Unparsable date: " + docTime);
            }
        } else {
            LOG.warn(config.getSourceField() + " is null");
        }
        
        return item;
    }

}
