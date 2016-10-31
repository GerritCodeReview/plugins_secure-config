# Secure Config plugin

Encrypt all the values contained in the Gerrit's secure.config.

## How to build

Clone the secure-config plugin into a Gerrit source tree under the
directory plugins/secure-config, and then run:

```
   $ buck build plugins/secure-config
```

Resulting plugin jar is generated under /buck-out/gen/plugins/secure-config/secure-config.jar

## How to install

Differently from the other plugins, secure-config needs to be copied to the /lib directory of
Gerrit installation.

Example:

```
   $ cp buck-out/gen/plugins/secure-config/secure-config.jar $GERRIT_SITE/lib/
```

## How to configure

Add the gerrit.secureStoreClass configuration entry in gerrit.config to instruct Gerrit
to use the secure-store plugin for the encryption and decryption of all values contained
in your secure.config file.

Example:

```
   $ cat - >> $GERRIT_SITE/etc/gerrit.config
   [gerrit]
     secureStoreClass = com.googlesource.gerrit.plugins.secureconfig.SecureConfigStore
   ^D
```

