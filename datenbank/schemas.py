from datetime import time
from typing import Union
import re


from pydantic import BaseModel, validator


class UserBase(BaseModel):
    email: str


class UserCreate(UserBase):
    name: str
    password: str


class User(UserBase):
    id: int

    class Config:
        orm_mode = True


class HomeInformationEntryBase(BaseModel):
    title: str


class HomeInformationEntryCreate(HomeInformationEntryBase):
    description: str
    information_time: str
    event_today: bool

    @validator("information_time")
    def validate_information_time_format(cls, value):
        # Validate and format the input to HH:MM format
        match = re.match(r'^([01]\d|2[0-3]):([0-5]\d)$', value)
        if not match:
            raise ValueError("Invalid time format. Use HH:MM.")
        return value


class HomeInformationEntry(HomeInformationEntryCreate):
    entry_id: int
    user_id: int


class PinEntryBase(BaseModel):
    image: str
    text: Union[str, None] = None


class PinEntryCreate(PinEntryBase):
    image: bytes 


class PinEntry(PinEntryBase):
    entry_id: int
    user_id: int

    class Config:
        orm_mode = True
