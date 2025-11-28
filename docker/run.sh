docker run -d \
  --gpus all \
  -p 2722:22 \
  -v $SSH_AUTH_SOCK:/ssh-agent \
  -e SSH_AUTH_SOCK:=/ssh-agent \
  -v ~/.gitconfig:/home/developer/.gitconfig \
  -v .:/code/java-llama-cpp-capi \
  -v ~/mlearn/models:/models \
  --name java-llama-cpp-capi-dev \
  htring/java-llama:0.0.1
