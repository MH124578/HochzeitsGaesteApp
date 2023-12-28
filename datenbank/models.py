from sqlalchemy import Column, ForeignKey, Integer, String, LargeBinary
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
