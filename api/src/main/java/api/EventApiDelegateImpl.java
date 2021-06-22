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

import javax.servlet.http.HttpServletRequest;
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

    @Override
    public ResponseEntity<EventDetails> getEvent(Long eventId) {
        if(validateToken()) {
            //TODO get event from ticket module
            return new ResponseEntity<>(null, HttpStatus.NOT_IMPLEMENTED);
        } else {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public ResponseEntity<List<Event>> getEvents() {
        if(validateToken()) {
            //TODO get events from ticket module
            return new ResponseEntity<>(null, HttpStatus.NOT_IMPLEMENTED);
        } else {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public ResponseEntity<SuccessfulReservation> reserveSeat(Long eventId, String seatId, String cardId) {
        if(validateToken()) {
            //TODO send request to ticket
            return new ResponseEntity<>(null, HttpStatus.NOT_IMPLEMENTED);
        } else {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean validateToken() {
        String token = httpRequest.getHeader("User-Token");
        if(token == null) {
            return false;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String result = restTemplate.getForObject(coreUrl + "/validateToken?token={token}", String.class, token);
            return Boolean.parseBoolean(result);
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
                return false;
            }
        }
    }
}
