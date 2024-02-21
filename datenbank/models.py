from sqlalchemy import Column, ForeignKey, Integer, String, LargeBinary, ForeignKey
from sqlalchemy.orm import relationship
from .database import Base

class User(Base):
    __tablename__ = 'users'
    id = Column(Integer, primary_key=True)
    name = Column(String)
    email = Column(String, unique=True)
    password = Column(String)

class PinEntry(Base):
    __tablename__ = 'pin_entries'
    entry_id = Column(Integer, primary_key=True)
    user_id = Column(Integer, ForeignKey('users.id'))
    image = Column(LargeBinary)  # Speichern Sie Bilder als Binärdaten
    text = Column(String)
    category_id = Column(Integer, ForeignKey('categories.id'))

    category = relationship("Category", back_populates="pins")

class Category(Base):
    __tablename__ = 'categories'
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, unique=True, index=True)
    
    pins = relationship("PinEntry", back_populates="category")