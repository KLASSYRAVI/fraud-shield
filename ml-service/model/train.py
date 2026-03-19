import os
import pandas as pd
import numpy as np
import xgboost as xgb
from sklearn.model_selection import train_test_split
import joblib

MODEL_PATH = os.path.join(os.path.dirname(__file__), "fraud_model.pkl")

def generate_data(num_samples=5000):
    np.random.seed(42)
    
    amount = np.random.exponential(scale=100, size=num_samples)
    hour_of_day = np.random.randint(0, 24, size=num_samples)
    is_foreign_location = np.random.binomial(1, 0.05, size=num_samples)
    device_type = np.random.randint(0, 3, size=num_samples) # 0:mobile, 1:desktop, 2:tablet
    transactions_last_hour = np.random.poisson(lam=1, size=num_samples)
    
    df = pd.DataFrame({
        'amount': amount,
        'hour_of_day': hour_of_day,
        'is_foreign_location': is_foreign_location,
        'device_type': device_type,
        'transactions_last_hour': transactions_last_hour
    })
    
    # Simple rule-based generation of labels to allow the model to learn something
    fraud_prob = (
        (df['amount'] > 500) * 0.3 + 
        (df['is_foreign_location'] == 1) * 0.4 + 
        ((df['hour_of_day'] < 5) | (df['hour_of_day'] > 22)) * 0.2 +
        (df['transactions_last_hour'] > 4) * 0.2
    )
    
    # Scale probabilities and add noise
    fraud_prob = np.clip(fraud_prob + np.random.normal(0, 0.1, size=num_samples), 0, 1)
    
    # Target 10% fraud rate roughly
    threshold = np.percentile(fraud_prob, 90)
    df['fraud'] = (fraud_prob >= threshold).astype(int)
    
    return df

def train_model():
    if os.path.exists(MODEL_PATH):
        print("Model already exists. Skipping training.")
        return
    
    print("Generating synthetic data...")
    df = generate_data()
    
    X = df.drop('fraud', axis=1)
    y = df['fraud']
    
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
    
    print("Training XGBoost model...")
    model = xgb.XGBClassifier(
        n_estimators=100,
        max_depth=4,
        learning_rate=0.1,
        eval_metric='logloss'
    )
    
    model.fit(X_train, y_train)
    
    accuracy = model.score(X_test, y_test)
    print(f"Model trained with accuracy: {accuracy:.4f}")
    
    os.makedirs(os.path.dirname(MODEL_PATH), exist_ok=True)
    joblib.dump(model, MODEL_PATH)
    print(f"Model saved to {MODEL_PATH}")

if __name__ == "__main__":
    train_model()
