package com.example.backend;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity                 // 이 클래스는 DB 테이블과 연결된다
@Table(name = "posts")  // 테이블 이름은 posts
@Getter @Setter         // (Lombok) getXxx(), setXxx() 함수를 자동으로 만들어 줌
@NoArgsConstructor      // (Lombok) 빈 생성자를 자동으로 만들어 줌 (JPA에 필요)
public class Post {

    @Id                                                  // 기본키 (글마다 다른 고유 번호)
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 1, 2, 3... 자동 증가
    private Long id;

    @Column(nullable = false, length = 50)               // 비워 둘 수 없음, 최대 50자
    private String author;

    @Column(nullable = false, length = 200)
    private String title;

    @CreationTimestamp                                   // 글을 저장할 때 현재 시간이 자동으로 들어감
    @Column(updatable = false)                           // 작성 시간은 수정할 수 없음
    private LocalDateTime createdAt;
}
