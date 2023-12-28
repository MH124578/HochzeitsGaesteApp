from sqlalchemy.orm import Session

from . import models, schemas


def get_user(db: Session, id: int):
    return db.query(models.User).filter(models.User.id == id).first()


def get_user_by_email(db: Session, email: str):
    return db.query(models.User).filter(models.User.email == email).first()


def get_users(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.User).offset(skip).limit(limit).all()


def create_user(db: Session, user: schemas.UserCreate):
    fake_password = user.password + "notreallyhashed"
    db_user = models.User(email=user.email, password=fake_password)
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user

def delete_pinentry(db: Session, entry_id: int):
    pinentry = db.query(models.PinEntry).filter(models.PinEntry.entry_id == entry_id).first()
    if pinentry:
        db.delete(pinentry)
        db.commit()
        return True
    else:
        return False
    
def delete_all_pinentries(db: Session):
    try:
        db.query(models.PinEntry).delete()
        db.commit()
        return True
    except:
        return False

def get_pinentry(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.PinEntry).offset(skip).limit(limit).all()


def create_pinentry(db: Session, pinentry: schemas.PinEntryCreate, user_id: int):
    db_pinentry = models.PinEntry(**pinentry.dict(), user_id=user_id)
    db_pinentry.image = pinentry.image
    db.add(db_pinentry)
    db.commit()
    db.refresh(db_pinentry)
    return db_pinentry

def get_all_pinentries(db: Session):
    return db.query(models.PinEntry).all()

def update_pinentry_text(db: Session, entry_id: int, new_text: str):
    pinentry = get_pinentry_by_id(db, entry_id)
    if pinentry:
        pinentry.text = new_text
        db.commit()
        return pinentry
    else:
        return None
    
def get_pinentry_by_id(db: Session, entry_id: int):
    return db.query(models.PinEntry).filter(models.PinEntry.entry_id == entry_id).first()
