#/bin/bash
SCRIPT_ID=`curl -s --header "Content-Type:text/plain" --data-binary @$1 http://www.sandbox.p.iraten.ch/`
curl -s --header "Content-Type:text/plain" --data-binary @$2 http://www.sandbox.p.iraten.ch/$SCRIPT_ID 
