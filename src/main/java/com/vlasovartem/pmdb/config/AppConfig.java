package com.vlasovartem.pmdb.config;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.vlasovartem.pmdb.parser.EpisodeParser;
import com.vlasovartem.pmdb.parser.SeasonParser;
import com.vlasovartem.pmdb.parser.SeriesParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by artemvlasov on 30/11/15.
 */
@Configuration
@EnableMongoRepositories(basePackages = {"com.vlasovartem.pmdb.repository"})
@EnableMongoAuditing
@EnableScheduling
public class AppConfig extends AbstractMongoConfiguration {

    @Override
    protected String getDatabaseName() {
        return "heroku_r183c426";
    }

    @Override
    public Mongo mongo() throws Exception {
        return new MongoClient(new MongoClientURI("mongodb://heroku_r183c426:qjempqqji3ukn89v4f6icj8k3a@ds059644.mongolab.com:59644/heroku_r183c426"));
    }

    @Override
    protected String getMappingBasePackage() {
        return "com.vlasovartem.pmdb";
    }

    @Bean
    public EpisodeParser episodeParser () {
        return new EpisodeParser();
    }

    @Bean
    public SeasonParser seasonParser () {
        return new SeasonParser(episodeParser());
    }

    @Bean
    public SeriesParser seriesParser () {
        return new SeriesParser(seasonParser());
    }
}
