package ru.practicum.event.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.event.dto.SearchEventCriteria;
import ru.practicum.event.dto.SearchEventCriteriaAdmin;
import ru.practicum.event.model.Event;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

import static ru.practicum.event.sort.SortEvent.EVENT_DATE;
import static ru.practicum.event.sort.SortEvent.VIEWS;
import static ru.practicum.event.state.EventState.PUBLISHED;

@Repository
@AllArgsConstructor
public class CustomSearchEventRepository {
    private final EntityManager entityManager;

    public List<Event> getEventsByCriteriaByAdmin(SearchEventCriteriaAdmin criteria) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = builder.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);

        List<Predicate> predicates = new ArrayList<>();

        if (criteria.getUsers() != null && !criteria.getUsers().isEmpty()) {
            predicates.add(root.get("initiator").in(criteria.getUsers()));
        }

        if (criteria.getStates() != null && !criteria.getStates().isEmpty()) {
            predicates.add(root.get("eventState").in(criteria.getStates()));
        }

        if (criteria.getCategories() != null && !criteria.getCategories().isEmpty()) {
            predicates.add(root.get("category").in(criteria.getCategories()));
        }

        if (criteria.getRangeStart() != null && criteria.getRangeEnd() != null) {
            predicates.add(builder.between(root.get("eventDate"), criteria.getRangeStart(), criteria.getRangeEnd()));
        }

        query.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(query)
                .setFirstResult(criteria.getFrom())
                .setMaxResults(criteria.getSize())
                .getResultList();
    }

    public List<Event> getEventsByCriteriaByAll(SearchEventCriteria criteria) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = builder.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.equal(root.get("eventState"), PUBLISHED));

        if (criteria.getText() != null && !criteria.getText().isEmpty()) {
            String searchText = "%" + criteria.getText().toUpperCase() + "%";
            List<Predicate> orPredicates = new ArrayList<>();

            orPredicates.add(builder.like(builder.upper(root.get("annotation")), searchText));
            orPredicates.add(builder.like(builder.upper(root.get("description")), searchText));
            predicates.add(builder.or(orPredicates.toArray(new Predicate[0])));
        }

        if (criteria.getCategories() != null && !criteria.getCategories().isEmpty()) {
            predicates.add(root.get("category").in(criteria.getCategories()));
        }

        if (criteria.getPaid() != null) {
            predicates.add(builder.equal(root.get("paid"), criteria.getPaid()));
        }

        if (criteria.getRangeStart() != null && criteria.getRangeEnd() != null) {
            predicates.add(builder.between(root.get("eventDate"), criteria.getRangeStart(), criteria.getRangeEnd()));
        }

        if (criteria.getOnlyAvailable() != null && criteria.getOnlyAvailable().equals(true)) {
            List<Predicate> orPredicates = new ArrayList<>();
            orPredicates.add(builder.equal(root.get("participantLimit"), 0));
            orPredicates.add(builder.gt(root.get("participantLimit"), root.get("confirmedRequests")));

            predicates.add(builder.or(orPredicates.toArray(new Predicate[0])));
        }

        if (criteria.getSort() != null && EVENT_DATE.equals(criteria.getSort())) {
            query.orderBy(builder.desc(root.get("eventDate")));
        } else if (criteria.getSort() != null && VIEWS.equals(criteria.getSort())) {
            query.orderBy(builder.desc(root.get("views")));
        }

        query.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(query)
                .setFirstResult(criteria.getFrom())
                .setMaxResults(criteria.getSize())
                .getResultList();
    }
}
