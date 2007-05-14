// Copyright (2007) Schibsted Søk AS
/*
 * P4SearchCommand.java
 *
 * Created on June 26, 2006, 10:42 AM
 *
 */

package no.schibstedsok.searchportal.mode.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
import org.apache.log4j.Logger;

/**
 * Command used for P4 summer compatition. There is one trigger word for each
 * day. For each word there is a code. The words, dates and codes are read
 * from the dailyWords.txt file.
 */
public final class DailyWordCommand extends AbstractSearchCommand {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(DailyWordCommand.class);

    private static final String FIELD_WORD = "word";
    private static final String FIELD_CODE = "code";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final Map<String,DailyWord> WORDS = new HashMap<String,DailyWord>();


    // Attributes ----------------------------------------------------


    // Static --------------------------------------------------------

    static{

        final InputStream wordStream = DailyWordCommand.class.getResourceAsStream("/dailyWords.txt");

        if( null != wordStream ){
            try {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(wordStream, "UTF-8"));
                String row = "";
                while ((row = reader.readLine()) != null) {
                    addDailyWord(row);
                }

            } catch (ParseException ex) {
                LOG.error(ex.getMessage(), ex);
            } catch (UnsupportedEncodingException ex) {
                LOG.error(ex.getMessage(), ex);
            } catch (IOException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }
    }

    // Constructors --------------------------------------------------

    /** Creates a new instance of P4SearchCommand 
     * @param cxt 
     */
    public DailyWordCommand(final Context cxt) {

        super(cxt);
    }

    // Public --------------------------------------------------------

    public ResultList<? extends ResultItem> execute() {

        final ResultList<ResultItem> result = new BasicSearchResult<ResultItem>();

        result.setHitCount(0);

        if (WORDS.containsKey(datamodel.getQuery().getString().toLowerCase())) {
            final DailyWord word = WORDS.get(datamodel.getQuery().getString().toLowerCase());
            if (word.isActive(new Date())) {
                ResultItem item = new BasicSearchResultItem();
                item = item.addField(FIELD_WORD, word.getWord());
                item = item.addField(FIELD_CODE, word.getCode());
                result.addResult(item);
                result.setHitCount(1);
            }
        }
        return result;
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    private static void addDailyWord(final String row) throws ParseException {

        final String[] fields = row.split(";");

        if (fields.length == 4) {
            final Date startDate = new SimpleDateFormat(DATE_FORMAT).parse(fields[0]);
            final Date endDate = new SimpleDateFormat(DATE_FORMAT).parse(fields[1]);

            final DailyWord word = new DailyWord(fields[2], startDate, endDate, fields[3]);
            WORDS.put(word.getWord(), word);
        }
    }

    // Inner classes -------------------------------------------------

    private static class DailyWord {
        final String word;
        final Date startDate;
        final Date endDate;
        final String code;

        public DailyWord(final String word, final Date startDate, final Date endDate, final String code) {
            this.word = word;
            this.startDate = startDate;
            this.endDate = endDate;
            this.code = code;
        }

        public boolean isActive(final Date date) {
            return (date.equals(startDate) || date.after(startDate)) && (date.equals(endDate) || date.before(endDate));
        }

        public String getCode() {
            return code;
        }


        public Date getStartDate() {
            return startDate;
        }

        public Date getEndDate() {
            return endDate;
        }

        public String getWord() {
            return word;
        }
    }
}
