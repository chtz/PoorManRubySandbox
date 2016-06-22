#/bin/bash
WF_ID=`curl -s --header "Content-Type:text/plain" --data-binary @testdata/scripts/test7_wf.rb http://www.sandbox.p.iraten.ch/wf`

IN_ID=`curl -s --header "Content-Type:text/plain" --data-binary '{}' http://www.sandbox.p.iraten.ch/wf/$WF_ID`

echo $WF_ID > testdata/t7_wf.id
echo $IN_ID > testdata/t7_in.id