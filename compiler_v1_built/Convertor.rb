#!/usr/bin/ruby -w
if ((!fileName = ARGV[0]) || (!File.exists?("#{fileName}.Opt.circuit")) || (!File.exists?("#{fileName}.Opt.fmt"))) then
	puts "Usage: <filename>\n"
	puts "<filename>: the name of the files: <filename>.Opt.circuit and <filename>.Opt.fmt\n"
	puts "The output of the pins will be in: <filename>.cnv\n"
	exit(1)
end

resFile = File.new("#{fileName}.cnv", "w")

resFile.puts("FMT - Input:");
File.open("#{fileName}.Opt.fmt") do |f|
	f.each do |line|
		# bidder input integer "bidder[0].input=" [ 2 3 4 5 ]
		if (line =~ /.*?input.*?\"(.*?)\.(.*)=\" \[(.*?)\].*/)
			resFile.write("input,#{$1},#{$2}")
			wires=($3.split)
			wires.each do |i|
				j = Integer(i) - 2
				resFile.write(",#{j}")
			end
			resFile.write("\n")
		end	
	end
	f.close
end
resFile.puts("Gates:");
File.open("#{fileName}.Opt.circuit") do |f|
	f.each do |line|
		# 101 gate arity 3 table [ 0 1 1 0 1 0 0 1 ] inputs [ 100 97 92 ]
		if (line =~ /(.*?) gate arity (\d+).*?\[ (.*?) \] inputs \[ (.*?) \].*/)
			if (Integer("#{$2}") > 1)
				resFile.write("gate,#{Integer($1)-2},#{$2},#{$3}")
				wires=($4.split)
        wires.each do |i|
					j = Integer(i) - 2
					resFile.write(",#{j}")
				end
				resFile.write("\n")
			end
		end
	end
	f.close
end
resFile.puts("FMT - Output:");
File.open("#{fileName}.Opt.fmt") do |f|
	f.each do |line|
		if (line =~ /.*output.*?\"(.*?)\.(.*)=\"\s+\[(.*?)\].*/)
			resFile.write("output,#{$1},#{$2}")
			wires=($3.split)
			wires.each do |i|
				j = Integer(i) - 2
				resFile.write(",#{j}")
			end
			resFile.write("\n")
		end
	end
	f.close
end
resFile.close

# change negative to consts!

finished = false
index = 0
psh = 0
while (!finished)
	finished = true
	resFile = File.new("#{fileName}.cnvT", "w")
	File.open("#{fileName}.cnv") do |f|
		f.each do |line|
			# gate,28,3,0 0 0 1 0 1 1 1,25,20,1
			parts = line.strip!.split(",")
			if finished
				if (parts[0].eql?("gate") && (Integer(parts[2]) > 2))
					finished = false
					index = Integer(parts[1])
					if (parts[3].eql?("0 1 0 1 0 0 1 1"))
						type = parts[3].split
						resFile.write("gate,#{index},2,0 1 0 0,#{parts[4]},#{parts[6]}\n")
						resFile.write("gate,#{index+1},2,0 0 0 1,#{parts[5]},#{parts[6]}\n")
						resFile.write("gate,#{index+2},2,0 1 1 0,#{index},#{index+1}\n")
						psh = 2
          elsif (parts[3].eql?("1 0 1 0 0 0 1 1"))  
						type = parts[3].split
						resFile.write("gate,#{index},2,1 0 0 0,#{parts[4]},#{parts[6]}\n")
						resFile.write("gate,#{index+1},2,0 0 0 1,#{parts[5]},#{parts[6]}\n")
						resFile.write("gate,#{index+2},2,0 1 1 0,#{index},#{index+1}\n")
						psh = 2
					else
						type = parts[3].split
						#resFile.write("bla#{line}\n")
						# gate,28,3,0 0 0 1 0 1 1 1,25,20,1
						resFile.write("gate,#{index},2,#{type.values_at(0..3).join(" ")},#{parts[4]},#{parts[5]}\n")
						resFile.write("gate,#{index+1},2,#{type.values_at(4..7).join(" ")},#{parts[4]},#{parts[5]}\n")
						resFile.write("gate,#{index+2},2,0 1 0 0,#{index},#{parts[6]}\n")
						resFile.write("gate,#{index+3},2,0 0 0 1,#{index+1},#{parts[6]}\n")
						resFile.write("gate,#{index+4},2,0 1 1 0,#{index+2},#{index+3}\n")
						psh = 4
					end
				else
					resFile.write("#{line}\n")
				end
			else
				if (parts[0].eql?("gate"))
					parts[1] = Integer(parts[1]) + psh
					0.upto(Integer(parts[2]) - 1) do |i|
						if (Integer(parts[4+i]) >= index)
							parts[4+i] = Integer(parts[4+i]) + psh
						end
					end
				elsif (parts[0].eql?("output"))
					3.upto(parts.length-1) do |i|
						if (Integer(parts[i]) >= index)
							parts[i] = Integer(parts[i]) + psh
						end						
					end
				end
				j = parts.join(",")
				resFile.write("#{j}\n")
			end
		end
		f.close
	end
	resFile.close
	#finished = true
	File.rename("#{fileName}.cnvT","#{fileName}.cnv")
end

