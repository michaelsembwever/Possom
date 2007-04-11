package no.schibstedsok.searchportal.query.transform;

import no.schibstedsok.searchportal.query.Clause;
import org.apache.log4j.Logger;


public class NewsMyNewsQueryTransformer extends AbstractQueryTransformer {
    private static final Logger LOG = Logger.getLogger(NewsMyNewsQueryTransformer.class);
    private NewsMyNewsQueryTransformerConfig config;


    public NewsMyNewsQueryTransformer(QueryTransformerConfig config) {
        this.config = (NewsMyNewsQueryTransformerConfig) config;
    }

    public void visitImpl(final Clause clause) {
        String myNews = (String) getContext().getDataModel().getJunkYard().getValue("myNews");
        LOG.debug("Transforming query according to cookie myNews = " + myNews);
    }
}
