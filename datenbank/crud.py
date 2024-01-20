from sqlalchemy import desc
from sqlalchemy.orm import Session

from . import models, schemas


def get_user(db: Session, id: int):
    return db.query(models.User).filter(models.User.id == id).first()


def get_user_by_email(db: Session, email: str):
    return db.query(models.User).filter(models.User.email == email).first()


def get_users(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.User).offset(skip).limit(limit).all()


def check_user_email(db: Session, email: str):
    user = db.query(models.User).filter(models.User.email == email).first()
    return user is not None


def add_user_email(db: Session, user: schemas.UserBase):
    db_user_email = models.User(email=user.email)
    db.add(db_user_email)
    db.commit()
    db.refresh(db_user_email)
    return db_user_email


def fill_out_email_user(db: Session, user: schemas.UserCreate):
    db_user = db.query(models.User).filter(models.User.email == user.email).first()

    db_user.name = user.name
    db_user.password = user.password + "notreallyhashed"

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


def create_home_information_entry(db: Session, home_information_entry: schemas.HomeInformationEntryCreate, user_id: int):
    db_home_information_entry = models.HomeInformationEntry(title=home_information_entry.title, description=home_information_entry.description,
                                                            information_time=home_information_entry.information_time, event_today=home_information_entry.event_today,
                                                            user_id=user_id)
    db.add(db_home_information_entry)
    db.commit()
    db.refresh(db_home_information_entry)
    return db_home_information_entry

def get_all_home_information_entries(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.HomeInformationEntry).order_by(desc(models.HomeInformationEntry.event_today), models.HomeInformationEntry.information_time).offset(skip).limit(limit).all()


def get_pinentry(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.PinEntry).offset(skip).limit(limit).all()


def create_pinentry(db: Session, pinentry: schemas.PinEntryCreate, user_id: int):
    db_pinentry = models.PinEntry(**pinentry.dict(), user_id=user_id)
    db_pinentry.image = pinentry.image
    db.add(db_pinentry)
    db.commit()
    db.refresh(db_pinentry)
    return db_pinentry
