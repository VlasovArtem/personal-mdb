package com.vlasovartem.pmdb.service.impl;

import com.vlasovartem.pmdb.entity.Series;
import com.vlasovartem.pmdb.repository.SeriesRepository;
import com.vlasovartem.pmdb.service.UIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by artemvlasov on 04/12/15.
 */
@Service
public class UIServiceImpl implements UIService {

    @Qualifier("seriesRepository")
    @Autowired
    private SeriesRepository seriesRepository;

    @Override
    public List<String> findParsedSeriesTitle() {
        return seriesRepository.findParsedSeriesTitles().stream().map(Series::getTitle).sorted().collect(Collectors.toList());
    }

    @Override
    public Series findSeries(String title) {
        return seriesRepository.findByTitleIgnoreCase(title);
    }

    @Override
    public boolean seriesExists(String title) {
        return seriesRepository.countByTitleIgnoreCase(title) > 0;
    }
}
