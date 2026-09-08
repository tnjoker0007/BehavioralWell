import uvicorn
import os

if __name__ == "__main__":
    print("Starting BehavioralWell Backend FastAPI Server on http://0.0.0.0:8000 (accessible via http://127.0.0.1:8000 and http://10.0.2.2:8000)...")
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
