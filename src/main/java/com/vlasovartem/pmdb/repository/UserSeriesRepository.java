package com.vlasovartem.pmdb.repository;

import com.vlasovartem.pmdb.entity.UserSeries;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by artemvlasov on 04/12/15.
 */
public interface UserSeriesRepository extends MongoRepository<UserSeries, String> {
    int countByTitleIgnoreCase(String title);
}
