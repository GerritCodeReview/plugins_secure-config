// Copyright (C) 2016 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.plugins.secureconfig;

import com.google.gerrit.server.config.SitePaths;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.bouncycastle.util.encoders.Base64;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Singleton
public class SecureConfigSettings {
  private static final String PASSWORD_DEVICE = "secureStorePasswordDevice";
  private static final String PASSWORD_LENGTH = "secureStorePasswordLength";
  private static final int DEF_PASSWORD_LENGTH = 8;
  private static final String CIPHER = "secureStoreCipher";
  private static final String GERRIT = "gerrit";
  private final FileBasedConfig gerritConfig;
  private final char[] password;

  @Inject
  public SecureConfigSettings(SitePaths site) throws IOException,
      ConfigInvalidException {
    Config baseConfig = new Config();
    baseConfig.setString(GERRIT, null, CIPHER, "PBEWithMD5AndDES");
    baseConfig.setString(GERRIT, null, PASSWORD_DEVICE, "/dev/random");
    baseConfig.setInt(GERRIT, null, PASSWORD_LENGTH, DEF_PASSWORD_LENGTH);
    gerritConfig =
        new FileBasedConfig(baseConfig, site.gerrit_config.toFile(),
            FS.DETECTED);
    gerritConfig.load();
    password = readPassword();
  }

  String getCipher() {
    return gerritConfig.getString(GERRIT, null, CIPHER);
  }

  public char[] getPassword() {
    return password;
  }

  private char[] readPassword() throws IOException {
    Path passwordDevice =
        Paths.get(gerritConfig.getString(GERRIT, null, PASSWORD_DEVICE));
    try (FileInputStream in = new FileInputStream(passwordDevice.toFile())) {
      byte[] passphrase =
          new byte[gerritConfig.getInt(GERRIT, PASSWORD_LENGTH,
              DEF_PASSWORD_LENGTH)];
      in.read(passphrase);
      return new String(Base64.encode(passphrase)).toCharArray();
    }
  }
}
