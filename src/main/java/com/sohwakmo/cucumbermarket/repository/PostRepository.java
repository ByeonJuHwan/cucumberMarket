package com.sohwakmo.cucumbermarket.repository;


import com.sohwakmo.cucumbermarket.domain.Post;
import com.sohwakmo.cucumbermarket.repository.query.post.PostRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
public interface PostRepository extends JpaRepository<Post,Integer>, PostRepositoryCustom {
    //TODO QueryDsl 로 처리
    List<Post> findByTitleIgnoreCaseContainingOrContentIgnoreCaseContainingOrMemberNicknameIgnoreCaseContainingOrderByPostNoDesc(String title, String content,String nickname);
    List<Post> findByTitleIgnoreCaseContainingOrContentIgnoreCaseContainingOrMemberNicknameIgnoreCaseContainingAndMemberAddressIgnoreCaseContainingOrderByPostNoDesc(String title, String content,String nickname,String address);

    Post findByImageName01(String imageSrc);
    Post findByImageName02(String imageSrc);
    List<Post> findByOrderByClickCountDescPostNoDesc();
}
