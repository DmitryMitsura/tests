package com.example.ipcounter;

import java.nio.file.Path;
import java.io.IOException;

public interface IPAddressCounter {
    long countUnique(Path filePath) throws IOException;
}