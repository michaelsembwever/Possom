package no.schibstedsok.front.searchportal.query;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class NormalizePhoneNumberTransformer extends AbstractQueryTransformer{

    private transient static Pattern countryPrefix = Pattern.compile("(\\+|00)47");
    private transient static Pattern phoneNumber = Pattern.compile("(\\d)\\s{0,1}(\\d)\\s{0,1}(\\d)\\s{0,1}(\\d)\\s{0,1}(\\d)\\s{0,1}(\\d)\\s{0,1}(\\d)\\s{0,1}(\\d)\\s{0,1}");

    public String getTransformedQuery(String originalQuery) {
        Matcher m = countryPrefix.matcher(originalQuery);

        originalQuery = m.replaceAll("");

        m = phoneNumber.matcher(originalQuery);

        if (m.matches()) {
            originalQuery = m.replaceAll("$1$2$3$4$5$6$7$8");
        }

        return originalQuery;
    }


    public static void main(String[] args) {
        QueryTransformer t = new NormalizePhoneNumberTransformer();
        System.out.println(t.getTransformedQuery("974 0 33 06"));
    }
}
