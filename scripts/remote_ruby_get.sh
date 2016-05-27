#/bin/bash
SCRIPT_ID=`curl -s --header "Content-Type:text/plain" --data-binary @$1 $2/`
curl -s $2/$SCRIPT_ID$3 
