package com.nerflix.clone.controller;

import com.nerflix.clone.dto.response.MessageResponse;
import com.nerflix.clone.dto.response.PageResponse;
import com.nerflix.clone.dto.response.VideoResponse;
import com.nerflix.clone.entity.Video;
import com.nerflix.clone.service.WatchListService;
import com.oracle.svm.core.annotate.Delete;
import jakarta.mail.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/watchList")
public class WatchListController {

    @Autowired
    private WatchListService watchListService;

    @PostMapping("/{videoId}")
    public ResponseEntity<MessageResponse> addToWatchlist(@PathVariable Long videoId, Authentication authentication){
        String email = authentication.getName();
        return ResponseEntity.ok(watchListService.addToWatchlist(email,videoId));
    }

    @DeleteMapping("/{videoId}")
    public ResponseEntity<MessageResponse> removeWatchList(@PathVariable Long videoId ,Authentication authentication){
        String email = authentication.getName();
        return ResponseEntity.ok(watchListService.removeWatchList(email,videoId));
    }

    @GetMapping
    public ResponseEntity<PageResponse<VideoResponse>> getWatchList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Authentication authentication){

        String email = authentication.getName();

        PageResponse<VideoResponse> response = watchListService.getWatchListPaginated(email,page,size,search);

        return ResponseEntity.ok(response);

    }

}
