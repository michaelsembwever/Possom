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
#-L 15500:10.16.195.250:15400 \

ssh \
-L 15000:cobraprod.bos3.fastsearch.net:15100 \
-L 15700:10.16.195.250:15100 \
-L 15200:10.16.195.249:15300 \
-L 15400:10.16.195.250:15200 \
-L 15500:10.16.195.250:15400 \
-L 15100:sch-fast-b01.dev.osl.basefarm.net:15100 \
-L 15151:81.93.165.134:25100 \
-L 15252:license.picsearch.com:80  \
-L 25100:sch-fast-se-admin01.osl.basefarm.net:15100 \
-L 25200:sch-fast-query-se.osl.basefarm.net:25200 \
$1@sch-login01.osl.basefarm.net # require username as first argument


