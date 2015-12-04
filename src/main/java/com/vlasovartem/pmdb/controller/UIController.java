package com.vlasovartem.pmdb.controller;

import com.vlasovartem.pmdb.repository.SeriesRepository;
import com.vlasovartem.pmdb.service.UIService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by artemvlasov on 01/12/15.
 */
@Controller
public class UIController {
    @Autowired
    private UIService uiService;

    @RequestMapping("/")
    public ModelAndView getMainPage () {
        Map<String, Object> modelMap = new HashMap<>();
        List<String> seriesTitles = uiService.findParsedSeriesTitle();
        modelMap.put("series", seriesTitles);
        modelMap.put("exampleSeries", uiService.findSeries(
                seriesTitles.get(
                        RandomUtils.nextInt(0, seriesTitles.size()))));
        return new ModelAndView("index", modelMap);
    }

    @RequestMapping("/search")
    @ResponseBody
    public ResponseEntity seriesSearch (@RequestParam String title) {
        if(!uiService.seriesExists(title)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
