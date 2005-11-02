package no.schibstedsok.front.searchportal.i18n;

import org.apache.struts.util.MessageResources;
import org.apache.struts.util.MessageResourcesFactory;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class TextMessages {

    private static MessageResources messages;

    static {
        MessageResourcesFactory factory = new MessageResourcesFixFactory();
        messages = factory.createResources("messages");
    }

    public static MessageResources getMessages() {
        return messages;
    }
}
