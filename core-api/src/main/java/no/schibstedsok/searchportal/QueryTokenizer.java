package no.schibstedsok.searchportal;

import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class QueryTokenizer {

    private static int quoteChar = '"';

    public static List<String> tokenize(String query) {

        List<String> tokens = new ArrayList<String>();

        if (query != null) {

            StreamTokenizer tkn = new StreamTokenizer(new StringReader(query));
            tkn.quoteChar('"');

            int found = StreamTokenizer.TT_WORD;

            while (found != StreamTokenizer.TT_EOF) {
                try {
                    found = tkn.nextToken();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (found == StreamTokenizer.TT_WORD || found == quoteChar) {
                    tokens.add(tkn.sval);
                }
            }
        }
        return tokens;
    }
}
