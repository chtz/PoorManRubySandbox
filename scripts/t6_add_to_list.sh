#/bin/bash
WF_ID=$(head -n1 testdata/t6_wf.id)
IN_ID=$(head -n1 testdata/t6_in.id)

echo IN_ID=curl -s --header "Content-Type:text/plain" --data-binary "{\"more\":$(jshon -s "$@")}" http://www.sandbox.p.iraten.ch/wf/$WF_ID/$IN_ID
IN_ID=`curl -s --header "Content-Type:text/plain" --data-binary "{\"more\":$(jshon -s "$@")}" http://www.sandbox.p.iraten.ch/wf/$WF_ID/$IN_ID`