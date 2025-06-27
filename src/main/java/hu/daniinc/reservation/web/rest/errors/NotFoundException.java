package hu.daniinc.reservation.web.rest.errors;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String entity, Object id) {
        super(entity + " not found with id: " + id);
    }
}
