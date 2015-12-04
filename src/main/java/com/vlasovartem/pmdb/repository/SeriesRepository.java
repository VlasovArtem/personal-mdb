package com.vlasovartem.pmdb.repository;

import com.vlasovartem.pmdb.entity.Season;
import com.vlasovartem.pmdb.entity.Series;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by artemvlasov on 30/11/15.
 */
@Repository
public interface SeriesRepository extends MongoRepository<Series, String> {
    @Query(value = "{}", fields = "{title : 1}")
    List<Series> findParsedSeriesTitles();

    int countByTitleIgnoreCase(String title);

    List<Series> findByFinishedIsFalse();

    List<Series> findByFinishedIsFalseAndNextEpisodeEpisodeDateGreaterThan(LocalDate today);

    Series findByTitleIgnoreCase(String title);

    List<Series> findBySeriesStartBetween(LocalDate yearStart, LocalDate yearEnd, Sort sort);

    List<Series> findBySeriesStartBetweenAndFinishedIsFalse(LocalDate yearStart, LocalDate yearEnd, Sort sort);

    List<Series> findByGenresIgnoreCase(String genre, Sort sort);

    List<Series> findByGenresIgnoreCaseAndFinishedIsFalse(String genre, Sort sort);

    List<Series> findByGenresIgnoreCaseAndSeriesStartBetween(String genre, LocalDate yearStart, LocalDate yearEnd, Sort sort);

    List<Series> findByGenresIgnoreCaseAndSeriesStartBetweenAndFinishedIsFalse(String genre, LocalDate yearStart, LocalDate yearEnd, Sort sort);
}
