#!/bin/bash
#
# Create background load on bleedingsearch
#
#  create jmeter slaves to create proper distribution of requests
#

cd /www/schibstedsok/home/mickw
echo "Fetching new copy of bleedingsearch-background-stress.jmx"
rm bleedingsearch-background-stress.jmx
wget https://dev.schibstedsok.no/svn/search-portal/trunk/src/test/stress/conf/bleedingsearch-background-stress.jmx
echo "Cleaning jmeter.log"
> jmeter.log
echo "Backgrounding jmeter in non-gui mode..."
screen -d -m jmeter/bin/jmeter -n -t bleedingsearch-background-stress.jmx -l jmeter.log
