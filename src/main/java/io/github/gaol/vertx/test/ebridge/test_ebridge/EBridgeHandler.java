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

import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="mailto:aoingl@gmail.com">Lin Gao</a>
 */
abstract class EBridgeHandler<T> {

  enum TYPE {
    BUILTIN,
    FROM_CLIENT,
    FROM_FILE
  }

  private final String name;
  private final TYPE type;
  private String script;

  EBridgeHandler(String name, TYPE type) {
    this.name = name;
    this.type = type;
  }

  EBridgeHandler(JsonObject json) {
    this(json.getString("name"), json.containsKey("type") ? TYPE.valueOf(json.getString("type")) : TYPE.FROM_FILE);
  }

  String name() {
    return name;
  }

  TYPE type() {
    return type;
  }

  String script() {
    return script;
  }

  abstract Handler<Message<T>> createHandler();

  JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.put("name", name);
    json.put("type", type.name());
    if (script != null) {
      json.put("script", script);
    }
    return json;
  }

}
