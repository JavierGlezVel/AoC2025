package infrastructure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileRedTileSource implements RedTileSource {
    private final String path;

    public FileRedTileSource(String path) {
        this.path = path;
    }

    @Override
    public List<String> getLines() throws IOException {
        return Files.readAllLines(Path.of(path));
    }
}
