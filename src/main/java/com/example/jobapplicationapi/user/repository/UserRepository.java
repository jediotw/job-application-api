package com.example.jobapplicationapi.user.repository;

import com.example.jobapplicationapi.user.model.User;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
  // doesn't comes with crudrepo so we have to defien by ourself
  Optional<User> findByEmail(String email);
}
