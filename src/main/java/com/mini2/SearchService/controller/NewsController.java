package com.mini2.SearchService.controller;


import com.mini2.SearchService.dto.response.FavoriteNewsLinkResponse;
import com.mini2.SearchService.dto.response.NewsResponse;
import com.mini2.SearchService.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "News API", description = "뉴스 조회 및 AI 요약 관련 API")
public class NewsController {

    private final NewsService newsService;

    @GetMapping("/api/search/v1/{keyword}/{userId}")
    public ResponseEntity<List<NewsResponse>> searchNews(
             @PathVariable String keyword,
             @PathVariable Long userId) {
        List<NewsResponse> newsList = newsService.getSearchNews(keyword,userId);
        return ResponseEntity.ok(newsList);
    }


    // feinclient를 사용하여 favoritenewsLinks를 불러온다.
    @GetMapping("/favorite/newslinks")
    public List<String> getFavoriteNewsLinks(@RequestParam Long userId) {
        return newsService.getUserFavoriteNewsLinks(userId);
    }
//    @GetMapping("/api/favorite/newslinks")
//    public ResponseEntity<List<FavoriteNewsLinkResponse>> getFavoriteNewsLinks(
//            @RequestParam("userId") Long userId){
//        List<FavoriteNewsLinkResponse> favoriteNewsLinkList = newsService.getUserFavoriteLink(userId);
//        return ResponseEntity.ok(favoriteNewsLinkList);
//    };


//    fein client 요청을 받는걸 테스트 해야한다.
    @GetMapping("/api/favorite/v1/newslinks/{userId}")
    public ResponseEntity<List<String>> favoriteNewsLinks(
            @PathVariable Long userId
    ){
        List<String> favoriteNewsLinkList = newsService.getUserFavoriteLink(userId);
        return ResponseEntity.ok(favoriteNewsLinkList);

    }
}
