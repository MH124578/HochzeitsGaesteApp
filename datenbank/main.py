from fastapi import FastAPI, File, UploadFile, Depends
from sqlalchemy.orm import Session
from . import crud, models, schemas
from fastapi.responses import Response
from fastapi import HTTPException
from .database import SessionLocal
from fastapi.responses import StreamingResponse
from io import BytesIO

app = FastAPI()

# Funktion zur Erstellung der Datenbankverbindung
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@app.get("/")
async def root():
    return {"message": "Hello World"}

# Endpunkt zum Hochladen von Bildern
@app.post("/upload_image/{user_id}")
async def upload_image(user_id: int, file: UploadFile = File(...), db: Session = Depends(get_db)):
    image_data = await file.read()
    pin_entry = models.PinEntry(user_id=user_id, image=image_data, text="Optional Text")
    db.add(pin_entry)
    db.commit()
    db.refresh(pin_entry)
    return {"filename": file.filename, "entry_id": pin_entry.entry_id}

@app.get("/images/{image_id}")
async def get_image(image_id: int, db: Session = Depends(get_db)):
    image_entry = db.query(models.PinEntry).filter(models.PinEntry.entry_id == image_id).first()
    if image_entry is None:
        raise HTTPException(status_code=404, detail="Image not found")

    if not isinstance(image_entry.image, bytes):
        raise HTTPException(status_code=500, detail="Image data is not in bytes format")

    return StreamingResponse(BytesIO(image_entry.image), media_type="image/jpeg")

@app.get("/all_images/")
async def get_all_images(db: Session = Depends(get_db)):
    images = crud.get_all_pinentries(db)
    return [{"entryId": image.entry_id, "imageUrl": f"http://10.0.2.2:8000/images/{image.entry_id}"} for image in images]




