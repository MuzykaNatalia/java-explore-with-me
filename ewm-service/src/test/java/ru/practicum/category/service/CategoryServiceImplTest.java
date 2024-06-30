package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.service.EventService;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.location.model.Location;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CategoryServiceImplTest {
    private final CategoryService categoryService;
    private final EventService eventService;
    private final UserService userService;
    private NewUserRequest newUserRequest;
    private NewCategoryDto newCategoryDtoOne;
    private NewCategoryDto newCategoryDtoTwo;
    private NewEventDto newEventDto;

    @BeforeEach
    public void setUp() {
        newUserRequest = new NewUserRequest("Ivan", "ivan@mail.ru");
        newCategoryDtoOne = new NewCategoryDto("Concerts");
        newCategoryDtoTwo = new NewCategoryDto("Exhibition");
        newEventDto = new NewEventDto("RADIO TAPOK is coming to your city!", null, "RADIO " +
                "TAPOK is coming to your city! At the concert you will be able to hear all your favorite hits " +
                "that have accumulated on the channel over the years, and the artist’s original material!\n" +
                "Even more drive awaits you, indescribable concert energy, a sea of sound, light and " +
                "selected rock music. You can't miss this!", LocalDateTime.now().plusMonths(1),
                new Location(null, 44.895750f, 37.314678f),
                true, 200, true, "RADIO TAPOK");
    }

    @DisplayName("Должен создать категорию")
    @Test
    public void shouldCreateCategory() {
        CategoryDto categoryDto = categoryService.createCategory(newCategoryDtoOne);
        CategoryDto test = new CategoryDto(null, "Concerts");
        test.setId(categoryDto.getId());

        assertThat(categoryDto, is(equalTo(test)));
    }

    @DisplayName("Должен выдать исключение при попытке создать категорию с name, которое уже существует")
    @Test
    public void shouldNotCreateCategory() {
        categoryService.createCategory(newCategoryDtoOne);

        assertThrows(DataIntegrityViolationException.class, () -> categoryService.createCategory(newCategoryDtoOne));
    }

    @DisplayName("Должен удалить категорию по id")
    @Test
    public void shouldDeleteCategoryById() {
        CategoryDto categoryDtoOne = categoryService.createCategory(newCategoryDtoOne);
        CategoryDto categoryDtoTwo = categoryService.createCategory(newCategoryDtoTwo);

        CategoryDto testOne = new CategoryDto(null, "Concerts");
        CategoryDto testTwo = new CategoryDto(null, "Exhibition");

        testOne.setId(categoryDtoOne.getId());
        testTwo.setId(categoryDtoTwo.getId());

        List<CategoryDto> categoryDtoListOne = categoryService.getCategories(0, 10);

        assertEquals(categoryDtoListOne.size(), 2);
        assertThat(categoryDtoListOne, is(equalTo(List.of(testOne, testTwo))));

        categoryService.deleteCategory(testOne.getId());

        List<CategoryDto> categoryDtoListTwo = categoryService.getCategories(0, 10);

        assertEquals(categoryDtoListTwo.size(), 1);
        assertThat(categoryDtoListTwo, is(equalTo(List.of(testTwo))));
    }

    @DisplayName("Должен выдать исключение при попытке удалить категорию по id, которой не существует")
    @Test
    public void shouldNotDeleteCategoryById() {
        long categoryId = -200000;

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> categoryService.deleteCategory(categoryId)
        );

        assertEquals("Category with id=" + categoryId + " was not found", exception.getMessage());
    }

    @DisplayName("Должен выдать исключение при попытке удалить категорию, которая связана с событиями")
    @Test
    public void shouldNotDeleteCategory() {
        CategoryDto categoryDto = categoryService.createCategory(newCategoryDtoOne);
        newEventDto.setCategory(categoryDto.getId());
        UserDto createdUser = userService.createUser(newUserRequest);
        eventService.createOwnerEvent(createdUser.getId(), newEventDto);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> categoryService.deleteCategory(categoryDto.getId())
        );

        assertEquals("The category is not empty", exception.getMessage());
    }

    @DisplayName("Должен обновить категорию")
    @Test
    public void shouldUpdateCategory() {
        CategoryDto categoryDtoOne = categoryService.createCategory(newCategoryDtoOne);

        CategoryDto testOne = new CategoryDto(null, "Concerts");
        testOne.setId(categoryDtoOne.getId());

        assertThat(categoryDtoOne, is(equalTo(testOne)));

        CategoryDto testTwo = new CategoryDto(null, "Musicals");
        testOne.setId(categoryDtoOne.getId());

        CategoryDto result = categoryService.updateCategory(testTwo, categoryDtoOne.getId());

        assertThat(testTwo, is(equalTo(result)));
    }

    @DisplayName("Должен выдать исключение при попытке обновить категорию с name, которое уже существует")
    @Test
    public void shouldNotUpdateCategoryIfNameExist() {
        CategoryDto categoryDtoOne = categoryService.createCategory(newCategoryDtoOne);
        CategoryDto categoryDtoTwo = categoryService.createCategory(newCategoryDtoTwo);

        categoryDtoOne.setName(categoryDtoTwo.getName());

        assertThrows(DataIntegrityViolationException.class,
                () -> categoryService.updateCategory(categoryDtoOne, categoryDtoOne.getId()));
    }

    @DisplayName("Должен выдать исключение при попытке обновить категорию с неправильным id")
    @Test
    public void shouldNotUpdateCategoryIfIdNotExist() {
        CategoryDto test = new CategoryDto(null, "Theater");
        long categoryId = -200000;

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> categoryService.updateCategory(test, categoryId)
        );

        assertEquals("Category with id=" + categoryId + " was not found", exception.getMessage());

    }

    @DisplayName("Должен вернуть категории в соответствии с пагинацией")
    @Test
    public void shouldGetCategories() {
        CategoryDto categoryDto = categoryService.createCategory(newCategoryDtoOne);
        CategoryDto categoryDtoTwo = categoryService.createCategory(newCategoryDtoTwo);

        List<CategoryDto> test = categoryService.getCategories(0, 10);

        assertThat(test, is(equalTo(List.of(categoryDto, categoryDtoTwo))));

        List<CategoryDto> testTwo = categoryService.getCategories(0, 1);

        assertThat(testTwo, is(equalTo(List.of(categoryDto))));
    }

    @DisplayName("Должен вернуть одну категорию по id")
    @Test
    public void shouldGetOneCategoryDto() {
        CategoryDto categoryDto = categoryService.createCategory(newCategoryDtoOne);
        CategoryDto categoryDtoTwo = categoryService.createCategory(newCategoryDtoTwo);

        CategoryDto test = categoryService.getOneCategoryDto(categoryDto.getId());

        assertThat(test, is(equalTo(categoryDto)));

        CategoryDto testTwo = categoryService.getOneCategoryDto(categoryDtoTwo.getId());

        assertThat(testTwo, is(equalTo(categoryDtoTwo)));
    }

    @DisplayName("Должен выдать исключение при попытке получить категорию по id, которой не существует")
    @Test
    public void shouldNotGetOneCategoryDto() {
        long categoryId = -200000;

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> categoryService.getOneCategoryDto(categoryId)
        );

        assertEquals("Category with id=" + categoryId + " was not found", exception.getMessage());
    }
}