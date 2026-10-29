FROM python:3.10-slim

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends libgomp1 && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# Train the synthetic-data demo model during build, not on every wake-up.
ENV OMP_NUM_THREADS=1
RUN python model/train.py

EXPOSE 8000

# On startup, run train to generate model if missing, then start server
CMD ["sh", "-c", "exec uvicorn main:app --host 0.0.0.0 --port ${PORT:-8000}"]
