require 'json'
print JSON.pretty_generate(JSON.parse(STDIN.read))