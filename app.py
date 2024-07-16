from flask import Flask, jsonify
from flask_cors import CORS
import logging
import os

app = Flask(__name__)

# Enable CORS for all routes
CORS(app)

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

@app.route('/')
def hello():
    logger.info("Hello route was accessed")
    return jsonify(message=f"Hello {os.getenv('NAME', 'World')}!")

@app.route('/health')
def health():
    logger.info("Health check route was accessed")
    return jsonify(status="UP")

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=3000)

