#/bin/bash
WF_ID=$(head -n1 testdata/t6_wf.id)
IN_ID=$(head -n1 testdata/t6_in.id)

curl -s http://www.sandbox.p.iraten.ch/wf/$WF_ID/$IN_ID | jq -r .token.variables.more2
