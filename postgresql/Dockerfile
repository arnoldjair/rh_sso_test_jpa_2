FROM postgres:16-alpine

# On Windows root will own the files, and they will have permissions 755
COPY ca/server.key /var/lib/postgresql/server.key
COPY ca/server.crt /var/lib/postgresql/server.crt

# update the privileges on the .key, no need to touch the .crt  
RUN chmod 600 /var/lib/postgresql/server.key
RUN chown postgres:postgres /var/lib/postgresql/server.key