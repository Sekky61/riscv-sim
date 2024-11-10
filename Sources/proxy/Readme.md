# Traefik proxy

This directory contains part of the proxy configuration for the project.
It is not a necessary part of the project - you can use the ports exposed by the servers directly.

**Directory Contents:**

* `traefik.yaml`: The configuration file.

**Creating the Certs Directory:**

To use the proxy, you will need to create a `certs` directory within the `Sources/` directory. It should contain your SSL/TLS certificates called `certs/privkey.pem` and `certs/fullchain.pem` files.
Below is an example for creating such self-signed files.

```bash
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
 -keyout privkey.pem \
 -out fullchain.pem \
 -subj "/C=US/ST=State/L=City/O=Organization/CN=localhost"
```

This command is also available in `Sources/certs/create.sh`.

