package no.schibstedsok.searchportal.mode.config;

import org.w3c.dom.Element;

import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;


@Controller("VideoSearchCommand")
public class VideoCommandConfig extends AbstractXmlSearchConfiguration {

    private String searchType;
    private String videoLanguageOnTop;

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public String getVideoLanguageOnTop() {
        return videoLanguageOnTop;
    }

    public void setVideoLanguageOnTop(String videoLanguageOnTop) {
        this.videoLanguageOnTop = videoLanguageOnTop;
    }


    @Override
    public AbstractXmlSearchConfiguration readSearchConfiguration(
            final Element element,
            final SearchConfiguration inherit) {

        super.readSearchConfiguration(element, inherit);

        AbstractDocumentFactory.fillBeanProperty(this, inherit, "searchType", ParseType.String, element, "notset");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "videoLanguageOnTop", ParseType.String, element, "english");

        return this;
    }

}
