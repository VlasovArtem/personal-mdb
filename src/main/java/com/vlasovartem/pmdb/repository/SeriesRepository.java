package com.vlasovartem.pmdb.repository;

import com.vlasovartem.pmdb.entity.Series;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by artemvlasov on 30/11/15.
 */
public interface SeriesRepository extends MongoRepository<Series, String> {
    int countByTitle (String title);
    Series findByTitleIgnoreCase (String title);
    List<Series> findBySeriesStartBetween (LocalDate yearStart, LocalDate yearEnd);
    List<Series> findBySeriesStartBetween (LocalDate yearStart, LocalDate yearEnd, Sort sort);
    List<Series> findBySeriesStartBetweenAndFinishedFalse (LocalDate yearStart, LocalDate yearEnd);
    List<Series> findBySeriesStartBetweenAndFinishedFalse (LocalDate yearStart, LocalDate yearEnd, Sort sort);
    List<Series> findByGenres (String genre);
    List<Series> findByGenres (String genre, Sort sort);
    List<Series> findByGenresAndFinishedFalse (String genre);
    List<Series> findByGenresAndFinishedFalse (String genre, Sort sort);

}
