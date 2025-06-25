package com.mini2.SearchService.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class FavoriteNewsLinkResponse {
    private List<String> newsLinks;
}
