package com.community.community.repository;

import com.community.community.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Integer>, PostRepositoryCustom {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.postId = :postId")
    int increaseLikeCount(@Param("postId") Integer postId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.postId = :postId AND p.likeCount > 0")
    int decreaseLikeCount(@Param("postId") Integer postId);

    @Query("SELECT p.likeCount FROM Post p WHERE p.postId = :postId")
    int findLikeCountByPostId(@Param("postId") Integer postId);
}
