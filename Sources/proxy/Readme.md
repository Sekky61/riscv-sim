# Nginx proxy

This directory contains the Nginx proxy configuration for the project.
It is not a necessary part of the project and the app works without it (ignore the failing container) and use the ports exposed by the servers directly.

**Directory Contents:**

* `nginx.conf`: The main Nginx configuration file.
* `certs/`: A directory containing SSL/TLS certificates for secure connections.

**Creating the Certs Directory:**

To use this configuration, you will need to create a `certs` directory within
this directory. It should contain your SSL/TLS certificates.

The proxy expects a `certs/privkey.pem` and `certs/fullchain.pem` files.
Below is an example for creating such self-signed files.

```bash
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
 -keyout privkey.pem \
 -out fullchain.pem \
 -subj "/C=US/ST=State/L=City/O=Organization/CN=localhost"
```

**Running the Nginx Proxy:**

To run the Nginx proxy as a Docker container, navigate to the parent directory
and run the containers.
This is more precisely described in the main Readme.

```bash
cd Sources
./build_container.sh
./run_container.sh
```

