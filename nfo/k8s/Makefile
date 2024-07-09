# Makefile for nfo Project

# Variables
PYTHON = python3
PIP = pip3
VENV = venv_nfo
REQ_FILE = requirements.txt
DJANGO_MANAGE = $(PYTHON) manage.py
APP_NAME = apis

# Targets
.PHONY: help
help:
	@echo "Available targets:"
	@echo "  setup         Setup Python, virtual environment, and dependencies"
	@echo "  install       Install dependencies"
	@echo "  create-venv   Create virtual environment"
	@echo "  activate-venv Activate virtual environment"
	@echo "  install-packages Install required Python packages"
	@echo "  startapp      Create a new Django app named 'apis'"
	@echo "  configure-env Configure environment settings in config.ini"
	@echo "  migrate       Apply database migrations"
	@echo "  runserver     Run Django development server"
	@echo "  clean         Clean up temporary files and virtual environment"

build: clean create-venv activate-venv install-packages migrate runserver

# install:
# 	# Ensure Python 3.12 and necessary tools are installed
# 	# Install pip if not already installed
# 	# Install Helm and Kubernetes Client/Server separately

create-venv:
	# Install virtualenv if not already installed
	# $(PIP) install virtualenv
	$(PYTHON) -m  venv $(VENV)
	# Create virtual environment named venv_nfo
	#  virtualenv $(VENV)

activate-venv:
	# Activate virtual environment
	/bin/bash -c "source $(VENV)/bin/activate" # Use `$(VENV)\Scripts\activate` on Windows

install-packages:
	# Install required Python packages listed in requirements.txt
	$(PIP) install -r $(REQ_FILE)

# startapp:
# 	# Create a new Django app named 'apis'
# 	$(DJANGO_MANAGE) startapp $(APP_NAME)

configure-env:
	# Perform configuration tasks for environment settings in config.ini
	# Change Kubernetes config path
	# Change Helm executable path

migrate:
	# Apply database migrations
	$(DJANGO_MANAGE) makemigrations
	$(DJANGO_MANAGE) migrate

runserver:
	# Run Django development server
	$(DJANGO_MANAGE) runserver 0.0.0.0:8000

clean:
	# Clean up temporary files and virtual environment
	find . -type f -name '*.pyc' -delete
	rm -rf $(VENV) __pycache__

# Note: Adjust paths and commands based on your specific environment and requirements
