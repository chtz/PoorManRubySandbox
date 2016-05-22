puts 'begin rm'
puts `rm -fR /`
puts 'begin ls'
begin
  puts `ls -la`
ensure
  puts 'end ls'
end