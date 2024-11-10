# Traefik proxy

This directory contains part of the proxy configuration for the project.
It is not a necessary part of the project - you can use the ports exposed by the servers directly.

## Creating the Certs Directory

To use the proxy with HTTPS, you will need to create a `certs` directory within the `Sources/` directory.
The directory location can be customized with `--certs-path`.

The directory may contain your SSL/TLS certificates called `certs/privkey.pem` and `certs/fullchain.pem` files.
Without the files present, the proxy still works, but only the HTTP port.

Below is an example for creating such self-signed files.

```bash
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
 -keyout privkey.pem \
 -out fullchain.pem \
 -subj "/C=US/ST=State/L=City/O=Organization/CN=localhost"
```

This command is also available in `Sources/certs/create.sh`.

## Directory Contents

* `traefik.yaml`: The configuration file.

