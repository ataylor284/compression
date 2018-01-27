package ca.redtoad.compression;

import java.util.Map;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public interface Codec {
    String getName();
    void setParameters(Map<String, String> parameters);
    void encode(InputStream input, OutputStream out) throws IOException;
    void decode(InputStream input, OutputStream out) throws IOException;
}
