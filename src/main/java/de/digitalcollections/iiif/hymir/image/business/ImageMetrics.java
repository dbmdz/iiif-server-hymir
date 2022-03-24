package de.digitalcollections.iiif.hymir.image.business;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Small component to allow for convenient measurements of image data operations.
 */
@Component
public class ImageMetrics {
  public enum ImageDataOp {
    GET_INFO,
    DECODE,
    ENCODE,
    TRANSFORM,
    ALPHACONVERT,
  }

  private static final String DATA_OP_TIMER = "hymir.image.data.op.duration.seconds";
  private static final String DATA_OP_PIX_COUNTER = "hymir.image.data.op.pixels.total";
  private static final Random rng = new Random();

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "We need the registry ")
  private final MeterRegistry registry;

  private final ConcurrentMap<Integer, Long> runningTimers = new ConcurrentHashMap<>();

  public ImageMetrics(MeterRegistry registry) {
    this.registry = registry;
  }

  private String[] buildTags(ImageDataOp op, String format) {
    ArrayList<String> tagList = new ArrayList<>();
    tagList.add("op");
    tagList.add(op.toString().toLowerCase(Locale.ROOT));
    tagList.add("format");
    tagList.add(format == null ? "none" : format.toLowerCase(Locale.ROOT));
    return tagList.toArray(String[]::new);
  }

  public void clearTimer(int key) {
    this.runningTimers.remove(key);
  }

  /**
   * Clear out stale timers once in a while to prevent a memory leak from failed tasks that don't
   * clean up after themselves.
   */
  @Scheduled(fixedRate = 15, timeUnit = TimeUnit.MINUTES)
  public void clearStaleTimers() {
    long now = System.nanoTime();
    this.runningTimers.entrySet().stream()
        .filter(e -> now - e.getValue() > Duration.ofMinutes(5).getNano())
        .forEach(e -> runningTimers.remove(e.getKey()));
  }

  /**
   * Start measuring an image operation.
   *
   * @return Key to stop the measurement with.
   */
  public int startImageOp() {
    int key = rng.nextInt();
    this.runningTimers.put(key, System.nanoTime());
    return key;
  }

  public void endImageOp(int key, ImageDataOp op, int numPixels) {
    endImageOp(key, op, "none", numPixels);
  }

  /** End measuring an image operation with some metadata. */
  public void endImageOp(int key, ImageDataOp op, String format, int numPixels) {
    Long startTime = this.runningTimers.remove(key);
    if (startTime == null) {
      return;
    }
    String[] tags = buildTags(op, format);
    Timer.builder(DATA_OP_TIMER)
        .description("Latency for operations on image data")
        .tags(tags)
        .register(registry)
        .record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
    Counter.builder(DATA_OP_PIX_COUNTER)
        .description("Number of input pixels processed in an image data operation")
        .tags(tags)
        .register(registry)
        .increment(numPixels);
  }
}
