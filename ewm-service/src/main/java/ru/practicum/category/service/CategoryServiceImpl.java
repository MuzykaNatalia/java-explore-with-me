package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        Category newCategory = categoryMapper.toCategoryFromNewCategoryDto(newCategoryDto);
        Category createdCategory = categoryRepository.save(newCategory);
        log.info("Category has been created={}", createdCategory);
        return categoryMapper.toCategoryDto(createdCategory);
    }

    @Transactional
    @Override
    public void deleteCategory(Long catId) {
        checkExistsCategory(catId);

        if (eventRepository.existsByCategory_Id(catId)) {
            log.warn("The category id={} is not empty", catId);
            throw new ConflictException("The category is not empty",
                    Collections.singletonList("No events should be associated with the category"));
        }

        log.info("Category with id={} deleted", catId);
        categoryRepository.deleteAllById(Collections.singleton(catId));
    }

    @Transactional
    @Override
    public CategoryDto updateCategory(CategoryDto categoryDto, Long catId) {
        checkExistsCategory(catId);
        categoryDto.setId(catId);
        Category updatedCategory = categoryRepository.save(categoryMapper.toCategoryFromCategoryDto(categoryDto));

        log.info("Category updated={}", updatedCategory);
        return categoryMapper.toCategoryDto(updatedCategory);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Order.asc("id")));
        List<Category> allCategories = categoryRepository.findAll(pageable).getContent();

        log.info("Received categories");
        return categoryMapper.toCategoryDtoList(allCategories);
    }

    @Transactional(readOnly = true)
    @Override
    public CategoryDto getOneCategoryDto(Long catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(() ->
                new NotFoundException("Category with id=" + catId + " was not found",
                        Collections.singletonList("Category id does not exist")));

        log.info("Received category={} by id={}", category, catId);
        return categoryMapper.toCategoryDto(category);
    }

    private void checkExistsCategory(Long catId) {
        if (!categoryRepository.existsById(catId)) {
            log.warn("Category with id={} catId was not found", catId);
            throw new NotFoundException("Category with id=" + catId + " was not found",
                    Collections.singletonList("Category id does not exist"));
        }
    }
}
