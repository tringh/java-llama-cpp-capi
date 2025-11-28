package io.github.tringh.jallama.tokenizer;

import io.github.tringh.jallama.tokenizer.internal.llama_tokenizer_h;

import java.lang.foreign.*;

class TokenizerImpl implements Tokenizer {

    private final String modelPath;

    private final MemorySegment self;

    private final Arena arena;

    public TokenizerImpl(String modelPath, Arena arena) {
        this.modelPath = modelPath;
        this.arena = arena;
        self = llama_tokenizer_h.llama_tokenizer_create(arena.allocateFrom(modelPath));
        if (self == null || self == MemorySegment.NULL) {
            throw new TokenizerException("Failed to create tokenizer");
        }
    }

    @Override
    public void close() {
        llama_tokenizer_h.llama_tokenizer_destroy(self);
    }

    @Override
    public int[] tokenize(String text) {
        var textSegment = arena.allocateFrom(text);
        var tokensSize = countTokenizationTokens(textSegment, text.length());
        if (tokensSize < 0) {
            throw new TokenizerException("Tokenization failed");
        }
        var tokenBuf = arena.allocate(AddressLayout.JAVA_INT, tokensSize);
        llama_tokenizer_h.llama_tokenizer_tokenize(
                self,
                textSegment,
                text.length(),
                tokenBuf,
                tokensSize,
                true,
                false);
        return tokenBuf.toArray(ValueLayout.JAVA_INT);
    }

    private int countTokenizationTokens(MemorySegment text, int textLen) {
        return llama_tokenizer_h.llama_tokenizer_tokenize(self, text, textLen,
                MemorySegment.NULL, 0, true, false);
    }

    @Override
    public String detokenize(int[] tokens) {
        var tokensSegment = arena.allocateFrom(ValueLayout.JAVA_INT, tokens);
        var textSize = countDetokenizationTextSize(tokensSegment, tokens.length);
        if (textSize < 0) {
            throw new TokenizerException("Detokenization failed");
        }
        var textBuf = arena.allocate(ValueLayout.JAVA_CHAR, textSize);
        llama_tokenizer_h.llama_tokenizer_detokenize(
                self,
                tokensSegment,
                tokens.length,
                textBuf,
                textSize,
                true,
                false);
        return textBuf.getString(0);
    }

    private int countDetokenizationTextSize(MemorySegment tokens, int tokensSize) {
        return llama_tokenizer_h.llama_tokenizer_detokenize(self, tokens, tokensSize, MemorySegment.NULL,
                0, true, false);
    }
}
