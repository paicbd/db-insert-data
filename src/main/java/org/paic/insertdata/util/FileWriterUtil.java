package org.paic.insertdata.util;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Component
public class FileWriterUtil {

    public void write(Path path, byte[] bytes, StandardOpenOption... options) throws IOException {
        java.nio.file.Files.write(path, bytes, options);
    }

    public boolean notExists(Path path) {
        return java.nio.file.Files.notExists(path);
    }

    public void createDirectories(Path path) throws IOException {
        java.nio.file.Files.createDirectories(path);
    }
}
