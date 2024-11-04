#!/usr/bin/env bash

until printf "" 2>>/dev/null >>/dev/tcp/cassandra/9042; do
    sleep 5;
    echo "Waiting for cassandra...";
done

echo "Creating keyspace and table..."
cqlsh cassandra -u cassandra -p cassandra -e "CREATE KEYSPACE IF NOT EXISTS junit WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy', 'dc1' : 1 };"
cqlsh cassandra -u cassandra -p cassandra -k junit -e "CREATE TABLE IF NOT EXISTS accountentity (id TEXT PRIMARY KEY, name TEXT);"