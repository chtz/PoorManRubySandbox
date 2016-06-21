#Trigger https://maker.ifttt.com/trigger/start_sample_event/with/key/cs5UoBxhul6iNSJ-KN5mCx?value1=666

@definition = Workflow.define do 
  start_node :start,   
             :default_transition => :process
              
  state_node :process,
             :end_transition =>  :end,     :end_condition  => "variables['more'] == 'end'",
             :loop_transition => :process, :loop_condition => "variables['more'] != 'end'",
             :leave_action => lambda { |token|
               if token["more2"] 
                 token["more2"] = token["more2"] + "\n" + token["more"]
               else
                 token["more2"] = token["more"]
               end
             }
                            
  end_node   :end
end
