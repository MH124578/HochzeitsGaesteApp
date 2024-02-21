from sqlalchemy import desc
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


def create_category(db: Session, category: schemas.CategoryCreate):
    db_category = models.Category(name=category.name)
    db.add(db_category)
    db.commit()
    db.refresh(db_category)
    return db_category


def get_categories(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.Category).offset(skip).limit(limit).all()


def get_pinentries_by_category(db: Session, category_id: int):
    return db.query(models.PinEntry).filter(models.PinEntry.category_id == category_id).all()


def get_all_home_information_entries(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.HomeInformationEntry).order_by(desc(models.HomeInformationEntry.event_today), models.HomeInformationEntry.information_time).offset(skip).limit(limit).all()


def create_home_information_entry(db: Session, home_information_entry: schemas.HomeInformationEntryCreate, user_id: int):
    db_home_information_entry = models.HomeInformationEntry(title=home_information_entry.title, description=home_information_entry.description,
                                                            information_time=home_information_entry.information_time, event_today=home_information_entry.event_today,
                                                            user_id=user_id)
    db.add(db_home_information_entry)
    db.commit()
    db.refresh(db_home_information_entry)
    return db_home_information_entry


def create_relationship(db: Session, relationship: schemas.RelationshipCreate):
    db_relationship = models.Relationship(**relationship.dict())
    db.add(db_relationship)
    db.commit()
    db.refresh(db_relationship)
    return db_relationship


def create_role(db: Session, role: schemas.RoleCreate):
    db_role = models.Role(**role.dict())
    db.add(db_role)
    db.commit()
    db.refresh(db_role)
    return db_role


def create_guest_role(db: Session, guest_role: schemas.GuestRoleCreate):
    db_guest_role = models.GuestRole(**guest_role.dict())
    db.add(db_guest_role)
    db.commit()
    db.refresh(db_guest_role)
    return db_guest_role


def create_family(db: Session, family: schemas.FamilyCreate):
    db_family = models.Family(**family.dict())
    db.add(db_family)
    db.commit()
    db.refresh(db_family)
    return db_family


def create_family_member(db: Session, family_member: schemas.FamilyMemberCreate):
    db_family_member = models.FamilyMember(**family_member.dict())
    db.add(db_family_member)
    db.commit()
    db.refresh(db_family_member)
    return db_family_member


def get_members_by_family(db: Session, family_name: str):
    family = db.query(models.Family).filter(models.Family.family_name == family_name).first()
    if not family:
        return []

    members = (
        db.query(models.FamilyMember)
        .filter(models.FamilyMember.family_id == family.id)
        .join(models.User, models.User.id == models.FamilyMember.user_id)
        .order_by(desc(models.User.birthdate))
        .all()
    )
    return members


def get_members_by_role(db: Session, role_id: int):
    members = db.query(models.GuestRole).filter(models.GuestRole.role_id == role_id).all()
    return members


def get_user_id_by_email(db: Session, email: str):
    return db.query(models.User).filter(models.User.email == email).first()


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
    db_user.birthdate = user.birthdate
    db_user.profile_picture = user.profile_picture

    db.commit()
    db.refresh(db_user)
    return db_user


def get_all_users_with_names_or_emails(db: Session):
    users = db.query(models.User).all()

    users_with_names = [user for user in users if user.name]
    users_without_names = [user for user in users if not user.name]

    sorted_users_with_names = sorted(users_with_names, key=lambda user: user.name)

    sorted_users_without_names = sorted(users_without_names, key=lambda user: user.email)

    user_info = []
    for user in sorted_users_with_names:
        user_info.append({"id": user.id, "name": user.name})

    for user in sorted_users_without_names:
        user_info.append({"id": user.id, "name": user.email})

    return user_info


def create_default_roles(db: Session):
    existing_roles = db.query(models.Role).count()

    if existing_roles == 0:
        default_roles = ["Mother", "Father", 
                        "Daughter", "Son", 
                        "Sister", "Brother", 
                        "Half-sister", "Half-brother", 
                        "Stepsister", "Stepbrother", 
                        "Grandmother", "Grandfather", 
                        "Granddaughter", "Grandson", 
                        "Aunt", "Uncle", 
                        "Great Aunt", "Great Uncle", 
                        "First cousin", "Second cousin", "Third cousin", 
                        "Mother-in-law", "Father-in-law", 
                        "Sister-in-law", "Brother-in-law", 
                        "Son-in-law", "Daughter-in-law", 
                        "Wife", "Husband", 
                        "Maid of Honor", "Best Man", 
                        "Bridesmaid", "Groomsman", 
                        "Flower Girl", "Ring Bearer",  
                        "Elementary School Friend", "Middle School Friend",
                        "High School Friend", "College/Uni Friend",
                        "Work Colleague", "Friend from Studies Abroad",
                        "Officiant"]

        for role_name in default_roles:
            role = models.Role(role_name=role_name)
            db.add(role)

        db.commit()


def get_all_roles(db: Session):
    return db.query(models.Role).all()


def create_guest_role(db: Session, guest_role: schemas.GuestRoleCreate):
    db_guest_role = models.GuestRole(**guest_role.dict())
    db.add(db_guest_role)
    db.commit()
    db.refresh(db_guest_role)
    return db_guest_role


def create_family(db: Session, family: schemas.FamilyCreate):
    db_family = models.Family(**family.dict())
    db.add(db_family)
    db.commit()
    db.refresh(db_family)
    return db_family


def create_family_member(db: Session, family_member: schemas.FamilyMemberCreate):
    db_family_member = models.FamilyMember(**family_member.dict())
    db.add(db_family_member)
    db.commit()
    db.refresh(db_family_member)
    return db_family_member


def get_members_by_family(db: Session, family_name: str):
    family = db.query(models.Family).filter(models.Family.family_name == family_name).first()
    if not family:
        return []

    members = (
        db.query(models.FamilyMember)
        .filter(models.FamilyMember.family_id == family.id)
        .join(models.User, models.User.id == models.FamilyMember.user_id)
        .order_by(desc(models.User.birthdate))
        .all()
    )
    return members


def get_members_by_role(db: Session, role_id: int):
    members = db.query(models.GuestRole).filter(models.GuestRole.role_id == role_id).all()
    return members