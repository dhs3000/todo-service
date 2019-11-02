#! /bin/bash

docker run -p 5432:5432 -e POSTGRES_PASSWORD=postgres -d todo-service-postgresql