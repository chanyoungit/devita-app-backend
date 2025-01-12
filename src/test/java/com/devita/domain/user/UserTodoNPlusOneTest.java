package com.devita.domain.user;

import com.devita.domain.todo.domain.Todo;
import com.devita.domain.todo.repository.TodoRepository;
import com.devita.domain.user.domain.AuthProvider;
import com.devita.domain.user.domain.User;
import com.devita.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@Transactional
class UserTodoNPlusOneTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TodoRepository todoRepository;

    @PersistenceContext
    private EntityManager em;

    @BeforeEach
    void setUp() {
        saveSampleData(); // 유저 3명, 각 유저당 Todo 3개씩 저장
        em.flush();
        em.clear();
        System.out.println("------------ 영속성 컨텍스트 비우기 -----------\n\n");
    }

    @Test
    @DisplayName("N+1 문제 발생 테스트")
    void testNPlusOne() {
        System.out.println("------------ USER 전체 조회 요청 ------------");
        List<User> users = userRepository.findAll();
        System.out.println("------------ USER 전체 조회 완료.------------\n\n");

        System.out.println("------------ USER의 기본 정보 조회 요청 ------------");
        users.forEach(user -> {
            System.out.println("USER 닉네임: [%s], EMAIL: [%s]".formatted(user.getNickname(), user.getEmail()));
        });
        System.out.println("------------ USER 기본 정보 조회 완료. [추가적인 쿼리 발생하지 않음]------------\n\n");

        System.out.println("------------ USER에 달린 Todo 조회 요청 ------------");
        System.out.println("아래에서 todo 조회 시 추가 쿼리가 발생하는지 확인");
        users.forEach(user -> {
            // Todo 목록 접근 시 User 수(N=3) 만큼 추가적인 쿼리 발생 가능
            user.getTodoEntities().forEach(todo -> {
                System.out.println("USER [%s]의 TODO 제목: [%s], 날짜: [%s]"
                        .formatted(user.getNickname(), todo.getTitle(), todo.getDate()));
            });
        });
        System.out.println("------------ USER의 Todo 조회 완료 ------------\n\n");

        // 간단한 검증
        assertFalse(users.isEmpty());
    }

    private void saveSampleData() {
        // User 3명 생성
        IntStream.rangeClosed(1, 3).forEach(i -> {
            User user = User.builder()
                    .email("user%d@example.com".formatted(i))
                    .nickname("User%d".formatted(i))
                    .provider(AuthProvider.KAKAO)
                    .profileImage(null)
                    .build();
            userRepository.save(user);
            em.flush();
            em.clear();
        });

        List<User> users = userRepository.findAll();
        // 각 User 마다 Todo 3개씩 생성
        users.forEach(u -> IntStream.rangeClosed(1, 3).forEach(j -> {
            Todo todo = Todo.builder()
                    .user(u)
                    .category(null) // Category는 필요 시 추가
                    .missionCategory("Daily Task")
                    .title("Todo %d for %s".formatted(j, u.getNickname()))
                    .status(false)
                    .date(LocalDate.now())
                    .build();
            todoRepository.save(todo);
        }));
    }
}