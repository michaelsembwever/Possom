#!/bin/bash

# Description of port mappings for the development profile. PELASE KEEP UPTODATE.
# 15000 ---> queryServerURL.3  
# 15100 ---> queryServerURL.1, sesam.se newsQueryServerURL
# 15200 ---> queryServerURL.2
# 15400 ---> tokenevaluator, relevantqueries
# 15500 ---> queryServerURL.4  DIV index
# 15151 ---> queryServerURL.5
# 15252 ---> picsearch
# 25100 ---> sesam.se queryServerURL.1
# 25200 ---> sesam.se tokenevaluator
# 25300 ---> sesam.se fast server (svd2,n24)
#-L 15500:10.16.195.250:15400 \

ssh \
-L 15000:cobraprod.bos3.fastsearch.net:15100 \
-L 15700:sch-fast-query.osl.basefarm.net:15100 \
-L 15200:sch-fast-search.osl.basefarm.net:15300 \
-L 15400:sch-fast-query01.osl.basefarm.net:15200 \
-L 15500:sch-fast-query.osl.basefarm.net:15400 \
-L 15100:sch-fast-b01.dev.osl.basefarm.net:15100 \
-L 15151:sch-fast02.dev.osl.basefarm.net:25100 \
-L 15252:license.picsearch.com:80  \
-L 25100:sch-fast-se-admin01.osl.basefarm.net:15100 \
-L 25200:sch-fast-query-se.osl.basefarm.net:25200 \
-L 25300:sch-fast-query-se.osl.basefarm.net:25100 \
$1@sch-login01.osl.basefarm.net # require username as first argument


