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

import com.google.common.collect.FluentIterable;
import com.google.gerrit.common.FileUtil;
import com.google.gerrit.server.config.SitePaths;
import com.google.gerrit.server.securestore.SecureStore;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.internal.storage.file.LockFile;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class SecureConfigStore extends SecureStore {
  private static final String SECURE_CONFIG_FILE = "secure.config";

  private final FileBasedConfig sec;
  private final Map<String, FileBasedConfig> pluginSec;
  private final SitePaths site;
  private final Codec codec;
  private long secFileLastmodified;
  private final Path secure_config;

  @Inject
  SecureConfigStore(SitePaths site, PBECodec codec) {
    this.site = site;
    secure_config = site.etc_dir.resolve(SECURE_CONFIG_FILE);

    this.codec = codec;
    sec = new FileBasedConfig(secure_config.toFile(), FS.DETECTED);
    try {
      sec.load();
      secFileLastmodified = sec.getFile().lastModified();
    } catch (IOException | ConfigInvalidException e) {
      throw new RuntimeException("Cannot load " + SECURE_CONFIG_FILE, e);
    }
    this.pluginSec = new HashMap<>();
  }

  @Override
  public String[] getList(String section, String subsection, String name) {
    return Arrays.stream(sec.getStringList(section, subsection, name))
        .map(codec::decode)
        .toArray(String[]::new);
  }

  @Override
  public synchronized String[] getListForPlugin(String pluginName,
      String section, String subsection, String name) {
    FileBasedConfig cfg = null;
    if (pluginSec.containsKey(pluginName)) {
      cfg = pluginSec.get(pluginName);
    } else {
      String filename = pluginName + "." + SECURE_CONFIG_FILE;
      File pluginConfigFile = site.etc_dir.resolve(filename).toFile();
      if (pluginConfigFile.exists()) {
        cfg = new FileBasedConfig(pluginConfigFile, FS.DETECTED);
        try {
          cfg.load();
          pluginSec.put(pluginName, cfg);
        } catch (IOException | ConfigInvalidException e) {
          throw new RuntimeException("Cannot load " + filename, e);
        }
      }
    }
    return cfg != null ? FluentIterable
        .from(cfg.getStringList(section, subsection, name))
        .transform(codec::decode).toArray(String.class) : null;
  }

  @Override
  public void setList(String section, String subsection, String name,
      List<String> values) {
    if (values != null) {
      sec.setStringList(section, subsection, name,
          values.stream()
          .map(codec::encode)
          .collect(Collectors.toList()));
    } else {
      sec.unset(section, subsection, name);
    }
    save();
  }

  @Override
  public void unset(String section, String subsection, String name) {
    sec.unset(section, subsection, name);
    save();
  }

  @Override
  public Iterable<EntryKey> list() {
    List<EntryKey> result = new ArrayList<>();
    for (String section : sec.getSections()) {
      for (String subsection : sec.getSubsections(section)) {
        for (String name : sec.getNames(section, subsection)) {
          result.add(new EntryKey(section, subsection, name));
        }
      }
      for (String name : sec.getNames(section)) {
        result.add(new EntryKey(section, null, name));
      }
    }
    return result;
  }

  /** @return <code>true</code> if currently loaded values are outdated */
  public boolean isOutdated() {
    long secFileCurrLastModified = sec.getFile().lastModified();
    return secFileCurrLastModified > secFileLastmodified;
  }

  /** Reload the values */
  public void reload() {
    try {
      sec.load();
    } catch (IOException | ConfigInvalidException e) {
      throw new IllegalStateException(e);
    }
  }

  private void save() {
    try {
      saveSecure(sec);
    } catch (IOException e) {
      throw new RuntimeException("Cannot save " + SECURE_CONFIG_FILE, e);
    }
  }

  private void saveSecure(final FileBasedConfig sec) throws IOException {
    if (FileUtil.modified(sec)) {
      final byte[] out = Constants.encode(sec.toText());
      final File path = sec.getFile();
      final LockFile lf = new LockFile(path);
      if (!lf.lock()) {
        throw new IOException("Cannot lock " + path);
      }
      try {
        FileUtil.chmod(0600, new File(path.getParentFile(), path.getName()
            + ".lock"));
        lf.write(out);
        if (!lf.commit()) {
          throw new IOException("Cannot commit write to " + path);
        }
      } finally {
        lf.unlock();
      }
    }
  }
}
