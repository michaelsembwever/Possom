#!/bin/bash
#
# Example script sesam.no uses to generate sitemaps
# author: mick@semb.wever.org
# Id: $Id$
#

JAVA_HOME=~/jdk1.6.0_04
JAVA_JAR=$JAVA_HOME/bin/jar
JAVA_JAVA=$JAVA_HOME/bin/java

ENVIRONMENT=beta.test.sesam.no
WEBAPP_DIR=/www/schibstedsok/$ENVIRONMENT
cd /www/schibstedsok/sitemaps/generator-$ENVIRONMENT
$JAVA_JAR -xvf $WEBAPP_DIR/ROOT.war `$JAVA_JAR -tvf $WEBAPP_DIR/ROOT.war WEB-INF/lib/ | grep sesat-sitemap-generator | gawk '{print $8}'`
cd WEB-INF/lib
$JAVA_JAVA -cp sesat-sitemap-generator-*-jar-with-dependencies.jar no.sesat.sitemap.SitemapGenerator http://$ENVIRONMENT $WEBAPP_DIR/export/sitemap/$ENVIRONMENT http://$ENVIRONMENT/export/sitemap/$ENVIRONMENT/

ENVIRONMENT=sesam.no
WEBAPP_DIR=/www/schibstedsok-prod/$ENVIRONMENT
cd /www/schibstedsok/sitemaps/generator-$ENVIRONMENT
$JAVA_JAR -xvf $WEBAPP_DIR/ROOT.war `$JAVA_JAR -tvf $WEBAPP_DIR/ROOT.war WEB-INF/lib/ | grep sesat-sitemap-generator | gawk '{print $8}'`
cd WEB-INF/lib
$JAVA_JAVA -cp sesat-sitemap-generator-*-jar-with-dependencies.jar no.sesat.sitemap.SitemapGenerator http://$ENVIRONMENT $WEBAPP_DIR/export/sitemap/$ENVIRONMENT http://$ENVIRONMENT/export/sitemap/$ENVIRONMENT/
