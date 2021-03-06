package io.github.gaol.vertx.test.ebridge.test_ebridge;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetServerOptions;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.BridgeOptions;
import io.vertx.ext.eventbus.bridge.tcp.BridgeEvent;
import io.vertx.ext.eventbus.bridge.tcp.TcpEventBusBridge;

public class MainVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger("test.ebridge.main.verticle");

  private TcpEventBusBridge eventBusBridge;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    EBridgeHandlerRegistration.getInstance()
      .loadInitialConsumers(config().getJsonArray("handlers"))
      .handlerMap().forEach((address, handler) -> vertx.eventBus().consumer(address, handler.createHandler()));
    final int port = config().getInteger("port", 7000);
    BridgeOptions bridgeOptions = new BridgeOptions(config().getJsonObject("bridge-options", new JsonObject()));
    log.info("BridgeOptions: " + bridgeOptions.toJson());
    JsonObject serverJson = config().getJsonObject("server-options", new JsonObject());
    String sslStr = serverJson.getString("ssl", "false");
    serverJson.put("ssl", Boolean.valueOf(sslStr));
    NetServerOptions serverOptions = new NetServerOptions(serverJson);
    log.info("NetServerOptions: " + serverOptions.toJson());

    eventBusBridge = TcpEventBusBridge.create(vertx, bridgeOptions, serverOptions, this::handleBridgeEvent);
    eventBusBridge
      .listen(port)
      .onComplete(v -> {
        if (v.succeeded()) {
          log.info("TCP EventBus Bridge started on port: " + port);
          log.info(config().getString("welcome.message", "Welcome to use EventBus Starter"));
          startPromise.complete();
        } else {
          log.error("Failed to start the TCP EventBus Bridge", v.cause());
          startPromise.fail(v.cause());
        }
      });
  }

  private void handleBridgeEvent(BridgeEvent bridgeEvent) {
    final JsonObject rawMessage = bridgeEvent.getRawMessage();
    if (BridgeEventType.REGISTER == bridgeEvent.type()) {
      String address = rawMessage.getString("address");
      JsonObject headers = rawMessage.getValue("headers") != null ? rawMessage.getJsonObject("headers") : new JsonObject();
      String name = headers.getString("name", address);
      @SuppressWarnings("rawtypes")
      EBridgeHandler<?> handlerFromClient = new EBridgeHandler(name, EBridgeHandler.TYPE.FROM_CLIENT) {
        @Override
        Handler<Message<?>> createHandler() {
          return m -> {
            throw new RuntimeException("This should not get called.");
          };
        }
      };
      // only register the Handler meta, the real handler has been registered by the bridge
      EBridgeHandlerRegistration.getInstance().register(address, handlerFromClient);
      log.info("Handler Registered with message: " + rawMessage.encode());
    } else if (BridgeEventType.UNREGISTER == bridgeEvent.type()) {
      String address = rawMessage.getString("address");
      log.info("Unregister handler with address: " + address);
      EBridgeHandlerRegistration.getInstance().unregister(address);
    }
    bridgeEvent.complete(true);
  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception {
    EBridgeHandlerRegistration.getInstance().clear();
    eventBusBridge.close(stopPromise);
  }

}
