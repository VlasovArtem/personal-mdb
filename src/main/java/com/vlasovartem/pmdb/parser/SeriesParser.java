package com.vlasovartem.pmdb.parser;

import com.vlasovartem.pmdb.entity.Episode;
import com.vlasovartem.pmdb.entity.Season;
import com.vlasovartem.pmdb.entity.Series;
import com.vlasovartem.pmdb.entity.UserSeries;
import com.vlasovartem.pmdb.repository.SeriesRepository;
import com.vlasovartem.pmdb.repository.UserSeriesRepository;
import com.vlasovartem.pmdb.utils.exception.SeriesParsingException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jni.Local;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.vlasovartem.pmdb.utils.HtmlElementUtils.findText;
import static java.util.Objects.*;
import static java.util.Objects.nonNull;

/**
 * Created by artemvlasov on 29/11/15.
 */
@Component
public class SeriesParser {

    private static final String IMDB_SERIES_SEARCH_URL = "http://www.imdb" +
            ".com/find?q=search_title&s=tt&ttype=tv&ref_=fn_tv";
    private static final String IMDB_SERIES_SEASON_URL_PATTERN = "http://www.imdb" +
            ".com/title/@ID/episodes?season=@NUMBER&ref_=tt_eps_sn_@NUMBER";
    private static final String TITLE_SEARCH_CAP = "search_title";
    private static final Log LOG = LogFactory.getLog(SeriesParser.class);
    private static final String IMDB_SERIES_URL_PATTERN = "http://www\\.imdb\\.com/title/tt\\d+/\\?ref_=fn_tv_tt_1";
    private static final String IMDB_INITIAL_URL = "http://www.imdb.com";
    private SeasonParser seasonParser;

    @Autowired
    public SeriesParser(SeasonParser seasonParser) {
        this.seasonParser = seasonParser;
    }

    /**
     * Parse series by Series title
     * @param title Series title
     * @return Parsed series
     */
    public Series parse (String title) {
        LOG.info(String.format("%s is parsing", title));
        String seriesUrl = findSearchUrl (title);
        if(nonNull(seriesUrl) && seriesUrl.matches(IMDB_SERIES_URL_PATTERN)) {
            return parseSeriesUrlContent(seriesUrl);
        } else {
            LOG.error(String.format("Series url %s is not matcher pattern or null", seriesUrl));
        }
        return null;
    }

    /**
     * Parse series from the list of series titles
     * @param titles list of series titles
     * @return List of the parsed Series
     */
    public List<Series> parse (List<String> titles) {
        return titles.stream().map(this::parse).collect(Collectors.toList());
    }

    /**
     * Parse series from file
     * @param file File that contains list of title (row by row)
     * @return List of parsed series
     */
    public List<Series> parse (File file) {
        try {
            return parse(FileUtils.readLines(file));
        } catch (IOException e) {
            LOG.error(String.format("File %s is not found", file.getName()));
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Update imdb rating from series html page.
     * @param series updated series
     */
    public void updateImdbRating (Series series) {
        try {
            Document document = Jsoup.connect(series.getImdbUrl()).timeout(200000).get();
            Element element = document.select("#title-overview-widget-layout").first();
            if(nonNull(element)) {
                series.setImdbRating(parseImbdRating(element));
            }
        } catch (SocketTimeoutException e) {
            updateImdbRating(series);
        } catch (IOException e) {
            LOG.warn(String.format("Series %s is invalid", series.getImdbUrl()));
            e.printStackTrace();
        }
    }

    /**
     * Update series next episode
     * @param series updated series
     */
    public void updateNextEpisode (Series series) {
        series.setNextEpisode(parseNextEpisode(series));
    }

    /**
     * Check series is finished, if it`s not finished update next episode
     * @param series updated series
     */
    public void update (Series series) {
        if (nonNull(series)) {
            series.setFinished(checkSeriesIsFinished(series));
            if(!series.isFinished()) {
                if(!checkEndOfTheSeries(series)) {
                    series.setNextEpisode(parseNextEpisode(series));
                    if(series.getSeasons().size() < findAmountOfSeasons(series.getImdbUrl())) {
                        Season season = seasonParser
                                .parse(IMDB_SERIES_SEASON_URL_PATTERN
                                        .replace("@ID", series.getId())
                                        .replace("@NUMBER", String.valueOf(series.getSeasons().size() + 1)));
                        if(Objects.nonNull(season)) {
                            series.getSeasons().add(season);
                        }
                    }
                    seasonParser.update(filterSeasonForUpdate(series.getSeasons()));
                }
            } else if (series.isFinished() && Objects.isNull(series.getSeriesEnd())) {
                series.setSeriesEnd(parseSeriesEndDate(series.getSeasons()));
            }
        }
    }

    /**
     * Update list of series
     * @param series updated list of series
     */
    public void update (List<Series> series) {
        series.forEach(this::update);
    }

    /**
     * Prepare search url for particular series title
     * @param title title of the Series
     * @return prepared series search url
     */
    private String prepareSearchUrl (String title) {
        return IMDB_SERIES_SEARCH_URL.replaceFirst(TITLE_SEARCH_CAP, title.replaceAll(" ", "+"));
    }

    /**
     * Search for title link from find title url
     * @param title Title of the series
     * @return String url of the search series
     */
    private String findSearchUrl (String title) {
        String preparedUrl = prepareSearchUrl(title);
        try {
            Document document = Jsoup.connect(preparedUrl).timeout(200000).get();
            Element element = document.select("#main .findSection .result_text > a").first();
            if(nonNull(element)) {
                if(StringUtils.equalsIgnoreCase(element.text(), title)) {
                    return IMDB_INITIAL_URL + element.attr("href");
                } else {
                    LOG.warn(String.format("Title of the series %s does not match any series", title));
                    throw new SeriesParsingException(String.format("Title of the series %s does not match any series", title));
                }
            } else {
                LOG.info(String.format("Url %s is not contains findList", preparedUrl));
                return null;
            }
        } catch (SocketTimeoutException e) {
            findSearchUrl(title);
        } catch (IOException e) {
            LOG.error(String.format("Connection to url %s cause some problems", preparedUrl));
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Parse Series all content from url
     * @param seriesUrl url of the series on imdb
     * @return converted Series
     */
    private Series parseSeriesUrlContent (String seriesUrl) {
        try {
            Document document = Jsoup.connect(seriesUrl).get();
            Element titleLayout = document.select("#title-overview-widget-layout").first();
            Series series = new Series();
            if(nonNull(titleLayout)) {
                series.setId(parseSeriesId(seriesUrl));
                series.setImdbUrl(seriesUrl);
                series.setPosterUrl(checkProperty(parseSeriesImageUrl(titleLayout), seriesUrl, "image url"));
                checkProperty(parseSeriesHeader(titleLayout, series), seriesUrl, "title or finished info");
                series.setGenres(checkProperty(parseGenres(titleLayout), seriesUrl, "genres"));
                series.setImdbRating(checkProperty(parseImbdRating(titleLayout), seriesUrl, "imdb rating"));
                series.setPlot(checkProperty(parseDescription(titleLayout), seriesUrl, "description"));
                series.setCreators(checkProperty(parseCreators(titleLayout), seriesUrl, "creators"));
                series.setActors(checkProperty(parseActors(titleLayout), seriesUrl, "actors"));
                series.setSeasons(checkProperty(parseSeasons(document.getElementById("title-episode-widget")), seriesUrl, "seasons"));
                series.setSeriesStart(checkProperty(parseSeriesStartDate(series.getSeasons()), seriesUrl, "start date"));
                series.setSeriesEnd(checkProperty(parseSeriesEndDate(series.getSeasons()), seriesUrl, "end date"));
                series.setNextEpisode(checkProperty(parseNextEpisode(series), seriesUrl, "next episode"));
                return series;
            } else {
                LOG.warn(createErrorMessage(seriesUrl, "layout element"));
                return null;
            }
        } catch (SocketTimeoutException e) {
            parseSeriesUrlContent(seriesUrl);
        } catch (IOException e) {
            LOG.error(String.format("Series url %s is invalid", seriesUrl));
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Parse series image url from parsed content
     * @param titleLayout parser content
     * @return url of the image, or return null
     */
    private String parseSeriesImageUrl (Element titleLayout) {
        Element imgElement = titleLayout.select("#img_primary > .image > a > img").first();
        if(nonNull(imgElement)) {
            return imgElement.attr("src");
        }
        return null;
    }

    /**
     * Parse element from the header tag. Title of the Series and check that the series is finished
     * @param titleLayout element that contains parsed element
     * @param series Series that should pick parsed properties
     * @return true if all data parsed successfully otherwise return false
     */
    private Boolean parseSeriesHeader (Element titleLayout, Series series) {
        String seriesFinishedPattern = "\\(\\d{4}-\\d{4}\\)";
        Elements headerElements = titleLayout.select("#overview-top > h1 > span");
        if(nonNull(headerElements)) {
            boolean hasExtraTitle = headerElements.stream().anyMatch(element1 -> element1.hasClass("title-extra"));
            for (Element element : headerElements) {
                if (element.hasClass("itemprop") && !hasExtraTitle) {
                    series.setTitle(element.text());
                } else if (element.hasClass("title-extra")) {
                    String parsedText = element.text();
                    for (String title : parsedText.split("\"")) {
                        if (title.matches("(\\w+\\s?)+")) {
                            series.setTitle(title);
                        }
                    }
                } else if (element.hasClass("nobr")) {
                    String result = element.text();
                    if (result.matches(seriesFinishedPattern)) {
                        series.setFinished(true);
                    }
                }
            }
            return true;
        }
        return null;
    }

    /**
     * Parse Genre of the Series from parsed element
     * @param titleLayout Parsed data from url
     * @return List of the Genres
     */
    private List<String> parseGenres (Element titleLayout) {
        Elements genresElements = titleLayout.select("#overview-top > div.infobar > a > span[itemprop=genre]");
        if(nonNull(genresElements)) {
            return genresElements.stream().map(Element::text).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * Parse IMDB Rating of the Series
     * @param titleLayout parsed element
     * @return parse rating or null of element was not found
     */
    private Double parseImbdRating (Element titleLayout) {
        String rating = findText(".star-box-giga-star", titleLayout);
        if (nonNull(rating) && rating.matches("\\d\\.\\d")) {
            return Double.valueOf(rating);
        }
        return null;
    }

    /**
     * Parse description of the Series
     * @param titleLayout parse element
     * @return Parsed description
     */
    private String parseDescription (Element titleLayout) {
        return findText("#overview-top > p[itemprop=description]", titleLayout);
    }

    /**
     * Parse creators of the series
     * @param titleLayout parsed element
     * @return List of creators
     */
    private List<String> parseCreators (Element titleLayout) {
        return parsePersons(titleLayout, "creator");
    }

    /**
     * Parse actors of the series
     * @param titleLayout parsed element
     * @return List of creators;
     */
    private List<String> parseActors (Element titleLayout) {
        return parsePersons(titleLayout, "actors");
    }

    /**
     * Additional parser for creators and actors
     * @param titleLayout parsed element
     * @param castType cast type (actors or creators)
     * @return List of parsed objects
     */
    private List<String> parsePersons (Element titleLayout, String castType) {
        Elements castElements = titleLayout.select(
                String.format("#overview-top > div[itemprop=%s] > a[itemprop=url] > span[itemprop=name]",
                        castType));
        if(nonNull(castElements)) {
            return castElements.stream().map(Element::text).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * Parse id of the Series
     * @param seriesUrl series url
     * @return parsed id of the series
     */
    private String parseSeriesId (String seriesUrl) {
        for(String urlPart : seriesUrl.split("/")) {
            if(urlPart.matches("tt\\d{7}")) {
                return urlPart;
            }
        }
        return null;
    }

    /**
     * Parser series seasons
     * @param titleLayout parse element
     * @return list of the seasons
     */
    private List<Season> parseSeasons (Element titleLayout) {
        String seasonUrlPatten = "/title/tt\\d{7}/episodes\\?season=\\d.+";
        Elements seasonUrls = titleLayout.select("#title-episode-widget > .seasons-and-year-nav a");
        if(nonNull(seasonUrls)) {
            List<Element> seasons = seasonUrls.stream()
                    .filter(e -> e.attr("href").matches(seasonUrlPatten))
                    .collect(Collectors.toList());
            if(nonNull(seasons) && seasons.size() > 0) {
                int lastSeasonNumber = Integer.valueOf(seasons.stream()
                        .map(Element::text)
                        .max(Comparator.comparingInt(Integer::valueOf)).get());
                String seriesId = parseSeriesId(seasons.get(0).attr("href"));
                List<String> listSeasonUrls = new ArrayList<>(lastSeasonNumber);
                if(nonNull(seriesId)) {
                    for (int i = 0; i < lastSeasonNumber; i++) {
                        listSeasonUrls.add(IMDB_SERIES_SEASON_URL_PATTERN
                                .replace("@ID", seriesId)
                                .replace("@NUMBER", String.valueOf(i + 1)));
                    }
                    return seasonParser.parse(listSeasonUrls);
                }
            }

        }
        return null;
    }

    /**
     * Parse series start date
     * @param seasons list of the seasons
     * @return series start date
     */
    private LocalDate parseSeriesStartDate(List<Season> seasons) {
        if (nonNull(seasons)) {
            if(seasons.get(0).getSeasonNumber() != 1) {
                for (Season season : seasons) {
                    if(season.getSeasonNumber() == 1) {
                        return season.getSeasonStart();
                    }
                }
            } else {
                return seasons.get(0).getSeasonStart();
            }
        }
        return null;
    }

    /**
     * Parse series end date
     * @param seasons list of the seasons
     * @return series end date
     */
    private LocalDate parseSeriesEndDate(List<Season> seasons) {
        if(nonNull(seasons)) {
            if(seasons.get(seasons.size() - 1).getSeasonNumber() != seasons.size()) {
                return seasons.stream().max(Comparator.comparingInt(Season::getSeasonNumber)).orElse(null).getSeasonEnd();
            } else {
                return seasons.get(seasons.size() - 1).getSeasonEnd();
            }
        }
        return null;
    }

    /**
     * Parse next episode for the particular Series.
     * If Next episode is the end of the season. Than method try to find next episode from next season.
     * Otherwise if the next episode is not null, method will find list of the episode associated with current season
     * and find next episode.
     * If next episode is null, then method will parse throw season and find next episode. First it will filter
     * season which season end date is null or is after current day, then will find and return first episode which
     * date is after current date.
     * @param series updated series
     * @return date of the next episode.
     */
    private Episode parseNextEpisode(Series series) {
        if(Objects.nonNull(series)) {
            if(checkEndOfTheSeason(series)) {
                Season nextSeason = series.getSeasons().stream()
                        .filter(s -> s.getSeasonNumber() == series.getNextEpisode().getSeasonNumber() + 1).findFirst().get();
                if(Objects.nonNull(nextSeason.getEpisodes()) && nextSeason.getEpisodes().size() != 0) {
                    return nextSeason.getEpisodes().get(0);
                }
            } else if(Objects.nonNull(series.getNextEpisode())) {
                List<Episode> episodes = series.getSeasons().stream()
                        .filter(s -> s.getSeasonNumber() == series.getNextEpisode().getSeasonNumber())
                        .findFirst().get().getEpisodes();
                if(Objects.nonNull(episodes) && episodes.size() != 0) {
                    return episodes.stream()
                            .filter(e -> e.getEpisodeNumber() == series.getNextEpisode().getEpisodeNumber() + 1)
                            .findFirst().get();
                }
            } else {
                List<Season> matchedSeasons = series.getSeasons().stream()
                        .filter(s -> Objects.isNull(s.getSeasonEnd())
                                || s.getSeasonEnd().isAfter(LocalDate.now()))
                        .sorted(Comparator.comparingInt(Season::getSeasonNumber))
                        .collect(Collectors.toList());
                if(Objects.nonNull(matchedSeasons) && matchedSeasons.size() != 0) {
                    for (Season matchedSeason : matchedSeasons) {
                        for (Episode episode : matchedSeason.getEpisodes()) {
                            if(Objects.nonNull(episode.getEpisodeDate())
                                    && episode.getEpisodeDate().isAfter(LocalDate.now())) {
                                return episode;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Check episode date if episode not null or current date is before episode date and return true otherwise false
     * @param episodeDate Episode date
     * @return true if episode date is after current date otherwise false
     */
    private boolean checkEpisodeDate (LocalDate episodeDate) {
        return nonNull(episodeDate) && LocalDate.now().isBefore(episodeDate);
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
        if(isNull(t)) {
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

    private boolean checkSeriesIsFinished (Series series) {
        if(nonNull(series)) {
            if(nonNull(series.getSeriesEnd())
                    && LocalDate.now().minusYears(1).isAfter(series.getSeriesEnd())) return true;
            int currentSeason = series.getNextEpisode().getSeasonNumber();
            int currentEpisode = series.getNextEpisode().getEpisodeNumber();
            if(series.getSeasons().size() == currentSeason) {
                Season season = series.getSeasons().stream()
                        .filter(s -> s.getSeasonNumber() == currentSeason)
                        .findFirst()
                        .get();
                if(season.getEpisodes().size() == currentEpisode) {
                    if(findAmountOfSeasons(series.getImdbUrl()) == currentSeason) return true;
                }
            }

        }
        return false;
    }

    /**
     * Parse number of season of the Series
     * @param seriesUrl series imdb url
     * @return number of seasons
     */
    private int findAmountOfSeasons(String seriesUrl) {
        try {
            Document document = Jsoup.connect(seriesUrl).timeout(400000).get();
            Elements seasonUrls = document.select("#title-episode-widget > .seasons-and-year-nav a");
            return seasonUrls.size();
        } catch (IOException e) {
            LOG.error(String.format("Series url %s is invalid", seriesUrl));
            e.printStackTrace();
        }
        return 0;
    }

    private boolean checkEndOfTheSeries(Series series) {
        if(Objects.nonNull(series) && Objects.nonNull(series.getNextEpisode())) {
            int currentSeason = series.getNextEpisode().getSeasonNumber();
            if(series.getSeasons().size() == currentSeason) {
                return checkEndOfTheSeason(series);
            }
        }
        return false;
    }
    private boolean checkEndOfTheSeason (Series series) {
        if(Objects.nonNull(series) && Objects.nonNull(series.getNextEpisode())) {
            int currentSeason = series.getNextEpisode().getSeasonNumber();
            int currentEpisode = series.getNextEpisode().getEpisodeNumber();
            Season season;
            if(series.getSeasons().get(series.getSeasons().size() - 1).getSeasonNumber() != currentSeason) {
                season = series.getSeasons().stream()
                        .filter(s -> s.getSeasonNumber() == currentSeason)
                        .findFirst().get();
            } else {
                season = series.getSeasons().get(series.getSeasons().size() - 1);
            }
            return Objects.nonNull(season.getEpisodes()) && season.getEpisodes().size() == currentEpisode;
        }
        return false;
    }

    private List<Season> filterSeasonForUpdate (List<Season> seasons) {
        return seasons.stream()
                .filter(s -> Objects.isNull(s.getSeasonStart()) || Objects.isNull(s.getSeasonEnd()))
                .collect(Collectors.toList());
    }
}
