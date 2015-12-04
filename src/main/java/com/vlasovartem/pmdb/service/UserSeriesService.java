package com.vlasovartem.pmdb.service;

/**
 * Created by artemvlasov on 04/12/15.
 */
public interface UserSeriesService {
    void addUserService (String title);
    void parse(String id);

    void deleteUserSeries(String id);

    void updateUserSeries(String id, String title);
}
