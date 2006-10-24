#!/bin/bash
#
# Author: mick
# $Id$
#


# Check we're on correct machine
#
if ! (hostname | grep -q sch-login01); then
        echo "This script can only be executed on sch-login01.dev"
        exit 1
fi

# lock process so it cannot be run multiple times from different users
#
cd /www/schibstedsok
lockfile -l 900 update-test.lck

# which test environment to update
#
if [ "x" == "x$1" ] ; then
    RIG="alpha"
else
    RIG="$1"
fi
export $RIG

case "$RIG" in
	alpha|beta|gamma|nuclei|electron)
        ;;
    *)
        echo "This test environment ($RIG) does not exist."
        exit 1
        ;;
esac

#  Update update-test.sh
#
rm -f update-test.tmp
wget https://dev.schibstedsok.no/svn/search-portal/trunk/src/main/bin/update-test.sh -q -O update-test.tmp >/dev/null
DIFF=`diff -q update-test.sh update-test.tmp`
if [[ "x" != "x$DIFF" ]] ; then
    cp update-test.tmp update-test.sh
    rm update-test.tmp
    chmod u+x update-test.sh
    chmod g+x update-test.sh
    echo "Updated myself. Did not update $RIG.test.sesam. Please run again."
    rm -f update-test.lck
    exit 1
fi
rm -f update-test.tmp
unset DIFF

#  Update test-projects.sh
#
rm -f test-projects.tmp
wget https://dev.schibstedsok.no/svn/search-portal/trunk/src/main/bin/test-projects.sh -q -O test-projects.tmp >/dev/null
DIFF=`diff -q test-projects.sh test-projects.tmp`
if [[ "x" != "x$DIFF" ]] ; then
    cp test-projects.tmp test-projects.sh
    rm test-projects.tmp
    chmod u+x test-projects.sh
    chmod g+x test-projects.sh
    echo "Updated test-projects.sh"
fi
rm -f test-projects.tmp
unset DIFF


#
# Start Update
#

# XXX parametise which project list we load
. test-projects.sh

cnt=${#PROJECTS[*]}

# For each project
#
for (( i = 0 ; i < cnt ; i++ )) ; do

    # Find the latest version (eg trunk build)
    #
    for f in /www/schibstedsok/data/$RIG-builds/${PROJECTS[$i]}/* ; do
            if [ -d $f ] ; then
                    versionDir=$f
            fi
    done

    # Find the latest build in the version
    #
    for f in $versionDir/*.war ; do
            warFile=$f
    done

    # Check that it's not already being used and copy it to tomcat's deploy directory
    #
    while ! diff $warFile /www/schibstedsok/$RIG.test.sesam.no/${CONTEXTS[$i]} >/dev/null ; do
            echo "cp $warFile   ${CONTEXTS[$i]}"
            cp $warFile /www/schibstedsok/$RIG.test.sesam.no/${CONTEXTS[$i]}
    done

    # Keep permissions on it friendly
    #
    chmod g+w /www/schibstedsok/$RIG.test.sesam.no/${CONTEXTS[$i]} 2>/dev/null

done

# Update our version.txt accordingly
#
date +"%Y%m%d%H%M" > $RIG.test.sesam.no/version.txt
date >> $RIG.test.sesam.no/version.txt
whoami >> $RIG.test.sesam.no/version.txt
chmod g+w version.txt 2>/dev/null

# Clean lock
#
rm -f update-test.lck
