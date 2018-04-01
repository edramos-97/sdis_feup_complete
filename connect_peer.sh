#!/usr/bin/env bash

PEER_ID = $1
echo "Peer ID is: " + PEER_ID

#echo "Changed locations"
#cd ~/sdis_feup

# put here the compile part to out
echo "Compiling to out/"

# opening rmiregistry
echo "Opening rmiregistry and waiting 1 second"
cd out/production/sdis_feup
rmiregistry &
sleep 1

# going back
echo "Running peer"
#echo "java Executables/Peer $1 228.5.6.7 6789 228.5.6.8 6790 228.5.6.9 6791"
java Executables/Peer $1 228.5.6.7 6789 228.5.6.8 6790 228.5.6.9 6791




