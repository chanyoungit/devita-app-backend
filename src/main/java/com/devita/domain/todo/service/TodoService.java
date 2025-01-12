package com.devita.domain.todo.service;

import com.devita.common.exception.AccessDeniedException;
import com.devita.common.exception.ErrorCode;
import com.devita.common.exception.ResourceNotFoundException;
import com.devita.domain.category.domain.Category;
import com.devita.domain.character.service.RewardService;
import com.devita.domain.mission.dto.ai.FreeMissionAiResDTO;
import com.devita.domain.todo.domain.Todo;
import com.devita.domain.todo.dto.CalenderDTO;
import com.devita.domain.todo.dto.TodoAIReqDTO;
import com.devita.domain.todo.dto.TodoReqDTO;
import com.devita.domain.category.repository.CategoryRepository;
import com.devita.domain.todo.repository.TodoRepository;
import com.devita.domain.user.domain.User;
import com.devita.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TodoService {

    private final RestTemplate restTemplate;
    private final TodoRepository todoRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final RewardService rewardService;

    @Value("${ai.address}")
    private String aiAddress;

    private static final String MISSION_SAVE_API = "/ai/v1/mission/save";

    // 할 일 추가
    public Todo addTodo(Long userId, TodoReqDTO todoReqDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        Category category = categoryRepository.findById(todoReqDTO.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));

        Todo todo = Todo.builder()
                .user(user)
                .category(category)
                .title(todoReqDTO.title())
                .status(false)
                .date(todoReqDTO.date())
                .build();

        return todoRepository.save(todo);
    }

    public void deleteTodo(Long userId, Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .filter(t -> t.getUser().getId().equals(userId))  // userId가 일치하는지 확인
                .orElseThrow(() -> new AccessDeniedException(ErrorCode.TODO_ACCESS_DENIED));

        todoRepository.delete(todo);
    }

    public Todo updateTodo(Long userId, Long todoId, TodoReqDTO todoReqDTO) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.TODO_NOT_FOUND));  // 투두가 없으면 ResourceNotFoundException 발생

        if (!todo.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(ErrorCode.TODO_ACCESS_DENIED);  // userId가 일치하지 않으면 AccessDeniedException 발생
        }

        Category category = categoryRepository.findById(todoReqDTO.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));

        todo.updateDetails(category, todoReqDTO.title(), todoReqDTO.date());

        return todoRepository.save(todo);
    }

    @Transactional
    public boolean toggleTodo(Long userId, Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .filter(t -> t.getUser().getId().equals(userId))
                .orElseThrow(() -> new AccessDeniedException(ErrorCode.TODO_ACCESS_DENIED));

        log.debug("Before toggle - status: {}, isDone: {}", todo.getStatus(), todo.getIsDone());
        todo.toggleStatus();  // 메소드 이름 수정
        log.debug("After toggle - status: {}, isDone: {}", todo.getStatus(), todo.getIsDone());

        if (!todo.getIsDone()) {
//            if (todo.getCategory().getName().equals("일일 미션") || todo.getCategory().getName().equals("자율 미션")){
//                sendFinishedMission(todo, userId);
//            }
            try {
                rewardService.processReward(todo.getUser(), todo);
            } catch (IllegalStateException e) {
                // 보상 지급 실패 시 로깅하되, 할 일 완료 상태는 유지
                log.warn("보상 지급 실패 {}: {}", todoId, e.getMessage());
            }
        }

        todo.isDone();

        todoRepository.save(todo);

        return todo.getStatus();
    }

    private void sendFinishedMission(Todo todo, Long userId){
        TodoAIReqDTO request = new TodoAIReqDTO(todo.getTitle(), todo.getDate(), todo.getCategory().getName(), todo.getMissionCategory(), userId);

        restTemplate.postForObject(
                aiAddress + MISSION_SAVE_API,
                request,
                FreeMissionAiResDTO.class
        );
    }

    public List<CalenderDTO> getCalendar(Long userId, String viewType) {
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate;

        // viewType에 따라 조회 기간 설정
        switch (viewType.toLowerCase()) {
            case "weekly":
                startDate = today.with(java.time.DayOfWeek.MONDAY);
                endDate = today.with(java.time.DayOfWeek.SUNDAY);
                break;

            case "monthly":
                startDate = today.with(TemporalAdjusters.firstDayOfMonth()); // 월의 시작일
                endDate = today.with(TemporalAdjusters.lastDayOfMonth());    // 월의 끝일
                break;

            default:
                throw new IllegalArgumentException("Invalid viewType. Use 'daily', 'weekly', or 'monthly'.");
        }

        List<Todo> todoEntities = todoRepository.findByUserIdAndDateBetween(userId, startDate, endDate);

        return todoEntities.stream().map(CalenderDTO::fromEntity).toList();
    }
}