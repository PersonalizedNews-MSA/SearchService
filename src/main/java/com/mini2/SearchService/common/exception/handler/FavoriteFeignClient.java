package com.mini2.SearchService.common.exception.handler;

import com.mini2.SearchService.dto.response.FavoriteNewsLinkResponse;
import com.mini2.SearchService.dto.response.NewsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "favorite-service", url = "http://k8s-favorite-service:8080")
public interface FavoriteFeignClient {
    @GetMapping("/api/favorite/newslinks/{userId}")
    List<String> getFavoriteNewsLinks(@PathVariable("userId") Long userId);
}