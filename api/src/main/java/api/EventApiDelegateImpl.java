package api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import api.exception.ErrorResponseException;
import model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ticket.client.api.TicketApi;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
public class EventApiDelegateImpl implements EventApiDelegate {

    @Value("${core.url}")
    private String coreUrl;



    private final HttpServletRequest httpRequest;

    @Autowired
    public EventApiDelegateImpl(HttpServletRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    private ErrorResponseException errorResponseExpection(Long errorCode) {
        ErrorResponse res = new ErrorResponse();
        res.setErrorCode(errorCode);
        res.setSuccess(false);
        return new ErrorResponseException(res);
    }

    private List<Event> getEventList() {
        TicketApi clnt = new TicketApi();
        try {
            List<ticket.client.model.Event> clntEventList = clnt.getEvents();
            if(clntEventList == null) {
                throw errorResponseExpection(404L);
            }

            List<Event> eventList = new ArrayList<>();
            for(ticket.client.model.Event event : clntEventList) {
                Event e = new Event();
                e.setEventId(event.getEventId());
                e.setLocation(event.getLocation());
                e.setTitle(event.getTitle());
                e.setStartTimeStamp(event.getStartTimeStamp());
                e.setEndTimeStamp(event.getEndTimeStamp());

                eventList.add(e);
            }

            return eventList;
        } catch (HttpClientErrorException ex) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                ErrorResponse res = mapper.readValue(ex.getResponseBodyAsString(), ErrorResponse.class);
                throw new ErrorResponseException(res);
            } catch (JsonProcessingException ex2) {
                throw errorResponseExpection(500L);
            }
        }
    }

    private EventDetails getEventDetails(Long eventId) {
        TicketApi clnt = new TicketApi();
        try {
            ticket.client.model.EventDetails clntEvent = clnt.getEvent(eventId);
            if(clntEvent == null || clntEvent.getSeats() == null) {
                throw errorResponseExpection(404L);
            }

            EventDetails details = new EventDetails();
            details.setEventId(clntEvent.getEventId());
            for(ticket.client.model.Seat seat : clntEvent.getSeats()) {
                Seat s = new Seat();
                s.setId(seat.getId());
                s.setCurrency(seat.getCurrency());
                s.setPrice(seat.getPrice());
                s.setReserved(seat.getReserved());

                details.addSeatsItem(s);
            }

            return details;
        } catch (HttpClientErrorException ex) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                ErrorResponse res = mapper.readValue(ex.getResponseBodyAsString(), ErrorResponse.class);

                throw new ErrorResponseException(res);
            } catch (JsonProcessingException ex2) {
                throw errorResponseExpection(500L);
            }
        }
    }

    private SuccessfulReservation payAndReserveSeat(Long userId, Long eventId, String seatId, String cardId) {
        TicketApi clnt = new TicketApi();
        try {
            ticket.client.model.SuccessfulReservation clntRes = clnt.reserveSeat(userId, eventId, seatId, cardId);
            if(clntRes == null) {
                throw errorResponseExpection(404L);
            }

            SuccessfulReservation res = new SuccessfulReservation();
            res.setReservationId(clntRes.getReservationId());
            res.setSuccess(clntRes.getSuccess());

            return res;
        } catch (HttpClientErrorException ex) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                ErrorResponse res = mapper.readValue(ex.getResponseBodyAsString(), ErrorResponse.class);

                throw new ErrorResponseException(res);
            } catch (JsonProcessingException ex2) {
                throw errorResponseExpection(500L);
            }
        }
    }

    @Override
    public ResponseEntity<EventDetails> getEvent(Long eventId) {
        Long userId = validateToken();
        if(userId != -1) {
            EventDetails details = getEventDetails(eventId);
            return ResponseEntity.ok(details);
        } else {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public ResponseEntity<List<Event>> getEvents() {
        Long userId = validateToken();
        if(userId != -1) {
            List<Event> eventList = getEventList();
            return ResponseEntity.ok(eventList);
        } else {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public ResponseEntity<SuccessfulReservation> reserveSeat(Long eventId, String seatId, String cardId) {
        Long userId = validateToken();
        if(userId != -1) {
            SuccessfulReservation res = payAndReserveSeat(userId, eventId, seatId, cardId);
            return ResponseEntity.ok(res);
        } else {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }

    private Long validateToken() {
        String token = httpRequest.getHeader("User-Token");
        if(token == null) {
            return -1L;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String result = restTemplate.getForObject(coreUrl + "/validateToken?token={token}", String.class, token);
            if(result == null) {
                return -1L;
            }
            return Long.parseLong(result);
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
                return -1L;
            }
        }
    }
}
