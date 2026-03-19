from pydantic import BaseModel

class TransactionFeature(BaseModel):
    amount: float
    hour_of_day: int
    is_foreign_location: int
    device_type: int
    transactions_last_hour: int

class RiskScoreResponse(BaseModel):
    fraud_probability: float
    risk_level: str
