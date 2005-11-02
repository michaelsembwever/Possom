package no.schibstedsok.front.searchportal.i18n;

import org.apache.struts.util.MessageResourcesFactory;
import org.apache.struts.util.MessageResources;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class MessageResourcesFixFactory extends MessageResourcesFactory {
    public MessageResources createResources(String string) {
        return new MessageResourcesFix(this, string);
    }
}
