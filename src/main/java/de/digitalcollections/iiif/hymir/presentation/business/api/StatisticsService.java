package de.digitalcollections.iiif.hymir.presentation.business.api;

public interface StatisticsService {

  /**
   * Increases the gauge counter
   * @param name Name of the gauge, postfixed with <tt>.amount</tt>
   * @param tag Name of the tag
   */
  void increaseCounter(String name, String tag);

  /**
   * Increases the gauge counter and logs its accompanied duration
   * @param name Name of the gauge, postfixed with <tt>.amount</tt> and name of the timer, postfixed with <tt>.duration</tt>
   * @param tag Name of the tag
   * @param durationMillis Duration in milliseconds
   */
  void increaseCounter(String name, String tag, Long durationMillis);

}
