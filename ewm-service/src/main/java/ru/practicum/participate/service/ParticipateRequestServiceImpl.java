package ru.practicum.participate.service;

import ru.practicum.participate.dto.EventRequestStatusUpdateRequest;
import ru.practicum.participate.dto.EventRequestStatusUpdateResult;
import ru.practicum.participate.dto.ParticipationRequestDto;

import java.util.List;

public class ParticipateRequestServiceImpl implements ParticipateRequestService {
    //добавление запроса от пользователя на участие в событии
    //нельзя добавить повторный запрос (Ожидается код ошибки 409)
    //инициатор события не может добавить запрос на участие в своём событии (Ожидается код ошибки 409)
    //нельзя участвовать в неопубликованном событии (Ожидается код ошибки 409)
    //если у события достигнут лимит запросов на участие - необходимо вернуть ошибку (Ожидается код ошибки 409)
    //если для события отключена пре-модерация запросов на участие,
    // то запрос должен автоматически перейти в состояние подтвержденного
    @Override // 400, 404, 409
    public ParticipationRequestDto createRequestToParticipateInEvent(Long userId, Long eventId) {
        return null;
    }
    //если для события лимит заявок равен 0 или отключена пре-модерация заявок, то подтверждение заявок не требуется
    //нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие (Ожидается код ошибки 409)
    //статус можно изменить только у заявок, находящихся в состоянии ожидания (Ожидается код ошибки 409)
    //если при подтверждении данной заявки, лимит заявок для события исчерпан,
    // то все неподтверждённые заявки необходимо отклонить
    @Override// 400, 404, 409
    public EventRequestStatusUpdateResult updateRequestStatusParticipateOwnerEvent(
            Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatus) {
        return null;
    }
    // отмена своего запроса на участие в событии
    @Override // 404
    public ParticipationRequestDto cancelRequestToParticipateInEvent(Long userId, Long requestId) {
        return null;
    }
    //В случае, если по заданным фильтрам не найдено ни одной заявки, возвращает пустой список
    @Override// 400
    public ParticipationRequestDto getRequestForOwnerEvent(Long userId, Long eventId) {
        return null;
    }

    // для работы с запросами текущего пользователя на участие в событиях
    //В случае, если по заданным фильтрам не найдено ни одной заявки, возвращает пустой список
    @Override// 400, 404
    public List<ParticipationRequestDto> getInfoOnRequestsForUserInOtherEvents(Long userId) {
        return null;
    }
}
