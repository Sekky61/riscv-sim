#!/bin/bash
   
# Generate self-signed certificate
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
 -keyout privkey.pem \
 -out fullchain.pem \
 -subj "/C=US/ST=State/L=City/O=Organization/CN=localhost"

# Set appropriate permissions
chmod 644 fullchain.pem
chmod 600 privkey.pem

echo "Self-signed certificate generated successfully."
