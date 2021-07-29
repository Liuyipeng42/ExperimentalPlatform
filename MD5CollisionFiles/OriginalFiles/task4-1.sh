# shellcheck disable=SC1113
#/bin/bash

# Compile task4.c
gcc task4.c -o task4
chmod a+x task4

###########################################
# You need to find the correct value for X  
X=$((16#0000))
Y=$((X + 128 + 1))
###########################################

# Get prefix and suffix
head -c $X  task4 > prefix
tail -c +$Y task4 > suffix

# Generate collision, and get P & Q
./md5collgen -p prefix -o t1.bin t2.bin
tail -c 128 t1.bin > P
tail -c 128 t2.bin > Q


