/*
 *  Copyright (c) 2020 - 2021 The original author or authors
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of Apache License v2.0 which
 *  accompanies this distribution.
 *
 *       The Apache License v2.0 is available at
 *       http://www.opensource.org/licenses/apache2.0.php
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package io.github.gaol.vertx.test.ebridge.test_ebridge;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="mailto:aoingl@gmail.com">Lin Gao</a>
 */
public class Starter extends Launcher {
  private static final Logger log = LoggerFactory.getLogger("test.ebridge.starter");

  private JsonObject defaultConfig;

  @Override
  public void afterStartingVertx(Vertx vertx) {
    defaultConfig = new JsonObject(vertx.fileSystem().readFileBlocking("config.json"));
  }

  @Override
  protected String getMainVerticle() {
    return MainVerticle.class.getName();
  }

  @Override
  public void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {
    JsonObject mainVerticleConfig = deploymentOptions.getConfig();
    if (mainVerticleConfig.isEmpty()) {
      mainVerticleConfig.mergeIn(defaultConfig);
    } else {
      if (!mainVerticleConfig.containsKey("port")) {
        mainVerticleConfig.put("port", defaultConfig.getInteger("port"));
      }
      if (!mainVerticleConfig.containsKey("bridge-options")) {
        mainVerticleConfig.put("bridge-options", defaultConfig.getJsonObject("bridge-options"));
      }
      if (!mainVerticleConfig.containsKey("server-options")) {
        mainVerticleConfig.put("server-options", defaultConfig.getJsonObject("server-options"));
      }
    }
  }

  public static void main(String[] args) {
    log.info("Starting Test EventBus Bridge...");
    new Starter().dispatch(args);
  }
}
