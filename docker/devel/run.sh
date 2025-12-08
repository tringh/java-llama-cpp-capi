docker run -d \
  --gpus all \
  -p 2722:22 \
  -v $SSH_AUTH_SOCK:/ssh-agent \
  -e SSH_AUTH_SOCK:=/ssh-agent \
  -v ~/.gitconfig:/home/developer/.gitconfig \
  -v .:/code/java-llama-capi \
  -v ~/mlearn/models:/models \
  --name java-llama-capi-dev \
  tringh/java-llama-capi-devel:0.0.1
