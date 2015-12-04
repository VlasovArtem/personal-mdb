package com.vlasovartem.pmdb.entity;

import java.time.LocalDate;

/**
 * Created by artemvlasov on 29/11/15.
 */
public class Episode {
    private int episodeNumber;
    private int seasonNumber;
    private LocalDate episodeDate;
    private String summary;
    private String title;
    private Double imdbRating;
    private String url;

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(int episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public void setSeasonNumber(int seasonNumber) {
        this.seasonNumber = seasonNumber;
    }

    public LocalDate getEpisodeDate() {
        return episodeDate;
    }

    public void setEpisodeDate(LocalDate episodeDate) {
        this.episodeDate = episodeDate;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getImdbRating() {
        return imdbRating;
    }

    public void setImdbRating(Double imdbRating) {
        this.imdbRating = imdbRating;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Episode)) return false;

        Episode episode = (Episode) o;

        if (episodeNumber != episode.episodeNumber) return false;
        if (seasonNumber != episode.seasonNumber) return false;
        if (episodeDate != null ? !episodeDate.equals(episode.episodeDate) : episode.episodeDate != null) return false;
        if (summary != null ? !summary.equals(episode.summary) : episode.summary != null) return false;
        if (title != null ? !title.equals(episode.title) : episode.title != null) return false;
        if (imdbRating != null ? !imdbRating.equals(episode.imdbRating) : episode.imdbRating != null) return false;
        return !(url != null ? !url.equals(episode.url) : episode.url != null);

    }

    @Override
    public int hashCode() {
        int result = episodeNumber;
        result = 31 * result + seasonNumber;
        result = 31 * result + (episodeDate != null ? episodeDate.hashCode() : 0);
        result = 31 * result + (summary != null ? summary.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (imdbRating != null ? imdbRating.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }
}
