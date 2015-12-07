package com.vlasovartem.pmdb;

import com.vlasovartem.pmdb.config.AppConfig;
import com.vlasovartem.pmdb.entity.Episode;
import com.vlasovartem.pmdb.entity.Season;
import com.vlasovartem.pmdb.entity.Series;
import com.vlasovartem.pmdb.parser.SeriesParser;
import com.vlasovartem.pmdb.repository.SeriesRepository;
import org.apache.commons.io.FileUtils;
import org.springframework.cglib.core.Local;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by artemvlasov on 29/11/15.
 */
public class ParserTest {
    public static void main(String[] args) throws IOException {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        SeriesRepository seriesRepository = (SeriesRepository) context.getBean("seriesRepository");
        SeriesParser parser = (SeriesParser) context.getBean("seriesParser");
//        Series series = parser.parse("Criminal Minds");
//        System.out.println(series);
        File file = new File("series-titles.txt");
        if(file.exists()) {
            for (String s : FileUtils.readLines(file)) {
                if(seriesRepository.countByTitleIgnoreCase(s) == 0) {
                    Series series = parser.parse(s);
                    seriesRepository.save(series);
                }
            }
        }
    }
}
