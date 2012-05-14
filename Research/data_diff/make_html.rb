#!/usr/bin/ruby

short_name = ARGV[0].split("/").last

`mkdir html/#{short_name}`

last_file_num = `ls #{ARGV[0]}/`.split("\n").last.split("_")[1].to_i - 1;

files = `ls #{ARGV[0]}/`.split("\n") - ["trash"]

files.each_with_index do |file_name,i|
  if(i == files.size - 1)
    break
  end
    
  next_file_name = files[i+1]

  puts "diff2html #{ARGV[0]}/#{file_name} #{ARGV[0]}/#{next_file_name} > html/#{short_name}/out_#{sprintf("%03d",i)}.html"
  puts `diff2html #{ARGV[0]}/#{file_name} #{ARGV[0]}/#{next_file_name} > html/#{short_name}/out_#{sprintf("%03d",i)}.html`
end
