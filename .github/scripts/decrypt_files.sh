#!/bin/sh

# Decrypt secret files to be used in GitHub Actions/CI (dev keys, google-services, etc)
# --batch to prevent interactive command --yes to assume "yes" for questions

# Decrypt google-service.json
gpg --quiet --batch --yes --decrypt --passphrase="$SECRET_PASSPHRASE" \
--output app/google-services.json app/google-services.json.gpg

#Decrypt keys.properties
gpg --quiet --batch --yes --decrypt --passphrase="$SECRET_PASSPHRASE" \
  --output keys.properties keys.properties.gpg
