bind = "10.0.0.194:5000"  # Replace with your desired host and port
accesslog = '/opt/stack/logs/gunicorn/access.log'  # Log requests to stdout
errorlog = '/opt/stack/logs/gunicorn/error.log'  # Log errors to stderr
# access-logfile = '/opt/stack/logs/gunicorn/access.log'
# error-logfile = '/opt/stack/logs/gunicorn/error.log'
workers = 2

# accesslog = '-'  # Log requests to stdout
# errorlog = '-'  # Log errors to stderr

# Restart on worker failure (5 attempts in 5 seconds)
max_requests = 1000  # Restart after a certain number of requests
max_requests_jitter = 500  # Add randomness to restart timing
timeout = 60  # Restart if a worker doesn't respond for 60 seconds
graceful_timeout = 30  # Time to finish existing requests before restart
