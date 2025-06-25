package com.mini2.SearchService.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mini2.SearchService.common.exception.CustomException;
import com.mini2.SearchService.common.exception.ErrorCode;
import com.mini2.SearchService.common.exception.handler.FavoriteFeignClient;
import com.mini2.SearchService.domain.Favorite;
import com.mini2.SearchService.dto.response.FavoriteNewsLinkResponse;
import com.mini2.SearchService.dto.response.NewsResponse;
import com.mini2.SearchService.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;



@RequiredArgsConstructor
@Slf4j
@Service
public class NewsService {
    @Value("${naver.client.id}")
    private String CLIENT_ID;
    @Value("${naver.search.key}")
    private String CLIENT_SECRET;

    private final FavoriteRepository favoriteRepository;
    private final ObjectMapper objectMapper;
    private final FavoriteFeignClient favoriteFeignClient;

    public List<NewsResponse> getSearchNews(String search, Long userId) {
        log.info("search keyword : {}", search);
        log.info("user id : {}", userId);
        List<NewsResponse> dtos = new ArrayList<>();
        String response = naverSearchApi(search, null, null);
        List<String> favorites = getUserFavoriteNewsLinks(userId);

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("items");
            if (items.isArray()) {
                for (JsonNode item : items) {
                    String link = item.path("link").asText();
                    if (!link.contains("https://n.news.naver.com") &&
                            !link.contains("https://news.naver.com") &&
                            !link.contains("https://m.sports.naver.com") &&
                            !link.contains("https://m.entertain.naver.com"))
                        continue; // 네이버 뉴스 기사만 가져옴

                    String title = Jsoup.parse(item.path("title").asText()).text();
                    String description = Jsoup.parse(item.path("description").asText()).text();
                    List<String> newInfo = getNewsInfo(link);
                    if (newInfo == null)
                        continue;
                    String pubDate = dateParser(Jsoup.parse(item.path("pubDate").asText()).text());
                    boolean isFavorite = favorites != null && favorites.contains(link);
                    dtos.add(NewsResponse.builder()
                            .title(title)
                            .link(link)
                            .thumbnail(newInfo.get(0))
                            .category(newInfo.get(1))
                            .favorite(isFavorite)
                            .description(description)
                            .pubDate(pubDate)
                            .build());
                }
            } else {
                log.info("네이버 뉴스 응답 객체 비었음");
            }
        } catch (Exception e) {
            log.error("[News Service] getSearchNews");
            e.printStackTrace();
            throw new CustomException(ErrorCode.NEWS_PARSING_ERROR);
        }

        return dtos;
    }
    public List<String> getNewsInfo(String path) {
        try {

            List<String> newsInfo = new ArrayList<>();
            String imageUrl = null;
            String category = null;
            Document doc = Jsoup.connect(path).get();
            if (path.contains("https://n.news.naver.com") || path.contains("https://news.naver.com")) {
                org.jsoup.select.Elements newsLists = doc.select("div[id^=img_a1]");
                for (Element news : newsLists) {
                    Element img = news.selectFirst("img");
                    if (img != null) {
                        imageUrl = img.attr("data-src");
                        newsInfo.add(imageUrl);
                        break;
                    }
                }
                Element selectedLink = doc.selectFirst("a.Nitem_link[aria-selected=true]");

                if (selectedLink != null) {
                    Element span = selectedLink.selectFirst("span.Nitem_link_menu");
                    if (span != null) {
                        category = span.text();
                        newsInfo.add(category);
                    }
                }
                if (newsInfo.size() < 2)
                    return null;

                return newsInfo;
            }
            org.jsoup.select.Elements newsLists = doc.select("span[class^=ArticleImage_image_wrap]");
            for (Element news : newsLists) {
                Element img = news.selectFirst("img");
                if (img != null) {
                    imageUrl = img.attr("src");
                    newsInfo.add(imageUrl);
                    break;
                }
            }
            if (path.contains("sports"))
                newsInfo.add("스포츠");
            newsInfo.add("엔터테인먼트");
            if (newsInfo.size() < 2)
                return null;

            return newsInfo;
        } catch (Exception e) {
            log.error("[News Service] getThumbnail");
            throw new CustomException(ErrorCode.NEWS_PARSING_ERROR);
        }
    }


    public String naverSearchApi(String query, Integer display, Integer start) {
        if (display == null)
            display = 100;
        if (start == null)
            start = 1;
        URI uri = UriComponentsBuilder
                .fromUriString("https://openapi.naver.com")
                .path("/v1/search/news.json")
                .queryParam("query", query)
                .queryParam("display", display)
                .queryParam("start", start)
                // .queryParam("sort", "date")
                .encode(Charset.forName("UTF-8"))
                .build()
                .toUri();

        WebClient webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-Naver-Client-Id", CLIENT_ID)
                .defaultHeader("X-Naver-Client-Secret", CLIENT_SECRET)
                .build();

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    @Transactional
    public List<String> getUserFavorite(Long userId) {
        List<Favorite> list = favoriteRepository.findByUserId(userId);
        if (list.isEmpty())
            return null;
        System.out.println("test\n");
//        for (Favorite favorite : list) {
//            System.out.println(favorite.getNewsLink()+"\n");
//        }
//        System.out.println("test end \n");
        return list.stream()
                .map(Favorite::getNewsLink)
                .collect(Collectors.toList());
    }



    public List<String> getUserFavoriteNewsLinks(Long userId) {
        List<String> response = favoriteFeignClient.getFavoriteNewsLinks(userId);
        return response;
    }

    @Transactional
    public List<String> getUserFavoriteLink(Long userId) {
        List<Favorite> list = favoriteRepository.findByUserId(userId);
        // 전체를 받아온다.
        if (list.isEmpty())
            return null;
        List<String> responses = new ArrayList<>();
        for (Favorite favorite : list) {
//            System.out.println(favorite.getNewsLink()+"\n");
            responses.add(favorite.getNewsLink());

        }


        return responses;
    }

    public String dateParser(String input) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 EEEE HH:mm", Locale.KOREAN);

        OffsetDateTime dateTime = OffsetDateTime.parse(input, inputFormatter);
        return dateTime.format(outputFormatter);
    }

}
