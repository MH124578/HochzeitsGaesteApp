from sqlalchemy.orm import Session

from . import models, schemas


def get_user(db: Session, id: int):
    return db.query(models.User).filter(models.User.id == id).first()


def get_user_by_email(db: Session, email: str):
    return db.query(models.User).filter(models.User.email == email).first()


def get_users(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.User).offset(skip).limit(limit).all()


def add_user_email(db: Session, user: schemas.UserBase):
    db_user_email = models.User(email=user.email)
    db.add(db_user_email)
    db.commit()
    db.refresh(db_user_email)
    return db_user_email


def fill_out_email_user(db: Session, user: schemas.UserCreate):
    db_user = db.query(models.User).filter(models.User.email == user.email).first()

    if db_user:
        db_user.name = user.name
        db_user.password = user.password + "notreallyhashed"
    else:
        print("User not found with the provided email.")
        return None

    db.commit()
    db.refresh(db_user)
    return db_user


def create_user(db: Session, user: schemas.UserCreate):
    fake_password = user.password + "notreallyhashed"
    db_user = models.User(email=user.email, name=user.name, password=fake_password)
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user


def get_pinentry(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.PinEntry).offset(skip).limit(limit).all()


def create_pinentry(db: Session, pinentry: schemas.PinEntryCreate, user_id: int):
    db_pinentry = models.PinEntry(**pinentry.dict(), user_id=user_id)
    db_pinentry.image = pinentry.image
    db.add(db_pinentry)
    db.commit()
    db.refresh(db_pinentry)
    return db_pinentry
