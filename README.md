# Secure Config plugin

Encrypt all the values contained in the Gerrit's secure.config.

## How to build

Clone the secure-config plugin into a Gerrit source tree under the
directory plugins/secure-config, and then run:

```
   $ buck build plugins/secure-config
```

Resulting plugin jar is generated under
/buck-out/gen/plugins/secure-config/secure-config.jar

## How to install

Differently from the other plugins, secure-config needs to be copied to the /lib
directory of Gerrit installation.

Example:

```
   $ cp buck-out/gen/plugins/secure-config/secure-config.jar $GERRIT_SITE/lib/
```

## How to configure

Add the gerrit.secureStoreClass configuration entry in gerrit.config to instruct
Gerrit to use the secure-store plugin for the encryption and decryption of all
values contained in your secure.config file.

Example:

```
   $ cat - >> $GERRIT_SITE/etc/gerrit.config
   [gerrit]
     secureStoreClass = com.googlesource.gerrit.plugins.secureconfig.SecureConfigStore
   ^D
```

## How to run

This plugin will decode values in secure.config, it will fail if there
is an existing secure.config which contains values that are not encrypted.
If the values in the current secure.config are not encrypted you will need
to either clear out secure.config or back it up by moving it to another file
before running this plugin.

Example:
```

   $ mv $GERRIT_SITE/etc/secure.config $GERRIT_SITE/etc/secure.config.bakup

```

Gerrit secure.config properties need to be generated and managed using the
Gerrit init wizard. All the passwords entered at init will be stored as
encrypted values and then decrypted *on-the-fly* when needed at runtime.

Example:

```
   $ cd $GERRIT_SITE && java -jar bin/gerrit.war init
   Using secure store: com.googlesource.gerrit.plugins.secureconfig.SecureConfigStore

   *** Gerrit Code Review 2.13.2-1146-ga89e6a3
   [...]


   $ cat etc/secure.config
   [auth]
	registerEmailPrivateKey = hfMC1Yi9NF5N3Yz7cVNUdJNPQfbb2g47RnaPElTraTh0MMB2OE+xeg==

```

## Customising encryption settings

Default settings are fully working but are meant to be use for DEMO purpose
only. You typicallty need to customize them according to your Company's Policies
about passwords and confidential data encryption standards.

See below the gerrit.config parameters to customize the encryption security
settings.

### secureConfig.jceProvider

The JCE cryptographic provider for the encryption algorithms
and security keys.

Default: SunJCE

### secureConfig.cipher

The encyrption algorithm to be used for encryption. Different JCE providers
provide a different set of cryptographic algorithms.

Default: PBEWithMD5AndDES.

*NOTE - The default value is considered insecure and should not be used in
production*

### secureConfig.passwordDevice

The device or file where to retrieve the encryption passphrase.

Default: /dev/zero

*NOTE - The all-zeros password is considered insecure and should not be used in
production*

### secureConfig.passwordLength

The length in bytes of the password read from the passwordDevice.

Default: 8

*NOTE - A 8-bytes (64-bit) password length is considered insecure and should not
be used in production*

### secureConfig.encoding

Encoding to use when encrypting/decrypting values from secure.config.

Default: UTF-8

