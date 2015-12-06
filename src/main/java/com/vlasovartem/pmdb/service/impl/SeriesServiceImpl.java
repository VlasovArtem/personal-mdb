package com.vlasovartem.pmdb.service.impl;

import com.vlasovartem.pmdb.entity.Series;
import com.vlasovartem.pmdb.parser.SeriesParser;
import com.vlasovartem.pmdb.repository.SeriesRepository;
import com.vlasovartem.pmdb.service.SeriesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Objects;

/**
 * Created by artemvlasov on 29/11/15.
 */
@Service
public class SeriesServiceImpl implements SeriesService {

    private SeriesRepository seriesRepository;
    private SeriesParser seriesParser;

    @Autowired
    public SeriesServiceImpl(@Qualifier("seriesRepository") SeriesRepository seriesRepository, SeriesParser seriesParser) {
        this.seriesParser = seriesParser;
        this.seriesRepository = seriesRepository;
    }

    @Override
    public Series findByTitle(String title) {
        return seriesRepository.findByTitleIgnoreCase(title);
    }

    @Override
    public List<Series> findByYear(int year, Sort sort, boolean hideFinished) {
        LocalDate yearStart = LocalDate.of(year, Month.JANUARY, 1);
        LocalDate yearEnd = LocalDate.of(year, Month.DECEMBER, 31);
        if (!hideFinished) {
            return seriesRepository.findBySeriesStartBetween(yearStart, yearEnd, sort);
        } else {
            seriesRepository.findBySeriesStartBetweenAndFinishedIsFalse(yearStart, yearEnd, sort);
        }
        return null;
    }

    @Override
    public List<Series> findByGenre(String genre, Sort sort, boolean hideFinished) {
        if(!hideFinished) {
            return seriesRepository.findByGenresIgnoreCase(genre, sort);
        } else {
            return seriesRepository.findByGenresIgnoreCaseAndFinishedIsFalse(genre, sort);
        }
    }

    @Override
    public List<Series> findByGenreAndYear(Integer year, String genre, Sort sort, Boolean hideFinished) {
        LocalDate yearStart = LocalDate.of(year, Month.JANUARY, 1);
        LocalDate yearEnd = LocalDate.of(year, Month.DECEMBER, 31);
        if(!hideFinished) {
            return seriesRepository.findByGenresIgnoreCaseAndSeriesStartBetween(genre, yearStart, yearEnd, sort);
        } else {
            return seriesRepository.findByGenresIgnoreCaseAndSeriesStartBetweenAndFinishedIsFalse(genre, yearStart, yearEnd, sort);
        }
    }

    @Override
    public void updateSeries(String type) {
        List<Series> series = null;
        switch (type.toUpperCase()) {
            case "ALL":
                series = seriesRepository.findByFinishedIsFalse();
                if(Objects.nonNull(series)) {
                    for (Series sery : series) {
                        seriesParser.update(sery);
                        seriesRepository.save(sery);
                    }
                }
                break;
            case "RATING":
                series = seriesRepository.findAll();
                if(Objects.nonNull(series)) {
                    for (Series sery : series) {
                        seriesParser.updateImdbRating(sery);
                        seriesRepository.save(sery);
                    }
                }
                break;
            case "EPISODES":
                series = seriesRepository.findSeriesForUpdateNextEpisode(LocalDate.now());
                if(Objects.nonNull(series)) {
                    for (Series sery : series) {
                        seriesParser.updateNextEpisode(sery);
                        seriesRepository.save(sery);
                    }
                }
                break;
        }
    }
}
