package io.github.tringh.jallama.tokenizer;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class TokenizerTest {

    private Tokenizer tokenizer;

    private static TokenizerService service;

    @BeforeAll
    static void globalSetup() {
        service = TokenizerService.getInstance();
    }

    @AfterAll
    static void globalTeardown() throws Exception  {
        service.close();
    }

    @BeforeEach
    void setup() {
        var modelPath = getClass().getResource("/models/llama-3.1-8b-instruct-vocab.gguf").getPath();
        tokenizer = service.newTokenizer(modelPath);
    }

    @AfterEach
    void teardown() throws Exception {
        tokenizer.close();
    }

    @Test
    void testTokenize() {
        var text = "Hello, world!";
        var tokens = tokenizer.tokenize(text);
        assertArrayEquals(new int[] {128000, 9906, 11, 1917, 0}, tokens);
    }

    @Test
    void testDetokenize() {
        var tokens = new int[] {128000, 9906, 11, 1917, 0};
        var text = tokenizer.detokenize(tokens);
        assertEquals("Hello, world!", text);
    }

}
