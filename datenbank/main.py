from fastapi import FastAPI, File, UploadFile, Depends, Form , HTTPException
from sqlalchemy.orm import Session
<<<<<<< HEAD

from datetime import datetime, time

from . import crud, models, schemas
=======
from . import crud, models, schemas, database
from fastapi.responses import Response, JSONResponse
>>>>>>> 985a91879e7211d07fc006ef00b83cdc76202b41
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


@app.on_event("startup")
async def startup_db_client():
    db = SessionLocal()
    try:
        crud.create_default_roles(db)
    finally:
        db.close()


@app.get("/check_user_email/")
def check_user_email(email: str, db: Session = Depends(get_db)) -> bool:
    user_existence = crud.check_user_email(db, email)
    return user_existence


@app.post("/add_user_email/", response_model=schemas.User)
def add_user_email(user: schemas.UserBase, db: Session = Depends(get_db)):
    db_user = crud.get_user_by_email(db=db, email=user.email)
    if db_user:
        raise HTTPException(status_code=400, detail="Email already registered")

    return crud.add_user_email(db=db, user=user)


@app.post("/fill_out_email_user/", response_model=schemas.User)
def fill_out_email_user(user: schemas.UserCreate, db: Session = Depends(get_db)):
    db_user = crud.get_user_by_email(db=db, email=user.email)

    if db_user is None:
        raise HTTPException(status_code=404, detail="No user with the provided email")

    return crud.fill_out_email_user(db=db, user=user)


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

<<<<<<< HEAD
@app.get("/users/{id}", response_model=schemas.User)
def read_user(id: int, db: Session = Depends(get_db)):
    db_user = crud.get_user(db, id=id)
    if db_user is None:
        raise HTTPException(status_code=404, detail="User not found")
    return db_user

@app.post("/home_information/", response_model=schemas.HomeInformationEntry)
def create_home_information_entry(user_id: int, home_information: schemas.HomeInformationEntryCreate, db: Session = Depends(get_db)):
    return crud.create_home_information_entry(db, home_information, user_id=user_id)

@app.get("/home_information/", response_model=list[schemas.HomeInformationEntry])
def get_all_home_information_entries(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):

    return crud.get_all_home_information_entries(db, skip=skip, limit=limit)


@app.post("/users/{id}/pin_entries/", response_model=schemas.PinEntry)
def create_pinentry_for_user(
    id: int, pinentry: schemas.PinEntryCreate, db: Session = Depends(get_db)
):
    return crud.create_user_pinentry(db=db, pinentry=pinentry, user_id=id)


@app.get("/pin_entries/", response_model=list[schemas.PinEntry])
def read_items(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    pinentry = crud.get_pinentry(db, skip=skip, limit=limit)
    return pinentry


@app.get("/users_info/", response_model=list[dict])
def read_all_users_with_names_or_emails(db: Session = Depends(get_db)):
    users_info = crud.get_all_users_with_names_or_emails(db)
    return users_info

@app.post("/relationships/", response_model=schemas.Relationship)
def create_relationship(relationship: schemas.RelationshipCreate, db: Session = Depends(get_db)):
    return crud.create_relationship(db, relationship)

@app.post("/roles/", response_model=schemas.Role)
def create_role(role: schemas.RoleCreate, db: Session = Depends(get_db)):
    return crud.create_role(db, role)

@app.get("/roles/", response_model=list[schemas.Role])
def read_all_roles(db: Session = Depends(get_db)):
    roles = crud.get_all_roles(db)
    return roles

@app.post("/guest_roles/", response_model=schemas.GuestRole)
def create_guest_role(guest_role: schemas.GuestRoleCreate, db: Session = Depends(get_db)):
    return crud.create_guest_role(db, guest_role)

@app.post("/families/", response_model=schemas.Family)
def create_family(family: schemas.FamilyCreate, db: Session = Depends(get_db)):
    return crud.create_family(db, family)

@app.post("/family_members/", response_model=schemas.FamilyMember)
def create_family_member(family_member: schemas.FamilyMemberCreate, db: Session = Depends(get_db)):
    return crud.create_family_member(db, family_member)

@app.get("/families/{family_name}/members/", response_model=list[schemas.FamilyMember])
def get_family_members(family_name: str, db: Session = Depends(get_db)):
    return crud.get_members_by_family(db, family_name)

@app.get("/roles/{role_id}/members/", response_model=list[schemas.GuestRole])
def get_role_members(role_id: int, db: Session = Depends(get_db)):
    return crud.get_members_by_role(db, role_id)
=======
>>>>>>> 985a91879e7211d07fc006ef00b83cdc76202b41
