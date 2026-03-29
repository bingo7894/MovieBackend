package com.nerflix.clone.service;

import com.nerflix.clone.dto.response.MessageResponse;
import com.nerflix.clone.dto.response.PageResponse;
import com.nerflix.clone.dto.response.VideoResponse;

public interface WatchListService {
    MessageResponse addToWatchlist(String email, Long videoId);

    MessageResponse removeWatchList(String email, Long videoId);

    PageResponse<VideoResponse> getWatchListPaginated(String email, int page, int size, String search);
}
