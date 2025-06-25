package com.mini2.SearchService.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mini2.SearchService.domain.Favorite;
import com.mini2.SearchService.domain.User;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long>{
    List<Favorite> findByUserId(Long userId);

    Optional<Favorite> findByUserAndNewsLink(User user, String newsLink);

    void deleteByUser(User user);
}
