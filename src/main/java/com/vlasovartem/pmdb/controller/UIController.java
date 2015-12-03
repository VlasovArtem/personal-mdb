package com.vlasovartem.pmdb.controller;

import com.vlasovartem.pmdb.controller.model.Search;
import com.vlasovartem.pmdb.repository.SeriesRepository;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by artemvlasov on 01/12/15.
 */
@Controller
public class UIController {

    private SeriesRepository seriesRepository;

    @Autowired
    public UIController(SeriesRepository seriesRepository) {
        this.seriesRepository = seriesRepository;
    }

    @RequestMapping("/")
    public ModelAndView getMainPage () {
        Map<String, Object> modelMap = new HashMap<>();
        modelMap.put("series", getParsedSeries());
        modelMap.put("exampleSeries", seriesRepository.findByTitleIgnoreCase("Grimm"));
        return new ModelAndView("index", modelMap);
    }

    @RequestMapping("/search")
    @ResponseBody
    public ResponseEntity seriesSearch (@RequestParam String title) {
        int titleExists = seriesRepository.countByTitleIgnoreCase(title);
        if(titleExists != 0) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    private List<String> getParsedSeries() {
        File file = new File("series-titles.txt");
        if(file.exists()) {
            try {
                return FileUtils.readLines(file).stream().sorted(String::compareTo).collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
}
