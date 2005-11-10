cp tabs.xml tabs2.xml

perl -p -i -e 's:@(.*?)@:\${$1}:' tabs2.xml
