#!/usr/bin/env bash

PEERS=( 10 20 30 40 50 60 70 80 90 100 )

cd out/

echo "Creating keys"

keytool -genseckey -alias key1 -keyalg AES -keysize 256 -KeyStore symencryption.keystore -storetype pkcs12 -storepass password123

for peerID in "${PEERS[@]}"
do
    keytool -genseckey -alias keyPeer${peerID} -keyalg AES -keysize 256 -KeyStore peer${peerID}.keystore -storetype pkcs12 -storepass peer${peerID}
done
