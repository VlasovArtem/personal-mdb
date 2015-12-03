package com.vlasovartem.pmdb.parser;

import com.vlasovartem.pmdb.entity.Episode;
import com.vlasovartem.pmdb.entity.Season;
import com.vlasovartem.pmdb.utils.HtmlElementUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.vlasovartem.pmdb.utils.HtmlElementUtils.*;

/**
 * Created by artemvlasov on 29/11/15.
 */
@Component
public class SeasonParser {
    private static final String IMDB_INITIAL_URL = "http://www.imdb.com";
    private static final Log LOG = LogFactory.getLog(SeasonParser.class);
    private EpisodeParser episodeParser;

    public SeasonParser() {
    }

    @Autowired
    public SeasonParser (EpisodeParser episodeParser) {
        this.episodeParser = episodeParser;
    }

    /**
     * Parse Season content
     * @param seasonUrl season url
     * @return parsed Season object
     */
    public Season parse (String seasonUrl) {
        if(Objects.nonNull(seasonUrl)) {
            return parseContent(seasonUrl);
        }
        return null;
    }

    /**
     * Parse list of Seasons
     * @param seasonsUrls list of season urls
     * @return list of seasons
     */
    public List<Season> parse(List<String> seasonsUrls) {
        List<Season> seasons = new ArrayList<>(seasonsUrls.size());
        for (String seasonsUrl : seasonsUrls) {
            Season parsedSeason = parse(seasonsUrl);
            if(Objects.nonNull(parsedSeason)) {
                seasons.add(parsedSeason);
            }
        }
        return seasons;
    }

    /**
     * Update season
     * @param season updated Season
     */
    public void update (Season season) {
        if(Objects.nonNull(season)) {
            episodeParser.updateEpisodes(filterEpisodesForUpdate(season.getEpisodes()));
            if(Objects.isNull(season.getSeasonEnd())) {
                season.setSeasonEnd(parseEndDate(season.getEpisodes()));
            }
            if(Objects.isNull(season.getSeasonStart())) {
                season.setSeasonStart(parseStartDate(season.getEpisodes()));
            }
        }
    }

    /**
     * Update list of seasons
     * @param seasons lsit of seasons
     */
    public void update (List<Season> seasons) {
        seasons.forEach(this::update);
    }

    /**
     * Parse episode urls from element
     * @param episodeList parse season element
     * @return list of urls
     */
    private List<String> parseEpisodeUrls (Element episodeList) {
        String episodeUrlPattern = "/title/tt\\d{7}/\\?ref_=ttep_ep\\d+";
        Elements elements = episodeList.select(".list.detail.eplist > .list_item > .info a");
        if(Objects.nonNull(elements) && elements.size() > 0) {
            return elements.stream()
                    .filter(e -> e.attr("href").matches(episodeUrlPattern))
                    .map(e -> IMDB_INITIAL_URL + e.attr("href"))
                    .collect(Collectors.toList());
        }
        return null;
    }

    /**
     * Parse season content from the season page url
     * @param seasonUrl season page url
     * @return season
     */
    private Season parseContent (String seasonUrl) {
        try {
            Document document = Jsoup.connect(seasonUrl).timeout(200000).get();
            Element episodeContent = document.select("#episodes_content").first();
            if(Objects.nonNull(episodeContent)) {
                Season season = new Season();
                season.setSeasonNumber(checkProperty(parseSeasonNumber(episodeContent), seasonUrl, "season number"));
                season.setUrl(seasonUrl);
                List<String> episodesUrls = checkProperty(parseEpisodeUrls(episodeContent), seasonUrl, "episodes urls");
                if (Objects.nonNull(episodesUrls)) {
                    season.setEpisodes(episodeParser.parse(episodesUrls));
                    season.setSeasonStart(
                            checkProperty(
                                    parseStartDate(season.getEpisodes()), seasonUrl, "season start"));
                    season.setSeasonEnd(
                            checkProperty(
                                    parseEndDate(season.getEpisodes()), seasonUrl, "season end"));
                }
                return season;
            }
            return null;
        } catch (SocketTimeoutException e) {
            parseContent(seasonUrl);
        } catch (IOException e) {
            LOG.warn(String.format("Season %s is invalid", seasonUrl));
            return null;
        }
        return null;
    }

    /**
     * Parse season number
     * @param element parsed element
     * @return season number
     */
    private Integer parseSeasonNumber (Element element) {
        Pattern pattern = Pattern.compile("\\d+");
        String episodeTop = findText("#episode_top", element);
        if(Objects.nonNull(episodeTop)) {
            Matcher matcher = pattern.matcher(episodeTop);
            Integer seasonNumber = null;
            while (matcher.find()) {
                if (matcher.group().matches("\\d+")) {
                    seasonNumber = Integer.valueOf(matcher.group());
                }
            }
            return seasonNumber;
        }
        return null;
    }

    /**
     * Parse season start date
     * @param episodes list of season episodes
     * @return episode start date
     */
    private LocalDate parseStartDate (List<Episode> episodes) {
        if(Objects.nonNull(episodes) && episodes.size() > 0) {
            if(episodes.get(0).getEpisodeNumber() != 1) {
                for (Episode episode : episodes) {
                    if (episode.getEpisodeNumber() == 1) {
                        return episode.getEpisodeDate();
                    }
                }
            } else {
                return episodes.get(0).getEpisodeDate();
            }
        }
        return null;
    }

    /**
     * Parse end date of the season
     * @param episodes list of episodes
     * @return parsed end date
     */
    private LocalDate parseEndDate (List<Episode> episodes) {
        if(Objects.nonNull(episodes) && episodes.size() > 0) {
            if(episodes.get(episodes.size() - 1).getEpisodeNumber() != episodes.size()) {
                return episodes.stream()
                        .max(Comparator.comparingInt(Episode::getEpisodeNumber))
                        .orElse(null).getEpisodeDate();
            } else {
                return episodes.get(episodes.size() - 1).getEpisodeDate();
            }
        }
        return null;
    }

    /**
     * Check is Series property is null
     * @param t Series property
     * @param seriesUrl series url
     * @param errorObject error Object
     * @param <T> Series property
     * @return Series property
     */
    private <T> T checkProperty(T t, String seriesUrl, String errorObject) {
        if(Objects.isNull(t)) {
            LOG.warn(createErrorMessage(seriesUrl, errorObject));
        }
        return t;
    }

    /**
     * Create error message if parsed document does not contains particular element (imdb rating, title, etc.)
     * @param seriesUrl parsed url
     * @param errorObject absent property
     * @return Converted error message
     */
    private String createErrorMessage (String seriesUrl, String errorObject) {
        return String.format("Series with url %s, does not contains %s.", seriesUrl, errorObject);
    }

    private int parseNumberOfEpisodes (Element element) {
        return element.select(".list.detail.eplist > .list-item").size();
    }

    private List<Episode> filterEpisodesForUpdate (List<Episode> episodes) {
        return episodes.stream().filter(e -> Objects.isNull(e.getEpisodeDate()) || Objects.isNull(e.getSummary()) ||
                Objects.isNull(e.getImdbRating()) || e.getImdbRating() == 0).collect(Collectors.toList());
    }
}
