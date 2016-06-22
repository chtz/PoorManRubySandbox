@definition = Workflow.define do 
  start_node :start,   
             :default_transition => :process
              
  state_node :process,
             :end_transition =>  :end,     :end_condition  => "variables['more2'] >  24*60*60",
             :loop_transition => :process, :loop_condition => "variables['more2'] <= 24*60*60",
             :enter_action => lambda { |token|
               token["command"] = ["sleep", "60"]
               if token["more2"]
                 token["more2"] = token["more2"] + 1
               else
                 token["more2"] = 0
               end
             }
                            
  end_node   :end
end
