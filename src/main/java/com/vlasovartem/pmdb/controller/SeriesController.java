package com.vlasovartem.pmdb.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.vlasovartem.pmdb.entity.Series;
import com.vlasovartem.pmdb.service.SeriesService;
import com.vlasovartem.pmdb.utils.view.SeriesView;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Created by artemvlasov on 29/11/15.
 */
@RestController
@RequestMapping("/api/v1/series")
public class SeriesController {

    private SeriesService seriesService;
    private Map<String, String> sortProperties;

    @Autowired
    public SeriesController(SeriesService seriesService) {
        this.seriesService = seriesService;
        sortProperties = new HashMap<>();
        sortProperties.put("rating", "imdbRating");
        sortProperties.put("year", "year");
        sortProperties.put("title", "title");
    }

    @RequestMapping(method = GET)
    public Series findByTitle (@RequestParam String title) {
        return seriesService.findByTitle(title);
    }

    @RequestMapping(method = GET, path = "/short/year/{year}")
    @JsonView(SeriesView.ShortInfoView.class)
    public ResponseEntity findByYear (@PathVariable Integer year,
                                      @RequestParam(required = false) String sort,
                                      @RequestParam(required = false, defaultValue = "DESC") String order,
                                      @RequestParam(required = false, defaultValue = "false") Boolean hideFinished) {
        if(Objects.isNull(sort) || sortProperties.containsKey(sort)) {
            Sort objectSort = Objects.nonNull(sort) ? new Sort(Sort.Direction.valueOf(order), sortProperties.get
                    (sort)) : null;
            List<Series> series = seriesService.findByYear(year, objectSort, hideFinished);
            if (Objects.isNull(series)) {
                return ResponseEntity.status(NOT_FOUND).body(String.format("Nothing is found with %d year",
                        year));
            } else {
                return ResponseEntity.ok(series);
            }
        } else {
            return ResponseEntity.status(FORBIDDEN).body(String.format("Sort property %s is not accepted", sort));
        }
    }

    @RequestMapping(method = GET, path = "/short/genre/{genre}")
    @JsonView(SeriesView.ShortInfoView.class)
    public ResponseEntity findByGenre (@PathVariable String genre,
                                       @RequestParam(required = false) String sort,
                                       @RequestParam(required = false, defaultValue = "DESC") String order,
                                       @RequestParam(required = false, defaultValue = "false") Boolean hideFinished) {
        if(Objects.isNull(sort) || sortProperties.containsKey(sort)) {
            Sort objectSort = Objects.nonNull(sort) ? new Sort(Sort.Direction.valueOf(order), sortProperties.get
                    (sort)) : null;
            List<Series> series = seriesService.findByGenre(StringUtils.capitalize(genre), objectSort, hideFinished);
            if(Objects.isNull(series)) {
                return ResponseEntity.status(NOT_FOUND).body(String.format("Nothing is found with %s genre",
                        genre));
            } else {
                return ResponseEntity.ok(series);
            }
        } else {
            return ResponseEntity.status(FORBIDDEN).body(String.format("Sort property %s is not accepted", sort));
        }
    }
}
