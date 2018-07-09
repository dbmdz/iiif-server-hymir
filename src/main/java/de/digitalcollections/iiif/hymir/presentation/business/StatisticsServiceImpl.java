package de.digitalcollections.iiif.hymir.presentation.business;

import de.digitalcollections.iiif.hymir.presentation.business.api.StatisticsService;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatisticsServiceImpl implements StatisticsService {

  private Map<String, Long> counters = new HashMap<>();
  private Map<String,Set<Tag>> counterTags = new HashMap<>();
  private Map<String, Timer> timers = new HashMap<>();

  @Autowired
  private MeterRegistry meterRegistry;

  @Override
  public void increaseCounter(String name, String tag) {
    increaseCounter(name, tag, null);
  }

  @Override
  public void increaseCounter(String name, String tag, Long durationMillis) {
    String key = name + "." + tag;
    // Increase counter value
    counters.put(key, counters.getOrDefault(key, 0L) + 1);

    // Register counter, if it doesn't exist yet
    if (counterTags.get(key) == null) {
      counterTags.put(key, new HashSet<>());
      counterTags.get(key).add(new ImmutableTag("type", tag));
      meterRegistry.gauge(name + ".amount", counterTags.get(key), key,
          counters::get);
    }

    if (durationMillis != null) {
      // Register Timer
      if (timers.get(key) == null) {
        Timer timer = Timer.builder(name + ".duration")
            .publishPercentiles(0.5, 0.95)
            .publishPercentileHistogram()
            .tag("type", tag)
            .register(meterRegistry);
        timers.put(key, timer);
      }
      // Record time
      timers.get(key).record(durationMillis, TimeUnit.MILLISECONDS);
    }
  }
}
