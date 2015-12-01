package com.vlasovartem.pmdb.utils.parser;

import com.vlasovartem.pmdb.entity.Episode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by artemvlasov on 29/11/15.
 */
public class EpisodeParser {
    private static final Log LOG = LogFactory.getLog(EpisodeParser.class);

    /**
     * Parse episode
     * @param episodeUrl episode url
     * @return parsed episode
     */
    public Episode parse(String episodeUrl) {
        if(Objects.nonNull(episodeUrl)) {
            return parseEpisodeContent(episodeUrl);
        }
        return null;
    }

    /**
     * Parse episodes from the list of episodes urls
     * @param episodesUrls list of episode urls
     * @return list of Episode
     */
    public List<Episode> parse(List<String> episodesUrls) {
        return episodesUrls.stream().map(this::parse).collect(Collectors.toList());
    }

    /**
     * Update episode
     * @param episode updated episode
     */
    public void updateEpisode (Episode episode) {
        try {
            Document document = Jsoup.connect(episode.getUrl()).get();
            Element element = document.getElementById("overview-top");
            if(Objects.isNull(episode.getEpisodeDate())) {
                parseTitleAndDate(element, episode);
            }
            if(Objects.isNull(episode.getImdbRating()) || episode.getImdbRating() == 0) {
                episode.setImdbRating(parseImdbRating(element));
            }
            if(Objects.isNull(episode.getSummary())) {
                episode.setSummary(parseSummary(element));
            }
        } catch (IOException e) {
            LOG.warn(String.format("Url %s is invalid", episode.getUrl()));
            e.printStackTrace();
        }
    }

    /**
     * Parse list of episodes
     * @param episodes updated list of episodes
     */
    public void updateEpisodes (List<Episode> episodes) {
        episodes.forEach(this::updateEpisode);
    }

    /**
     * Parse episode content from the episode url
     * @param episodeUrl episode url
     * @return episode
     */
    private Episode parseEpisodeContent (String episodeUrl) {
        try {
            Document document = Jsoup.connect(episodeUrl).get();
            Element element = document.getElementById("overview-top");
            Episode episode = new Episode();
            episode.setUrl(episodeUrl);
            checkProperty(parseEpisodeInfo(element, episode), episodeUrl, "season and episode numbers");
            checkProperty(parseTitleAndDate(element, episode), episodeUrl, "title and date");
            if(Objects.nonNull(episode.getEpisodeDate())) {
                episode.setImdbRating(checkProperty(parseImdbRating(element), episodeUrl, "imdb rating"));
                episode.setSummary(checkProperty(parseSummary(element), episodeUrl, "summary"));
            }
            return episode;
        } catch (IOException e) {
            LOG.warn(String.format("Episode %s is invalid", episodeUrl));
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parse episode info about number of the season and number of the episode
     * @param episodeInfo parsed document
     * @param episode update episode
     * @return true if parse is pass successfully otherwise return null
     */
    private Boolean parseEpisodeInfo (Element episodeInfo, Episode episode) {
        Pattern pattern = Pattern.compile("Season \\d+|Episode \\d+");
        try {
            String episodeData = episodeInfo
                    .getElementsByClass("tv_header").first()
                    .getElementsByClass("nobr").first().text();
            Matcher matcher = pattern.matcher(episodeData);
            while (matcher.find()) {
                if(matcher.group().toLowerCase().contains("episode")) {
                    String[] elems = matcher.group().split(" ");
                    for(String elem : elems) {
                        if(elem.matches("\\d+")) {
                            episode.setEpisodeNumber(Integer.parseInt(elem));
                        }
                    }
                } else if (matcher.group().toLowerCase().contains("season")) {
                    String[] elems = matcher.group().split(" ");
                    for(String elem : elems) {
                        if(elem.matches("\\d+")) {
                            episode.setSeasonNumber(Integer.parseInt(elem));
                        }
                    }
                }
            }
            return true;
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Parse title and date of the episode
     * @param episodeInfo parsed element info
     * @param episode Updated episode
     * @return true if element contains information about episode title and date otherwise return null
     */
    private Boolean parseTitleAndDate (Element episodeInfo, Episode episode) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("(d MMM uuuu)");
        try {
            Elements headerElements = episodeInfo.getElementsByClass("header").first().getElementsByTag("span");
            for(Element element : headerElements) {
                if (element.attr("itemprop").matches("name")) {
                    episode.setTitle(element.text());
                } else if (element.hasClass("nobr")) {
                    try {
                        episode.setEpisodeDate(LocalDate.parse(element.text().replaceAll("\\.", ""), formatter));
                    } catch (DateTimeParseException ignored) {}
                }
            }
            return true;
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Parse episode imdb rating
     * @param episodeInfo parsed element
     * @return imdb rating or null if its not found
     */
    private Double parseImdbRating (Element episodeInfo) {
        try {
            return Double.valueOf(episodeInfo.getElementsByClass("star-box-giga-star").first().text());
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Parse Episode summary
     * @param episodeInfo parsed element
     * @return Summary of the episode
     */
    private String parseSummary (Element episodeInfo) {
        try {
            String description = episodeInfo.getElementsByAttributeValue("itemprop", "description").first().text();
            if("Add a Plot".equals(description)) {
                return null;
            }
            if (description.contains("...")) {
                description = description.replaceFirst("\\.\\.\\.", "@");
                if(description.lastIndexOf(".") >= 0) {
                    return description.substring(0, description.lastIndexOf(".") + 1);
                } else {
                    return description.substring(0, description.lastIndexOf("@"));
                }
            }
            return description;
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Check is Series property is null
     * @param t Series property
     * @param episodeUrl series url
     * @param errorObject error Object
     * @param <T> Series property
     * @return Series property
     */
    private <T> T checkProperty(T t, String episodeUrl, String errorObject) {
        if(Objects.isNull(t)) {
            LOG.warn(createErrorMessage(episodeUrl, errorObject));
        }
        return t;
    }

    /**
     * Create error message if parsed document does not contains particular element (imdb rating, title, etc.)
     * @param episodeUrl parsed url
     * @param errorObject absent property
     * @return Converted error message
     */
    private String createErrorMessage (String episodeUrl, String errorObject) {
        return String.format("Episode with url %s, does not contains %s.", episodeUrl, errorObject);
    }
}
