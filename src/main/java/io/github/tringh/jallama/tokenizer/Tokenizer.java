package io.github.tringh.jallama.tokenizer;

public interface Tokenizer extends AutoCloseable {

    int[] tokenize(String text);

    String detokenize(int[] tokens);
}
