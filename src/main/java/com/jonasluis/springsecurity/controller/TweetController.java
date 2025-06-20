package com.jonasluis.springsecurity.controller;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.jonasluis.springsecurity.dto.CreateTweetDTO;
import com.jonasluis.springsecurity.dto.FeedDTO;
import com.jonasluis.springsecurity.dto.FeedItemDTO;
import com.jonasluis.springsecurity.entities.Role;
import com.jonasluis.springsecurity.entities.Tweet;
import com.jonasluis.springsecurity.repository.TweetRepository;
import com.jonasluis.springsecurity.repository.UserRepository;

@RestController
public class TweetController {
    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;

    public TweetController(TweetRepository tweetRepository, UserRepository userRepository) {
        this.tweetRepository = tweetRepository;
        this.userRepository = userRepository;
    }


    @GetMapping("/feed")
    public ResponseEntity<FeedDTO> feed(@RequestParam(value = "page", defaultValue = "0") int page,
                                        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize ){

    var tweets = tweetRepository.findAll(
        PageRequest.of(page, pageSize, Sort.Direction.DESC, "creationTimestamp"))
        .map(tweet -> new FeedItemDTO(
                                tweet.getTweetId(), 
                                tweet.getContent(), 
                                tweet.getUser().getUsername())
            );

    return ResponseEntity.ok(new FeedDTO(
                                        tweets.getContent(), 
                                        page, 
                                        pageSize, 
                                        tweets.getTotalPages(), 
                                        tweets.getTotalElements())
                            );

    }



    @PostMapping("/tweets")
    public ResponseEntity<Void> createTweet(@RequestBody CreateTweetDTO dto, JwtAuthenticationToken token) {
        var user = userRepository.findById(UUID.fromString(token.getName()));      
        
        var tweet = new Tweet();
        tweet.setUser(user.get());
        tweet.setContent(dto.content());

        tweetRepository.save(tweet);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tweets/{id}")
    ResponseEntity<Void> deleteTweet(@PathVariable Long id, JwtAuthenticationToken token) {

        var user = userRepository.findById(UUID.fromString(token.getName())); 
        var tweet = tweetRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var isAdmin = user.get().getRoles()
                .stream().anyMatch(role -> role.getName().equalsIgnoreCase(Role.Value.ADMIN.name()));
        
        if (isAdmin || tweet.getUser().getUserId().equals(UUID.fromString(token.getName()))) {
            tweetRepository.deleteById(id);
        }else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok().build();
    }




}
