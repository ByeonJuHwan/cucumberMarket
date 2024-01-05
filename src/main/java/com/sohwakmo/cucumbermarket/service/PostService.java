package com.sohwakmo.cucumbermarket.service;

import com.sohwakmo.cucumbermarket.domain.Member;
import com.sohwakmo.cucumbermarket.domain.Post;
import com.sohwakmo.cucumbermarket.dto.*;
import com.sohwakmo.cucumbermarket.repository.MemberRepository;
import com.sohwakmo.cucumbermarket.repository.PostRepository;

import com.sohwakmo.cucumbermarket.repository.ReplyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    /**
     * 내용, 제목으로 검색해서 결과를 페이지로 가져오기
     * @param searchText 검색 내용
     * @return 결과 페이지들을 리턴
     */
    public Page<SearchPostDto> searchPost(String searchText,String address, Pageable pageable){
        String[] memberAddressArr = address.split(" ");
        address = memberAddressArr[0];
        return postRepository.selectPostBySearch(searchText,address,pageable);
    }

    public List<Post> readByClickCountDesc(){
        return postRepository.findByOrderByClickCountDescPostNoDesc();
    }

    /**
     * modify 에서 detail 페이지로 다시 넘어오는경우 조회수가 2번 늘어나는 것을 방지하고자 -1 을해준다.
     * @param postNo 게시글 id
     * @param clickCount 조회수
     */
    public int setClickCount(Integer postNo, Integer clickCount) {
        return findPostByPostNo(postNo).getClickCount() - 1;
    }

    @Transactional
    public PostDetailDto findPostByIdAndUpdateClickCount(Integer postNo, Integer clickCount) {
        postRepository.findById(postNo)
                .orElseThrow(() -> new EntityNotFoundException("게시물이 없습니다 : " + postNo)).plusClickCount(clickCount);
        return postRepository.selectDetailPostMember(postNo);
    }

    /**
     * 글 작성시 post 객체를 생성해서 DB에 저장
     */
    public void insertPost(PostCreateDto dto, Member member, List<MultipartFile> files) throws Exception {
        Post post = PostCreateDto.builder()
                .title(dto.getTitle()).content(dto.getContent()).clickCount(dto.getClickCount()).member(member).build().toEntity();
        createPost(post,files);
    }

    public void createPost(Post post, List<MultipartFile> files)throws Exception{
        if (!files.stream().findFirst().get().isEmpty()) {
            for (MultipartFile multipartFile : files) {
                String fileName = saveImage(multipartFile);
                post.saveImage(fileName);
            }
        }
        postRepository.save(post);
    }
    public void deletePost(Integer id) {
        postRepository.deleteById(id);
    }

    public Post findPostByPostNo(Integer postNo) {
        return postRepository.findById(postNo)
                .orElseThrow(() -> new EntityNotFoundException("게시물이 없습니다 : " + postNo));
    }

    @Transactional
    public Integer modifyPost(PostUpdateDto dto) {
        Post post = postRepository.findById(dto.getPostNo()).get();
        Post newPost = post.update(dto.getTitle(), dto.getContent());
        log.info(newPost.toString());
        return post.getPostNo();
    }

// ---------------------RestController 에서 온 api

    @Transactional
    public String insertImage(Integer postNo, MultipartFile data)throws Exception {
        String fileName = saveImage(data);
        Post post = postRepository.findById(postNo)
                .orElseThrow(() -> new EntityNotFoundException("게시물이 없습니다 : " + postNo));
        return post.saveImage(fileName);
    }

    @Transactional
    public String modifyImage01(Integer postNo, MultipartFile data)throws Exception {
        String fileName = saveImage(data);
        Post post = postRepository.findById(postNo)
                .orElseThrow(() -> new EntityNotFoundException("게시물이 없습니다 : " + postNo));
        extractImage(post.getImageName01());
        post.saveImage01NameAndUrl(fileName);
        return "files/"+fileName;
    }

    @Transactional
    public String modifyImage02(Integer postNo, MultipartFile data)throws Exception {
        String fileName = saveImage(data);
        Post post = postRepository.findById(postNo)
                .orElseThrow(() -> new EntityNotFoundException("게시물이 없습니다 : " + postNo));
        extractImage(post.getImageName02());
        post.saveImage02NameAndUrl(fileName);
        return "files/"+fileName;
    }

    /**
     * 사진 삭제를 누를경우 사진 삭제를 바로바로 해준다.
     * @param imageSrc
     * @return 몇번 사진을 삭제했는지 알려준다.
     * @throws Exception
     */
    @Transactional
    public String checkImageNumAndDeleteImage(Integer postNo,String imageSrc) throws Exception{
        Post post = postRepository.findById(postNo)
                .orElseThrow(() -> new EntityNotFoundException("게시물이 없습니다 : " + postNo));
        if (post.getImageName01().equals(imageSrc)) {
            extractImage(imageSrc);
            post.saveImage01NameAndUrl("");
        }else{
            extractImage(imageSrc);
            post.saveImage02NameAndUrl("");
        }
        return null;
    }
    private  String saveImage(MultipartFile files) throws IOException {
        String projectPath = System.getProperty("user.dir") + "/src/main/resources/static/files";

        String fileName = files.getOriginalFilename();

        File saveFile = new File(projectPath, fileName);

        files.transferTo(saveFile);
        return fileName;
    }

    /**
     * static  폴더 안에 있는 사진 경로를 찾아내서 삭제
     * @param imageSrc 전달받은 이미지 경로
     * @throws IOException
     */
    private void extractImage(String imageSrc) throws IOException {
        // 경로는 능동적으로 변경
        Path filePath = Paths.get(System.getProperty("user.dir")+"/src/main/resources/static/files/" + imageSrc);
        Files.delete(filePath);
    }
}


