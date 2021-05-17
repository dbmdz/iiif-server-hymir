package de.digitalcollections.iiif.hymir.frontend;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class CurrentUrlHandlerInterceptor implements HandlerInterceptor {

  @Override
  public void postHandle(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull Object handler,
      ModelAndView modelAndView)
      throws Exception {
    if (modelAndView != null) {
      String currentUrl = getCurrentUrl(request);
      modelAndView.addObject("currentUrl", currentUrl);
    }
  }

  String getCurrentUrl(HttpServletRequest request) {
    String currentUrl = request.getRequestURI();
    String contextPath = request.getContextPath();
    if (contextPath.length() > 1) {
      currentUrl = currentUrl.replaceFirst(contextPath, "");
    }

    if (request.getQueryString() == null) {
      return currentUrl;
    }

    String language = request.getParameter("language");
    String queryString = request.getQueryString()
        .replaceAll("&language=" + language, "")
        .replaceAll("language=" + language + "&", "")
        .replaceAll("language=" + language, "");

    if (queryString.length() == 0) {
      return currentUrl;
    }

    return currentUrl + "?" + queryString;
  }

}