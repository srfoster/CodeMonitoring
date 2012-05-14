require "fileutils"

dir_name = ARGV[0]
source_dir = Dir.new(dir_name).to_a - ["."] - [".."]

source_dir.each do |file_name|
  puts "Extracting " + file_name

  FileUtils.cp(dir_name + "/" + file_name, dir_name + "/../data.db")

  `./extract_data`
end
