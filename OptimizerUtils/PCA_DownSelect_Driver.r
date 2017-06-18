#----------------------------------
# This is a driver to invoke R functions.
# It reads a csv file of raw data and outputs
# another csv file with transformed data
#----------------------------------

# prints working directory
c("Working directory:",getwd())

# reads command line arguments
args = commandArgs()

# When we execute rscript, args[6] is the first command line argument
input = args[6]
c("Input (raw data):", input)	# print the name of the input file (raw user activity)
output = args[7]
c("Output (PCA):", output)		# print the name of the output file (detectors' alert days)

# load the libraries
library(reshape2)
source("PCA_DownSelect.r")

# calculate alert days and output to file
ADD.Test <- ComputeAlertDays(datafile=input)

# workaround: consider NA users as 0
ADD.Test$user[is.na(ADD.Test$user)] = 0

write.csv( cbind(userid=ADD.Test$U2G$userid, ADD.Test$user, ADD.Test$group), file=output, row.names=F)
