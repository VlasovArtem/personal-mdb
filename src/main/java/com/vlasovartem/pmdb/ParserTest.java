package com.vlasovartem.pmdb;

import com.vlasovartem.pmdb.config.AppConfig;
import com.vlasovartem.pmdb.parser.SeriesParser;
import com.vlasovartem.pmdb.repository.SeriesRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;

/**
 * Created by artemvlasov on 29/11/15.
 */
public class ParserTest {
    public static void main(String[] args) throws IOException {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        SeriesRepository seriesRepository = (SeriesRepository) context.getBean("seriesRepository");
        SeriesParser parser = (SeriesParser) context.getBean("seriesParser");
        System.out.println(parser);
    }
}
