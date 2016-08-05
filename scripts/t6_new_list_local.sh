#/bin/bash
WF_ID=`curl -s --header "Content-Type:text/plain" --data-binary @testdata/scripts/test6_wf.rb http://localhost:8080/wf`

IN_ID=`curl -s --header "Content-Type:text/plain" --data-binary '{}' http://localhost:8080/wf/$WF_ID`

echo $WF_ID > testdata/t6_wf_local.id
echo $IN_ID > testdata/t6_in_local.id
