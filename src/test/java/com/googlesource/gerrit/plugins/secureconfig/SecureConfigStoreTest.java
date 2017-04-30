// Copyright (C) 2017 The Android Open Source Project
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
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jgit.errors.ConfigInvalidException;
import org.junit.Test;

import com.google.gerrit.server.config.SitePaths;

public class SecureConfigStoreTest {

  @Test
  public void shouldNotBeOutdatedWhenLoaded() throws Exception {
    SecureConfigStore secureConfigStore = newSecureStore(createTempSecureConfigInSitePaths());

    assertThat(secureConfigStore.isOutdated(), is(equalTo(false)));
  }

  @Test
  public void shouldBeOutdatedWhenFileIsChangedAfterLoad() throws Exception {
    SitePaths sitePaths = createTempSecureConfigInSitePaths();
    SecureConfigStore secureConfigStore = newSecureStore(sitePaths);

    Thread.sleep(1000L);

    File secureConfigFile = sitePaths.secure_config.toFile();
    try (Writer writer = new FileWriter(secureConfigFile, true)) {
      writer.write("foo");
    }

    assertThat(secureConfigStore.isOutdated(), is(equalTo(true)));
  }

  @Test
  public void shouldReloadNewContent() throws Exception {
    SitePaths sitePaths = createTempSecureConfigInSitePaths();
    PBECodec codec = newCodec(sitePaths);
    SecureConfigStore secureConfigStore = new SecureConfigStore(sitePaths, codec);

    File secureConfigFile = sitePaths.secure_config.toFile();
    try (Writer writer = new FileWriter(secureConfigFile, true)) {
      writer.write("[test]\n" + "foo=" + codec.encode("bar"));
    }

    secureConfigStore.reload();

    assertThat(secureConfigStore.get("test", null, "foo"), is(equalTo("bar")));
  }

  private SitePaths createTempSecureConfigInSitePaths() {
    try {
      Path gerritSite = Files.createTempDirectory(SecureConfigStoreTest.class.getName());
      gerritSite.resolve("etc").toFile().mkdirs();
      File secureConfigFile = gerritSite.resolve("etc").resolve("secure.config").toFile();
      secureConfigFile.createNewFile();
      return new SitePaths(gerritSite);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private SecureConfigStore newSecureStore(SitePaths sitePaths) throws IOException, ConfigInvalidException {
    return new SecureConfigStore(sitePaths, newCodec(sitePaths));
  }

  private PBECodec newCodec(SitePaths sitePaths) throws ConfigInvalidException, IOException {
    return new PBECodec(new SecureConfigSettings(sitePaths));
  }
}
