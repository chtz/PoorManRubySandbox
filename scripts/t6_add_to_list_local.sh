#/bin/bash
WF_ID=$(head -n1 testdata/t6_wf_local.id)
IN_ID=$(head -n1 testdata/t6_in_local.id)

echo IN_ID=curl -s --header "Content-Type:text/plain" --data-binary "{\"more\":$(jshon -s "$@")}" http://localhost:8080/wf/$WF_ID/$IN_ID

IN_ID=`curl -s --header "Content-Type:text/plain" --data-binary "{\"more\":$(jshon -s "$@")}" http://localhost:8080/wf/$WF_ID/$IN_ID`
