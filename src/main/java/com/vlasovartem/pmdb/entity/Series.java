package com.vlasovartem.pmdb.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vlasovartem.pmdb.utils.serializer.LocalDateSerializer;
import com.vlasovartem.pmdb.utils.view.SeriesView;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by artemvlasov on 29/11/15.
 */
@Document(collection = "series")
public class Series {
    private String id;
    private String posterUrl;
    private String imdbUrl;
    private String title;
    private LocalDate seriesStart;
    private LocalDate seriesEnd;
    private boolean finished;
    private List<String> genres;
    private Double imdbRating;
    private String plot;
    private List<String> creators;
    private List<String> actors;
    @JsonView(SeriesView.FullInfoView.class)
    private List<Season> seasons;
    private Episode nextEpisode;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getImdbUrl() {
        return imdbUrl;
    }

    public void setImdbUrl(String imdbUrl) {
        this.imdbUrl = imdbUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getSeriesStart() {
        return seriesStart;
    }

    public void setSeriesStart(LocalDate seriesStart) {
        this.seriesStart = seriesStart;
    }

    public LocalDate getSeriesEnd() {
        return seriesEnd;
    }

    public void setSeriesEnd(LocalDate seriesEnd) {
        this.seriesEnd = seriesEnd;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public Double getImdbRating() {
        return imdbRating;
    }

    public void setImdbRating(Double imdbRating) {
        this.imdbRating = imdbRating;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public List<String> getCreators() {
        return creators;
    }

    public void setCreators(List<String> creators) {
        this.creators = creators;
    }

    public List<String> getActors() {
        return actors;
    }

    public void setActors(List<String> actors) {
        this.actors = actors;
    }

    public List<Season> getSeasons() {
        return seasons;
    }

    public void setSeasons(List<Season> seasons) {
        this.seasons = seasons;
    }

    public Episode getNextEpisode() {
        return nextEpisode;
    }

    public void setNextEpisode(Episode nextEpisode) {
        this.nextEpisode = nextEpisode;
    }
}
