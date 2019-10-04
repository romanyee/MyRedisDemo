package roman;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
//
public interface Command {
    void setArgs(List<Object> args);

    void run(OutputStream os) throws IOException;
}
