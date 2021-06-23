package api;

import api.exception.ErrorResponseException;
import api.model.CoreError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import partner.client.ApiClient;
import partner.client.api.PartnerApi;

import java.util.*;

@Service
public class TicketApiDelegateImpl implements TicketApiDelegate {

    @Value("${core.url}")
    private String coreUrl;

    @Value("${partner.apiKey}")
    private String partnerApiKey;

    private ErrorResponseException errorResponseException(Long errorCode) {
        ErrorResponse res = new ErrorResponse();
        res.setErrorCode(errorCode);
        res.setSuccess(false);
        return new ErrorResponseException(res);
    }

    private List<Event> getEventList() {
        PartnerApi clnt = new PartnerApi();
        ApiClient apiClient = clnt.getApiClient();
        apiClient.addDefaultHeader("apiKey", this.partnerApiKey);
        try {
            partner.client.model.EventList clntEventList = clnt.getEvents();
            if(clntEventList == null || clntEventList.getData() == null) {
                throw errorResponseException(404L);
            }

            List<Event> eventList = new ArrayList<>();
            for(partner.client.model.Event event : clntEventList.getData()) {
                Event e = new Event();
                e.setEventId(event.getEventId());
                e.setLocation(event.getLocation());
                e.setTitle(event.getTitle());
                e.setStartTimeStamp(event.getStartTimeStamp());
                e.setEndTimeStamp(event.getEndTimeStamp());

                eventList.add(e);
            }

            return eventList;
        } catch (ResourceAccessException ex) {
            throw errorResponseException(20404L);
        } catch (HttpClientErrorException ex) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                ErrorResponse res = mapper.readValue(ex.getResponseBodyAsString(), ErrorResponse.class);
                throw new ErrorResponseException(res);
            } catch (JsonProcessingException ex2) {
                throw errorResponseException(500L);
            }
        }
    }

    private EventDetails getEventDetails(Long eventId) {
        PartnerApi clnt = new PartnerApi();
        ApiClient apiClient = clnt.getApiClient();
        apiClient.addDefaultHeader("apiKey", this.partnerApiKey);
        try {
            partner.client.model.EventDetails clntEvent = clnt.getEvent(eventId);
            if(clntEvent == null || clntEvent.getData() == null || clntEvent.getData().getSeats() == null) {
                throw errorResponseException(404L);
            }

            EventDetails details = new EventDetails();
            details.setEventId(clntEvent.getData().getEventId());
            for(partner.client.model.Seat seat : clntEvent.getData().getSeats()) {
                Seat s = new Seat();
                s.setId(seat.getId());
                s.setCurrency(seat.getCurrency());
                s.setPrice(seat.getPrice());
                s.setReserved(seat.getReserved());

                details.addSeatsItem(s);
            }

            return details;
        } catch (ResourceAccessException ex) {
            throw errorResponseException(20404L);
        } catch (HttpClientErrorException ex) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                ErrorResponse res = mapper.readValue(ex.getResponseBodyAsString(), ErrorResponse.class);
                if (res.getErrorCode() == 90001L) {
                    res.setErrorCode(20001L);
                }

                throw new ErrorResponseException(res);
            } catch (JsonProcessingException ex2) {
                throw errorResponseException(500L);
            }
        }
    }

    private void decreaseBalance(Long userId, String cardId, Long amount) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.put(coreUrl + "/decreaseBalance?userId={userId}&cardId={cardId}&amount={amount}", null, userId, cardId, amount);
        } catch (HttpClientErrorException e) {
            try {
                CoreError error = new ObjectMapper().readValue(e.getResponseBodyAsString(), CoreError.class);

                ErrorResponse response = new ErrorResponse();
                response.setErrorCode(error.getErrorCode());
                response.setSuccess(false);

                throw new ErrorResponseException(response);
            } catch (JsonProcessingException ex) {
                Logger logger = LoggerFactory.getLogger(this.getClass());
                logger.error("Could not parse CoreError");
                logger.error(e.getResponseBodyAsString());
                logger.error(ex.getMessage());
            }
        }
    }

    private void increaseBalance(Long userId, String cardId, Long amount) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.put(coreUrl + "/increaseBalance?userId={userId}&cardId={cardId}&amount={amount}", null, userId, cardId, amount);
        } catch (HttpClientErrorException e) {
            try {
                CoreError error = new ObjectMapper().readValue(e.getResponseBodyAsString(), CoreError.class);

                ErrorResponse response = new ErrorResponse();
                response.setErrorCode(error.getErrorCode());
                response.setSuccess(false);

                throw new ErrorResponseException(response);
            } catch (JsonProcessingException ex) {
                Logger logger = LoggerFactory.getLogger(this.getClass());
                logger.error("Could not parse CoreError");
                logger.error(e.getResponseBodyAsString());
                logger.error(ex.getMessage());
            }
        }
    }

    @Override
    public ResponseEntity<EventDetails> getEvent(Long eventId) {
        EventDetails details = getEventDetails(eventId);
        return ResponseEntity.ok(details);
    }

    @Override
    public ResponseEntity<List<Event>> getEvents() {
        List<Event> eventList = getEventList();
        return ResponseEntity.ok(eventList);
    }

    @Override
    public ResponseEntity<SuccessfulReservation> reserveSeat(Long userId, Long eventId, String seatId, String cardId) {
        List<Event> eventList = getEventList();
        final long nowStr = new Date().toInstant().getEpochSecond();
        Optional<Event> e = eventList.stream().filter(event -> event.getEventId().equals(eventId)).findAny();
        if(e.isEmpty()) {
            throw errorResponseException(20001L);
        }

        if(Long.parseLong(e.get().getStartTimeStamp()) < nowStr) {
            throw errorResponseException(20011L);
        }

        EventDetails details = getEventDetails(eventId);
        Optional<Seat> s = details.getSeats().stream().filter(seat -> seat.getId().equals(seatId)).findAny();
        if(s.isEmpty()) {
            throw errorResponseException(20002L);
        }

        if(s.get().getReserved()) {
            throw errorResponseException(20010L);
        }

        decreaseBalance(userId, cardId, s.get().getPrice());

        try {
            PartnerApi clnt = new PartnerApi();
            ApiClient apiClient = clnt.getApiClient();
            apiClient.addDefaultHeader("apiKey", this.partnerApiKey);
            partner.client.model.SuccessfulReservation clntRes = clnt.reserveSeat(eventId, seatId);

            SuccessfulReservation res = new SuccessfulReservation();
            res.setSuccess(clntRes.getSuccess());
            res.setReservationId(clntRes.getReservationId());

            return ResponseEntity.ok(res);
        } catch (ResourceAccessException ex) {
            increaseBalance(userId, cardId, s.get().getPrice());
            throw errorResponseException(20404L);
        } catch (HttpClientErrorException ex) {
            increaseBalance(userId, cardId, s.get().getPrice());
            try {
                ObjectMapper mapper = new ObjectMapper();
                ErrorResponse res = mapper.readValue(ex.getResponseBodyAsString(), ErrorResponse.class);

                throw new ErrorResponseException(res);
            } catch (JsonProcessingException ex2) {
                throw errorResponseException(500L);
            }
        }
    }
}
