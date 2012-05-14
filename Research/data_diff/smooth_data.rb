#!/usr/bin/ruby

short_name = ARGV[0].split("/").last

last_file_num = `ls #{ARGV[0]}/`.split("\n").last.split("_")[1].to_i - 4;

candidates = []

0.upto(last_file_num) do |n|
  diff1 = `diff #{ARGV[0]}/#{short_name}_#{sprintf("%03d",n)} #{ARGV[0]}/#{short_name}_#{sprintf("%03d",n+1)} | wc -c `.chomp.to_i
  diff2 = `diff #{ARGV[0]}/#{short_name}_#{sprintf("%03d",n)} #{ARGV[0]}/#{short_name}_#{sprintf("%03d",n+2)} | wc -c `.chomp.to_i
  diff3 = `diff #{ARGV[0]}/#{short_name}_#{sprintf("%03d",n)} #{ARGV[0]}/#{short_name}_#{sprintf("%03d",n+3)} | wc -c `.chomp.to_i
  diff4 = `diff #{ARGV[0]}/#{short_name}_#{sprintf("%03d",n)} #{ARGV[0]}/#{short_name}_#{sprintf("%03d",n+4)} | wc -c `.chomp.to_i

  puts short_name + "_" + sprintf("%03d",n) + " " + diff1.to_s + " " + diff2.to_s + " " + diff3.to_s + " " + diff4.to_s

  if diff1 > diff2 * 5
    candidates << "#{ARGV[0]}/#{short_name}_#{sprintf("%03d",n+1)}"
  elsif diff1 > diff3 * 5 
    candidates << "#{ARGV[0]}/#{short_name}_#{sprintf("%03d",n+1)}"
    candidates << "#{ARGV[0]}/#{short_name}_#{sprintf("%03d",n+1)}"
  elsif diff1 > diff4 * 5
    candidates << "#{ARGV[0]}/#{short_name}_#{sprintf("%03d",n+1)}"
    candidates << "#{ARGV[0]}/#{short_name}_#{sprintf("%03d",n+1)}"
    candidates << "#{ARGV[0]}/#{short_name}_#{sprintf("%03d",n+1)}"
  end
end

  

candidates.each do |c|
  num = c.split("/").last.split("_").last.to_i
  
  puts "About to delete #{c}.  Are you sure?"

  conf = STDIN.gets.chomp

  if conf != "y"
      puts "Exiting"
      next
  end

  `mkdir #{ARGV[0]}/trash`
  `mv #{c} #{ARGV[0]}/trash/`
end
