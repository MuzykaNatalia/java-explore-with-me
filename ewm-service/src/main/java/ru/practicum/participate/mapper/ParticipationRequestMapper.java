package ru.practicum.participate.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.participate.dto.ParticipationRequestDto;
import ru.practicum.participate.model.ParticipateRequest;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.Constant.FORMATTER;

@Component
public class ParticipationRequestMapper {
    public ParticipationRequestDto toParticipationRequestDto(ParticipateRequest participationRequest) {
        return ParticipationRequestDto.builder()
                .id(participationRequest.getId())
                .created(participationRequest.getCreated().format(FORMATTER))
                .event(participationRequest.getEvent())
                .requester(participationRequest.getRequester())
                .status(participationRequest.getStatus())
                .build();
    }

    public List<ParticipationRequestDto> toParticipationRequestDtoList(List<ParticipateRequest> participateRequestList) {
        return participateRequestList.stream().map(this::toParticipationRequestDto).collect(Collectors.toList());
    }
}
