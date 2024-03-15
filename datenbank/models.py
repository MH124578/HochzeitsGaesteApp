from sqlalchemy import Column, ForeignKey, Integer, String, Boolean, LargeBinary, BLOB
from sqlalchemy.orm import relationship
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()

class User(Base):
    __tablename__ = 'users'
    id = Column(Integer, primary_key=True)
    name = Column(String)
    email = Column(String, unique=True)
    password = Column(String)
    birthdate = Column(String, nullable=True)
    profile_picture = Column(BLOB, nullable=True)

class HomeInformationEntry(Base):
    __tablename__ = 'home_information_entries'
    entry_id = Column(Integer, primary_key=True)
    title = Column(String)
    description = Column(String)
    information_time = Column(String, nullable=False)
    event_today = Column(Boolean, nullable=False)
    user_id = Column(Integer, ForeignKey('users.id'))

class PinEntry(Base):
    __tablename__ = 'pin_entries'
    entry_id = Column(Integer, primary_key=True)
    user_id = Column(Integer, ForeignKey('users.id'))
    image = Column(LargeBinary)  
    text = Column(String)
    category_id = Column(Integer, ForeignKey('categories.id'), nullable=True)

    category = relationship("Category", back_populates="pins", lazy="joined")

class Category(Base):
    __tablename__ = 'categories'
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, unique=True, index=True)
    
    pins = relationship("PinEntry", back_populates="category")

class Relationship(Base):
    __tablename__ = 'relationships'
    id = Column(Integer, primary_key=True)
    guest_id_1 = Column(Integer, ForeignKey('users.id'))
    guest_id_2 = Column(Integer, ForeignKey('users.id'))
    relationship_type = Column(String)

class Role(Base):
    __tablename__ = 'roles'
    id = Column(Integer, primary_key=True)
    role_name = Column(String)

class GuestRole(Base):
    __tablename__ = 'guest_roles'
    id = Column(Integer, primary_key=True)
    user_id = Column(Integer, ForeignKey('users.id'))
    role_id = Column(Integer, ForeignKey('roles.id'))

class Family(Base):
    __tablename__ = 'families'
    id = Column(Integer, primary_key=True)
    family_name = Column(String)

class FamilyMember(Base):
    __tablename__ = 'family_members'
    id = Column(Integer, primary_key=True)
    user_id = Column(Integer, ForeignKey('users.id'))
    family_id = Column(Integer, ForeignKey('families.id'))
