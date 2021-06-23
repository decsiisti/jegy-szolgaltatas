package api;

import api.exception.ErrorResponseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;

@Service
public class PartnerApiDelegateImpl implements PartnerApiDelegate {
    @Value("classpath:getEvents.json")
    private Resource eventsResource;

    @Value("${security.apiKey}")
    private String apiKey;

    private ObjectMapper mapper = new ObjectMapper();

    private final HttpServletRequest request;

    @Autowired
    public PartnerApiDelegateImpl(HttpServletRequest request) {
        this.request = request;
    }

    private ErrorResponseException errorResponseException(Long errorCode) {
        ErrorResponse res = new ErrorResponse();
        res.setErrorCode(errorCode);
        res.setSuccess(false);
        return new ErrorResponseException(res);
    }

    private void checkApiKey() {
        String clntApiKey = request.getHeader("apiKey");
        if(clntApiKey == null || !clntApiKey.equals(this.apiKey)) {
            throw errorResponseException(401L);
        }
    }

    @Override
    public ResponseEntity<EventDetails> getEvent(Long eventId) {
        checkApiKey();

        try {
            EventDetails details = mapper.readValue(new ClassPathResource("getEvent" + eventId + ".json").getFile(), EventDetails.class);
            return ResponseEntity.ok(details);
        } catch (IOException ex) {
            throw errorResponseException(90001L);
        }
    }

    @Override
    public ResponseEntity<EventList> getEvents() {
        checkApiKey();

        try {
            EventList eventList = mapper.readValue(eventsResource.getFile(), EventList.class);
            return ResponseEntity.ok(eventList);
        } catch (IOException ex) {
            throw errorResponseException(500L);
        }
    }

    @Override
    public ResponseEntity<SuccessfulReservation> reserveSeat(Long eventId, String seatId) {
        checkApiKey();

        try {
            ClassPathResource resource = new ClassPathResource("getEvent" + eventId + ".json");
            EventDetails details = mapper.readValue(resource.getFile(), EventDetails.class);
            details.getData().getSeats().stream().filter(seat -> seat.getId().equals(seatId))
                    .findAny().ifPresentOrElse(seat -> {
                        if(seat.getReserved()) {
                            throw errorResponseException(90010L);
                        }
                    }, () -> {
                        throw errorResponseException(90002L);
                    });

            SuccessfulReservation res = new SuccessfulReservation();
            res.setSuccess(true);
            res.setReservationId(new Date().getTime());
            return ResponseEntity.ok(res);
        } catch (IOException ex) {
            throw errorResponseException(90001L);
        }
    }
}
