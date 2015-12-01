package com.vlasovartem.pmdb.utils.parser;

import com.vlasovartem.pmdb.entity.Episode;
import com.vlasovartem.pmdb.entity.Season;
import com.vlasovartem.pmdb.entity.Series;
import com.vlasovartem.pmdb.repository.SeriesRepository;
import org.apache.commons.io.FileUtils;
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
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

/**
 * Created by artemvlasov on 29/11/15.
 */
@Component
public class SeriesParser {

    private static final String IMDB_SERIES_SEARCH_URL = "http://www.imdb" +
            ".com/find?q=search_title&s=tt&ttype=tv&ref_=fn_tv";
    private static final String TITLE_SEARCH_CAP = "search_title";
    private static final Log LOG = LogFactory.getLog(SeriesParser.class);
    private static final String IMDB_SERIES_URL_PATTERN = "http://www\\.imdb\\.com/title/tt\\d+/\\?ref_=fn_tv_tt_1";
    private static final String IMDB_INITIAL_URL = "http://www.imdb.com";
    private SeasonParser seasonParser;
    @Autowired
    private SeriesRepository seriesRepository;

    @Autowired
    public SeriesParser(SeasonParser seasonParser) {
        this.seasonParser = seasonParser;
    }

    public SeriesParser() {
    }

    /**
     * Parse series by Series title
     * @param title Series title
     * @return Parsed series
     */
    public Series parse (String title) {
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
     * Update imdb rating
     * @param series updated series
     */
    public void updateImdbRating(Series series) {
        try {
            Document document = Jsoup.connect(series.getImdbUrl()).get();
            Element element = document.getElementById("title-overview-widget-layout");
            series.setImdbRating(parseImbdRating(element));
        } catch (IOException e) {
            LOG.warn(String.format("Series %s is invalid", series.getImdbUrl()));
            e.printStackTrace();
        }
    }

    /**
     * Update series data
     * @param series updated series
     */
    public void update(Series series) {
        if (Objects.nonNull(series)) {
            seasonParser.update(series.getSeasons());
            if(!series.isFinished()) {
                series.setSeriesEnd(parseSeriesEndDate(series.getSeasons()));
                series.setFinished(parseSeriesIsFinished(series));
            }
            series.setNextEpisode(parseNextEpisodeDate(series));
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
            Document document = Jsoup.connect(preparedUrl).get();
            Elements elements = document.getElementsByClass("findList");
            if(elements.size() != 0) {
                return IMDB_INITIAL_URL + elements.get(0)
                        .getElementsByClass("odd").stream().findFirst().orElseThrow(() -> new RuntimeException("Find list is not contais odd elements"))
                        .getElementsByClass("result_text").stream().findFirst().orElseThrow(() -> new RuntimeException("Find Result is not contains result text"))
                        .getElementsByTag("a").stream().findFirst().orElseThrow(() -> new RuntimeException("Result text us not contains a tag"))
                        .attr("href");
            } else {
                LOG.info(String.format("Url %s is not contains findList", preparedUrl));
            }
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
            Element titleLayout = document.getElementById("title-overview-widget-layout");
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
                series.setFinished(parseSeriesIsFinished(series));
                series.setNextEpisode(checkProperty(parseNextEpisodeDate(series), seriesUrl, "next episode date"));
                return series;
            } else {
                LOG.warn(createErrorMessage(seriesUrl, "layout element"));
                return null;
            }
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
        try {
            return titleLayout.getElementById("img_primary").getElementsByTag("img").first().attr("src");
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Parse element from the header tag. Title of the Series and check that the series is finished
     * @param titleLayout element that contains parsed element
     * @param series Series that should pick parsed properties
     * @return true if all data parsed successfully otherwise return false
     */
    private Boolean parseSeriesHeader (Element titleLayout, Series series) {
        String seriesFinishedPattern = "\\(\\d{4}-\\d{4}\\)";
        try {
            Elements headerElements = titleLayout.getElementById("overview-top").getElementsByClass("header").first()
                    .getElementsByTag("span");
            boolean hasExtraTitle = headerElements.stream().anyMatch(element1 -> "title-extra".equals(element1.attr
                    ("class")));
            for (Element element : headerElements) {
                if (element.hasClass("itemprop") && !hasExtraTitle) {
                    series.setTitle(element.text());
                } else if (element.hasClass("title-extra")) {
                    String parsedText = element.text();
                    for(String title : parsedText.split("\"")) {
                        if(title.matches("(\\w+\\s?)+")) {
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
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parse Genre of the Series from parsed element
     * @param titleLayout Parsed data from url
     * @return List of the Genres
     */
    private List<String> parseGenres (Element titleLayout) {
        try {
            return titleLayout.getElementsByClass("infobar")
                    .first()
                    .getElementsByTag("span")
                    .stream()
                    .filter(e -> "genre".equals(e.attr("itemprop")))
                    .map(Element::text)
                    .collect(Collectors.toList());
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Parse IMDB Rating of the Series
     * @param titleLayout parsed element
     * @return parse rating or null of element was not found
     */
    private Double parseImbdRating (Element titleLayout) {
        try {
            return Double.valueOf(titleLayout.getElementsByClass("star-box-giga-star").first().text());
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Parse description of the Series
     * @param titleLayout parse element
     * @return Parsed description
     */
    private String parseDescription (Element titleLayout) {
        try {
            return titleLayout.getElementById("overview-top").getElementsByAttributeValue("itemprop", "description").text();
        } catch (NullPointerException e) {
            return null;
        }
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
        try {
            return titleLayout.getElementById("overview-top")
                    .getElementsByAttributeValue("itemprop", castType).first()
                    .getElementsByAttributeValue("itemprop", "name")
                    .stream()
                    .map(Element::text)
                    .collect(Collectors.toList());
        } catch (NullPointerException e) {
            return null;
        }
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
        String seasonUrlExample = "/title/@ID/episodes?season=@NUMBER&ref_=tt_eps_sn_@NUMBER";
        try {
            List<Element> elements = titleLayout
                    .getElementById("title-episode-widget")
                    .getElementsByClass("seasons-and-year-nav").first()
                    .getElementsByTag("a").stream().filter(e -> e.attr("href").matches(seasonUrlPatten)).collect(Collectors.toList());
            int lastSeasonNumber = Integer.valueOf(elements.stream()
                    .map(Element::text)
                    .max(Comparator.comparingInt(Integer::valueOf)).get());
            String seriesId = parseSeriesId(elements.stream().findFirst().get().attr("href"));
            List<String> listSeasonUrls = new ArrayList<>(lastSeasonNumber);
            if(Objects.nonNull(seriesId)) {
                for (int i = 0; i < lastSeasonNumber; i++) {
                    listSeasonUrls.add(IMDB_INITIAL_URL + seasonUrlExample
                            .replace("@ID", seriesId)
                            .replace("@NUMBER", String.valueOf(i + 1)));
                }
            }
            return seasonParser.parse(listSeasonUrls);
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Parse series start date
     * @param seasons list of the seasons
     * @return series start date
     */
    private LocalDate parseSeriesStartDate(List<Season> seasons) {
        if (Objects.nonNull(seasons)) {
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
        if(Objects.nonNull(seasons)) {
            if(seasons.get(seasons.size() - 1).getSeasonNumber() != seasons.size()) {
                return seasons.stream().max(Comparator.comparingInt(Season::getSeasonNumber)).orElse(null).getSeasonEnd();
            } else {
                return seasons.get(seasons.size() - 1).getSeasonEnd();
            }
        }
        return null;
    }

    /**
     * Parse that series is finished
     * @param series updated series
     * @return true if series is finished otherwise false
     */
    private boolean parseSeriesIsFinished (Series series) {
        if(Objects.isNull(series.getSeriesEnd())) {
            return false;
        } else {
            return LocalDate.now().isAfter(series.getSeriesEnd());
        }
    }

    /**
     * Parse next episode date
     * @param series updated series
     * @return date of the next episode.
     */
    public LocalDate parseNextEpisodeDate (Series series) {
        if(!series.isFinished()) {
            List<Season> seasons = series.getSeasons();
            if (Objects.nonNull(seasons) && seasons.size() != 0) {
                List<Episode> episodes = seasons.get(seasons.size() - 1).getEpisodes();
                if (Objects.nonNull(episodes) && episodes.size() != 0) {
                    LocalDate nextEpisodeDate = null;
                    LocalDate today = LocalDate.now();
                    for (Episode episode : episodes) {
                        if(Objects.nonNull(episode.getEpisodeDate()) && episode.getEpisodeDate().isAfter(today)) {
                            nextEpisodeDate = episode.getEpisodeDate();
                            break;
                        }
                    }
                    if (Objects.isNull(nextEpisodeDate)) {
                        nextEpisodeDate = LocalDate.of(today.getYear() + 1, Month.JANUARY, 1);
                    }
                    return nextEpisodeDate;
                }
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
}
