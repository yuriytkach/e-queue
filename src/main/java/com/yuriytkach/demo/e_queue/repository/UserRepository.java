package com.yuriytkach.demo.e_queue.repository;

import com.yuriytkach.demo.e_queue.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, String> {

}
