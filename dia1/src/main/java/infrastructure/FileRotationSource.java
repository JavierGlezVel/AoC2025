package infrastructure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileRotationSource implements RotationSource {
    private final String path;

    public FileRotationSource(String path) {
        this.path = path;
    }

    @Override
    public List<String> getLines() throws IOException {
        return Files.readAllLines(Path.of(path));
    }
}
