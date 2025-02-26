import logging
import logging.handlers
import requests
import base64
from flask import Flask, request, jsonify

app = Flask(__name__)
base_uri = "/your-callback-endpoint"  # Replace with your desired path

# Authentication credentials (replace with your actual values)
notification_username = "nfv_user"
notification_password = "devstack"
token_endpoint = "http://10.0.0.51/identity/v3/auth/tokens"  # Replace with the IP address of the server where the Keystone service is running

# Logging configuration
logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)

# File logging configuration
fh = logging.handlers.RotatingFileHandler(
    '/opt/stack/logs/gunicorn/access.log', maxBytes=10485760, backupCount=5
)

# Formatter with date and time
formatter = logging.Formatter("%(asctime)s - %(levelname)s - %(message)s")
fh.setFormatter(formatter)  # Apply formatter to the file handler
logger.addHandler(fh)


# Helper function to create authentication headers
def get_oauth2_token(client_id, client_password, token_endpoint):
    """Retrieves an OAuth2 token using client credentials grant."""
    data = {
        "auth": {
            "identity": {
                "methods": ["password"],
                "password": {
                    "user": {
                        "name": client_id,
                        "password": client_password,
                        "domain": {"id": "default"},
                    }
                },
            },
            "grant_type": "client_credentials",
            "client_id": client_id,
            "client_secret": client_password,
        }
    }
    logger.info("Authentication token data: %s", data)

    headers = {
        "Authorization": (
            "Basic "
            + base64.b64encode(
                 (client_id + ":" + client_password).encode()
            )
            .decode()
        ),
        "Content-Type": "application/json",
    }
    logger.info("Authentication token headers: %s", headers)

    try:
        logger.info("Token endpoint: %s", token_endpoint)
        response = requests.post(token_endpoint, headers=headers, json=data)
        x_subject_token = response.headers.get("X-Subject-Token")
        if response.status_code == 201:
            logger.info("Token creation successful: %s", x_subject_token)
            return x_subject_token
        else:
            logger.error(
                "Token creation failed with status code: %s, response: %s",
                response.status_code,
                response.text,
            )
    except requests.exceptions.RequestException as e:
        logger.error("Error retrieving OAuth2 token: %s", str(e))
        return None


def create_auth_headers(access_token):
    return {"X-Auth-Token": f"{access_token}"}


def create_version_headers():
    # Default version if not provided
    # version = request.headers.get("Version", "2.0.0")
    version = "2.0.0"
    logger.info("Version: %s", version)
    return {"Version": f"{version}"}


@app.route(base_uri, methods=["GET"])
def callback_get():
    # Get OAuth2 token using provided authentication data
    logger.info("Received GET request with valid authentication")

    # Function to extract and validate authorization headers from the request
    logger.info("Authentication headers: %s", request.headers)
    # logger.info("Authentication json: %s", request.json)

    auth_type = request.headers.get("Authorization", "").split(" ")[0].upper()
    logger.info("Received authentication type: %s", auth_type)

    if auth_type == "BASIC":
        try:
            encoded_creds = (
                request.headers.get("Authorization", "").split(" ")[1]
            )
            logger.info("Authentication encoded_creds: %s", encoded_creds)
            decoded_creds = base64.b64decode(encoded_creds).decode()
            logger.info("Authentication decoded_creds: %s", decoded_creds)
            username, password = decoded_creds.split(":")
            logger.info("Authentication Username: %s", username)
            logger.info("Authentication Password: %s", password)

            if username == notification_username\
                    and password == notification_password:
                logger.info("GET request authentication successful")
                return 'GET request authentication successful', 204
            else:
                return 'GET request authentication fail', 401
        except Exception as e:
            logger.error("Invalid Basic authentication format: %s", e)
            raise Exception("Invalid authentication")
    else:
        return 'GET request authentication successful', 204
        #logger.error("Unsupported authentication type: %s", auth_type)
        #raise Exception("Unsupported authentication method")


@app.route(base_uri, methods=['POST'])
def callback_post():
    logger.info("Received POST request with valid authentication and data:\
                %s", request.json)
    # logger.info("Authentication POST headers: %s", request.headers)

    try:
        data = request.json  # Assuming JSON notification payload
        # Extract VNF instance ID
        vnfInstanceId = (
            data['alarm']['_links']['objectInstance']['href'].split('/')[-1]
        )
        logger.info("VNF Instance ID: %s", vnfInstanceId)

        if vnfInstanceId:
            heal_data = {
                "cause": "healing"  # Default cause
            }
            heal_data["additionalParams"] = {
                "all": False        # Default "all" value
            }

            # Extract VNFC instance IDs
            vnfcInstanceIds = data['alarm']['vnfcInstanceIds']
            logger.info("VNFC Instance ID: %s", vnfcInstanceIds)
            if vnfcInstanceIds:
                heal_data["vnfcInstanceId"] = vnfcInstanceIds

            logger.info("Heal request with data: %s", heal_data)

            # Get OAuth2 token using provided authentication data
            auth_type = (
                request.headers.get('Authorization', '')
                .split(" ")[0]
                .upper()
            )
            logger.info("Received authentication type: %s", auth_type)
            if auth_type == "BASIC":
                encoded_creds = (
                    request.headers.get('Authorization', '')
                    .split(" ")[1]
                )
                logger.info("Authentication encoded_creds: %s", encoded_creds)
                decoded_creds = base64.b64decode(encoded_creds).decode()
                logger.info("Authentication decoded_creds: %s", decoded_creds)
                client_id, client_password = decoded_creds.split(":")
                logger.info("Authentication client_id: %s", client_id)
                logger.info(
                    "Authentication client_password: %s", client_password
                )

                access_token = get_oauth2_token(
                    client_id, client_password, token_endpoint
                )
                if access_token:
                    # Merge authentication and version headers
                    auth_headers = create_auth_headers(access_token)
                    version_headers = create_version_headers()
                    combined_headers = {**auth_headers, **version_headers}
                    logger.info("Heal request headers: %s", combined_headers)
                else:
                    logger.error(
                        "Failed to retrieve OAuth2 token for heal request"
                    )
                    return jsonify({'error': 'Failed to retrieve\
                                    access token'}), 401
            else:
                logger.error("Unsupported authentication type or missing data")
                return jsonify({'error': 'Unsupported authentication or\
                                missing data'}), 401

            # Send heal request with authentication headers
            base_url = "http://10.0.0.51:9890/vnflcm/v2/vnf_instances/"  # Replace with the IP address of the server where the tacker service is running
            path = f"{vnfInstanceId}/heal"
            url = base_url + path

            # Authentication headers with heal_data
            response = requests.post(
                url, json=heal_data, headers=combined_headers
            )
            # Authentication headers without heal_data
            # response = requests.post(url, headers=combined_headers)

            if response.status_code == 202:
                logger.info(
                    "Heal request accepted by Tacker for VNF instance %s",
                    vnfInstanceId
                )
                return 'Heal request received', 204
            else:
                logger.error(
                    "Error sending heal request for VNF instance %s: %s",
                    vnfInstanceId,
                    response.text
                )
    except Exception as e:
        logger.error("Error processing POST request: %s", str(e))
        return jsonify({"error": "Internal server error"}), 500
