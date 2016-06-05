@definition = Workflow.define do 
  start_node  :start,   
              :default_transition => :fork
                        
  fork_node   :fork,
              :a_transition => :state_a,
              :b_transition => :state_b
              
  state_node  :state_a,
              :default_transition => :join,
              :enter_action => lambda { |token|
                token["command"] = "RANDOM"
              },
              :leave_action => lambda { |token|
                token["random"] = token["result"]
                #puts "a rand: #{token["random"]}"
              }
              
  state_node  :state_b,
              :default_transition => :join,
              :enter_action => lambda { |token|
                token["command"] = "RANDOM"
              },
              :leave_action => lambda { |token|
                token["random"] = token["result"] * -1
                #puts "a rand: #{token["random"]}"
              }            

  join_node   :join,
              :default_transition => :end,
              :enter_action => lambda { |token|
                unless token.parent["sum"]
                  token.parent["sum"] = token["random"]
                else
                  token.parent["sum"] = token.parent["sum"] + token["random"]
                end
              },
              :leave_action => lambda { |token|
                #puts "sum: #{token["sum"]}"
              }
              
  end_node    :end
end
