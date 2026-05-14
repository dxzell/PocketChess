package com.dxzell.pocketchess.spigot.module;

import com.dxzell.pocketchess.spigot.config.DatabaseConfig;
import com.dxzell.pocketchess.spigot.config.MessageConfig;
import com.google.inject.AbstractModule;

public class ConfigModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(DatabaseConfig.class).asEagerSingleton();
    bind(MessageConfig.class).asEagerSingleton();
  }
}
