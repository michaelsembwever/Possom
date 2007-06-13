package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.result.handler.AbstractResultHandlerConfig.Controller;
import org.w3c.dom.Element;

/**
 * Configuration for the NewsSearchSetBodyResultHandler ...
 * <p/>
 * Created: Jun 13, 2007 1:14:57 PM
 * Author: Ola MH Sagli <a href="ola@sesam.no">ola at sesam.no</a>
 */
@Controller("NewsSearchSetBodyResultHandler")
public class NewsSearchSetBodyResultHandlerConfig extends AbstractResultHandlerConfig {

    private String source;


    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {
        source = element.getAttribute("source");
        return this;
    }
}
