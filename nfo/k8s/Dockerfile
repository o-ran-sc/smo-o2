# Start with a base image that includes the tools needed
FROM alpine:latest

# Install necessary tools (curl, tar, git, Python, Helm dependencies)
RUN apk add --no-cache \
    curl \
    tar \
    git \
    python3 \
    py3-pip \
    libc-dev \
    gcc

# Install Helm
ENV HELM_VERSION v3.8.0
ENV FILENAME helm-${HELM_VERSION}-linux-amd64.tar.gz

# Download and extract Helm binary
RUN curl -fsSL -o /tmp/${FILENAME} https://get.helm.sh/${FILENAME} \
    && tar -zxvf /tmp/${FILENAME} -C /tmp \
    && mv /tmp/linux-amd64/helm /usr/local/bin/helm \
    && rm /tmp/${FILENAME} \
    && rm -rf /tmp/linux-amd64

# Set up application directory
ENV PYTHONUNBUFFERED 1
ENV DJANGO_SETTINGS_MODULE nfo_microservice.settings


# Set work directory
WORKDIR /app

# Install dependencies
COPY requirements.txt /app/

RUN python -m venv /venv
ENV PATH="/venv/bin:$PATH"

RUN pip install -r requirements.txt

# Copy project files into the container
COPY . /app/

# Expose port (if necessary)
EXPOSE 8000

# Command to run the application
CMD ["python", "manage.py", "runserver", "0.0.0.0:8000"]
