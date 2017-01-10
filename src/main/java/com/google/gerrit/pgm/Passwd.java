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
import com.google.gerrit.pgm.init.api.InitFlags;
import com.google.gerrit.pgm.init.api.InstallAllPlugins;
import com.google.gerrit.pgm.init.api.InstallPlugins;
import com.google.gerrit.pgm.init.api.Section;
import com.google.gerrit.server.config.GerritServerConfigModule;
import com.google.gerrit.server.config.SitePath;
import com.google.gerrit.server.securestore.SecureStoreClassName;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Providers;

import com.googlesource.gerrit.plugins.secureconfig.SecureConfigStore;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class Passwd {
  private Path sitePath;
  private String userVar;
  private String user;
  private String passwordVar;
  private String password;
  private Injector injector;

  @Inject
  private InitFlags initFlags;

  @Option(name = "--site-path", aliases = {"-d"}, usage = "Local directory containing site data", required = true)
  private void setSitePath(String path) {
    sitePath = Paths.get(path);
  }

  @Option(name = "--user-var", aliases = {"-u"}, usage = "User config variable", required = true)
  private void setUserVar(String userVar) {
    this.userVar = userVar;
  }

  @Option(name = "--user", aliases = {"-U"}, usage = "User", required = true)
  private void setUser(String user) {
    this.user = user;
  }

  @Option(name = "--password-var", aliases = {"-p"}, usage = "Password config variable", required = true)
  private void setPasswordVar(String passwordVar) {
    this.passwordVar = passwordVar;
  }

  @Option(name = "--password", aliases = {"-P"}, usage = "Password")
  private void setPassword(String password) {
    this.password = password;
  }

  public final int main(final String[] argv) throws Exception {
    final CmdLineParser clp = new CmdLineParser(this);
    try {
      clp.parseArgument(argv);
    } catch (CmdLineException err) {
      err.printStackTrace();
      return 1;
    }
    return run();
  }

  public int run() throws Exception {
    setup();
    InitPasswd init = injector.getInstance(InitPasswd.class);
    init.run(userVar, user, passwordVar, password);
    initFlags.cfg.save();
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
