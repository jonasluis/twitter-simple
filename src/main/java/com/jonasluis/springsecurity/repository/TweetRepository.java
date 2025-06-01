package com.jonasluis.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.jonasluis.springsecurity.entities.Tweet;

@Repository
public interface TweetRepository  extends JpaRepository<Tweet,  Long>{
}
