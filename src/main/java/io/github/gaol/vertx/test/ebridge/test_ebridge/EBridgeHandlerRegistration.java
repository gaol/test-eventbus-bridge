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
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:aoingl@gmail.com">Lin Gao</a>
 */
class EBridgeHandlerRegistration {

  private static final Logger log = LoggerFactory.getLogger("test.ebridge.registration");

  private static final EBridgeHandlerRegistration instance = new EBridgeHandlerRegistration();
  private EBridgeHandlerRegistration() {
    // private
  }

  static EBridgeHandlerRegistration getInstance() {
    return instance;
  }

  private final Map<String, EBridgeHandler<?>> handlersMap = new ConcurrentHashMap<>();

  EBridgeHandlerRegistration loadInitialConsumers(JsonArray handlers) {
    loadBuiltinHandlers();
    if (handlers == null || handlers.isEmpty()) {
      log.info("Nothing to initialize from config");
      return this;
    }
    for (Object obj: handlers) {
      JsonObject json = (JsonObject)obj;
      @SuppressWarnings("rawtypes")
      EBridgeHandler<?> handler = new EBridgeHandler(json) {
        @Override
        Handler<Message<?>> createHandler() {
          return null;
        }
      };
      handlersMap.putIfAbsent(json.getString("address"), handler);
    }
    return this;
  }

  Map<String, EBridgeHandler<?>> handlerMap() {
    return this.handlersMap;
  }

  private void loadBuiltinHandlers() {
    handlersMap.putIfAbsent("consume", new ConsumeHandler());
    handlersMap.putIfAbsent("echo", new EchoHandler());
    handlersMap.putIfAbsent("time", new TimeHandler());
    handlersMap.putIfAbsent("list", new ListHandler());
  }

  void clear() {
    this.handlersMap.clear();
  }

  void register(String address, EBridgeHandler<?> handlerFromClient) {
    handlersMap.putIfAbsent(address, handlerFromClient);
  }

  public void unregister(String address) {
    handlersMap.remove(address);
  }

  private static class ConsumeHandler extends EBridgeHandler<JsonObject> {
    private ConsumeHandler() {
      super("consume", TYPE.BUILTIN);
    }

    @Override
    Handler<Message<JsonObject>> createHandler() {
      return m -> log.info("Got Message to Consume: \n" + messageInfo(m));
    }
  }

  private static <T> String messageInfo(Message<T> message) {
    StringBuilder sb = new StringBuilder();
    sb.append("{'headers': ").append(message.headers().toString());
    if (message.body() != null) {
      sb.append(", 'body': ").append(message.body().toString());
    }
    sb.append("\n}");
    return sb.toString();
  }

  private static class EchoHandler extends EBridgeHandler<JsonObject> {
    private EchoHandler() {
      super("Echo", TYPE.BUILTIN);
    }

    @Override
    Handler<Message<JsonObject>> createHandler() {
      return m -> {
        log.info("Got Message to Echo: \n" + messageInfo(m));
        DeliveryOptions dops = new DeliveryOptions();
        m.headers().forEach((e) -> dops.addHeader(e.getKey(), e.getValue()));
        m.reply(m.body(), dops);
      };
    }
  }

  private static class TimeHandler extends EBridgeHandler<JsonObject> {
    private TimeHandler() {
      super("Time", TYPE.BUILTIN);
    }

    @Override
    Handler<Message<JsonObject>> createHandler() {
      return m -> {
        log.info("Got Message from client: \n" + messageInfo(m));
        log.info("Returning time back to client");
        DeliveryOptions dops = new DeliveryOptions();
        m.headers().forEach((e) -> dops.addHeader(e.getKey(), e.getValue()));
        m.reply(new JsonObject().put("time", Instant.now()), dops);
      };
    }
  }

  private class ListHandler extends EBridgeHandler<JsonObject> {
    private ListHandler() {
      super("List", TYPE.BUILTIN);
    }

    @Override
    Handler<Message<JsonObject>> createHandler() {
      return m -> {
        JsonArray list = new JsonArray();
        handlersMap.forEach((k, v) -> list.add(new JsonObject().put("address", k).put("handler", v.toJson())));
        log.info("Got Message from client: \n" + messageInfo(m));
        log.info("Returning list of handlers: " + list.encodePrettily());
        DeliveryOptions dops = new DeliveryOptions();
        m.headers().forEach((e) -> dops.addHeader(e.getKey(), e.getValue()));
        m.reply(new JsonObject().put("list", list), dops);
      };
    }
  }

}
