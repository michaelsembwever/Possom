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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import no.schibstedsok.searchportal.mode.command.*;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;

/**
 * Command used for P4 summer compatition. There is one trigger word for each
 * day. For each word there is a code. The words, dates and codes are read
 * from the dailyWords.txt file. 
 */
public class DailyWordCommand extends AbstractSearchCommand {

    private final static String FIELD_WORD = "word";
    private final static String FIELD_CODE = "code";
    private final static String DATE_FORMAT = "yyyy-MM-dd";
    
    static Map<String, DailyWord> words;

    /** Creates a new instance of P4SearchCommand */
    public DailyWordCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);
    }

    public SearchResult execute() {

        if (words == null) {
            words = new HashMap();
            final InputStream wordStream = DailyWordCommand.class.getResourceAsStream("/dailyWords.txt");
            BufferedReader reader;
            try {
            reader = new BufferedReader(new InputStreamReader(wordStream, "UTF-8"));
            String row = "";
                while ((row = reader.readLine()) != null) {
                    addDailyWord(row);
                }
            } catch (ParseException ex) {
                ex.printStackTrace();
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        final SearchResult result = new BasicSearchResult(this);

        result.setHitCount(0);
        
        if (words.containsKey(context.getQuery().getQueryString().toLowerCase())) {
            final DailyWord word = words.get(context.getQuery().getQueryString().toLowerCase());
            if (word.isActive(new Date())) {
                final SearchResultItem item = new BasicSearchResultItem();
                item.addField(FIELD_WORD, word.getWord());
                item.addField(FIELD_CODE, word.getCode());
                result.addResult(item);
                result.setHitCount(1);
            }
        }
        return result;
    }


    private static void addDailyWord(String row) throws ParseException {
        final String[] fields = row.split(";");

        if (fields.length == 4) {
            final Date startDate = new SimpleDateFormat(DATE_FORMAT).parse(fields[0]);
            final Date endDate = new SimpleDateFormat(DATE_FORMAT).parse(fields[1]);

            final DailyWord word = new DailyWord(fields[2], startDate, endDate, fields[3]);
            words.put(word.getWord(), word);
        }
    }
    
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
