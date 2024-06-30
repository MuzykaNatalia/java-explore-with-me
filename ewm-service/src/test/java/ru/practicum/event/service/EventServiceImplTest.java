package ru.practicum.event.service;

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
import ru.practicum.event.dto.*;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.location.dto.LocationDto;
import ru.practicum.location.model.Location;
import ru.practicum.location.repository.LocationRepository;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.Constant.FORMATTER;
import static ru.practicum.event.state.AdminStateAction.PUBLISH_EVENT;
import static ru.practicum.event.state.AdminStateAction.REJECT_EVENT;
import static ru.practicum.event.state.EventState.CANCELED;
import static ru.practicum.event.state.EventState.PENDING;
import static ru.practicum.event.state.UserStateAction.CANCEL_REVIEW;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class EventServiceImplTest {
    private final EventService eventService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final LocationRepository locationRepository;
    private EventFullDto event;
    private EventFullDto eventTwo;
    private EventFullDto eventThree;
    private UserDto userDtoOne;
    private UserDto userDtoTwo;
    private UserDto userDtoThree;
    private NewEventDto newEvent;
    private NewEventDto newEventTwo;
    private NewEventDto newEventThree;
    private CategoryDto categoryDto;
    private CategoryDto categoryDtoTwo;
    private CategoryDto categoryDtoThree;

    @BeforeEach
    public void setUp() {
        userDtoOne = userService.createUser(new NewUserRequest("Ivan", "ivan@mail.ru"));
        userDtoTwo = userService.createUser(new NewUserRequest("Lisa", "lisa@mail.ru"));
        userDtoThree = userService.createUser(new NewUserRequest("Maria", "maria@mail.ru"));

        categoryDto = categoryService.createCategory(new NewCategoryDto("Concerts"));
        categoryDtoTwo = categoryService.createCategory(new NewCategoryDto("Exhibition"));
        categoryDtoThree = categoryService.createCategory(new NewCategoryDto("Theater"));

        Location location = new Location(null, 44.895750f, 37.314678f);

        newEvent = new NewEventDto("RADIO TAPOK is coming to your city!", categoryDto.getId(),
                "RADIO TAPOK is coming to your city! At the concert you will be able to hear all your " +
                        "favorite hits that have accumulated on the channel over the years, and the artist’s original " +
                        "material!\nEven more drive awaits you, indescribable concert energy, a sea of sound, light and " +
                        "selected rock music. You can't miss this!", LocalDateTime.now().plusMonths(1), location,
                true, 1, true, "RADIO TAPOK");
        newEventTwo = new NewEventDto("Exhibition of antique dolls of the world!",
                categoryDtoTwo.getId(), "More than 1000 masters will take part in the unique exhibition, " +
                "presenting tens of thousands of exhibits to visitors: dolls of the shadow theater of the East " +
                "and “glamor puppets”, industrial dolls of the Soviet period and traditional dolls of the peoples " +
                "of the world, mechanical and interior dolls, theatrical and cartoon dolls, as well as antique " +
                "dolls from private collections.", LocalDateTime.now().plusMonths(2), location,
                false, 0, false, "Exhibition of antique dolls");
        newEventThree = new NewEventDto("SHADOW THEATER is coming to your city!",
                categoryDtoThree.getId(), "Shadow theater is an ancient art that originated in Asia more " +
                "than two thousand years ago, but it is impossible to say for sure whether it was in India or China. " +
                "According to some theories, even Egypt is considered the birthplace of theater.",
                LocalDateTime.now().plusMonths(3), location, false, 0,
                true, "SHADOW THEATER");
    }

    @DisplayName("Должен создать событие")
    @Test
    public void shouldCreateOwnerEvent() {
        eventThree = eventService.createOwnerEvent(userDtoThree.getId(), newEventThree);

        EventFullDto test = new EventFullDto(eventThree.getId(), "SHADOW THEATER is coming to your city!",
                categoryDtoThree, 0, eventThree.getCreatedOn(), "Shadow theater is an ancient " +
                "art that originated in Asia more than two thousand years ago, but it is impossible to say for sure " +
                "whether it was in India or China. According to some theories, even Egypt is considered the " +
                "birthplace of theater.", eventThree.getEventDate(), new UserShortDto(userDtoThree.getId(),
                userDtoThree.getName()), new LocationDto(44.895750f, 37.314678f), false,
                0, null, true, PENDING,"SHADOW THEATER", 0L);

        assertThat(eventThree, is(equalTo(test)));
    }

    @DisplayName("Не должен создать событие, если время до его начала меньше, чем 2 часа")
    @Test
    public void shouldNotCreateOwnerEventIfTimeBeforeStartsIsLessThenTwoHours() {
        LocalDateTime eventDate = LocalDateTime.now().minusMinutes(10);
        newEventThree.setEventDate(eventDate);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> eventService.createOwnerEvent(userDtoThree.getId(), newEventThree)
        );

        assertEquals("Field: eventDate. Error: must contain a date that has not yet occurred. Value: "
                 + eventDate, exception.getMessage());
    }

    @DisplayName("При попытке сохранения одинаковых локаций должен сохранить только одну из них, " +
            "либо вернуть уже существующую")
    @Test
    public void shouldSaveOneLocationInsteadOfTwoIdenticalOnes() {
        event = eventService.createOwnerEvent(userDtoOne.getId(), newEvent);
        eventTwo = eventService.createOwnerEvent(userDtoTwo.getId(), newEventTwo);

        List<Location> locations = locationRepository.findAll();

        assertThat(locations, hasSize(1));
    }

    @DisplayName("Должен обновить событие организатора")
    @Test
    public void shouldUpdateOwnerEvent() {
        eventThree = eventService.createOwnerEvent(userDtoThree.getId(), newEventThree);

        UpdateEventUserRequest test = new UpdateEventUserRequest("SHADOW THEATER is coming to your city!",
                categoryDtoThree.getId(), "SHADOW-THEATER is an ancient " +
                "art that originated in Asia more than two thousand years ago, but it is impossible to say for sure " +
                "whether it was in India or China.", LocalDateTime.parse(eventThree.getEventDate(), FORMATTER),
                new Location(null, 44.8950f, 37.378f), false,
                1, false, CANCEL_REVIEW,"SHADOW-THEATER");

        EventFullDto testOne = new EventFullDto(eventThree.getId(), "SHADOW THEATER is coming to your city!",
                categoryDtoThree, 0, eventThree.getCreatedOn(), "SHADOW-THEATER is an ancient " +
                "art that originated in Asia more than two thousand years ago, but it is impossible to say for sure " +
                "whether it was in India or China.", eventThree.getEventDate(), new UserShortDto(userDtoThree.getId(),
                userDtoThree.getName()), new LocationDto(44.8950f, 37.378f), false,
                1, null, false, CANCELED,"SHADOW-THEATER", 0L);

        EventFullDto result = eventService.updateOwnerEvent(userDtoThree.getId(), eventThree.getId(), test);

        assertThat(result, is(equalTo(testOne)));
    }

    @DisplayName("Не должен обновить событие, если это не его владелец")
    @Test
    public void shouldNotUpdateOwnerEventIfThisNotOwnerOfEvent() {
        eventThree = eventService.createOwnerEvent(userDtoThree.getId(), newEventThree);

        UpdateEventUserRequest test = new UpdateEventUserRequest("SHADOW THEATER is coming to your city!",
                categoryDtoThree.getId(), "SHADOW-THEATER is an ancient " +
                "art that originated in Asia more than two thousand years ago, but it is impossible to say for sure " +
                "whether it was in India or China.", LocalDateTime.parse(eventThree.getEventDate(), FORMATTER),
                new Location(null, 44.8950f, 37.378f), false,
                1, false, CANCEL_REVIEW,"SHADOW-THEATER");

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> eventService.updateOwnerEvent(userDtoOne.getId(), eventThree.getId(), test)
        );

        assertEquals("Event with id=" + eventThree.getId() + " was not found", exception.getMessage());
    }

    @DisplayName("Не должен обновить событие, если оно опубликовано")
    @Test
    public void shouldNotUpdateOwnerEventIfStateEventPublished() {
        eventThree = eventService.createOwnerEvent(userDtoThree.getId(), newEventThree);


        UpdateEventAdminRequest request = new UpdateEventAdminRequest(eventThree.getAnnotation(),
                categoryDtoThree.getId(), eventThree.getDescription(), LocalDateTime.parse(eventThree.getEventDate(),
                FORMATTER), new Location(null, 44.895750f, 37.314678f), eventThree.getPaid(),
                eventThree.getParticipantLimit(), eventThree.getRequestModeration(), PUBLISH_EVENT,
                eventThree.getTitle());

        eventService.updateEventByAdmin(request, eventThree.getId());

        UpdateEventUserRequest test = new UpdateEventUserRequest("SHADOW THEATER is coming to your city!",
                categoryDtoThree.getId(), "SHADOW-THEATER is an ancient " +
                "art that originated in Asia more than two thousand years ago, but it is impossible to say for sure " +
                "whether it was in India or China.", LocalDateTime.parse(eventThree.getEventDate(), FORMATTER),
                new Location(null, 44.8950f, 37.378f), false,
                1, false, CANCEL_REVIEW,"SHADOW-THEATER");

        ConflictException exception = assertThrows(ConflictException.class,
                () -> eventService.updateOwnerEvent(userDtoThree.getId(), eventThree.getId(), test)
        );

        assertEquals("Event must not be published", exception.getMessage());
    }

    @DisplayName("Должен опубликовать событие")
    @Test
    public void shouldUpdateEventByAdmin() {
        eventThree = eventService.createOwnerEvent(userDtoThree.getId(), newEventThree);


        UpdateEventAdminRequest request = new UpdateEventAdminRequest(eventThree.getAnnotation(),
                categoryDtoThree.getId(), eventThree.getDescription(), LocalDateTime.parse(eventThree.getEventDate(),
                FORMATTER), new Location(null, 44.895750f, 37.314678f), eventThree.getPaid(),
                eventThree.getParticipantLimit(), eventThree.getRequestModeration(), PUBLISH_EVENT,
                eventThree.getTitle());

        EventFullDto result = eventService.updateEventByAdmin(request, eventThree.getId());

        eventThree = eventService.getOwnerOneEvent(userDtoThree.getId(), eventThree.getId());

        assertThat(result, is(equalTo(eventThree)));
    }

    @DisplayName("Должен отклонить событие")
    @Test
    public void shouldNotUpdateEventByAdmin() {
        eventThree = eventService.createOwnerEvent(userDtoThree.getId(), newEventThree);

        UpdateEventAdminRequest request = new UpdateEventAdminRequest(eventThree.getAnnotation(),
                categoryDtoThree.getId(), eventThree.getDescription(), LocalDateTime.parse(eventThree.getEventDate(),
                FORMATTER), new Location(null, 44.895750f, 37.314678f), eventThree.getPaid(),
                eventThree.getParticipantLimit(), eventThree.getRequestModeration(), REJECT_EVENT,
                eventThree.getTitle());

        EventFullDto result = eventService.updateEventByAdmin(request, eventThree.getId());

        eventThree = eventService.getOwnerOneEvent(userDtoThree.getId(), eventThree.getId());

        assertThat(result, is(equalTo(eventThree)));
    }

    @DisplayName("Не должен опубликовать событие не PENDING")
    @Test
    public void shouldNotUpdateOwnerEventIfEventNotPending() {
        eventThree = eventService.createOwnerEvent(userDtoThree.getId(), newEventThree);

        UpdateEventAdminRequest request = new UpdateEventAdminRequest(eventThree.getAnnotation(),
                categoryDtoThree.getId(), eventThree.getDescription(), LocalDateTime.parse(eventThree.getEventDate(),
                FORMATTER), new Location(null, 44.895750f, 37.314678f), eventThree.getPaid(),
                eventThree.getParticipantLimit(), eventThree.getRequestModeration(), PUBLISH_EVENT,
                eventThree.getTitle());

        eventService.updateEventByAdmin(request, eventThree.getId());

        ConflictException exception = assertThrows(ConflictException.class,
                () -> eventService.updateEventByAdmin(request, eventThree.getId())
        );

        assertEquals("Event is not PENDING", exception.getMessage());
    }

    @DisplayName("Не должен отклонить событие PUBLISHED")
    @Test
    public void shouldNotUpdateOwnerEventIfEventPublished() {
        eventThree = eventService.createOwnerEvent(userDtoThree.getId(), newEventThree);

        UpdateEventAdminRequest request = new UpdateEventAdminRequest(eventThree.getAnnotation(),
                categoryDtoThree.getId(), eventThree.getDescription(), LocalDateTime.parse(eventThree.getEventDate(),
                FORMATTER), new Location(null, 44.895750f, 37.314678f), eventThree.getPaid(),
                eventThree.getParticipantLimit(), eventThree.getRequestModeration(), PUBLISH_EVENT,
                eventThree.getTitle());

        eventService.updateEventByAdmin(request, eventThree.getId());
        request.setStateAction(REJECT_EVENT);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> eventService.updateEventByAdmin(request, eventThree.getId())
        );

        assertEquals("Cannot canceled the event because it's not in the right state: PUBLISHED",
                exception.getMessage());
    }

    @DisplayName("Должен вернуть одно событие организатора по id")
    @Test
    public void shouldReturnOwnerOneEvent() {
        eventThree = eventService.createOwnerEvent(userDtoThree.getId(), newEventThree);

        EventFullDto result = eventService.getOwnerOneEvent(userDtoThree.getId(), eventThree.getId());

        assertThat(result, is(equalTo(eventThree)));

    }

    @DisplayName("Не должен возвращать событие, если этот запрос не организатора")
    @Test
    public void shouldNotReturnOwnerOneEventIfThisNotOwnerOfEvent() {
        eventThree = eventService.createOwnerEvent(userDtoThree.getId(), newEventThree);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> eventService.getOwnerOneEvent(userDtoOne.getId(), eventThree.getId())
        );

        assertEquals("Event with id=" + eventThree.getId() + " was not found", exception.getMessage());

    }

    @DisplayName("Должен вернуть организатору его события")
    @Test
    public void shouldGetOwnerEvents() {
        eventThree = eventService.createOwnerEvent(userDtoThree.getId(), newEventThree);
        eventTwo = eventService.createOwnerEvent(userDtoThree.getId(), newEventTwo);

        EventShortDto testOne = new EventShortDto(eventThree.getId(), "SHADOW THEATER is coming to your city!",
                categoryDtoThree, 0, eventThree.getEventDate(),
                new UserShortDto(userDtoThree.getId(), userDtoThree.getName()), false,
                "SHADOW THEATER", 0L);
        EventShortDto testTwo = new EventShortDto(eventTwo.getId(), "Exhibition of antique dolls of the world!",
                categoryDtoTwo, 0, eventTwo.getEventDate(),
                new UserShortDto(userDtoThree.getId(), userDtoThree.getName()), false,
                "Exhibition of antique dolls", 0L);

        List<EventShortDto> result = eventService.getOwnerEvents(userDtoThree.getId(), 0, 10);

        assertThat(result, is(equalTo(List.of(testOne, testTwo))));
    }

    @DisplayName("Должен вернуть администратору список событий по определенным критериям")
    @Test
    public void shouldGetEventsForAdmin() {
        event = eventService.createOwnerEvent(userDtoOne.getId(), newEvent);
        eventTwo = eventService.createOwnerEvent(userDtoThree.getId(), newEventTwo);
        eventThree = eventService.createOwnerEvent(userDtoThree.getId(), newEventThree);

        EventFullDto testOne = new EventFullDto(eventThree.getId(), "SHADOW THEATER is coming to your city!",
                categoryDtoThree, 0, eventThree.getCreatedOn(), "Shadow theater is an ancient " +
                "art that originated in Asia more than two thousand years ago, but it is impossible to say for sure " +
                "whether it was in India or China. According to some theories, even Egypt is considered the " +
                "birthplace of theater.", eventThree.getEventDate(), new UserShortDto(userDtoThree.getId(),
                userDtoThree.getName()), new LocationDto(44.895750f, 37.314678f), false,
                0, null, true, PENDING,"SHADOW THEATER", 0L);

        EventFullDto testTwo = new EventFullDto(eventTwo.getId(), "Exhibition of antique dolls of the world!",
                categoryDtoTwo, 0, eventTwo.getCreatedOn(), "More than 1000 masters will " +
                "take part in the unique exhibition, presenting tens of thousands of exhibits to visitors: dolls " +
                "of the shadow theater of the East and “glamor puppets”, industrial dolls of the Soviet period " +
                "and traditional dolls of the peoples of the world, mechanical and interior dolls, theatrical and " +
                "cartoon dolls, as well as antique dolls from private collections.", eventTwo.getEventDate(),
                new UserShortDto(userDtoThree.getId(), userDtoThree.getName()),
                new LocationDto(44.895750f, 37.314678f), false, 0, null,
                false, PENDING,"Exhibition of antique dolls", 0L);

        List<EventFullDto> result = eventService.getEventsForAdmin(List.of(userDtoThree.getId()), null,
                null, null, null, 0, 10);

        assertThat(result, is(equalTo(List.of(testTwo, testOne))));

        EventFullDto testThree = new EventFullDto(event.getId(), "RADIO TAPOK is coming to your city!",
                categoryDto, 0, event.getCreatedOn(), "RADIO TAPOK is coming to your " +
                "city! At the concert you will be able to hear all your favorite hits that have accumulated on the " +
                "channel over the years, and the artist’s original material!\nEven more drive awaits you, " +
                "indescribable concert energy, a sea of sound, light and selected rock music. You can't miss this!",
                event.getEventDate(), new UserShortDto(userDtoOne.getId(), userDtoOne.getName()),
                new LocationDto(44.895750f, 37.314678f), true, 1, null,
                true, PENDING,"RADIO TAPOK", 0L);

        List<EventFullDto> resultTwo = eventService.getEventsForAdmin(null, null,
                List.of(categoryDto.getId()), null, null, 0, 10);

        assertThat(resultTwo, is(equalTo(List.of(testThree))));
    }
}