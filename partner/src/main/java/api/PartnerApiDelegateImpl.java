package api;

import api.exception.ErrorResponseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;

@Service
public class PartnerApiDelegateImpl implements PartnerApiDelegate {
    @Value("classpath:getEvents.json")
    private Resource eventsResource;

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public ResponseEntity<EventDetails> getEvent(Long eventId) {
        try {
            EventDetails details = mapper.readValue(new ClassPathResource("getEvent" + eventId + ".json").getFile(), EventDetails.class);
            return ResponseEntity.ok(details);
        } catch (IOException ex) {
            ErrorResponse res = new ErrorResponse();
            res.setSuccess(false);
            res.setErrorCode(90001L);
            throw new ErrorResponseException(res);
        }
    }

    @Override
    public ResponseEntity<EventList> getEvents() {
        try {
            EventList eventList = mapper.readValue(eventsResource.getFile(), EventList.class);
            return ResponseEntity.ok(eventList);
        } catch (IOException ex) {
            ErrorResponse res = new ErrorResponse();
            res.setSuccess(false);
            res.setErrorCode(500L);
            throw new ErrorResponseException(res);
        }
    }

    @Override
    public ResponseEntity<SuccessfulReservation> reserveSeat(Long eventId, String seatId) {
        try {
            ClassPathResource resource = new ClassPathResource("getEvent" + eventId + ".json");
            EventDetails details = mapper.readValue(resource.getFile(), EventDetails.class);
            details.getData().getSeats().stream().filter(seat -> seat.getId().equals(seatId))
                    .findAny().ifPresentOrElse(seat -> {
                        if(seat.getReserved()) {
                            ErrorResponse res = new ErrorResponse();
                            res.setSuccess(false);
                            res.setErrorCode(90010L);
                            throw new ErrorResponseException(res);
                        }
                    }, () -> {
                        ErrorResponse res = new ErrorResponse();
                        res.setSuccess(false);
                        res.setErrorCode(90002L);
                        throw new ErrorResponseException(res);
                    });

            SuccessfulReservation res = new SuccessfulReservation();
            res.setSuccess(true);
            res.setReservationId(new Date().getTime());
            return ResponseEntity.ok(res);
        } catch (IOException ex) {
            ErrorResponse res = new ErrorResponse();
            res.setSuccess(false);
            res.setErrorCode(90001L);
            throw new ErrorResponseException(res);
        }
    }
}
