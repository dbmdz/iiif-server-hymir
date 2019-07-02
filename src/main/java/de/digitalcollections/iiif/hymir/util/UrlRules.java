package de.digitalcollections.iiif.hymir.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class UrlRules {

  /**
   * Checks if the value of identifier could introduce vulnerabilities like path traversal.
   * @param identifier the identifier to check.
   * @return true, if there are possible vulnerabilities
   */
  public static boolean isInsecure(String identifier) {
    return identifier != null && (identifier.contains("..") || URLDecoder.decode(identifier, StandardCharsets.UTF_8).contains(".."));
  }

  /**
   * Checks if the value of any identifier could introduce vulnerabilities like path traversal.
   * @param identifiers the list of identifier to check.
   * @return true, if there are possible vulnerabilities in any identifiers
   */
  public static boolean anyIsInsecure(String... identifiers) {
    for (String identifier : identifiers) {
      if (isInsecure(identifier)) {
        return true;
      }
    }
    return false;
  }

}
