#/bin/bash
SCRIPT_ID=`curl -s --header "Content-Type:text/plain" --data-binary @$1 $3/`
curl -s --header "Content-Type:text/plain" --data-binary @$2 $3/$SCRIPT_ID 
