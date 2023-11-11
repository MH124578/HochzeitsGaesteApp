from fastapi import FastAPI, File, UploadFile, Depends
from sqlalchemy.orm import Session
from . import crud, models, schemas
from .database import SessionLocal

app = FastAPI()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@app.get("/")
async def root():
    return {"message": "Hello World"}

@app.post("/upload_image/{user_id}")
async def upload_image(user_id: int, file: UploadFile = File(...), db: Session = Depends(get_db)):
    image_data = await file.read()
    
    # Erstellen eines neuen PinEntry mit dem Bild als BLOB
    pin_entry = models.PinEntry(user_id=user_id, image=image_data, text="Optional Text")
    db.add(pin_entry)
    db.commit()
    db.refresh(pin_entry)

    return {"filename": file.filename, "entry_id": pin_entry.entry_id}
