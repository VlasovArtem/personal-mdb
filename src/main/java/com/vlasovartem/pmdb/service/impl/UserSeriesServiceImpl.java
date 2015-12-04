package com.vlasovartem.pmdb.service.impl;

import com.vlasovartem.pmdb.entity.Series;
import com.vlasovartem.pmdb.entity.UserSeries;
import com.vlasovartem.pmdb.parser.SeriesParser;
import com.vlasovartem.pmdb.repository.SeriesRepository;
import com.vlasovartem.pmdb.repository.UserSeriesRepository;
import com.vlasovartem.pmdb.service.UserSeriesService;
import com.vlasovartem.pmdb.utils.exception.SeriesFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Created by artemvlasov on 04/12/15.
 */
@Service
public class UserSeriesServiceImpl implements UserSeriesService {

    @Autowired
    private UserSeriesRepository userSeriesRepository;
    @Qualifier("seriesRepository")
    @Autowired
    private SeriesRepository seriesRepository;
    @Autowired
    private SeriesParser seriesParser;

    @Override
    public void addUserService(String title) {
        if(userSeriesRepository.countByTitleIgnoreCase(title) == 0
                && seriesRepository.countByTitleIgnoreCase(title) == 0) {
            userSeriesRepository.save(new UserSeries(title));
        } else {
            throw new SeriesFoundException();
        }
    }

    @Override
    public void parse(String id) {
        UserSeries userSeries = userSeriesRepository.findOne(id);
        Series series = seriesParser.parse(userSeries.getTitle());
        if(Objects.nonNull(series)) {
            seriesRepository.save(series);
            userSeriesRepository.delete(userSeries);
        }
    }

    @Override
    public void deleteUserSeries(String id) {
        userSeriesRepository.delete(id);
    }

    @Override
    public void updateUserSeries(String id, String title) {
        UserSeries userSeries = userSeriesRepository.findOne(id);
        userSeries.setTitle(title);
        userSeriesRepository.save(userSeries);
    }
}
