#/bin/bash
WF_ID=`curl -s --header "Content-Type:text/plain" --data-binary @testdata/scripts/test3_wf.rb http://www.sandbox.p.iraten.ch/wf`
IN_ID=`curl -s --header "Content-Type:text/plain" --data-binary '{"sum":123}' http://www.sandbox.p.iraten.ch/wf/$WF_ID`
TOK_ID=`curl -s http://www.sandbox.p.iraten.ch/wf/$WF_ID/$IN_ID | jq -r .token.childs[0].uuid`
IN_ID=`curl -s --header "Content-Type:text/plain" --data-binary '{"result":900}' http://www.sandbox.p.iraten.ch/wf/$WF_ID/$IN_ID/$TOK_ID`
TOK_ID=`curl -s http://www.sandbox.p.iraten.ch/wf/$WF_ID/$IN_ID | jq -r .token.childs[0].uuid`
IN_ID=`curl -s --header "Content-Type:text/plain" --data-binary '{"result":200}' http://www.sandbox.p.iraten.ch/wf/$WF_ID/$IN_ID/$TOK_ID`
curl -s http://www.sandbox.p.iraten.ch/wf/$WF_ID/$IN_ID | json_pp
