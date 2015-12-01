package com.vlasovartem.pmdb.service;

import com.vlasovartem.pmdb.entity.Series;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * Created by artemvlasov on 29/11/15.
 */
public interface SeriesService {
    Series findByTitle (String title);
    List<Series> findByYear (int year, Sort sort, boolean hideFinished);
    List<Series> findByGenre(String genre, Sort sort, boolean hideFinished);
}
