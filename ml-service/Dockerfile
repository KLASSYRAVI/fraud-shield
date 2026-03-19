FROM python:3.10-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

EXPOSE 8000

# On startup, run train to generate model if missing, then start server
CMD python model/train.py && uvicorn main:app --host 0.0.0.0 --port 8000
