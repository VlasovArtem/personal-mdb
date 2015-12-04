package com.vlasovartem.pmdb.controller;

import com.vlasovartem.pmdb.entity.UserSeries;
import com.vlasovartem.pmdb.parser.SeriesParser;
import com.vlasovartem.pmdb.service.UserSeriesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by artemvlasov on 04/12/15.
 */
@RestController
@RequestMapping("/user/series")
public class UserSeriesController {

    @Autowired
    private UserSeriesService userSeriesService;

    @RequestMapping(path = "/add", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void addSeries (@RequestParam String title) {
        userSeriesService.addUserService(title);
    }

    @RequestMapping(path = "/parse", method = RequestMethod.POST)
    public void parse (@RequestParam String id) {
        userSeriesService.parse(id);
    }
}
