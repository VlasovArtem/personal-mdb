package com.vlasovartem.pmdb;

import com.vlasovartem.pmdb.config.AppConfig;
import com.vlasovartem.pmdb.entity.Series;
import com.vlasovartem.pmdb.parser.SeriesParser;
import com.vlasovartem.pmdb.repository.SeriesRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by artemvlasov on 29/11/15.
 */
public class ParserTest {
    public static void main(String[] args) throws IOException {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        SeriesRepository seriesRepository = (SeriesRepository) context.getBean("seriesRepository");
        SeriesParser parser = (SeriesParser) context.getBean("seriesParser");
        List<Series> series = new ArrayList<>(2);
        series.add(seriesRepository.findByTitleIgnoreCase("south park"));
        series.add(seriesRepository.findByTitleIgnoreCase("Person of Interest"));
        for (Series sery : series) {
            parser.update(sery);
        }
    }
}
