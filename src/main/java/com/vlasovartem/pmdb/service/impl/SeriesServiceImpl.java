package com.vlasovartem.pmdb.service.impl;

import com.vlasovartem.pmdb.entity.Series;
import com.vlasovartem.pmdb.repository.SeriesRepository;
import com.vlasovartem.pmdb.service.SeriesService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public SeriesServiceImpl(SeriesRepository seriesRepository) {
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
        if (Objects.nonNull(sort)) {
            if (!hideFinished) {
                return seriesRepository.findBySeriesStartBetween(yearStart, yearEnd, sort);
            } else {
                seriesRepository.findBySeriesStartBetweenAndFinishedFalse(yearStart, yearEnd, sort);
            }
        } else {
            if(!hideFinished) {
                return seriesRepository.findBySeriesStartBetween(yearStart, yearEnd);
            } else {
                return seriesRepository.findBySeriesStartBetweenAndFinishedFalse(yearStart, yearEnd);
            }
        }
        return null;
    }

    @Override
    public List<Series> findByGenre(String genre, Sort sort, boolean hideFinished) {
        if(Objects.nonNull(sort)) {
            if(!hideFinished) {
                return seriesRepository.findByGenres(genre, sort);
            } else {
                return seriesRepository.findByGenresAndFinishedFalse(genre, sort);
            }
        } else {
            if(!hideFinished) {
                return seriesRepository.findByGenres(genre);
            } else {
                return seriesRepository.findByGenresAndFinishedFalse(genre);
            }
        }
    }
}
