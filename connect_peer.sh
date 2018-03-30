#!/usr/bin/env bash

PEER_ID = $1
echo "Peer ID is: " + PEER_ID

# put here the compile part

# compile to out

# all things must refer to ~/Desktop/sdis_feup/out/production/sdis_feup$

echo "Changed locations"
cd ~/Desktop/sdis_feup
echo "Opening rmi registry"
#rmiregistry &
echo "Sleeping to let rmi registry kick in"
sleep 1
java Executables/Peer $1 228.5.6.7 6789 228.5.6.8 6790 228.5.6.9 6791