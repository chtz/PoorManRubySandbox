#/bin/bash
WF_ID=$(head -n1 testdata/t7_wf.id)
IN_ID=$(head -n1 testdata/t7_in.id)

curl -s https://www.sandbox.p.iraten.ch/wf/$WF_ID/$IN_ID| jq -r .token.variables.more2
