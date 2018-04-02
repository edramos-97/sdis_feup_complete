#!/usr/bin/env bash

echo "Peers:"
for var in "$@"; do
    echo "Peer ID is: $var"
done

echo "Creating out/"
rm -rf out
mkdir out

# put here the compile part to out
echo "Compiling to out/"
javac -d ./out src/*/*.java

# opening rmiregistry
echo "Opening rmiregistry and waiting 1 second"
cd out/
rmiregistry &
sleep 1

# going back
echo "Running peer(s)"
for var in "$@"; do
    java Executables/Peer "$var" 228.5.6.7 6789 228.5.6.8 6790 228.5.6.9 6791
done
# echo "java Executables/Peer $1 228.5.6.7 6789 228.5.6.8 6790 228.5.6.9 6791"





