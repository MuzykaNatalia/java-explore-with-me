package ru.practicum.participate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.service.CategoryService;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.service.EventService;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.location.model.Location;
import ru.practicum.participate.dto.EventRequestStatusUpdateRequest;
import ru.practicum.participate.dto.EventRequestStatusUpdateResult;
import ru.practicum.participate.dto.ParticipationRequestDto;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static ru.practicum.Constant.FORMATTER;
import static ru.practicum.event.state.AdminStateAction.PUBLISH_EVENT;
import static ru.practicum.status.Status.*;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ParticipateRequestServiceImplTest {
    private final ParticipateRequestService participateRequestService;
    private final CategoryService categoryService;
    private final EventService eventService;
    private final UserService userService;
    private EventFullDto event;
    private EventFullDto eventTwo;
    private EventFullDto eventThree;
    private UserDto userDtoOne;
    private UserDto userDtoTwo;
    private UserDto userDtoThree;
    private UpdateEventAdminRequest request;
    private UpdateEventAdminRequest requestTwo;
    private UpdateEventAdminRequest requestThree;


    @BeforeEach
    public void setUp() {
        userDtoOne = userService.createUser(new NewUserRequest("Ivan", "ivan@mail.ru"));
        userDtoTwo = userService.createUser(new NewUserRequest("Lisa", "lisa@mail.ru"));
        userDtoThree = userService.createUser(new NewUserRequest("Maria", "maria@mail.ru"));

        CategoryDto categoryDto = categoryService.createCategory(new NewCategoryDto("Concerts"));
        Location location = new Location(null, 44.895750f, 37.314678f);
        NewEventDto newEvent = new NewEventDto("RADIO TAPOK is coming to your city!", categoryDto.getId(),
                "RADIO TAPOK is coming to your city! At the concert you will be able to hear all your " +
                "favorite hits that have accumulated on the channel over the years, and the artist’s original " +
                "material!\nEven more drive awaits you, indescribable concert energy, a sea of sound, light and " +
                "selected rock music. You can't miss this!", LocalDateTime.now().plusMonths(1), location,
                true, 1, false, "RADIO TAPOK");
        NewEventDto newEventTwo = new NewEventDto("LINKIN PARK is coming to your city!", categoryDto.getId(),
                "LINKIN PARK is coming to your city! At the concert you will be able to hear all your " +
                "favorite hits that have accumulated on the channel over the years, and the artist’s original " +
                "material!\nEven more drive awaits you, indescribable concert energy, a sea of sound, light and " +
                "selected rock music. You can't miss this!", LocalDateTime.now().plusMonths(1), location,
                true, 1, true, "LINKIN PARK");
        NewEventDto newEventThree = new NewEventDto("TRAVELING MUSICIANS is coming to your city!",
                categoryDto.getId(), "Even more drive awaits you, indescribable concert energy, " +
                "a sea of sound, light and selected rock music. You can't miss this!",
                LocalDateTime.now().plusMonths(1), location, false, 0,
                false, "TRAVELING MUSICIANS");

        event = eventService.createOwnerEvent(userDtoOne.getId(), newEvent);
        eventTwo = eventService.createOwnerEvent(userDtoThree.getId(), newEventTwo);
        eventThree = eventService.createOwnerEvent(userDtoTwo.getId(), newEventThree);

        request = new UpdateEventAdminRequest(event.getAnnotation(), categoryDto.getId(), event.getDescription(),
                LocalDateTime.parse(event.getEventDate(), FORMATTER),
                new Location(null, 44.50f, 37.38f), event.getPaid(),
                event.getParticipantLimit(), event.getRequestModeration(), PUBLISH_EVENT, event.getTitle());
        requestTwo = new UpdateEventAdminRequest(eventTwo.getAnnotation(), categoryDto.getId(),
                eventTwo.getDescription(), LocalDateTime.parse(eventTwo.getEventDate(), FORMATTER),
                new Location(null, 44.515f, 37.768f), eventTwo.getPaid(), eventTwo.getParticipantLimit(),
                eventTwo.getRequestModeration(), PUBLISH_EVENT, eventTwo.getTitle());
        requestThree = new UpdateEventAdminRequest(eventThree.getAnnotation(), categoryDto.getId(),
                eventThree.getDescription(), LocalDateTime.parse(eventThree.getEventDate(), FORMATTER),
                new Location(null, 44.5770f, 37.3877f), eventThree.getPaid(),
                eventThree.getParticipantLimit(), eventThree.getRequestModeration(),
                PUBLISH_EVENT, eventThree.getTitle());
    }

    @DisplayName("Должен создать запрос на участие в событии")
    @Test
    public void shouldCreateRequestToParticipateInEvent() {
        eventService.updateEventByAdmin(request, event.getId());
        ParticipationRequestDto participation = participateRequestService
                .createRequestToParticipateInEvent(userDtoTwo.getId(), event.getId());

        ParticipationRequestDto test = new ParticipationRequestDto(participation.getId(), participation.getCreated(),
                event.getId(), userDtoTwo.getId(), CONFIRMED);

        assertThat(participation, is(equalTo(test)));
    }

    @DisplayName("Должен выдать исключение если событие еще не опубликовано")
    @Test
    public void shouldNotCreateRequestToParticipateInEventIfEventNotPublished() {
        ConflictException exception = assertThrows(ConflictException.class,
                () -> participateRequestService.createRequestToParticipateInEvent(userDtoTwo.getId(), event.getId())
        );

        assertEquals("You cannot participate in an unpublished event", exception.getMessage());
    }

    @DisplayName("Должен выдать исключение если превышен лимит запросов")
    @Test
    public void shouldNotCreateRequestToParticipateInEventIfExceededRequestLimit() {
        eventService.updateEventByAdmin(request, event.getId());
        participateRequestService.createRequestToParticipateInEvent(userDtoTwo.getId(), event.getId());

        ConflictException exception = assertThrows(ConflictException.class,
                () -> participateRequestService.createRequestToParticipateInEvent(userDtoThree.getId(), event.getId())
        );

        assertEquals("The event has reached the limit of requests for participation", exception.getMessage());
    }

    @DisplayName("Должен выдать исключение если инициатор равен запрашивающему")
    @Test
    public void shouldNotCreateRequestToParticipateInEventIfInitiatorEqualsRequester() {
        eventService.updateEventByAdmin(request, event.getId());

        ConflictException exception = assertThrows(ConflictException.class,
                () -> participateRequestService.createRequestToParticipateInEvent(userDtoOne.getId(), event.getId())
        );

        assertEquals("The event initiator cannot add a request to participate in his event",
                exception.getMessage());
    }

    @DisplayName("Должен выдать исключение если запрос повторный")
    @Test
    public void shouldNotCreateRequestToParticipateInEventIfRepeatedRequest() {
        eventService.updateEventByAdmin(request, event.getId());
        participateRequestService.createRequestToParticipateInEvent(userDtoTwo.getId(), event.getId());

        ConflictException exception = assertThrows(ConflictException.class,
                () -> participateRequestService.createRequestToParticipateInEvent(userDtoTwo.getId(), event.getId())
        );

        assertEquals("You can't add a repeat request", exception.getMessage());
    }

    @DisplayName("Должен обновить статусы запросов на участие в событии владельца")
    @Test
    public void shouldUpdateRequestStatusParticipateOwnerEvent() {
        eventService.updateEventByAdmin(requestTwo, eventTwo.getId());

        ParticipationRequestDto participation = participateRequestService
                .createRequestToParticipateInEvent(userDtoOne.getId(), eventTwo.getId());
        ParticipationRequestDto participationTwo = participateRequestService
                .createRequestToParticipateInEvent(userDtoTwo.getId(), eventTwo.getId());

        List<Long> participationIds = List.of(participation.getId(), participationTwo.getId());
        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest(participationIds, CONFIRMED);

        EventRequestStatusUpdateResult result = participateRequestService
                .updateRequestStatusParticipateOwnerEvent(userDtoThree.getId(), eventTwo.getId(), updateRequest);

        participation.setStatus(CONFIRMED);
        List<ParticipationRequestDto> confirmedRequests = List.of(participation);
        participationTwo.setStatus(REJECTED);
        List<ParticipationRequestDto> rejectedRequests = List.of(participationTwo);

        assertThat(result.getConfirmedRequests(), is(equalTo(confirmedRequests)));
        assertThat(result.getRejectedRequests(), is(equalTo(rejectedRequests)));
    }

    @DisplayName("Не должен обновлять статус запросов на участие в событии владельца, " +
            "если событие не принадлежит этому пользователю")
    @Test
    public void shouldNotUpdateRequestStatusParticipateOwnerEventIfEventIsNotThisUser() {
        eventService.updateEventByAdmin(requestTwo, eventTwo.getId());

        ParticipationRequestDto participation = participateRequestService
                .createRequestToParticipateInEvent(userDtoOne.getId(), eventTwo.getId());

        List<Long> participationId = List.of(participation.getId());
        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest(participationId, CONFIRMED);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> participateRequestService.updateRequestStatusParticipateOwnerEvent(userDtoTwo.getId(),
                        eventTwo.getId(), updateRequest)
        );

        assertEquals("The user is not the initiator of the event", exception.getMessage());
    }

    @DisplayName("Должен вернуть пустой список, если не нужна пре-модерация заявок " +
            "или если для события лимит заявок равен 0")
    @Test
    public void shouldReturnEmptyListIfNotApplicationConfirmationRequired() {
        eventService.updateEventByAdmin(requestThree, eventThree.getId());

        ParticipationRequestDto participation = participateRequestService
                .createRequestToParticipateInEvent(userDtoOne.getId(), eventThree.getId());

        List<Long> participationIds = List.of(participation.getId());
        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest(participationIds, CONFIRMED);

        EventRequestStatusUpdateResult result = participateRequestService
                .updateRequestStatusParticipateOwnerEvent(userDtoTwo.getId(), eventThree.getId(), updateRequest);

        assertThat(result.getConfirmedRequests(), hasSize(0));
        assertThat(result.getRejectedRequests(), hasSize(0));
    }

    @DisplayName("Должен изменить статус запроса на отмененный")
    @Test
    public void shouldCanceledRequestToParticipateInEvent() {
        eventService.updateEventByAdmin(request, event.getId());
        ParticipationRequestDto request = participateRequestService
                .createRequestToParticipateInEvent(userDtoTwo.getId(), event.getId());
        List<ParticipationRequestDto> test = participateRequestService
                .getRequestsForOwnerEvent(userDtoOne.getId(), request.getEvent());
        EventFullDto eventOne = eventService.getOwnerOneEvent(userDtoOne.getId(), event.getId());

        assertThat(test.get(0), is(equalTo(request)));
        assertThat(eventOne.getConfirmedRequests(), is(equalTo(1)));

        participateRequestService.cancelRequestToParticipateInEvent(userDtoTwo.getId(), request.getId());

        List<ParticipationRequestDto> testTwo = participateRequestService
                .getRequestsForOwnerEvent(userDtoOne.getId(), request.getEvent());
        EventFullDto eventTwo = eventService.getOwnerOneEvent(userDtoOne.getId(), event.getId());

        assertThat(testTwo.get(0).getStatus(),  is(equalTo(CANCELED)));
        assertThat(eventTwo.getConfirmedRequests(), is(equalTo(0)));
    }

    @DisplayName("Должен выдать исключение, если запрос не существует")
    @Test
    public void shouldThrowExceptionIfRequestToParticipateInEventNotExist() {
        Long requestId = -15000L;

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> participateRequestService.cancelRequestToParticipateInEvent(userDtoTwo.getId(), requestId)
        );

        assertEquals("Participate request with id=" + requestId + " was not found", exception.getMessage());
    }

    @DisplayName("Должен выдать исключение, если запрос не принадлежит этому пользователю")
    @Test
    public void shouldThrowExceptionIfRequestIsNotThisUser() {
        eventService.updateEventByAdmin(request, event.getId());
        ParticipationRequestDto request = participateRequestService
                .createRequestToParticipateInEvent(userDtoTwo.getId(), event.getId());

        ConflictException exception = assertThrows(ConflictException.class,
                () -> participateRequestService.cancelRequestToParticipateInEvent(userDtoThree.getId(), request.getId())
        );

        assertEquals("Request is not this user", exception.getMessage());
    }

    @DisplayName("Должен выдать организатору запросы пользователей на участие в его событии")
    @Test
    public void shouldGetRequestForOwnerEvent() {
        eventService.updateEventByAdmin(request, event.getId());
        ParticipationRequestDto request = participateRequestService
                .createRequestToParticipateInEvent(userDtoTwo.getId(), event.getId());

        List<ParticipationRequestDto> test = participateRequestService
                .getRequestsForOwnerEvent(userDtoOne.getId(), event.getId());

        assertThat(test, is(equalTo(List.of(request))));
    }

    @DisplayName("Должен выдать исключение, если событие не принадлежит этому пользователю")
    @Test
    public void shouldThrowExceptionIfEventIsNotThisUser() {
        eventService.updateEventByAdmin(request, event.getId());

        ConflictException exception = assertThrows(ConflictException.class,
                () -> participateRequestService.getRequestsForOwnerEvent(userDtoThree.getId(), event.getId())
        );

        assertEquals("The user is not the initiator of the event", exception.getMessage());
    }

    @DisplayName("Должен выдать исключение, если события не существует")
    @Test
    public void shouldThrowExceptionIfEventNotExist() {
        Long eventId = -15000L;

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> participateRequestService.getRequestsForOwnerEvent(userDtoOne.getId(), eventId)
        );

        assertEquals("Event with id=" + eventId + " was not found", exception.getMessage());
    }

    @DisplayName("Должен получить информацию о запросах пользователя в других событиях")
    @Test
    public void getInfoOnRequestsForUserInOtherEvents() {
        eventService.updateEventByAdmin(request, event.getId());
        eventService.updateEventByAdmin(requestTwo, eventTwo.getId());

        ParticipationRequestDto participation = participateRequestService
                .createRequestToParticipateInEvent(userDtoOne.getId(), eventTwo.getId());

        List<ParticipationRequestDto> test = participateRequestService
                .getInfoOnRequestsForUserInOtherEvents(userDtoOne.getId());

        assertThat(test, is(equalTo(List.of(participation))));
    }
}