package com.vlasovartem.pmdb.repository;

import com.vlasovartem.pmdb.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

/**
 * Created by artemvlasov on 04/12/15.
 */
public interface UserRepository extends MongoRepository<User, String> {
    @Query("{'$and' : [{'$or' : [{'username' : ?0}, {'email' : ?0}]}]}")
    User loginUser (String loginData);
}
