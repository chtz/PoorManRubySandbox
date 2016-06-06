@definition = Workflow.define do 
  start_node  :start,   
              :default_transition => :state_a
              
  state_node  :state_a,
              :default_transition => :state_b,
              :enter_action => lambda { |token|
                token["command"] = ["HTTPGET", "http://tschenett.ch"]
              },
              :leave_action => lambda { |token|
                if  token["result"] =~ /\<title\>(.+?)\<\/title\>/m
                  token.variables.delete "result"
                  token["tschenett.title"] = $1
                end
              }
              
  state_node :state_b,
             :default_transition => :state_c,
             :enter_action => lambda { |token|
               token["command"] = ["HTTPGET", "http://furthermore.ch"]
             },
             :leave_action => lambda { |token|
               if  token["result"] =~ /\<title\>(.+?)\<\/title\>/m
                 token.variables.delete "result"
                 token["furthermore.title"] = $1
               end
             }
                            
  state_node :state_c,
             :default_transition => :state_d
              
  state_node :state_d,
             :default_transition => :end,
             :enter_action => lambda { |token|
               token["command"] = ["HTTPGET", "https://www.up4sure.ch"]
             },
             :leave_action => lambda { |token|
               if  token["result"] =~ /\<title\>(.+?)\<\/title\>/m
                 token.variables.delete "result"
                 token["up4sure.title"] = $1
               end
             }
                            
  end_node   :end
end
