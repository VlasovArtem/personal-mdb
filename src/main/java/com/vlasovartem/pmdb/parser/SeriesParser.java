package com.vlasovartem.pmdb.parser;

import com.sun.javadoc.Doc;
import com.vlasovartem.pmdb.entity.Episode;
import com.vlasovartem.pmdb.entity.Season;
import com.vlasovartem.pmdb.entity.Series;
import com.vlasovartem.pmdb.utils.exception.SeriesParsingException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.vlasovartem.pmdb.utils.HtmlElementUtils.findText;
import static java.util.Objects.*;
import static java.util.Objects.isNull;
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
        LOG.info(String.format("Update imdb rating for series %s", series.getTitle()));
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
        LOG.info(String.format("Update next episode for series %s", series.getTitle()));
        series.setNextEpisode(parseNextEpisode(series));
    }

    /**
     * Check series is finished, if it`s not finished update next episode
     * @param series updated series
     */
    public void update (Series series) {
        if (nonNull(series)) {
            LOG.info(String.format("Update series %s", series.getTitle()));
            checkSeriesIsFinished(series);
            if(!series.isFinished()) {
                if(!checkEndOfTheSeries(series)) {
                    series.setNextEpisode(parseNextEpisode(series));
                    if(series.getSeasons().size() < findAmountOfSeasons(series.getImdbUrl())) {
                        Season season = seasonParser
                                .parse(IMDB_SERIES_SEASON_URL_PATTERN
                                        .replace("@ID", series.getId())
                                        .replace("@NUMBER", String.valueOf(series.getSeasons().size() + 1)));
                        if(nonNull(season)) {
                            series.getSeasons().add(season);
                        }
                    }
                    seasonParser.update(filterSeasonForUpdate(series.getSeasons()));
                }
            } else if (series.isFinished() && isNull(series.getSeriesEnd())) {
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
            Element element = document.select("#main .findSection .result_text").first();
            boolean hasLocalizedTitle = element.text().toLowerCase().contains("aka");
            String localizeTitle = null;
            if(hasLocalizedTitle) {
                localizeTitle = element.text().toLowerCase();
            }
            element = element.select("a").first();
            if(nonNull(element)) {
                if(StringUtils.equalsIgnoreCase(element.text(), title) || (hasLocalizedTitle && localizeTitle
                        .contains(title.toLowerCase()))) {
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
                series.setTitle(checkProperty(parseTitle(titleLayout), seriesUrl, "title"));
                series.setFinished(checkProperty(parseSeriesIsFinished(titleLayout), seriesUrl, "finished"));
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
     * Parse title of the series, if series title contains locale titles, method will parse title-extra on the
     * English, otherwise will parse original title
     * @param titleLayout part of the series page
     * @return title of it is exists otherwise return null
     */
    private String parseTitle (Element titleLayout) {
        Elements headerElements = titleLayout.select("#overview-top > h1 > span");
        if(nonNull(headerElements)) {
            boolean hasExtraTitle = headerElements.stream().anyMatch(element1 -> element1.hasClass("title-extra"));
            if(hasExtraTitle) {
                String titleExtra = headerElements.stream()
                        .filter(el -> el.hasClass("title-extra")).findFirst().get().text();
                if(nonNull(titleExtra)) {
                    return Stream.of(titleExtra.split("\"")).filter(te -> te.matches("(\\w+\\s?)+")).findFirst()
                            .orElse(null);
                }
            } else {
                return headerElements.stream().filter(el -> el.hasClass("itemprop")).findFirst().get().text();
            }
        }
        return null;
    }

    private Boolean parseSeriesIsFinished (Element titleLayout) {
        String seriesFinishedPattern = "\\(\\d{4}(-|–)\\d{4}\\)";
        Elements headerElements = titleLayout.select("#overview-top > h1 > span");
        if(nonNull(headerElements)) {
            Optional<Element> finishedElement = headerElements.stream().filter(el -> el.hasClass("nobr")).findFirst();
            if(finishedElement.isPresent()) {
                return finishedElement.get().text().matches(seriesFinishedPattern);
            }

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
        if(nonNull(seriesUrl)) {
            return Stream.of(seriesUrl.split("/")).filter(u -> u.matches("tt\\d{7}")).findFirst().orElse(null);
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
            return seasons.stream().min(Comparator.comparingInt(Season::getSeasonNumber)).get().getSeasonStart();
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
            return seasons.stream()
                    .max(Comparator.comparingInt(Season::getSeasonNumber))
                    .get().getEpisodes().stream()
                    .max(Comparator.comparingInt(Episode::getEpisodeNumber))
                    .get().getEpisodeDate();
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
        if(!checkNextEpisode(series.getNextEpisode()) && nonNull(series.getSeasons())) {
            return series.getSeasons().stream()
                    .filter(s -> isNull(s.getSeasonEnd()) || s.getSeasonEnd().isAfter(LocalDate.now()))
                    .flatMap(season -> season.getEpisodes().stream())
                    .filter(e -> nonNull(e.getEpisodeDate()))
                    .sorted((o1, o2) -> o2.getEpisodeDate().compareTo(o1.getEpisodeDate()))
                    .findFirst().orElse(null);
        } else {
            return series.getNextEpisode();
        }
    }

    /**
     * Check next episode. Method will return true if next episode not null and next episode date not null and next
     * episode date is after tomorrow.
     * @param episode next episode from the series
     * @return true if next episode is matches predicates otherwise false.
     */
    private boolean checkNextEpisode(Episode episode) {
        return nonNull(episode) && nonNull(episode.getEpisodeDate())
                && (episode.getEpisodeDate().isAfter(LocalDate.now().minusDays(1)));
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

    /**
     * Check that series is finished. Series is finished if last season of the series does not contains null episode
     * date or episode date is after today
     * @param series Checked series
     */
    private void checkSeriesIsFinished (Series series) {
        try {
            Document document = Jsoup.connect(series.getImdbUrl()).get();
            Element element = document.select("#title-overview-widget-layout").first();
            parseSeriesIsFinished(element);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parse number of season of the Series
     * @param seriesUrl series imdb url
     * @return number of seasons
     */
    private int findAmountOfSeasons(String seriesUrl) {
        try {
            String seasonUrlPatten = "/title/tt\\d{7}/episodes\\?season=\\d.+";
            Document document = Jsoup.connect(seriesUrl).timeout(400000).get();
            return (int) document.select("#title-episode-widget > .seasons-and-year-nav a").stream().filter
                    (el -> el.attr("href").matches(seasonUrlPatten)).count();
        } catch (IOException e) {
            LOG.error(String.format("Series url %s is invalid", seriesUrl));
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Check series is end
     * @param series
     * @return
     */
    private boolean checkEndOfTheSeries(Series series) {
        return nonNull(series.getSeasons()) && !series.getSeasons().stream()
                .max(Comparator.comparingInt(Season::getSeasonNumber)).get().getEpisodes().stream()
                .anyMatch(episode1 -> nonNull(episode1.getEpisodeDate()) && episode1.getEpisodeDate().isAfter(LocalDate.now()));
    }

    private List<Season> filterSeasonForUpdate (List<Season> seasons) {
        return seasons.stream()
                .filter(s -> isNull(s.getSeasonStart()) || isNull(s.getSeasonEnd()))
                .collect(Collectors.toList());
    }
}
