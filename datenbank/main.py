from fastapi import FastAPI, File, UploadFile, Depends, Form , HTTPException
from sqlalchemy.orm import Session
from . import crud, models, schemas, database
from fastapi.responses import Response, JSONResponse
from .database import SessionLocal, engine
from fastapi.responses import StreamingResponse
from io import BytesIO

models.Base.metadata.create_all(bind=engine)

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

@app.post("/upload_image/{user_id}")
async def upload_image(user_id: int, file: UploadFile = File(...), text: str = Form(None), category_id: int = Form(...), db: Session = Depends(get_db)):

    db_category = db.query(models.Category).filter(models.Category.id == category_id).first()
    if not db_category:
        raise HTTPException(status_code=400, detail="Kategorie nicht gefunden")

    image_data = await file.read()
    pin_entry = models.PinEntry(user_id=user_id, image=image_data, text=text, category_id=category_id)
    db.add(pin_entry)
    db.commit()
    db.refresh(pin_entry)
    return {
        "filename": file.filename,
        "entry_id": pin_entry.entry_id,
        "text": pin_entry.text,
        "category_id": pin_entry.category_id
    }


@app.delete("/rm_image/{entry_id}")
async def delete_image(entry_id: int, db: Session = Depends(get_db)):
    result = crud.delete_pinentry(db, entry_id)
    if result:
        return {"message": f"Eintrag mit ID {entry_id} erfolgreich gelöscht."}
    else:
        raise HTTPException(status_code=404, detail=f"Eintrag mit ID {entry_id} nicht gefunden.")
    
@app.delete("/rm_all_images/")
async def delete_all_images(db: Session = Depends(get_db)):
    result = crud.delete_all_pinentries(db)
    if result:
        return {"message": "Alle Einträge erfolgreich gelöscht."}
    else:
        raise HTTPException(status_code=500, detail="Fehler beim Löschen der Einträge.")


@app.get("/images/{image_id}")
async def get_image(image_id: int, db: Session = Depends(get_db)):
    image_entry = db.query(models.PinEntry).filter(models.PinEntry.entry_id == image_id).first()
    if image_entry is None:
        raise HTTPException(status_code=404, detail="Image not found")

    if not isinstance(image_entry.image, bytes):
        raise HTTPException(status_code=500, detail="Image data is not in bytes format")

    return StreamingResponse(BytesIO(image_entry.image), media_type="image/jpeg")

@app.get("/images_by_category/{category_id}")
async def get_images_by_category(category_id: int, db: Session = Depends(get_db)):
    images = crud.get_pinentries_by_category(db, category_id=category_id)
    image_responses = [{"entryId": image.entry_id, "imageUrl": f"http://10.0.2.2:8000/images/{image.entry_id}", "text": image.text, "category_id": image.category_id} for image in images]
    return JSONResponse(content=image_responses)


@app.post("/users/", response_model=schemas.User)
def create_user(user: schemas.UserCreate, db: Session = Depends(get_db)):
    db_user = crud.get_user_by_email(db, email=user.email)
    if db_user:
        raise HTTPException(status_code=400, detail="Email already registered")
    return crud.create_user(db=db, user=user)

@app.put("/edit_image/{entry_id}")
async def edit_image(entry_id: int, text: str = Form(...), db: Session = Depends(get_db)):
    updated_entry = crud.update_pinentry_text(db, entry_id, text)
    if updated_entry is None:
        raise HTTPException(status_code=404, detail="Eintrag nicht gefunden")
    
    return {"message": "Text erfolgreich aktualisiert."}

@app.post("/categories/")
def create_category(name: str = Form(...), db: Session = Depends(get_db)):
    category_schema = schemas.CategoryCreate(name=name)
    return crud.create_category(db=db, category=category_schema)

@app.get("/categories/", response_model=list[schemas.Category])
def read_categories(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    categories = crud.get_categories(db, skip=skip, limit=limit)
    return categories

