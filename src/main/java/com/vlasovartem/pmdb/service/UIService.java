package com.vlasovartem.pmdb.service;

import com.vlasovartem.pmdb.entity.Series;

import java.util.List;

/**
 * Created by artemvlasov on 04/12/15.
 */
public interface UIService {
    List<String> findParsedSeriesTitle();
    Series findSeries (String title);
    boolean seriesExists (String title);
}
