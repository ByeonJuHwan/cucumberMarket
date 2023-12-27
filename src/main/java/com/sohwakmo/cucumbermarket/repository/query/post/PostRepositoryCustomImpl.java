package com.sohwakmo.cucumbermarket.repository.query.post;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sohwakmo.cucumbermarket.domain.Post;
import com.sohwakmo.cucumbermarket.domain.QMember;
import com.sohwakmo.cucumbermarket.dto.SearchPostDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.stream.Collectors;

import static com.sohwakmo.cucumbermarket.domain.QMember.*;
import static com.sohwakmo.cucumbermarket.domain.QPost.*;
import static org.springframework.util.StringUtils.*;

public class PostRepositoryCustomImpl implements PostRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public PostRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Post selectPostByImageSrc(String fileName) {
        return queryFactory.selectFrom(post)
                .where(post.imageName01.eq(fileName).or(post.imageName02.eq(fileName)))
                .fetchOne();
    }

    @Override
    public Page<SearchPostDto> selectPostBySearch(String searchString, String address,Pageable pageable) {
        List<Post> result = queryFactory
                .selectFrom(post)
                .join(post.member,member).fetchJoin()
                .where(
                        searchAllCondition(searchString),
                        addressEq(address)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<SearchPostDto> posts = result.stream()
                .map(SearchPostDto::new).toList();

        JPAQuery<Long> count = queryFactory
                .select(post.count())
                .from(post)
                .where(
                        searchAllCondition(searchString),
                        addressEq(address)
                );
        return PageableExecutionUtils.getPage(posts, pageable, count::fetchOne);
    }

    private BooleanExpression searchAllCondition(String searchString) {
        return hasText((searchString)) ? post.title.like(searchString)
                .or(post.content.like(searchString))
                .or(post.member.nickname.like(searchString)) : null;
    }
    private BooleanExpression addressEq(String address) {
        return address.equals("전국") ? null : post.member.address.eq(address);
    }
}
