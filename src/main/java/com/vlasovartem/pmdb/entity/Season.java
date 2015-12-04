package com.vlasovartem.pmdb.entity;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by artemvlasov on 29/11/15.
 */
public class Season {
    private Integer seasonNumber;
    private LocalDate seasonStart;
    private LocalDate seasonEnd;
    private List<Episode> episodes;
    private String url;

    public Integer getSeasonNumber() {
        return seasonNumber;
    }

    public void setSeasonNumber(Integer seasonNumber) {
        this.seasonNumber = seasonNumber;
    }

    public LocalDate getSeasonStart() {
        return seasonStart;
    }

    public void setSeasonStart(LocalDate seasonStart) {
        this.seasonStart = seasonStart;
    }

    public LocalDate getSeasonEnd() {
        return seasonEnd;
    }

    public void setSeasonEnd(LocalDate seasonEnd) {
        this.seasonEnd = seasonEnd;
    }

    public List<Episode> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(List<Episode> episodes) {
        this.episodes = episodes;
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
        if (!(o instanceof Season)) return false;

        Season season = (Season) o;

        if (seasonNumber != null ? !seasonNumber.equals(season.seasonNumber) : season.seasonNumber != null)
            return false;
        if (seasonStart != null ? !seasonStart.equals(season.seasonStart) : season.seasonStart != null) return false;
        if (seasonEnd != null ? !seasonEnd.equals(season.seasonEnd) : season.seasonEnd != null) return false;
        if (episodes != null ? !episodes.equals(season.episodes) : season.episodes != null) return false;
        return !(url != null ? !url.equals(season.url) : season.url != null);

    }

    @Override
    public int hashCode() {
        int result = seasonNumber != null ? seasonNumber.hashCode() : 0;
        result = 31 * result + (seasonStart != null ? seasonStart.hashCode() : 0);
        result = 31 * result + (seasonEnd != null ? seasonEnd.hashCode() : 0);
        result = 31 * result + (episodes != null ? episodes.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }
}
