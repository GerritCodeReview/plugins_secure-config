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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import com.google.gerrit.server.config.SitePaths;

import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class CodecTest {
  static String sitePath;
  static File gerritConfigFile;

  @BeforeClass
  public static void setUp() throws IOException {
    sitePath = "/tmp/" + System.currentTimeMillis();
    new File(sitePath + "/etc").mkdirs();
    File gerritConfigFile = new File(sitePath + "/etc/gerrit.config");
    FileBasedConfig gerritConfig =
        new FileBasedConfig(gerritConfigFile,
            FS.DETECTED);
    gerritConfig.save();
    gerritConfigFile.deleteOnExit();
  }

  @Test
  public void encodedStringShouldBeDifferent() throws Exception {
    Codec codec = newPBECodec();

    String plainText = "a value";
    String cipherText = codec.encode(plainText);

    assertThat(cipherText, is(not(plainText)));
  }

  @Test
  public void decodedOrEncodedShouldBeTheSameValue() throws Exception {
    Codec codec = newPBECodec();

    String plainText = "plainText value";
    String cipherText = codec.encode(plainText);
    String decodedText = codec.decode(cipherText);

    assertThat(decodedText, is(equalTo(plainText)));
  }


  private Codec newPBECodec() throws IOException, ConfigInvalidException {
    SitePaths sitePaths = new SitePaths(Paths.get("/tmp"));
    SecureConfigSettings config = new SecureConfigSettings(sitePaths);
    Codec codec = new PBECodec(config);
    return codec;
  }

}
