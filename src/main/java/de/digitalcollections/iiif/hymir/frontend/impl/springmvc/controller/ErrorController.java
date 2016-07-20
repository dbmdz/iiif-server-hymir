package de.digitalcollections.iiif.hymir.frontend.impl.springmvc.controller;

import java.sql.Timestamp;
import java.util.Date;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController {

    /*
     * Delivers the timestamp as error code to the error page.
     */
    @RequestMapping({"/error/{errorCode}"})
    public String getErrorPage(@PathVariable String errorCode, Model model) {
        model.addAttribute("timestamp", new Timestamp(new Date().getTime()));
        if (errorCode != null) {
            model.addAttribute("errorCode", errorCode);
        }
        return "error";
    }

    /*
     * Delivers the timestamp as error code to the error page.
     */
    @RequestMapping({"/error"})
    public String getErrorPage(Model model) {
        model.addAttribute("timestamp", new Timestamp(new Date().getTime()));
        model.addAttribute("errorCode", null);
        return "error";
    }
}
