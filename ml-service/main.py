from fastapi import FastAPI, HTTPException
import joblib
import os
import pandas as pd
from schemas import TransactionFeature, RiskScoreResponse
import logging

app = FastAPI(title="Fraud Scoring ML Service")

MODEL_PATH = os.path.join(os.path.dirname(__file__), "model", "fraud_model.pkl")

# Load model globally so it's loaded only once when the server starts.
model = None

@app.on_event("startup")
def load_model():
    global model
    if os.path.exists(MODEL_PATH):
        model = joblib.load(MODEL_PATH)
        logging.info("Model loaded successfully.")
    else:
        logging.warning("Model not found. Predictions will fail until trained.")

@app.post("/predict", response_model=RiskScoreResponse)
def predict_fraud(feature: TransactionFeature):
    if model is None:
        raise HTTPException(status_code=500, detail="Model is not loaded.")
    
    # Convert input to DataFrame
    input_data = pd.DataFrame([{
        'amount': feature.amount,
        'hour_of_day': feature.hour_of_day,
        'is_foreign_location': feature.is_foreign_location,
        'device_type': feature.device_type,
        'transactions_last_hour': feature.transactions_last_hour
    }])
    
    # Predict probabilities
    probabilities = model.predict_proba(input_data)
    fraud_prob = float(probabilities[0][1])  # Class 1 probability
    
    if fraud_prob < 0.3:
        risk_level = "LOW"
    elif fraud_prob <= 0.7:
        risk_level = "MEDIUM"
    else:
        risk_level = "HIGH"
        
    return RiskScoreResponse(
        fraud_probability=fraud_prob,
        risk_level=risk_level
    )
