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

package com.google.gerrit.pgm;

import com.google.gerrit.extensions.config.FactoryModule;
import com.google.gerrit.pgm.init.api.ConsoleUI;
import com.google.gerrit.pgm.init.api.InstallAllPlugins;
import com.google.gerrit.pgm.init.api.InstallPlugins;
import com.google.gerrit.pgm.init.api.Section;
import com.google.gerrit.server.config.GerritServerConfigModule;
import com.google.gerrit.server.config.SitePath;
import com.google.gerrit.server.securestore.SecureStoreClassName;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Providers;

import com.googlesource.gerrit.plugins.secureconfig.SecureConfigStore;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class Passwd {
  private Injector injector;


  private Path sitePath;

  @Option(metaVar = "SITEPATH", name = "--site-path", aliases = {"-d"}, usage = "Local directory containing Gerrit site data", required = true)
  private void setSitePath(String path) {
    sitePath = Paths.get(path);
  }

  private String section;
  private String key;

  @Argument(metaVar = "SECTION.KEY", index = 0, required = true, usage = "Section and key separated by a dot of the password to set")
  private String sectionAndKey;

  @Argument(metaVar = "PASSWORD", index = 1, required = false, usage = "Password to set")
  private String password;

  public final int main(final String[] argv) throws Exception {
    final CmdLineParser clp = new CmdLineParser(this);
    String errorMsg = null;
    try {
      clp.parseArgument(argv);
      errorMsg = parsetSectionAndKey();
    } catch (CmdLineException err) {
      errorMsg = err.getLocalizedMessage();
    }

    if(errorMsg != null) {
      error(errorMsg);
      error(errorMsg.replaceAll(".", "*"));
      help(clp);
      return 1;
    }

    return run();
  }

  private String parsetSectionAndKey() {
    String[] varParts = sectionAndKey.split("\\.");
    if (varParts.length != 2) {
      return("Invalid name '" + sectionAndKey + "': expected section.key format");
    }
    section = varParts[0];
    key = varParts[1];
    return null;
  }

  private void error(String msg, Object... args) {
    System.err.println(String.format(msg, args));
  }

  private void help(CmdLineParser clp) {
    error("Use: %s -d <SITEPATH> <SECTION.KEY> [PASSWORD]\n",
        Passwd.class.getName());
    clp.printUsage(System.err);
  }

  public int run() throws Exception {
    setup();
    SetPasswd setPasswd = injector.getInstance(SetPasswd.class);
    setPasswd.run(section, key, password);
    return 0;
  }

  private void setup() {
    injector = initModule();
    injector.injectMembers(this);
  }

  private Injector initModule() {
    List<Module> modules = new ArrayList<>();
    modules.add(new FactoryModule() {
      @Override
      protected void configure() {
        bind(Path.class).annotatedWith(SitePath.class).toInstance(sitePath);
        bind(String.class).annotatedWith(SecureStoreClassName.class)
            .toProvider(
                Providers.of(SecureConfigStore.class.getCanonicalName()));
        bind(ConsoleUI.class).toInstance(ConsoleUI.getInstance(false));
        factory(Section.Factory.class);
        bind(Boolean.class).annotatedWith(InstallAllPlugins.class).toInstance(
            Boolean.FALSE);
        bind(new TypeLiteral<List<String>>() {}).annotatedWith(
            InstallPlugins.class).toInstance(new ArrayList<>());
      }
    });
    modules.add(new GerritServerConfigModule());
    return Guice.createInjector(modules);
  }
}
