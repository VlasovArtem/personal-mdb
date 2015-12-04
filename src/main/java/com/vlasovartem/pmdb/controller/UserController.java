package com.vlasovartem.pmdb.controller;

import com.vlasovartem.pmdb.repository.UserSeriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by artemvlasov on 04/12/15.
 */
@Controller
public class UserController {

    @Autowired
    private UserSeriesRepository userSeriesRepository;

    @RequestMapping(path = "/authentication", method = RequestMethod.GET)
    public String authentication() {
        return "login";
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login (@RequestParam String loginData, @RequestParam String password) {
        return "redirect:/admin";
    }

    @RequestMapping(path = "/admin", method = RequestMethod.GET)
    public ModelAndView admin () {
        return new ModelAndView("/admin/admin", "userSeries", userSeriesRepository.findAll());
    }
}
