#/bin/bash
echo "curl -s --header "Content-Type:text/plain" --data-binary @$1 $2/"
SCRIPT_ID=`curl -s --header "Content-Type:text/plain" --data-binary @$1 $2/`
echo "curl -s $2/$SCRIPT_ID$3"
curl -s $2/$SCRIPT_ID$3 
