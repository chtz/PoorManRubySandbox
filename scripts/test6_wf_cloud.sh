#/bin/bash
WF_ID=`curl -s --header "Content-Type:text/plain" --data-binary @testdata/scripts/test6_wf.rb http://www.sandbox.p.iraten.ch/wf`

IN_ID=`curl -s --header "Content-Type:text/plain" --data-binary '{"more":"Hello"}' http://www.sandbox.p.iraten.ch/wf/$WF_ID`

IN_ID=`curl -s --header "Content-Type:text/plain" --data-binary '{"more":"End"}' http://www.sandbox.p.iraten.ch/wf/$WF_ID/$IN_ID`

curl -s http://www.sandbox.p.iraten.ch/wf/$WF_ID/$IN_ID | json_pp
