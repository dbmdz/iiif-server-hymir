package de.digitalcollections.ehcache;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventLogger implements CacheEventListener<Object, Object> {

  private static final Logger LOGGER = LoggerFactory.getLogger(EventLogger.class);

  public EventLogger() {
    System.out.println("Adding logger");
  }

  @Override
  public void onEvent(CacheEvent<? extends Object, ? extends Object> event) {
    if (LOGGER.isDebugEnabled()) {
      switch (event.getType()) {
        case CREATED:
          LOGGER.debug("CACHE MISS for {}", event.getKey());
          break;
        default:
          LOGGER.debug("{} {}", event.getType(), event.getKey());
      }
    }
  }
}
