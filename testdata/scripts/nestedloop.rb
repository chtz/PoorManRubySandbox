puts "start"
STDOUT.flush
@x=0
(1..10000000).each do |x|
  (1..10000000).each do |y|
    @x = @x + 1
  end
end
puts @x
puts "end"
STDOUT.flush