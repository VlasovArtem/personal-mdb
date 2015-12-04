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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Series)) return false;

        Series series = (Series) o;

        if (finished != series.finished) return false;
        if (id != null ? !id.equals(series.id) : series.id != null) return false;
        if (posterUrl != null ? !posterUrl.equals(series.posterUrl) : series.posterUrl != null) return false;
        if (imdbUrl != null ? !imdbUrl.equals(series.imdbUrl) : series.imdbUrl != null) return false;
        if (title != null ? !title.equals(series.title) : series.title != null) return false;
        if (seriesStart != null ? !seriesStart.equals(series.seriesStart) : series.seriesStart != null) return false;
        if (seriesEnd != null ? !seriesEnd.equals(series.seriesEnd) : series.seriesEnd != null) return false;
        if (genres != null ? !genres.equals(series.genres) : series.genres != null) return false;
        if (imdbRating != null ? !imdbRating.equals(series.imdbRating) : series.imdbRating != null) return false;
        if (plot != null ? !plot.equals(series.plot) : series.plot != null) return false;
        if (creators != null ? !creators.equals(series.creators) : series.creators != null) return false;
        if (actors != null ? !actors.equals(series.actors) : series.actors != null) return false;
        if (seasons != null ? !seasons.equals(series.seasons) : series.seasons != null) return false;
        return !(nextEpisode != null ? !nextEpisode.equals(series.nextEpisode) : series.nextEpisode != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (posterUrl != null ? posterUrl.hashCode() : 0);
        result = 31 * result + (imdbUrl != null ? imdbUrl.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (seriesStart != null ? seriesStart.hashCode() : 0);
        result = 31 * result + (seriesEnd != null ? seriesEnd.hashCode() : 0);
        result = 31 * result + (finished ? 1 : 0);
        result = 31 * result + (genres != null ? genres.hashCode() : 0);
        result = 31 * result + (imdbRating != null ? imdbRating.hashCode() : 0);
        result = 31 * result + (plot != null ? plot.hashCode() : 0);
        result = 31 * result + (creators != null ? creators.hashCode() : 0);
        result = 31 * result + (actors != null ? actors.hashCode() : 0);
        result = 31 * result + (seasons != null ? seasons.hashCode() : 0);
        result = 31 * result + (nextEpisode != null ? nextEpisode.hashCode() : 0);
        return result;
    }
}
