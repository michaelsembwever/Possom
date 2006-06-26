/*
 * P4SearchCommand.java
 *
 * Created on June 26, 2006, 10:42 AM
 *
 */

package no.schibstedsok.front.searchportal.command;

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
import no.schibstedsok.front.searchportal.result.BasicSearchResult;
import no.schibstedsok.front.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.front.searchportal.result.SearchResult;
import no.schibstedsok.front.searchportal.result.SearchResultItem;

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
            if (isToday(word.getDay())) {
                final SearchResultItem item = new BasicSearchResultItem();
                item.addField(FIELD_WORD, word.getWord());
                item.addField(FIELD_CODE, word.getCode());
                result.addResult(item);
                result.setHitCount(1);
            }
        }
        return result;
    }

    private boolean isToday(Date date) {
        final Calendar today = Calendar.getInstance();
        final Calendar wordDay = Calendar.getInstance();
        
        wordDay.setTime(date);
        
        return today.get(Calendar.YEAR) == wordDay.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == wordDay.get(Calendar.DAY_OF_YEAR);
    }

    private static void addDailyWord(String row) throws ParseException {
        final String[] fields = row.split(";");

        if (fields.length == 3) {
            final Date wordDate = new SimpleDateFormat(DATE_FORMAT).parse(fields[0]);
            final DailyWord word = new DailyWord(fields[1], wordDate, fields[2]);
            words.put(word.getWord(), word);
        }
    }
    
    private static class DailyWord {
        final String word;
        final Date day;
        final String code;
        
        public DailyWord(final String word, final Date day, final String code) {
            this.word = word;
            this.day = day;
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public Date getDay() {
            return day;
        }

        public String getWord() {
            return word;
        }
    }
}
