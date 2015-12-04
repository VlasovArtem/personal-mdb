package com.vlasovartem.pmdb.entity;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

/**
 * Created by artemvlasov on 04/12/15.
 */
@Document(collection = "userSeries")
public class UserSeries {
    private String id;
    @CreatedDate
    private LocalDate createdDate;
    private String title;

    public UserSeries() {
    }

    public UserSeries(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
