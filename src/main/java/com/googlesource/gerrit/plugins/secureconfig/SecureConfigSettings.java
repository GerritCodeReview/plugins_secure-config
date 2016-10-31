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

import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Singleton
public class SecureConfigSettings {
  private static final String SECURE_CONFIG = "secureConfig";
  private static final String PASSWORD_DEVICE = "passwordDevice";
  private static final String PASSWORD_LENGTH = "passwordLength";
  private static final int DEF_PASSWORD_LENGTH = 8;
  private static final String CIPHER = "cipher";
  private static final String ENCODING = "encoding";
  private static final String DEF_ENCODING = "UTF-8";
  private static final String JCE_PROVIDER = "jceProvider";
  private static final String DEF_JCE_PROVIDER = "SunJCE";
  private final FileBasedConfig gerritConfig;
  private final char[] password;

  @Inject
  SecureConfigSettings(SitePaths site) throws IOException,
      ConfigInvalidException {
    Config baseConfig = new Config();
    baseConfig.setString(SECURE_CONFIG, null, CIPHER, "PBEWithMD5AndDES");
    baseConfig.setString(SECURE_CONFIG, null, PASSWORD_DEVICE, "/dev/zero");
    baseConfig.setInt(SECURE_CONFIG, null, PASSWORD_LENGTH, DEF_PASSWORD_LENGTH);
    baseConfig.setString(SECURE_CONFIG, null, ENCODING, DEF_ENCODING);
    baseConfig.setString(SECURE_CONFIG, null, JCE_PROVIDER, DEF_JCE_PROVIDER);

    gerritConfig =
        new FileBasedConfig(baseConfig, site.gerrit_config.toFile(),
            FS.DETECTED);
    gerritConfig.load();
    password = readPassword();
  }

  String getCipher() {
    return gerritConfig.getString(SECURE_CONFIG, null, CIPHER);
  }

  public char[] getPassword() {
    return password;
  }

  public String getEncoding() {
    return gerritConfig.getString(SECURE_CONFIG, null, ENCODING);
  }

  public String getJCEProvider() {
    return gerritConfig.getString(SECURE_CONFIG, null, JCE_PROVIDER);
  }

  private char[] readPassword() throws IOException {
    Path passwordDevice =
        Paths.get(gerritConfig.getString(SECURE_CONFIG, null, PASSWORD_DEVICE));
    try (FileInputStream in = new FileInputStream(passwordDevice.toFile())) {
      byte[] passphrase =
          new byte[gerritConfig.getInt(SECURE_CONFIG, PASSWORD_LENGTH,
              DEF_PASSWORD_LENGTH)];
      in.read(passphrase);
      return new String(Base64.getEncoder().encode(passphrase)).toCharArray();
    }
  }
}
