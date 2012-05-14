#!/usr/bin/ruby

require 'nokogiri'

num = ARGV[0].split("/").last.split("_").last.split(".").first


stream = open ARGV[0]
doc = Nokogiri::HTML stream

begin
    body = doc.css('body').first

    body.name = "div"
    body.set_attribute "class", "diff_" + num.to_i.to_s 
    body.set_attribute "style", "display:none" unless num.to_i == 0
rescue Exception => e
    raise "PROBLEM: " + ARGV[0] + " " + e.to_s
end
puts body
