package ca.redtoad.compression;

import java.util.ServiceLoader;

public final class CodecService {

    private static CodecService service;
    private final ServiceLoader<Codec> loader;

    private CodecService() {
        loader = ServiceLoader.load(Codec.class);
    }

    public static synchronized CodecService getInstance() {
        if (service == null) {
            service = new CodecService();
        }
        return service;
    }

    public Codec getCodec(String algorithm) {
        for (Codec codec : loader) {
            if (codec.getName().equals(algorithm)) {
                return codec;
            }
        }
        throw new IllegalArgumentException("no such algorithm " + algorithm);
    }
}
