from fastapi import Depends, FastAPI, HTTPException
from sqlalchemy.orm import Session

from datetime import datetime, time

from . import crud, models, schemas
from .database import SessionLocal, engine

models.Base.metadata.create_all(bind=engine)

app = FastAPI()


# Dependency
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


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


@app.get("/users/", response_model=list[schemas.User])
def read_users(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    users = crud.get_users(db, skip=skip, limit=limit)
    return users


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