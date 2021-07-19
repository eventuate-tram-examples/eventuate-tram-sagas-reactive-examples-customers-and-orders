ARG baseImageVersion
FROM  eventuateio/eventuate-tram-sagas-mysql:$baseImageVersion
COPY 8.initialize-database.sql /docker-entrypoint-initdb.d
