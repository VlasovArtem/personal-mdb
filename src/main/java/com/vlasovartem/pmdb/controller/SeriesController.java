package com.vlasovartem.pmdb.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.vlasovartem.pmdb.entity.Series;
import com.vlasovartem.pmdb.service.SeriesService;
import com.vlasovartem.pmdb.utils.exception.SeriesRequestException;
import com.vlasovartem.pmdb.utils.view.SeriesView;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("/series")
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

    @RequestMapping(method = GET, path = "/short")
    @JsonView(SeriesView.ShortInfoView.class)
    public ResponseEntity findSeries (@RequestParam(required = false) Integer year,
                                      @RequestParam(required = false) String genre,
                                      @RequestParam(required = false) String sort,
                                      @RequestParam(required =  false, defaultValue = "DESC") String order,
                                      @RequestParam(required = false, defaultValue = "false") Boolean hideFinished) {
        if(!String.valueOf(year).matches("20[0-2][0-9]")) {
            year = null;
        }
        if(Objects.isNull(year) && Objects.isNull(genre)) {
            return ResponseEntity.status(FORBIDDEN).body("Year and genre of the series cannot be null");
        } else if(Objects.isNull(year)) {
            return findByGenre(genre, sort, order, hideFinished);
        } else if(Objects.isNull(genre)) {
            return findByYear(year, sort, order, hideFinished);
        } else {
            return findByGenreAndYear(year, genre, sort, order, hideFinished);
        }
    }

    private ResponseEntity findByYear (Integer year, String sort, String order, Boolean hideFinished) {
        Sort seriesSort = getSort(sort, order);
        return getResponseEntity(
                seriesService.findByYear(year, seriesSort, hideFinished),
                String.format("Nothing is found with %d year", year));
    }

    private ResponseEntity findByGenre (String genre, String sort, String order, Boolean hideFinished) {
        Sort seriesSort = getSort(sort, order);
        return getResponseEntity(
                seriesService.findByGenre(StringUtils.capitalize(genre), seriesSort, hideFinished),
                String.format("Nothing is found with %s genre", genre));
    }

    private ResponseEntity findByGenreAndYear (Integer year, String genre, String sort, String order, Boolean
            hideFinished) {
        Sort seriesSort = getSort(sort, order);
        return getResponseEntity(
                seriesService.findByGenreAndYear(year, genre, seriesSort, hideFinished),
                String.format("Nothing is found with %s genre and %d year", genre, year));
    }

    private Sort getSort (String sort, String order) {
        if(Objects.isNull(sort) || sortProperties.containsKey(sort)) {
            return Objects.nonNull(sort) ? new Sort(Sort.Direction.fromString(order), sortProperties.get(sort)) : null;
        } else {
            throw new SeriesRequestException(String.format("Sort property %s is not accepted", sort));
        }
    }

    private <T> ResponseEntity getResponseEntity (List<T> findSeries, String notFoundMessage) {
        if(Objects.isNull(findSeries)) {
            return ResponseEntity.status(NOT_FOUND).body(notFoundMessage);
        } else {
            return ResponseEntity.ok(findSeries);
        }
    }
}
