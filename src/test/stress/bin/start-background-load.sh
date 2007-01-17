#!/bin/bash
#
# Create background load on bleedingsearch
#
#  create jmeter slaves to create proper distribution of requests
#

echo "Fetching new copy of our jmeter configuration file."
rm -f alpha-background-stress.jmx
wget -q https://dev.schibstedsok.no/svn/search-portal/trunk/src/test/stress/conf/alpha-background-stress.jmx 
cat alpha-background-stress.jmx | sed "s/alpha/beta/" > beta-background-stress.jmx

if [[ "x" == "x$1" ]] ; then
    echo "Usage: ./start-background-load.sh (alpha|beta)"
    exit 1
fi


echo "Cleaning jmeter.log"
> jmeter.log
echo "Backgrounding jmeter in non-gui mode..."
screen -d -m jmeter/bin/jmeter -n -t $1-background-stress.jmx -l jmeter.log
