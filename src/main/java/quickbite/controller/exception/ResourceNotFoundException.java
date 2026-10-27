package quickbite.controller.exception;

public class ResourceNotFoundException
        extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message + " tidak ditemukan");
    }
}
