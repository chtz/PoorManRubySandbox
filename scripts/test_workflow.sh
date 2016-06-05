#/bin/bash
WF_ID=`curl -s --header "Content-Type:text/plain" --data-binary @testdata/scripts/test3_wf.rb http://localhost:8080/wf`
IN_ID=`curl -s --header "Content-Type:text/plain" --data-binary '{"sum":123}' http://localhost:8080/wf/$WF_ID`
TOK_ID=`curl -s http://localhost:8080/wf/$WF_ID/$IN_ID | jq -r .token.childs[0].uuid`
IN_ID=`curl -s --header "Content-Type:text/plain" --data-binary '{"result":900}' http://localhost:8080/wf/$WF_ID/$IN_ID/$TOK_ID`
TOK_ID=`curl -s http://localhost:8080/wf/$WF_ID/$IN_ID | jq -r .token.childs[0].uuid`
IN_ID=`curl -s --header "Content-Type:text/plain" --data-binary '{"result":200}' http://localhost:8080/wf/$WF_ID/$IN_ID/$TOK_ID`
curl -s http://localhost:8080/wf/$WF_ID/$IN_ID | json_pp
