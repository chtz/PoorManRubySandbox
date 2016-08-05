#/bin/bash
WF_ID=$(head -n1 testdata/t6_wf_local.id)
IN_ID=$(head -n1 testdata/t6_in_local.id)

curl -s http://localhost:8080/wf/$WF_ID/$IN_ID | jq -r .token.variables.more2
