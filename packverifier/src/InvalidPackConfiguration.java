
public class InvalidPackConfiguration extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -5455725626189958474L;

    public InvalidPackConfiguration(String filename, String error) {
        super(String.format("Configuration file '%s' is invalid! Error: %s", filename, error));
    }
}
