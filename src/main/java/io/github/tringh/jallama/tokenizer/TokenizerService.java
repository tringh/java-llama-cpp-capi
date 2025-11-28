package io.github.tringh.jallama.tokenizer;

import io.github.tringh.jallama.tokenizer.internal.llama_tokenizer_h;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class TokenizerService implements AutoCloseable {

    private static final TokenizerService INSTANCE;

    private final Arena arena;

    private static final String NATIVE_LIB = "llama_tokenizer";

    static {
        INSTANCE = new TokenizerService(Arena.ofConfined());
    }

    public static TokenizerService getInstance() {
        return INSTANCE;
    }

    private TokenizerService(Arena arena) {
        this.arena = arena;
        loadLibraryFromClassPath(NATIVE_LIB, arena);
        llama_tokenizer_h.llama_tokenizer_set_log_level(
                llama_tokenizer_h.LLAMA_TOKENIZER_LOG_NONE());
        llama_tokenizer_h.llama_tokenizer_init();
    }

    @Override
    public void close() throws Exception {
        llama_tokenizer_h.llama_tokenizer_free_backend();
        arena.close();
    }

    public Tokenizer newTokenizer(String modelPath) {
        return new TokenizerImpl(modelPath, arena);
    }

    private static void loadLibraryFromClassPath(String libNameWithoutExtension, Arena arena) {
        try {
            var libName = System.mapLibraryName(libNameWithoutExtension);
            Path libPath;
            try (var binaryIn = TokenizerService.class.getClassLoader().getResourceAsStream(libName)) {
                if (binaryIn == null) {
                    throw new IllegalArgumentException("Library not found in classpath: " + libName);
                }
                var tempFile = Files.createTempFile(
                        "_native_" + libName.replace("/", "."), null);
                Files.copy(binaryIn, tempFile, StandardCopyOption.REPLACE_EXISTING);
                tempFile.toFile().deleteOnExit();
                libPath = tempFile.toAbsolutePath();
            }
            System.load(libPath.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
