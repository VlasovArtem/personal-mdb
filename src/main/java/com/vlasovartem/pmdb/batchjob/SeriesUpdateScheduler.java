package com.vlasovartem.pmdb.batchjob;

import com.vlasovartem.pmdb.parser.SeriesParser;
import com.vlasovartem.pmdb.repository.SeriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Created by artemvlasov on 03/12/15.
 */
@Component
public class SeriesUpdateScheduler {

    private SeriesRepository seriesRepository;
    private SeriesParser seriesParser;

    @Autowired
    public SeriesUpdateScheduler(SeriesRepository seriesRepository, SeriesParser seriesParser) {
        this.seriesParser = seriesParser;
        this.seriesRepository = seriesRepository;
    }

    @Scheduled(cron = "0 0 0 1/1 * ? *")
    public void updateNextEpisodeSchedule () {
        seriesRepository
                .findByFinishedIsFalseAndNextEpisodeEpisodeDateGreaterThan(LocalDate.now())
                .stream().peek(series -> seriesParser.updateNextEpisode(series));
    }

    @Scheduled(cron = "0 0 0 ? * MON *")
    public void updateSchedule () {
        seriesParser.update(seriesRepository.findByFinishedIsFalse());
    }

    @Scheduled(cron = "0 0 0 ? 1/1 MON#1 *")
    public void updateImdbRatingSchedule () {
        seriesRepository.findAll().stream().peek(series -> seriesParser.updateImdbRating(series));
    }
}
