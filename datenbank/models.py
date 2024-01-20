from sqlalchemy import Column, ForeignKey, Integer, String, Time, TypeDecorator, CheckConstraint, Boolean
from sqlalchemy import LargeBinary
import re

from .database import Base


class User(Base):
    __tablename__ = 'users'

    id = Column(Integer, primary_key=True)
    name = Column(String)
    email = Column(String, unique=True)
    password = Column(String)

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