package no.schibstedsok.searchportal.query.transform;

import no.schibstedsok.searchportal.query.transform.AbstractQueryTransformerConfig.Controller;
import org.w3c.dom.Element;

@Controller("NewsMyNewsQueryTransformer")
public class NewsMyNewsQueryTransformerConfig extends AbstractQueryTransformerConfig {
    private String filterField;
    private String type;
    private static final String TYPE = "type";
    private static final String FILTER_FIELD = "filter-field";
    private static final String POSITION = "position";
    private int position = -1;


    public String getFilterField() {
        return filterField;
    }

    public void setFilterField(String filterField) {
        this.filterField = filterField;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public NewsMyNewsQueryTransformerConfig readQueryTransformer(final Element element) {
        filterField = element.getAttribute(FILTER_FIELD);
        type = element.getAttribute(TYPE);
        if (element.getAttribute(POSITION) != null && element.getAttribute(POSITION).length() > 0) {
            position = Integer.parseInt(element.getAttribute(POSITION));
        }
        return this;
    }

}
