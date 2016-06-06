#/bin/bash
WF_ID=`curl -s --header "Content-Type:text/plain" --data-binary @testdata/scripts/test4_wf.rb http://www.sandbox.p.iraten.ch/wf`
IN_ID=`curl -s --header "Content-Type:text/plain" --data-binary '{}' http://www.sandbox.p.iraten.ch/wf/$WF_ID`
TOK_ID=`curl -s http://www.sandbox.p.iraten.ch/wf/$WF_ID/$IN_ID | jq -r .token.uuid`
IN_ID=`curl -s --header "Content-Type:text/plain" --data-binary '{}' http://www.sandbox.p.iraten.ch/wf/$WF_ID/$IN_ID/$TOK_ID`
curl -s http://www.sandbox.p.iraten.ch/wf/$WF_ID/$IN_ID | json_pp
